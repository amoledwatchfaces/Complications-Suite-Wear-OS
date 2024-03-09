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