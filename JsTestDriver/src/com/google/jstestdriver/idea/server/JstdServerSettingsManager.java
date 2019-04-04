package com.google.jstestdriver.idea.server;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JstdServerSettingsManager {

  private static final String PREFIX = "JsTestDriver.server.settings.";
  private static final String PORT_TIMEOUT = PREFIX + "port";
  private static final String BROWSER_TIMEOUT = PREFIX + "browserTimeout";
  private static final String RUNNER_MODE = PREFIX + "runnerMode";

  private static final List<Listener> myListeners = ContainerUtil.createEmptyCOWList();
  private static volatile JstdServerSettings mySettings;

  @NotNull
  public static JstdServerSettings loadSettings() {
    JstdServerSettings settings = mySettings;
    if (settings != null) {
      return settings;
    }
    JstdServerSettings.Builder builder = new JstdServerSettings.Builder();
    Integer port = toInteger(loadApplicationSetting(PORT_TIMEOUT));
    if (port != null) {
      builder.setPort(port);
    }
    Integer browserTimeout = toInteger(loadApplicationSetting(BROWSER_TIMEOUT));
    if (browserTimeout != null) {
      builder.setBrowserTimeoutMillis(browserTimeout);
    }
    JstdServerSettings.RunnerMode runnerMode = loadRunnerMode();
    if (runnerMode != null) {
      builder.setRunnerMode(runnerMode);
    }
    settings = builder.build();
    mySettings = settings;
    return settings;
  }

  public static void saveSettings(@NotNull JstdServerSettings settings) {
    if (!settings.equals(mySettings)) {
      storeApplicationSetting(PORT_TIMEOUT, String.valueOf(settings.getPort()));
      storeApplicationSetting(BROWSER_TIMEOUT, String.valueOf(settings.getBrowserTimeoutMillis()));
      storeApplicationSetting(RUNNER_MODE, settings.getRunnerMode().name());
      mySettings = settings;
      fireOnChanged(settings);
    }
  }

  private static void fireOnChanged(@NotNull JstdServerSettings settings) {
    for (Listener listener : myListeners) {
      listener.onChanged(settings);
    }
  }

  public static void addListener(@NotNull final Listener listener, @NotNull Disposable disposable) {
    myListeners.add(listener);
    Disposer.register(disposable, new Disposable() {
      @Override
      public void dispose() {
        myListeners.remove(listener);
      }
    });
  }

  @Nullable
  private static JstdServerSettings.RunnerMode loadRunnerMode() {
    String str = loadApplicationSetting(RUNNER_MODE);
    if (str != null) {
      try {
        return JstdServerSettings.RunnerMode.valueOf(str);
      }
      catch (IllegalArgumentException ignored) {
      }
    }
    return null;
  }

  @Nullable
  private static Integer toInteger(@Nullable String str) {
    if (str != null) {
      try {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException ignored) {
      }
    }
    return null;
  }

  @Nullable
  private static String loadApplicationSetting(@NotNull String key) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    return propertiesComponent.getValue(key);
  }

  private static void storeApplicationSetting(@NotNull String key, @NotNull String value) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(key, value);
  }

  public interface Listener {
    void onChanged(@NotNull JstdServerSettings settings);
  }
}
