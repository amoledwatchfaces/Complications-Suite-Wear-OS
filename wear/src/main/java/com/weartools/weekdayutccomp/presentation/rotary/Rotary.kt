/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalHorologistApi::class)

package com.weartools.weekdayutccomp.presentation.rotary

import android.view.ViewConfiguration
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.RotaryInputModifierNode
import androidx.compose.ui.input.rotary.RotaryScrollEvent
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * A modifier which connects rotary events with scrollable.
 * This modifier supports scroll with fling.
 *
 * @param scrollableState Scrollable state which will be scrolled while receiving rotary events
 * @param focusRequester Requests the focus for rotary input.
 * By default comes from [rememberActiveFocusRequester],
 * which is used with [HierarchicalFocusCoordinator]
 * @param flingBehavior Logic describing fling behavior. If null fling will not happen.
 * @param rotaryHaptics Class which will handle haptic feedback
 * @param reverseDirection Reverse the direction of scrolling. Should be aligned with
 * Scrollable `reverseDirection` parameter
 */
@OptIn(ExperimentalWearFoundationApi::class)
@ExperimentalHorologistApi
@Suppress("ComposableModifierFactory")
@Composable
fun Modifier.rotaryWithScroll(
    scrollableState: ScrollableState,
    focusRequester: FocusRequester = rememberActiveFocusRequester(),
    flingBehavior: FlingBehavior? = ScrollableDefaults.flingBehavior(),
    rotaryHaptics: RotaryHapticHandler = rememberRotaryHapticHandler(scrollableState),
    reverseDirection: Boolean = false,
): Modifier = rotaryHandler(
    rotaryScrollHandler = RotaryDefaults.rememberFlingHandler(scrollableState, flingBehavior),
    reverseDirection = reverseDirection,
    rotaryHaptics = rotaryHaptics,
    inspectorInfo = debugInspectorInfo {
        name = "rotaryWithFling"
        properties["scrollableState"] = scrollableState
        properties["focusRequester"] = focusRequester
        properties["flingBehavior"] = flingBehavior
        properties["rotaryHaptics"] = rotaryHaptics
        properties["reverseDirection"] = reverseDirection
    },
)
    .focusRequester(focusRequester)
    .focusable()

/**
 * Defaults for rotary modifiers
 */
@ExperimentalHorologistApi
object RotaryDefaults {

    /**
     * Returns whether the input is Low-res (a bezel) or high-res(a crown/rsb).
     */
    @ExperimentalHorologistApi
    @Composable
    fun isLowResInput(): Boolean = LocalContext.current.packageManager
        .hasSystemFeature("android.hardware.rotaryencoder.lowres")

    /**
     * Handles scroll with fling.
     * @param scrollableState Scrollable state which will be scrolled while receiving rotary events
     * @param flingBehavior Logic describing Fling behavior. If null - fling will not happen
     * @param isLowRes Whether the input is Low-res (a bezel) or high-res(a crown/rsb)
     */
    @Composable
    internal fun rememberFlingHandler(
        scrollableState: ScrollableState,
        flingBehavior: FlingBehavior? = null,
        isLowRes: Boolean = isLowResInput(),
    ): RotaryScrollHandler {
        val viewConfiguration = ViewConfiguration.get(LocalContext.current)

        return remember(scrollableState, flingBehavior, isLowRes) {
            // Remove unnecessary recompositions by disabling tracking of changes inside of
            // this block. This algorithm properly reads all updated values and
            // don't need recomposition when those values change.
            Snapshot.withoutReadObservation {
                debugLog { "isLowRes : $isLowRes" }
                fun rotaryFlingBehavior() = flingBehavior?.run {
                    RotaryFlingBehavior(
                        scrollableState,
                        flingBehavior,
                        viewConfiguration,
                        flingTimeframe = if (isLowRes) lowResFlingTimeframe else highResFlingTimeframe,
                    )
                }

                fun scrollBehavior() = RotaryScrollBehavior(scrollableState)

                if (isLowRes) {
                    LowResRotaryScrollHandler(
                        rotaryFlingBehaviorFactory = { rotaryFlingBehavior() },
                        scrollBehaviorFactory = { scrollBehavior() },
                    )
                } else {
                    HighResRotaryScrollHandler(
                        rotaryFlingBehaviorFactory = { rotaryFlingBehavior() },
                    ) { scrollBehavior() }
                }
            }
        }
    }

    private const val lowResFlingTimeframe: Long = 100L
    private const val highResFlingTimeframe: Long = 30L
}

/**
 * An interface for handling scroll events
 */
