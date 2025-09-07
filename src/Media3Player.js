import React from 'react';
import {requireNativeComponent, StyleSheet} from 'react-native';

// Link to native view
const NativeMedia3Player = requireNativeComponent('Media3PlayerView');

/**
 * Media3Player
 *
 * Props:
 * - source: { uri: string } ✅ required
 * - autoplay: boolean
 * - play: boolean
 * - mute: boolean
 * - onReady: function
 * - onEnd: function
 * - onError: function
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
  if (!source || !source.uri) {
    console.warn('Media3Player: "source" prop with a valid "uri" is required.');
    return null;
  }

  return (
    <NativeMedia3Player
      style={[styles.default, style]} // ensure style is never undefined
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

const styles = StyleSheet.create({
  default: {
    width: '100%',
    height: 250,
    backgroundColor: 'black',
  },
});
