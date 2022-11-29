/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.weartools.weekdayutccomp.databinding.ActivityMainBinding
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainMobileActivity : AppCompatActivity(), OnCapabilityChangedListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var capabilityClient: CapabilityClient
    private lateinit var nodeClient: NodeClient
    private lateinit var remoteActivityHelper: RemoteActivityHelper

    private lateinit var btnEmail : TextView
    private lateinit var devpage : TextView
    private lateinit var buttonrefresh : Button

    private lateinit var reviewManager: ReviewManager

    private var wearNodesWithApp: Set<Node>? = null
    private var allConnectedNodes: List<Node>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        capabilityClient = Wearable.getCapabilityClient(this)
        nodeClient = Wearable.getNodeClient(this)
        remoteActivityHelper = RemoteActivityHelper(this)

        binding.remoteOpenButton.setOnClickListener {
            openPlayStoreOnWearDevicesWithoutApp()
        }
        binding.btnRateNow.setOnClickListener {
            showRateDialog()
        }

        // Perform the initial update of the UI
        updateUI()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    // Initial request for devices with our capability, aka, our Wear app installed.
                    findWearDevicesWithApp()
                }
                launch {
                    // Initial request for all Wear devices connected (with or without our capability).
                    // Additional Note: Because there isn't a listener for ALL Nodes added/removed from network
                    // that isn't deprecated, we simply update the full list when the Google API Client is
                    // connected and when capability changes come through in the onCapabilityChanged() method.
                    findAllWearDevices()
                }
            }
        }

        btnEmail = findViewById(R.id.btnEmail)

        btnEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", "support@weartools.com", null))
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }
        devpage = findViewById(R.id.devpage)

        devpage.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                        "https://play.google.com/store/apps/dev?id=5591589606735981545")
                //setPackage("com.android.vending")
            }
            try {
                startActivity(i)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "Device not able to handle this link", Toast.LENGTH_SHORT).show()
            }
        }
        buttonrefresh = findViewById(R.id.buttonrefresh)

        buttonrefresh.setOnClickListener {
            finish()
            startActivity(intent)
        }

    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        capabilityClient.removeListener(this, CAPABILITY_WEAR_APP)
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        capabilityClient.addListener(this, CAPABILITY_WEAR_APP)
    }

    /*
     * Updates UI when capabilities change (install/uninstall wear app).
     */
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        wearNodesWithApp = capabilityInfo.nodes

        lifecycleScope.launch {
            // Because we have an updated list of devices with/without our app, we need to also update
            // our list of active Wear devices.
            findAllWearDevices()
        }
    }

    private suspend fun findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()")

        try {
            val capabilityInfo = capabilityClient
                .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL)
                .await()

            withContext(Dispatchers.Main) {
                Log.d(TAG, "Capability request succeeded.")
                wearNodesWithApp = capabilityInfo.nodes
                Log.d(TAG, "Capable Nodes: $wearNodesWithApp")
                updateUI()
            }
        } catch (cancellationException: CancellationException) {
            // Request was cancelled normally
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.d(TAG, "Capability request failed to return any results.")
        }
    }

    private suspend fun findAllWearDevices() {
        Log.d(TAG, "findAllWearDevices()")

        try {
            val connectedNodes = nodeClient.connectedNodes.await()

            withContext(Dispatchers.Main) {
                allConnectedNodes = connectedNodes
                updateUI()
            }
        } catch (cancellationException: CancellationException) {
            // Request was cancelled normally
        } catch (throwable: Throwable) {
            Log.d(TAG, "Node request failed to return any results.")
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun updateUI() {
        Log.d(TAG, "updateUI()")

        val wearNodesWithApp = wearNodesWithApp
        val allConnectedNodes = allConnectedNodes

        when {
            wearNodesWithApp == null || allConnectedNodes == null -> {
                Log.d(TAG, "Waiting on Results for both connected nodes and nodes with app")
                binding.informationTextView.text = getString(R.string.message_checking)
                binding.remoteOpenButton.isInvisible = true
                binding.buttonrefresh.isVisible = true
            }
            allConnectedNodes.isEmpty() -> {
                Log.d(TAG, "No devices")
                binding.informationTextView.text = getString(R.string.message_no_devices)
                binding.remoteOpenButton.isInvisible = true
                binding.buttonrefresh.isVisible = true
            }
            wearNodesWithApp.isEmpty() -> {
                Log.d(TAG, "Missing on all devices")
                binding.informationTextView.text = getString(R.string.message_missing_all)
                binding.remoteOpenButton.isVisible = true
                binding.buttonrefresh.isInvisible = true
            }
            wearNodesWithApp.size < allConnectedNodes.size -> {
                // TODO: Add your code to communicate with the wear app(s) via Wear APIs
                //       (MessageClient, DataClient, etc.)
                Log.d(TAG, "Installed on some devices")
                binding.informationTextView.text =
                    getString(R.string.message_some_installed, wearNodesWithApp.toString())
                binding.remoteOpenButton.isVisible = true
                binding.buttonrefresh.isVisible = true
            }
            else -> {
                // TODO: Add your code to communicate with the wear app(s) via Wear APIs
                //       (MessageClient, DataClient, etc.)
                Log.d(TAG, "Installed on all devices")
                binding.informationTextView.text =
                    getString(R.string.message_all_installed, wearNodesWithApp.toString())
                binding.remoteOpenButton.isInvisible = true
                binding.buttonrefresh.isInvisible = true
            }
        }
    }

    private fun openPlayStoreOnWearDevicesWithoutApp() {
        Log.d(TAG, "openPlayStoreOnWearDevicesWithoutApp()")

        val wearNodesWithApp = wearNodesWithApp ?: return
        val allConnectedNodes = allConnectedNodes ?: return

        // Determine the list of nodes (wear devices) that don't have the app installed yet.
        val nodesWithoutApp = allConnectedNodes - wearNodesWithApp

        Log.d(TAG, "Number of nodes without app: " + nodesWithoutApp.size)
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(PLAY_STORE_APP_URI))

        // In parallel, start remote activity requests for all wear devices that don't have the app installed yet.
        nodesWithoutApp.forEach { node ->
            lifecycleScope.launch {
                try {
                    remoteActivityHelper
                        .startRemoteActivity(
                            targetIntent = intent,
                            targetNodeId = node.id
                        )
                        .await()

                    Toast.makeText(
                        this@MainMobileActivity,
                        getString(R.string.store_request_successful),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (cancellationException: CancellationException) {
                    // Request was cancelled normally
                } catch (throwable: Throwable) {
                    Toast.makeText(
                        this@MainMobileActivity,
                        getString(R.string.store_request_unsuccessful),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }



    private fun showRateDialog() {
        reviewManager = ReviewManagerFactory.create(this@MainMobileActivity)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow = reviewManager.launchReviewFlow(this@MainMobileActivity, reviewInfo)
                flow.addOnCompleteListener {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.weartools.weekdayutccomp")
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainMobileActivity"

        // Name of capability listed in Wear app's wear.xml.
        // IMPORTANT NOTE: This should be named differently than your Phone app's capability.
        private const val CAPABILITY_WEAR_APP = "verify_remote_example_wear_app"

        // Links to Wear app (Play Store).
        // TODO: Replace with your links/packages.
        private const val PLAY_STORE_APP_URI =
            "market://details?id=com.weartools.weekdayutccomp"
    }
}
