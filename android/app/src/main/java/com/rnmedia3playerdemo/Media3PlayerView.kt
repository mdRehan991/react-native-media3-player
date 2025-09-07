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
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.bridge.Arguments

/**
 * Media3PlayerView
 *
 * Handles ExoPlayer playback and exposes events to React Native:
 * - onReady
 * - onEnd
 * - onError
 */
class Media3PlayerView(context: Context) : FrameLayout(context) {
    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView
    private var sourceUri: String? = null
    private var autoplay: Boolean = false
    private var play: Boolean = false
    private var mute: Boolean = false

    init {
        playerView = PlayerView(context)
        playerView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        addView(playerView)
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            playerView.player = exoPlayer

            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            sendEvent("onReady")
                            if (autoplay) {
                                exoPlayer?.playWhenReady = true
                                exoPlayer?.play()
                                Log.d("Media3Player", "Autoplay triggered when player became ready")
                            }
                        }
                        Player.STATE_ENDED -> {
                            sendEvent("onEnd")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    sendEvent("onError", error.message ?: "Unknown error")
                }
            })
        }
    }

    fun setSource(uriString: String?) {
        if (uriString.isNullOrEmpty()) return
        sourceUri = uriString

        initializePlayer()

        val item = MediaItem.fromUri(Uri.parse(uriString))
        exoPlayer?.setMediaItem(item)
        exoPlayer?.prepare()
    }

    fun setAutoplay(value: Boolean) {
        autoplay = value
    }

    fun setPlay(value: Boolean) {
        play = value
        // if you want to disable autoplay with play (prop) -
        // if (exoPlayer?.playbackState == Player.STATE_READY)
            if (play) {
                exoPlayer?.play()
            } else {
                exoPlayer?.pause()
            }
        // }
    }

    fun setMute(value: Boolean) {
        mute = value
        exoPlayer?.volume = if (mute) 0f else 1f
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    // 👇 helper function to send events to React Native
    private fun sendEvent(eventName: String, message: String? = null) {
        val reactContext = context as? ReactContext ?: return
        val event = Arguments.createMap()
        message?.let { event.putString("message", it) }
        reactContext
            .getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(id, eventName, event)
    }
}
