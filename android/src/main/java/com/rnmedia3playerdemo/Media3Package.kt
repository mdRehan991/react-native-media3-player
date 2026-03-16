package com.rnmedia3playerdemo

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * Media3Package integrates the custom Media3PlayerViewManager with React Native.
 *
 * This package does not register any NativeModules. It only provides the
 * Media3PlayerViewManager for use as a native UI component from JavaScript.
 */
class Media3Package : ReactPackage {
    /**
     * No native modules are exported by this package.
     */
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> =
        emptyList()

    /**
     * Registers Media3PlayerViewManager as the only custom ViewManager for this package.
     */
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
        listOf(Media3PlayerViewManager())
}
