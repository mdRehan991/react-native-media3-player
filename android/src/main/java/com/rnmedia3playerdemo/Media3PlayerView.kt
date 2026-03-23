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
import androidx.media3.common.C
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.ads.AdsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

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
    // IMA Ads loader
    private var adsLoader: ImaAdsLoader? = null
    // Currently loaded ad tag URL
    private var currentAdTagUrl: String? = null

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
     * Initializes the IMA Ads loader and sets it to the player.
     * @param adTagUrl The URL of the ad tag to load.
     */
    private fun initializeAdsLoader(adTagUrl: String) {
        if (adsLoader == null || currentAdTagUrl != adTagUrl) {
            adsLoader?.release()

            adsLoader = ImaAdsLoader.Builder(context).build()
            currentAdTagUrl = adTagUrl

            adsLoader?.setPlayer(exoPlayer)
        }
    }

    /**
     * Initializes ExoPlayer and attaches a listener for player events.
     * Ensures only one instance exists.
     */
    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            playerView.player = exoPlayer

            // Set the IMA Ads loader to the player
            adsLoader?.setPlayer(exoPlayer)

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
     * Maps a media type string from JavaScript to one of Media3's content type constants.
     *
     * This function interprets the "type" property provided from React Native JS props ("dash", "hls", "mp4")
     * and translates it to the corresponding Media3 content type constant used by ExoPlayer.
     * Returns null if the string is null, empty, or does not match a known type.
     *
     * @param type Optional string type from JS ("dash", "hls", "mp4")
     * @return Media3 content type constant (C.CONTENT_TYPE_DASH, etc.), or null if unrecognized
     */
    private fun mapTypeFromJS(type: String?): Int? {
        return when (type?.lowercase()) {
            "dash" -> C.CONTENT_TYPE_DASH
            "hls" -> C.CONTENT_TYPE_HLS
            "mp4" -> C.CONTENT_TYPE_OTHER
            else -> null
        }
    }

    /**
     * Infers the content type of a given URI for media playback.
     *
     * This method first uses Media3's built-in Util.inferContentType to try
     * to detect the type (DASH, HLS, SmoothStreaming, or Other) based on file extension or URI.
     * If this detection fails (returns CONTENT_TYPE_OTHER), it falls back to checking
     * for well-known streaming manifest extensions (.mpd for DASH, .m3u8 for HLS)
     * in the URI string. This fallback is important for cases where extensions are
     * not present (e.g. signed URLs or tokens).
     *
     * @param uri The Uri of the media source.
     * @return The detected content type as one of C.CONTENT_TYPE_* constants.
     */
    private fun inferContentTypeSafe(uri: Uri): Int {

        // Use Media3's built-in Util.inferContentType to try to detect the type.
        val detectedType = Util.inferContentType(uri)

        // If Media3 was able to determine the type, return it early.
        if (detectedType != C.CONTENT_TYPE_OTHER) {
            return detectedType
        }

        // Convert the Uri to a string
        val url = uri.toString()

        // Fallback: manually check for known manifest extensions in the URL
        // This helps handle sources where the content type can't be inferred automatically.
        return when {
            url.contains(".mpd", ignoreCase = true) -> C.CONTENT_TYPE_DASH
            url.contains(".m3u8", ignoreCase = true) -> C.CONTENT_TYPE_HLS
            else -> C.CONTENT_TYPE_OTHER
        }
    }

    /**
     * Builds a MediaSource instance required by ExoPlayer based on the content type of the given URI.
     * Handles DASH, HLS, SmoothStreaming, and generic progressive streams.
     *
     * @param uri The Uri of the media to play.
     * @param mediaItem The MediaItem (with possible DRM/config) for playback.
     * @return The constructed MediaSource for the ExoPlayer.
     */
    private fun buildMediaSource(
        uri: Uri,
        mediaItem: MediaItem,
        type: String?
    ): MediaSource {

        // Create a DefaultHttpDataSourceFactory for the MediaSource.
        // This is used to fetch the media content from the network.
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        // Check if a type was explicitly passed from JS (e.g., "hls", "dash", "mp4") and map to Media3 type constant.
        val overrideType = mapTypeFromJS(type)
        // Otherwise, infer the content type from the URI (file extension or stream manifest).
        val detectedType = inferContentTypeSafe(uri)
        // Use the JS override type if available, else fall back to detected type.
        val finalType = overrideType ?: detectedType

        // Log the content type detection process for debugging purposes.
        Log.d(
            "Media3Player",
            "Type → override: $overrideType detected: $detectedType final: $finalType url: $uri"
        )

        // Determine content type and return the appropriate MediaSource.
        // This is used to create the appropriate MediaSource for the ExoPlayer.
        return when (finalType) {
            // DASH (MPD) stream
            C.CONTENT_TYPE_DASH -> {
                DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            // HLS (M3U8) stream
            C.CONTENT_TYPE_HLS -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            // SmoothStreaming (ISM) stream
            C.CONTENT_TYPE_SS -> {
                SsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            // Progressive HTTP file (MP4, MP3, etc.)
            C.CONTENT_TYPE_OTHER -> {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            // Fallback to progressive for unknown types
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
        }
    }


    /**
     * Sets the video source (with optional DRM support) and prepares the player.
     * Called from the ViewManager when the JS "source" prop changes.
     *
     * @param uriString The URI of the media source to play.
     * @param licenseUrl If provided, enables DRM playback with this license URL.
     * @param headers Optional headers to add to DRM license requests.
     */
    fun setSource(
        uriString: String?,
        type: String?,
        licenseUrl: String?,
        headers: Map<String, String>?,
        adTagUrl: String?
    ) {
        // Return early if no valid URI is provided
        if (uriString.isNullOrEmpty()) return
        
        // Store the URI string for reference
        sourceUri = uriString

        
        // Release and reset the adsLoader if the ad tag URL has changed -
        // to prevent playing old ads or memory leaks.
        if (currentAdTagUrl != adTagUrl) {
            adsLoader?.setPlayer(null)
            adsLoader?.release()
            adsLoader = null
        }

        // Ensure the ExoPlayer instance is initialized before use
        initializePlayer()

        // Parse the URI string into a Uri object
        val uri = Uri.parse(uriString)

        // Build the MediaItem, adding DRM configuration if a license URL is given.
        // This includes setting the license URL and multi-session support for DRM streams.
        // If no license URL is provided, a simple MediaItem is created from the URI.
        val mediaItem =
            if (!licenseUrl.isNullOrEmpty()) {
                val drmBuilder = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(licenseUrl) // set the license URL
                    .setMultiSession(true) // allow multiple DRM sessions if the stream requires them

                headers?.let {
                    drmBuilder.setLicenseRequestHeaders(it)
                }

                MediaItem.Builder()
                    .setUri(uri)
                    .setDrmConfiguration(drmBuilder.build())
                    .build()
            } else {
                // No DRM: simple MediaItem from URI
                MediaItem.fromUri(uri)
            }

        // If an ad tag URL is provided, configure the player for ad playback using IMA:
        // - Initialize the AdsLoader (only if not already done for the current ad tag)
        // - Build a MediaItem that includes the ads configuration
        // - Create a DefaultMediaSourceFactory with ads and ad view providers
        // - Set the media source with ads on the player
        if (!adTagUrl.isNullOrEmpty()) {
            initializeAdsLoader(adTagUrl)

            val mediaItemWithAds = mediaItem.buildUpon()
                .setAdsConfiguration(
                    MediaItem.AdsConfiguration.Builder(Uri.parse(adTagUrl)).build()
                )
                .build()

            val mediaSourceFactory = DefaultMediaSourceFactory(DefaultHttpDataSource.Factory())
                .setAdsLoaderProvider { adsLoader }
                .setAdViewProvider(playerView)

            exoPlayer?.setMediaSource(
                mediaSourceFactory.createMediaSource(mediaItemWithAds)
            )
        } else {
            // If no ad tag URL, simply build and set the normal content media source
            val mediaSource = buildMediaSource(uri, mediaItem, type)
            exoPlayer?.setMediaSource(mediaSource)
        }
        // Prepare the player for playback (loads media and notifies listeners)
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
        // Release the IMA Ads loader
        adsLoader?.setPlayer(null)
        adsLoader?.release()
        adsLoader = null

        // Release the ExoPlayer
        exoPlayer?.release()
        exoPlayer = null
    }

    /**
     * Called when the view is detached from the window.
     * Releases the player and cleans up resources.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releasePlayer()
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
