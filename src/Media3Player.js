import React from 'react';
import {StyleSheet} from 'react-native';
// Import the native codegen-wrapped player view component
import NativeMedia3PlayerView from './Media3PlayerNativeComponent';

/**
 * Media3Player React component
 *
 * Props:
 * - style: (optional) additional style overrides
 * - source: { uri: string } – required, the media URI to load
 * - autoplay: (optional, default false) – whether playback should start automatically when ready
 * - play: (optional, default false) – whether playback should currently be running (true = play, false = pause)
 * - mute: (optional, default false) – whether audio should be muted
 * - onReady: callback when player is ready
 * - onEnd: callback when playback has ended
 * - onError: callback when an error occurs during playback or loading
 */
export default function Media3Player({
  style,
  source,
  autoplay = false,
  play = false,
  mute = false,
  onReady,
  onEnd,
  onError,
}) {
  // Validate that source.uri is provided; warn and render nothing if missing
  if (!source || !source.uri) {
    console.warn('Media3Player: "source.uri" is required.');
    return null;
  }

  // Render the native player view, passing all relevant props and composing the style
  return (
    <NativeMedia3PlayerView
      style={[styles.default, style]}
      source={source}
      autoplay={autoplay}
      play={play}
      mute={mute}
      onReady={onReady}
      onEnd={onEnd}
      onError={onError}
    />
  );
}

// Default styling applied to the player view unless overridden via 'style' prop
const styles = StyleSheet.create({
  default: {
    width: '100%', // Occupy full width of parent
    height: 250, // Fixed height for the player
    backgroundColor: 'black', // Default background color
  },
});