internal interface RotaryScrollHandler {
    /**
     * Handles scrolling events
     * @param coroutineScope A scope for performing async actions
     * @param event A scrollable event from rotary input, containing scrollable delta and timestamp
     * @param rotaryHaptics
     */
    suspend fun handleScrollEvent(
        coroutineScope: CoroutineScope,
        event: TimestampedDelta,
        rotaryHaptics: RotaryHapticHandler,
    )
}

/**
 * Class responsible for Fling behaviour with rotary.
 * It tracks and produces the fling when necessary
 */
internal class RotaryFlingBehavior(
    private val scrollableState: ScrollableState,
    private val flingBehavior: FlingBehavior,
    viewConfiguration: ViewConfiguration,
    private val flingTimeframe: Long,
) {

    // A time range during which the fling is valid.
    // For simplicity it's twice as long as [flingTimeframe]
    private val timeRangeToFling = flingTimeframe * 2

    //  A default fling factor for making fling slower
    private val flingScaleFactor = 0.7f

    private var previousVelocity = 0f

    private val rotaryVelocityTracker = RotaryVelocityTracker()

    private val minFlingSpeed = viewConfiguration.scaledMinimumFlingVelocity.toFloat()
    private val maxFlingSpeed = viewConfiguration.scaledMaximumFlingVelocity.toFloat()
    private var latestEventTimestamp: Long = 0

    private var flingVelocity: Float = 0f
    private var flingTimestamp: Long = 0

    /**
     * Starts a new fling tracking session
     * with specified timestamp
     */
    fun startFlingTracking(timestamp: Long) {
        rotaryVelocityTracker.start(timestamp)
        latestEventTimestamp = timestamp
        previousVelocity = 0f
    }

    /**
     * Observing new event within a fling tracking session with new timestamp and delta
     */
    fun observeEvent(timestamp: Long, delta: Float) {
        rotaryVelocityTracker.move(timestamp, delta)
        latestEventTimestamp = timestamp
    }

    /**
     * Performing fling if necessary and calling [beforeFling] lambda before it is triggered
     */
    suspend fun trackFling(beforeFling: () -> Unit) {
        val currentVelocity = rotaryVelocityTracker.velocity
        debugLog { "currentVelocity: $currentVelocity" }

        if (abs(currentVelocity) >= abs(previousVelocity)) {
            flingTimestamp = latestEventTimestamp
            flingVelocity = currentVelocity * flingScaleFactor
        }
        previousVelocity = currentVelocity

        // Waiting for a fixed amount of time before checking the fling
        delay(flingTimeframe)

        // For making a fling 2 criteria should be met:
        // 1) no more than
        // `rangeToFling` ms should pass between last fling detection
        // and the time of last motion event
        // 2) flingVelocity should exceed the minFlingSpeed
        debugLog {
            "Check fling:  flingVelocity: $flingVelocity " +
                "minFlingSpeed: $minFlingSpeed, maxFlingSpeed: $maxFlingSpeed"
        }
        if (latestEventTimestamp - flingTimestamp < timeRangeToFling &&
            abs(flingVelocity) > minFlingSpeed
        ) {
            // Stops scrollAnimationCoroutine because a fling will be performed
            beforeFling()
            val velocity = flingVelocity.coerceIn(-maxFlingSpeed, maxFlingSpeed)
            scrollableState.scroll(MutatePriority.UserInput) {
                with(flingBehavior) {
                    debugLog { "Flinging with velocity $velocity" }
                    performFling(velocity)
                }
            }
        }
    }
}

/**
 * A rotary event object which contains a [timestamp] of the rotary event and a scrolled [delta].
 */
internal data class TimestampedDelta(val timestamp: Long, val delta: Float)

/**This class does a smooth animation when the scroll by N pixels is done.
 * This animation works well on Rsb(high-res) and Bezel(low-res) devices.
 */
internal class RotaryScrollBehavior(
    private val scrollableState: ScrollableState,
) {
    private var sequentialAnimation = false
    private var scrollAnimation = AnimationState(0f)
    private var prevPosition = 0f

    /**
     * Handles scroll event to [targetValue]
     */
    suspend fun handleEvent(targetValue: Float) {
        scrollableState.scroll(MutatePriority.UserInput) {
            debugLog { "ScrollAnimation value before start: ${scrollAnimation.value}" }

            scrollAnimation.animateTo(
                targetValue,
                animationSpec = spring(),
                sequentialAnimation = sequentialAnimation,
            ) {
                val delta = value - prevPosition
                debugLog { "Animated by $delta, value: $value" }
                scrollBy(delta)
                prevPosition = value
                sequentialAnimation = value != this.targetValue
            }
        }
    }
}

