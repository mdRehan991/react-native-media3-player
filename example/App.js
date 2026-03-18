import React, {useState} from 'react';
import {View, Button, StyleSheet, Alert} from 'react-native';
import Media3Player from 'react-native-media3-player';

const AUTO_PLAY = false;

const App = () => {
  const [isMuted, setIsMuted] = useState(false);
  const [isPlaying, setIsPlaying] = useState(AUTO_PLAY);

  // Sample URLs for testing

  // const source = {
  //   uri: 'https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  // };

  // const source = {
  //   uri: 'https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd',
  //   drm: {
  //     licenseUrl: 'https://proxy.uat.widevine.com/proxy?provider=widevine_test',
  //   },
  // };

  const source = {
    uri: 'https://storage.googleapis.com/shaka-demo-assets/angel-one-widevine/dash.mpd',
    type: 'dash', // Stream type: 'dash', 'hls', 'mp4'
    drm: {
      licenseUrl: 'https://cwip-shaka-proxy.appspot.com/no_auth',
    },
  };

  return (
    <View style={styles.container}>
      <Media3Player
        mute={isMuted}
        autoplay={false}
        play={isPlaying}
        source={source}
        style={styles.player}
        onEnd={() => Alert.alert('Video ended')}
        onReady={() => console.log('Player is ready')}
        onError={e => Alert.alert('Error', e.message)}
      />

      {/* Controls */}
      <View style={styles.controls}>
        <Button
          title={isPlaying ? 'Pause' : 'Play'}
          onPress={() => setIsPlaying(prev => !prev)}
        />
        <Button
          title={isMuted ? 'Unmute' : 'Mute'}
          onPress={() => setIsMuted(prev => !prev)}
        />
      </View>
    </View>
  );
};

export default App;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#000',
  },
  player: {
    height: 250,
    width: '100%',
    backgroundColor: '#000',
  },
  controls: {
    width: '60%',
    marginTop: 20,
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
});
