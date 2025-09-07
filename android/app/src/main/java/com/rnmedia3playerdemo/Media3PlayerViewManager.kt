package com.rnmedia3playerdemo

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

/**
 * Media3PlayerViewManager
 *
 * Exposes the native view to React Native.
 * Props: source { uri: string }, autoplay, play, mute
 * Events: onReady, onEnd, onError
 */
class Media3PlayerViewManager(private val reactContext: ReactApplicationContext) :
    SimpleViewManager<Media3PlayerView>() {

    override fun getName(): String = "Media3PlayerView"

    override fun createViewInstance(reactContext: ThemedReactContext): Media3PlayerView {
        return Media3PlayerView(reactContext)
    }

    @ReactProp(name = "source")
    fun setSource(view: Media3PlayerView, source: ReadableMap?) {
        val uri: String? = source?.getString("uri")
        if (!uri.isNullOrEmpty()) {
            view.setSource(uri)
        }
    }

    @ReactProp(name = "autoplay", defaultBoolean = false)
    fun setAutoplay(view: Media3PlayerView, autoplay: Boolean) {
        view.setAutoplay(autoplay)
    }

    @ReactProp(name = "play", defaultBoolean = false)
    fun setPlay(view: Media3PlayerView, play: Boolean) {
        view.setPlay(play)
    }

    @ReactProp(name = "mute", defaultBoolean = false)
    fun setMute(view: Media3PlayerView, mute: Boolean) {
        view.setMute(mute)
    }

    override fun onDropViewInstance(view: Media3PlayerView) {
        super.onDropViewInstance(view)
        view.releasePlayer()
    }

    // 👇 Expose custom events to JS
    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        return mutableMapOf(
            "onReady" to mapOf("registrationName" to "onReady"),
            "onEnd" to mapOf("registrationName" to "onEnd"),
            "onError" to mapOf("registrationName" to "onError")
        )
    }
}
