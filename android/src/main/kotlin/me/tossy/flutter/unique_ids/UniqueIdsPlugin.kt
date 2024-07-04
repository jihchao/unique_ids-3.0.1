package me.tossy.flutter.unique_ids

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException
import java.util.*
import android.os.*

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin

class UniqueIdsPlugin : FlutterPlugin, MethodCallHandler {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext()
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "unique_ids")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when {
            call.method == "adId" -> {
                val backgroundThread = object : Thread("adId") {
                    override fun run() {
                        var id: String = "";

                        try {
                            id = getAdId(context)
                        } catch (e: IOException) {
                            // Unrecoverable error connecting to Google Play services (e.g.,
                            // the old version of the service doesn't support getting AdvertisingId).
                        } catch (e: GooglePlayServicesNotAvailableException) {
                            // Google Play services is not available entirely.
                        }
                        success(result, id)
                    }
                }
                backgroundThread.start()
            }

            call.method == "uuid" -> result.success(UUID.randomUUID().toString())
            else -> result.notImplemented()
        }
    }

    fun getAdId(context: Context): String {
        val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
        val isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled
        // if (isLimitAdTrackingEnabled) {
        return adInfo.id
        // }
        return ""
    }

    fun success(result: Result, id: String) {
        Handler(Looper.getMainLooper()).post {
            result.success(id)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
