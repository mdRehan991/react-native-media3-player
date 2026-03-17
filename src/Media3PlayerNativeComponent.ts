// Import the base ViewProps type from React Native to extend the native component's props
import type {ViewProps} from 'react-native';
// Import the DirectEventHandler type used for native-to-JS event callback typings
import type {DirectEventHandler} from 'react-native/Libraries/Types/CodegenTypes';
// Import codegenNativeComponent to register the native UI component for use in React Native
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

type HeaderEntry = Readonly<{
  key: string;
  value: string;
}>;

type DRMConfig = Readonly<{
  licenseUrl: string;
  headers?: ReadonlyArray<HeaderEntry>;
}>;
// Source defines the media source for the player component.
// - uri: The URI of the media to be played (required).
// - drm: (Optional) DRM configuration for protected content.
type Source = Readonly<{
  uri: string;
  drm?: DRMConfig;
}>;
// Event type emitted when the player is ready
type OnReadyEvent = Readonly<{}>;
// Event type emitted when playback reaches the end
type OnEndEvent = Readonly<{}>;
// Event type emitted when an error occurs during playback
type OnErrorEvent = Readonly<{
  message: string; // Error message describing what went wrong
}>;

// Props supported by the native Media3PlayerView component
export interface NativeProps extends ViewProps {
  // Source URI for the media to play
  source?: Source;
  // Whether playback should start automatically when ready
  autoplay?: boolean;
  // Whether playback should currently be running (true = play, false = pause)
  play?: boolean;
  // Whether audio should be muted
  mute?: boolean;

  // Callback invoked when the player is ready to play
  onReady?: DirectEventHandler<OnReadyEvent>;
  // Callback invoked when playback reaches the end of the media
  onEnd?: DirectEventHandler<OnEndEvent>;
  // Callback invoked when an error occurs in playback or loading
  onError?: DirectEventHandler<OnErrorEvent>;
}

// Create and export the code-generated native component using the props interface
export default codegenNativeComponent<NativeProps>('Media3PlayerView');
