/*
 * “Commons Clause” License Condition v1.0

 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.

 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.

 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.

 * Software: Complications Suite - Wear OS
 * License: Apache-2.0
 * Licensor: amoledwatchfaces™

 * Copyright (c) 2024 amoledwatchfaces™

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp.activity

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.databinding.FlashlightBinding


class FlashlightActivity : Activity() {

    private lateinit var binding: FlashlightBinding
    private var flashlightstate: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FlashlightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lp: WindowManager.LayoutParams = this@FlashlightActivity.window.attributes
        val brightness = 1.0f
        lp.screenBrightness = brightness
        this@FlashlightActivity.window.attributes = lp
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        flashlightstate = 1

        binding.flashlightContainer.setOnClickListener {
            changeImage()
        }
    }

    override fun onResume() {
        super.onResume()
        when (flashlightstate) {
            3 -> {
                val lp: WindowManager.LayoutParams = this@FlashlightActivity.window.attributes
                val brightness = 1.0f
                lp.screenBrightness = brightness
                this@FlashlightActivity.window.attributes = lp
                binding.flashlightContainer.setBackgroundResource(R.drawable.bg_white)
                binding.flashlightImage.setImageResource(R.drawable.ic_flashlight_on_1)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Keep screen -> ON
                flashlightstate = 1
            }
        }

    }

    private fun changeImage() {
        when (flashlightstate) {
            1 -> {
                binding.flashlightContainer.setBackgroundResource(R.drawable.bg_red)
                binding.flashlightImage.setImageResource(R.drawable.ic_flashlight_on_2)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Keep screen -> ON
                flashlightstate = 2
            }
            2 -> {
                val lp: WindowManager.LayoutParams = this@FlashlightActivity.window.attributes
                val brightness = 0.2f
                lp.screenBrightness = brightness
                this@FlashlightActivity.window.attributes = lp
                binding.flashlightContainer.setBackgroundResource(R.drawable.bg_black)
                binding.flashlightImage.setImageResource(R.drawable.ic_flashlight_off)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Keep screen -> OFF
                flashlightstate = 3
            }
            else -> {
                val lp: WindowManager.LayoutParams = this@FlashlightActivity.window.attributes
                val brightness = 1.0f
                lp.screenBrightness = brightness
                this@FlashlightActivity.window.attributes = lp
                binding.flashlightContainer.setBackgroundResource(R.drawable.bg_white)
                binding.flashlightImage.setImageResource(R.drawable.ic_flashlight_on_1)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Keep screen -> ON
                flashlightstate = 1
            }
        }
    }

}