package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.components.*;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * PhoneGapSettings.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/12.
 */
@SuppressWarnings("UnusedDeclaration")
@State(
        name = "PhoneGapSettings",
        storages = {@Storage(
                file = StoragePathMacros.APP_CONFIG + "/phonegap.xml")})
public final class PhoneGapSettings implements PersistentStateComponent<PhoneGapSettings.State> {

    // System ID
    public static final ProjectSystemId PHONEGAP_SYSTEM_ID = new ProjectSystemId("PHONEGAP");
    public static final ProjectSystemId CORDOVA_SYSTEM_ID = new ProjectSystemId("CORDOVA");

    // External tools PATH
    public static String NODEJS_PATH = "/usr/local/bin/node";
    public static String ANDROID_SDK = "android";
    public static String IOS_SIM = "ios-sim";

    // PhoneGap PATH
    public static String PHONEGAP_PATH = "/opt/local/bin/phonegap";
    public static String CORDOVA_PATH = "/opt/local/bin/cordova";

    // PhoneGap commands
    public static String PHONEGAP_TASK = "run";
    public static String PHONEGAP_PLATFORM_ANDROID = "android";
    public static String PHONEGAP_PLATFORM_IOS = "ios";
    public static String PHONEGAP_PLATFORM_WP = "windowsphone";
    public static String PHONEGAP_PLATFORM_RIPPLE = "ripple";


    public static String PHONEGAP_RELEASEBUILD = "--release";
    public static String PHONEGAP_FOLDERS_CORDOVA = ".cordova";
    public static String PHONEGAP_FOLDERS_HOOKS = "hooks";
    public static String PHONEGAP_FOLDERS_MERGES = "merges";
    public static String PHONEGAP_FOLDERS_NODE_MODULES = "node_modules";
    public static String PHONEGAP_FOLDERS_PLATFORMS = "platforms";
    public static String PHONEGAP_FOLDERS_PLUGINS = "plugins";
    public static String PHONEGAP_FOLDERS_WWW = "www";
    public static boolean isPhoneGapInstallded = false;

    private static final PhoneGapSettings INSTANCE = new PhoneGapSettings();

    public static class State {
        public String phoneGapExecutablePath;
        public String cordovaExecutablePath;

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            if (cordovaExecutablePath != null
                    ? !cordovaExecutablePath.equals(state.cordovaExecutablePath)
                    : state.cordovaExecutablePath != null)
                return false;
            if (phoneGapExecutablePath != null
                    ? !phoneGapExecutablePath.equals(state.phoneGapExecutablePath)
                    : state.phoneGapExecutablePath != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = phoneGapExecutablePath != null ? phoneGapExecutablePath.hashCode() : 0;
            result = 31 * result + (cordovaExecutablePath != null ? cordovaExecutablePath.hashCode() : 0);
            return result;
        }
    }

    public static PhoneGapSettings getInstance() {
        return ServiceManager.getService(PhoneGapSettings.class);
    }

    private State myState = new State();
    private volatile boolean isDetected = false;

    @NotNull
    @Override
    public State getState() {
        detectDefaultPaths();
        return myState;
    }

    @Override
    public void loadState(State state) {
        myState = state;
    }

    public boolean isCordovaAvailable() {
        return !StringUtil.isEmpty(getCordovaExecutablePath());
    }

    public boolean isPhoneGapAvailable() {
        return !StringUtil.isEmpty(getPhoneGapExecutablePath());
    }

    @Nullable
    public String getCordovaExecutablePath() {
        detectDefaultPaths();
        return myState.cordovaExecutablePath;
    }

    @Nullable
    public String getPhoneGapExecutablePath() {
        detectDefaultPaths();
        return myState.phoneGapExecutablePath;
    }

    private void detectDefaultPaths() {
        if (isDetected) {
            return;
        }

        if (!StringUtil.isEmpty(myState.cordovaExecutablePath) || !StringUtil.isEmpty(myState.phoneGapExecutablePath)) {
            isDetected = true;
            return;
        }

        File cordova = PathEnvironmentVariableUtil.findInPath("cordova");
        File phoneGap = PathEnvironmentVariableUtil.findInPath("phonegap");
        State state = new State();
        if (cordova != null && cordova.exists()) {
            state.cordovaExecutablePath = cordova.getAbsolutePath();
        }
        if (phoneGap != null && phoneGap.exists()) {
            state.phoneGapExecutablePath = phoneGap.getAbsolutePath();
        }
        myState = state;
        isDetected = true;
    }
}
