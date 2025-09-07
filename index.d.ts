import { ViewStyle } from 'react-native';
import * as React from 'react';

export interface Media3PlayerProps {
  style?: ViewStyle | ViewStyle[];
  source: { uri: string };
  autoplay?: boolean;
  play?: boolean;
  mute?: boolean;
  onReady?: () => void;
  onEnd?: () => void;
  onError?: (error: any) => void;
}

export default class Media3Player extends React.Component<Media3PlayerProps> {}
