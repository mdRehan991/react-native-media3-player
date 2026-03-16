import * as React from 'react';
import {ViewStyle} from 'react-native';

export interface Media3PlayerProps {
  style?: ViewStyle | ViewStyle[];
  source: {uri: string};
  autoplay?: boolean;
  play?: boolean;
  mute?: boolean;
  onReady?: () => void;
  onEnd?: () => void;
  onError?: (event: {nativeEvent: {message: string}}) => void;
}

declare const Media3Player: React.FC<Media3PlayerProps>;
export default Media3Player;
