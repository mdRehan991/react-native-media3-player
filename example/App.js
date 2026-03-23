import React, {useState} from 'react';
import {View, Button, StyleSheet, Alert} from 'react-native';
import Media3Player from 'react-native-media3-player';

const AUTO_PLAY = false;

const App = () => {
  const [isMuted, setIsMuted] = useState(false);
  const [isPlaying, setIsPlaying] = useState(AUTO_PLAY);

  // Sample URLs for testing

  const source = {
    uri: 'https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    ads: {
      adTagUrl:
        'https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpreonly&ciu_szs=300x250%2C728x90&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&correlator=',
    },
  };

  // const source = {
  //   uri: 'https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd',
  //   drm: {
  //     licenseUrl: 'https://proxy.uat.widevine.com/proxy?provider=widevine_test',
  //   },
  // };

  // const source = {
  //   uri: 'https://storage.googleapis.com/shaka-demo-assets/angel-one-widevine/dash.mpd',
  //   type: 'dash', // Stream type: 'dash', 'hls', 'mp4'
  //   drm: {
  //     licenseUrl: 'https://cwip-shaka-proxy.appspot.com/no_auth',
  //   },
  //   ads: {
  //     adTagUrl:
  //       'https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpreonly&ciu_szs=300x250%2C728x90&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&correlator=',
  //   },
  // };

  return (
    <View style={styles.container}>
      <Media3Player
        mute={isMuted}
        autoplay={true}
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