/**
 * A modifier which handles rotary events.
 * It accepts ScrollHandler as the input - a class where main logic about how
 * scroll should be handled is lying
 */
internal fun Modifier.rotaryHandler(
    rotaryScrollHandler: RotaryScrollHandler,
    reverseDirection: Boolean,
    rotaryHaptics: RotaryHapticHandler,
    inspectorInfo: InspectorInfo.() -> Unit,

    ): Modifier = this then RotaryHandlerElement(
    rotaryScrollHandler,
    reverseDirection,
    rotaryHaptics,
    inspectorInfo,
)

/**
 * A scroll handler for RSB(high-res) without snapping and with or without fling
 * A list is scrolled by the number of pixels received from the rotary device.
 *
 * This class is a little bit different from LowResScrollHandler class - it has a filtering
 * for events which are coming with wrong sign ( this happens to rsb devices,
 * especially at the end of the scroll)
 *
 * This scroll handler supports fling. It can be set with [RotaryFlingBehavior].
 */
internal class HighResRotaryScrollHandler(
    private val rotaryFlingBehaviorFactory: () -> RotaryFlingBehavior?,
    private val scrollBehaviorFactory: () -> RotaryScrollBehavior,
) : RotaryScrollHandler {

    // This constant is specific for high-res devices. Because that input values
    // can sometimes come with different sign, we have to filter them in this threshold
    private val gestureThresholdTime = 200L
    private var scrollJob: Job = CompletableDeferred<Unit>()
    private var flingJob: Job = CompletableDeferred<Unit>()

    private var previousScrollEventTime = 0L
    private var rotaryScrollDistance = 0f

    private var rotaryFlingBehavior: RotaryFlingBehavior? = rotaryFlingBehaviorFactory()
    private var scrollBehavior: RotaryScrollBehavior = scrollBehaviorFactory()

    override suspend fun handleScrollEvent(
        coroutineScope: CoroutineScope,
        event: TimestampedDelta,
        rotaryHaptics: RotaryHapticHandler,
    ) {
        val time = event.timestamp
        val isOppositeScrollValue = isOppositeValueAfterScroll(event.delta)

        if (isNewScrollEvent(time)) {
            debugLog { "New scroll event" }
            resetTracking(time)
            rotaryScrollDistance = event.delta
        } else {
            // Due to the physics of Rotary side button, some events might come
            // with an opposite axis value - either at the start or at the end of the motion.
            // We don't want to use these values for fling calculations.
            if (!isOppositeScrollValue) {
                rotaryFlingBehavior?.observeEvent(event.timestamp, event.delta)
            } else {
                debugLog { "Opposite value after scroll :${event.delta}" }
            }
            rotaryScrollDistance += event.delta
        }

        scrollJob.cancel()

        rotaryHaptics.handleScrollHaptic(event.delta)
        debugLog { "Rotary scroll distance: $rotaryScrollDistance" }

        previousScrollEventTime = time
        scrollJob = coroutineScope.async {
            scrollBehavior.handleEvent(rotaryScrollDistance)
        }

        if (rotaryFlingBehavior != null) {
            flingJob.cancel()
            flingJob = coroutineScope.async {
                rotaryFlingBehavior?.trackFling(beforeFling = {
                    debugLog { "Calling before fling section" }
                    scrollJob.cancel()
                    scrollBehavior = scrollBehaviorFactory()
                })
            }
        }
    }

    private fun isOppositeValueAfterScroll(delta: Float): Boolean =
        rotaryScrollDistance * delta < 0f &&
            (abs(delta) < abs(rotaryScrollDistance))

    private fun isNewScrollEvent(timestamp: Long): Boolean {
        val timeDelta = timestamp - previousScrollEventTime
        return previousScrollEventTime == 0L || timeDelta > gestureThresholdTime
    }

    private fun resetTracking(timestamp: Long) {
        scrollBehavior = scrollBehaviorFactory()
        rotaryFlingBehavior = rotaryFlingBehaviorFactory()
        rotaryFlingBehavior?.startFlingTracking(timestamp)
    }
}

/**
 * A scroll handler for Bezel(low-res) without snapping.
 * This scroll handler supports fling. It can be set with RotaryFlingBehavior.
 */
