// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class DartiumUtil {

  private static final String DART_FLAGS_ENV_VAR = "DART_FLAGS";
  private static final String CHECKED_MODE_OPTION = "--checked";
  private static final String ENABLE_ASYNC_OPTION = "--enable-async";

  private static final UUID DARTIUM_ID = UUID.fromString("BFEE1B69-A97D-4338-8BA4-25170ADCBAA6");

  @Nullable
  public static WebBrowser getDartiumBrowser() {
    WebBrowser browser = WebBrowserManager.getInstance().findBrowserById(DARTIUM_ID.toString());
    if (browser == null) {
      for (WebBrowser b : WebBrowserManager.getInstance().getActiveBrowsers()) {
        if (WebBrowserManager.isDartium(b)) {
          browser = b;
          break;
        }
      }
    }
    return browser != null && browser.getFamily() == BrowserFamily.CHROME ? browser : null;
  }

  private static boolean hasDartFlag(final Map<String, String> envVars, final String dartFlag) {
    final String dartFlags = envVars.get(DART_FLAGS_ENV_VAR);
    return dartFlags != null && (dartFlags.trim().equals(dartFlag) ||
                                 dartFlags.startsWith(dartFlag + " ") ||
                                 dartFlags.endsWith(" " + dartFlag) ||
                                 dartFlags.contains(" " + dartFlag + " "));
  }

  public static void setCheckedMode(@NotNull final Map<String, String> envVars, final boolean checkedMode) {
    setDartFlagState(envVars, CHECKED_MODE_OPTION, checkedMode);
  }

  private static void setDartFlagState(final Map<String, String> envVars, final String dartFlag, final boolean flagState) {
    final boolean oldFlagState = hasDartFlag(envVars, dartFlag);
    if (oldFlagState == flagState) return;

    final String dartFlags = envVars.get(DART_FLAGS_ENV_VAR);
    if (flagState) {
      if (dartFlags == null) {
        envVars.put(DART_FLAGS_ENV_VAR, dartFlag);
      }
      else {
        envVars.put(DART_FLAGS_ENV_VAR, dartFlags + " " + dartFlag);
      }
    }
    else {
      String newFlags = dartFlags;
      if (newFlags.trim().equals(dartFlag)) {
        newFlags = "";
      }
      newFlags = StringUtil.trimStart(newFlags, dartFlag + " ");
      newFlags = StringUtil.trimEnd(newFlags, " " + dartFlag);
      final int index = newFlags.indexOf(" " + dartFlag + " ");
      if (index != -1) {
        // keep one space between parts
        newFlags = newFlags.substring(0, index) + newFlags.substring(index + dartFlag.length() + 1);
      }

      if (StringUtil.isEmptyOrSpaces(newFlags)) {
        envVars.remove(DART_FLAGS_ENV_VAR);
      }
      else {
        envVars.put(DART_FLAGS_ENV_VAR, newFlags);
      }
    }
  }

  public static void resetDartiumFlags() {
    final WebBrowser dartium = getDartiumBrowser();
    final BrowserSpecificSettings browserSpecificSettings = dartium == null ? null : dartium.getSpecificSettings();
    if (!(browserSpecificSettings instanceof ChromeSettings)) return;

    final Map<String, String> envVars = browserSpecificSettings.getEnvironmentVariables();
    setDartFlagState(envVars, ENABLE_ASYNC_OPTION, false);
    setDartFlagState(envVars, CHECKED_MODE_OPTION, true);

    PropertiesComponent.getInstance().unsetValue("DARTIUM_KNOWN_PATHS");
  }
}
