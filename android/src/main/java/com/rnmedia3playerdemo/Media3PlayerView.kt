package com.rnmedia3playerdemo

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.FrameLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event

/**
 * Custom view that wraps ExoPlayer and PlayerView, integrating with React Native.
 */
class Media3PlayerView(context: Context) : FrameLayout(context) {
    // ExoPlayer instance to handle media playback
    private var exoPlayer: ExoPlayer? = null
    // PlayerView to display video content
    private var playerView: PlayerView
    // Currently loaded media source URI
    private var sourceUri: String? = null
    // Should video autoplay when ready
    private var autoplay: Boolean = false
    // Play state controlled by the JS prop
    private var play: Boolean = false
    // Mute state controlled by the JS prop
    private var mute: Boolean = false

    /**
     * Constructor: initialize the PlayerView and attach it to the layout.
     */
    init {
        playerView = PlayerView(context)
        playerView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        addView(playerView)
    }

    /**
     * Initializes ExoPlayer and attaches a listener for player events.
     * Ensures only one instance exists.
     */
    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            playerView.player = exoPlayer

            // Listen for playback state changes and errors
            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            // Notify React Native that player is ready
                            sendEvent("topReady")
                            // If autoplay is enabled, start playback when ready
                            if (autoplay) {
                                exoPlayer?.playWhenReady = true
                                exoPlayer?.play()
                                Log.d("Media3Player", "Autoplay triggered when player became ready")
                            }
                        }
                        Player.STATE_ENDED -> {
                            // Notify React Native that playback has ended
                            sendEvent("topEnd")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    // Report playback errors to React Native
                    sendEvent("topError", error.message ?: "Unknown error")
                }
            })
        }
    }

    /**
     * Sets the video source from a URI string and prepares the player.
     * @param uriString Video source URI as a string
     */
    fun setSource(uriString: String?) {
        if (uriString.isNullOrEmpty()) return
        sourceUri = uriString

        // Ensure player is initialized
        initializePlayer()

        // Set and prepare the media item for playback
        val item = MediaItem.fromUri(Uri.parse(uriString))
        exoPlayer?.setMediaItem(item)
        exoPlayer?.prepare()
    }

    /**
     * Sets the autoplay flag to control whether playback starts when player is ready.
     * @param value Boolean value from JS prop
     */
    fun setAutoplay(value: Boolean) {
        autoplay = value
    }

    /**
     * Set the play state of the player.
     * If true, the player will start/resume playback; otherwise, pause.
     * @param value Boolean value from JS prop
     */
    fun setPlay(value: Boolean) {
        play = value
        // Optionally: Disabling autoplay if play is used directly
        // Only play or pause if player is ready
        if (play) {
            exoPlayer?.play()
        } else {
            exoPlayer?.pause()
        }
    }

    /**
     * Sets the mute state of the player.
     * @param value Boolean indicating if the audio should be muted
     */
    fun setMute(value: Boolean) {
        mute = value
        exoPlayer?.volume = if (mute) 0f else 1f
    }

    /**
     * Releases the player and cleans up resources.
     * This should be called when the view is destroyed.
     */
    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    /**
     * Helper function to send events and optional data payloads to React Native JS side.
     * @param eventName Name of the event (e.g., "topReady", "topError")
     * @param message Optional message string to pass in the event payload
     */
    private fun sendEvent(eventName: String, message: String? = null) {
        val reactContext = context as? ReactContext ?: return
        val surfaceId = UIManagerHelper.getSurfaceId(this)
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
        val payload = Arguments.createMap()
        message?.let { payload.putString("message", it) }
        eventDispatcher?.dispatchEvent(
            Media3Event(surfaceId, id, eventName, payload)
        )
    }

    /**
     * Custom event class to bridge native events to React Native UIManager system.
     */
    private class Media3Event(
        surfaceId: Int,
        viewId: Int,
        private val name: String,
        private val payload: WritableMap
    ) : Event<Media3Event>(surfaceId, viewId) {
        override fun getEventName(): String = name
        override fun getEventData(): WritableMap = payload
    }
}
