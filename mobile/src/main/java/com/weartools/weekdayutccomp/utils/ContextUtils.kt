package com.weartools.weekdayutccomp.utils

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.weartools.weekdayutccomp.R

fun Context.openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

fun Context.openPlayStorePortfolio() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=5591589606735981545")))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=5591589606735981545")))
    }
}

fun Context.openTelegramSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_telegram))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openGithubSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_github))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openBuyMeACoffeeSocialLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.social_coffee))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openPrivacyPolicyLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_privacy))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.sendFeedbackEmail() {
    val emailIntent = Intent(Intent.ACTION_SENDTO,
        Uri.fromParts("mailto", getString(R.string.support), null))
    startActivity(Intent.createChooser(emailIntent, "Send email..."))
}

fun Context.openAmoledWebPage() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_website))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.openGuideLink() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_guide))))
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG,"No Browser available")
    }
}

fun Context.uninstallApp(context: Context) {
    val packageManager: PackageManager = context.packageManager
    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
    intent.data = Uri.parse("package:$packageName")

    context.startActivity(intent)
}