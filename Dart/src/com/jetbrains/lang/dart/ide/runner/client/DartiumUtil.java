// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DartiumUtil {

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
}