internal class LowResRotaryScrollHandler(
    private val rotaryFlingBehaviorFactory: () -> RotaryFlingBehavior?,
    private val scrollBehaviorFactory: () -> RotaryScrollBehavior,
) : RotaryScrollHandler {

    private val gestureThresholdTime = 200L
    private var previousScrollEventTime = 0L
    private var rotaryScrollDistance = 0f

    private var scrollJob: Job = CompletableDeferred<Unit>()
    private var flingJob: Job = CompletableDeferred<Unit>()

    private var rotaryFlingBehavior: RotaryFlingBehavior? = rotaryFlingBehaviorFactory()
    private var scrollBehavior: RotaryScrollBehavior = scrollBehaviorFactory()

    override suspend fun handleScrollEvent(
        coroutineScope: CoroutineScope,
        event: TimestampedDelta,
        rotaryHaptics: RotaryHapticHandler,
    ) {
        val time = event.timestamp

        if (isNewScrollEvent(time)) {
            resetTracking(time)
            rotaryScrollDistance = event.delta
        } else {
            rotaryFlingBehavior?.observeEvent(event.timestamp, event.delta)
            rotaryScrollDistance += event.delta
        }

        scrollJob.cancel()
        flingJob.cancel()

        rotaryHaptics.handleScrollHaptic(event.delta)
        debugLog { "Rotary scroll distance: $rotaryScrollDistance" }

        previousScrollEventTime = time
        scrollJob = coroutineScope.async {
            scrollBehavior.handleEvent(rotaryScrollDistance)
        }

        flingJob = coroutineScope.async {
            rotaryFlingBehavior?.trackFling(
                beforeFling = {
                    debugLog { "Calling before fling section" }
                    scrollJob.cancel()
                    scrollBehavior = scrollBehaviorFactory()
                },
            )
        }
    }

    private fun isNewScrollEvent(timestamp: Long): Boolean {
        val timeDelta = timestamp - previousScrollEventTime
        return previousScrollEventTime == 0L || timeDelta > gestureThresholdTime
    }

    private fun resetTracking(timestamp: Long) {
        scrollBehavior = scrollBehaviorFactory()
        debugLog { "Velocity tracker reset" }
        rotaryFlingBehavior = rotaryFlingBehaviorFactory()
        rotaryFlingBehavior?.startFlingTracking(timestamp)
    }
}

private data class RotaryHandlerElement(
    private val rotaryScrollHandler: RotaryScrollHandler,
    private val reverseDirection: Boolean,
    private val rotaryHaptics: RotaryHapticHandler,
    private val inspectorInfo: InspectorInfo.() -> Unit,
) : ModifierNodeElement<RotaryInputNode>() {
    override fun create(): RotaryInputNode = RotaryInputNode(
        rotaryScrollHandler,
        reverseDirection,
        rotaryHaptics,
    )

    override fun update(node: RotaryInputNode) {
        debugLog { "Update launched!" }
        node.rotaryScrollHandler = rotaryScrollHandler
        node.reverseDirection = reverseDirection
        node.rotaryHaptics = rotaryHaptics
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RotaryHandlerElement

        if (rotaryScrollHandler != other.rotaryScrollHandler) return false
        if (reverseDirection != other.reverseDirection) return false
        if (rotaryHaptics != other.rotaryHaptics) return false
        if (inspectorInfo != other.inspectorInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rotaryScrollHandler.hashCode()
        result = 31 * result + reverseDirection.hashCode()
        result = 31 * result + rotaryHaptics.hashCode()
        result = 31 * result + inspectorInfo.hashCode()
        return result
    }
}

private class RotaryInputNode(
    var rotaryScrollHandler: RotaryScrollHandler,
    var reverseDirection: Boolean,
    var rotaryHaptics: RotaryHapticHandler,
) : RotaryInputModifierNode, Modifier.Node() {

    val channel = Channel<TimestampedDelta>(capacity = Channel.CONFLATED)
    val flow = channel.receiveAsFlow()

    override fun onAttach() {
        coroutineScope.launch {
            flow
                .collectLatest {
                    debugLog {
                        "Scroll event received: " + "delta:${it.delta}, timestamp:${it.timestamp}"
                    }
                    rotaryScrollHandler.handleScrollEvent(this, it, rotaryHaptics)
                }
        }
    }

    override fun onRotaryScrollEvent(event: RotaryScrollEvent): Boolean = false

    override fun onPreRotaryScrollEvent(event: RotaryScrollEvent): Boolean {
        debugLog { "onPreRotaryScrollEvent" }
        channel.trySend(
            TimestampedDelta(
                event.uptimeMillis,
                event.verticalScrollPixels * if (reverseDirection) -1f else 1f,
            ),
        )
        return true
    }
}

/**
 * Debug logging that can be enabled.
 */
private const val DEBUG = false

private inline fun debugLog(generateMsg: () -> String) {
    if (DEBUG) {
        println("RotaryScroll: ${generateMsg()}")
    }
}
