# 📽️ Media3Player

> A lightweight, high-performance React Native video player powered by Android Media3 (ExoPlayer). Fully customizable and supports autoplay, mute, and event handling. This is **Android** only package.

---

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Props](#props)
- [DRM Support (Widevine)](#drm-support-widevine)
- [Events](#events)
- [TypeScript Support](#typescript-support)
- [Examples](#examples)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

---

## Installation

**Install via npm:**

```bash
npm install react-native-media3-player
```

**Or yarn:**

```bash
yarn add react-native-media3-player
```

> Requires **React Native >= 0.70**

---

**Note:** To use a specific Media3 version, add or update the following in your **android/build.gradle** (Project-level) file:

```groovy
buildscript {
    ext {
        media3Version = "1.4.1" // Set your desired Media3 version
    }
}
```

Be sure to sync your project after making this change.

---

## Usage

**Basic (non-DRM):**

```jsx
import React from 'react';
import {View} from 'react-native';
import Media3Player from 'react-native-media3-player';

export default function App() {
  return (
    <View style={{flex: 1}}>
      <Media3Player
        style={{width: '100%', height: 250}}
        source={{uri: 'https://example.com/video.mp4'}}
        autoplay={true}
        play={true}
        mute={false}
        onReady={() => console.log('Player is ready')}
        onEnd={() => console.log('Video ended')}
        onError={error => console.log('Player error:', error.message)}
      />
    </View>
  );
}
```

**DRM-protected content (Widevine):**

```jsx
import React from 'react';
import {View} from 'react-native';
import Media3Player from 'react-native-media3-player';

export default function App() {
  return (
    <View style={{flex: 1}}>
      <Media3Player
        style={{width: '100%', height: 250}}
        source={{
          uri: 'https://storage.googleapis.com/shaka-demo-assets/angel-one-widevine/dash.mpd',
          drm: {
            licenseUrl: 'https://cwip-shaka-proxy.appspot.com/no_auth',
          },
        }}
        autoplay={true}
        play={true}
        onReady={() => console.log('Player is ready')}
        onEnd={() => console.log('Video ended')}
        onError={error => console.log('Player error:', error.message)}
      />
    </View>
  );
}
```

---

## Props

| Prop       | Type                         | Default                          | Description                                                              |
| ---------- | ---------------------------- | -------------------------------- | ------------------------------------------------------------------------ |
| `source`   | `Source`                     | required                         | Video source object. Must include a valid `uri`. Supports optional `drm` config. |
| `autoplay` | `boolean`                    | `false`                          | Automatically start playback when the video is ready.                    |
| `play`     | `boolean`                    | `false`                          | Controls whether the player is playing. Overrides autoplay.              |
| `mute`     | `boolean`                    | `false`                          | Mutes or unmutes the video.                                              |
| `style`    | `ViewStyle` or `ViewStyle[]` | `{ width: '100%', height: 250 }` | Styling for the player container.                                        |

### `Source` Object

| Property | Type        | Required | Description                                          |
| -------- | ----------- | -------- | ---------------------------------------------------- |
| `uri`    | `string`    | Yes      | The URI of the media to play (MP4, DASH, HLS, etc.). |
| `drm`    | `DRMConfig` | No       | DRM configuration for protected content.             |

### `DRMConfig` Object

| Property     | Type                              | Required | Description                                           |
| ------------ | --------------------------------- | -------- | ----------------------------------------------------- |
| `licenseUrl` | `string`                          | Yes      | The Widevine license server URL.                      |
| `headers`    | `Array<{ key: string, value: string }>` | No       | Custom headers to include in the DRM license request. |

---

## DRM Support (Widevine)

This library supports **Widevine DRM** for playing protected DASH streams on Android. To enable DRM, pass a `drm` object inside the `source` prop.

### Basic DRM Playback

```jsx
<Media3Player
  source={{
    uri: 'https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd',
    drm: {
      licenseUrl: 'https://proxy.uat.widevine.com/proxy?provider=widevine_test',
    },
  }}
  autoplay
  play
/>
```

### DRM with Custom License Headers

Some license servers require authentication tokens or custom headers. Pass them as an array of `{ key, value }` objects:

```jsx
<Media3Player
  source={{
    uri: 'https://example.com/protected-stream/manifest.mpd',
    drm: {
      licenseUrl: 'https://license.example.com/widevine',
      headers: [
        {key: 'Authorization', value: 'Bearer <your-token>'},
        {key: 'X-Custom-Header', value: 'custom-value'},
      ],
    },
  }}
  autoplay
  play
  onError={e => console.error('DRM error:', e.message)}
/>
```

### Notes

- DRM is currently supported on **Android only** (Widevine L1/L3 depending on device).
- The `source.uri` should point to a DASH (`.mpd`) stream encrypted with Widevine.
- Multi-session DRM is enabled by default for streams that require it.
- If the license request fails, the `onError` callback will fire with the error details.

---

## Events

| Event     | Callback Signature                     | Description                                                                            |
| --------- | -------------------------------------- | -------------------------------------------------------------------------------------- |
| `onReady` | `() => void`                           | Fired when the player is ready to play.                                                |
| `onEnd`   | `() => void`                           | Fired when the video reaches the end.                                                  |
| `onError` | `(error: { message: string }) => void` | Fired when the player encounters an error. Error object includes a `message` property. |

---

## TypeScript Support

This library includes TypeScript definitions. Example:

```ts
import React from 'react';
import {ViewStyle} from 'react-native';
import Media3Player, {Media3PlayerProps} from 'react-native-media3-player';

const props: Media3PlayerProps = {
  source: {uri: 'https://example.com/video.mp4'},
  autoplay: true,
  play: true,
  mute: false,
  style: {width: '100%', height: 250},
  onReady: () => console.log('Ready'),
  onEnd: () => console.log('End'),
  onError: err => console.log(err.message),
};
```

**With DRM:**

```ts
const drmProps: Media3PlayerProps = {
  source: {
    uri: 'https://storage.googleapis.com/shaka-demo-assets/angel-one-widevine/dash.mpd',
    drm: {
      licenseUrl: 'https://cwip-shaka-proxy.appspot.com/no_auth',
      headers: [
        {key: 'Authorization', value: 'Bearer my-token'},
      ],
    },
  },
  autoplay: true,
  play: true,
  style: {width: '100%', height: 250},
  onError: err => console.log('DRM Error:', err.message),
};
```

---

## Examples

**Basic Player:**

```jsx
<Media3Player source={{uri: 'https://example.com/video.mp4'}} />
```

**Autoplay & Mute:**

```jsx
<Media3Player source={{uri: 'https://example.com/video.mp4'}} autoplay mute />
```

**Event Handling:**

```jsx
<Media3Player
  source={{uri: 'https://example.com/video.mp4'}}
  onReady={() => console.log('Ready')}
  onEnd={() => console.log('End')}
  onError={err => console.error(err.message)}
/>
```

**Widevine DRM Stream:**

```jsx
<Media3Player
  source={{
    uri: 'https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd',
    drm: {
      licenseUrl: 'https://proxy.uat.widevine.com/proxy?provider=widevine_test',
    },
  }}
  autoplay
  play
/>
```

**DRM with License Headers:**

```jsx
<Media3Player
  source={{
    uri: 'https://example.com/protected/manifest.mpd',
    drm: {
      licenseUrl: 'https://license.example.com/widevine',
      headers: [
        {key: 'Authorization', value: 'Bearer my-token'},
      ],
    },
  }}
  autoplay
  play
  onError={err => console.error('DRM Error:', err.message)}
/>
```

---

## Contributing

We welcome contributions!

1. Fork the repo
2. Create a branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'Add feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

> Please follow [Semantic Versioning](https://semver.org/) for commits.

---

## License

MIT © [Mohammad Rehan](https://github.com/mdRehan991)
