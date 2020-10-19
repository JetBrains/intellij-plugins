/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.changesBrowser;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;

public final class PerforceChangeBrowserSettings extends ChangeBrowserSettings {
  public boolean USE_CLIENT_FILTER = false;
  public @NlsSafe String CLIENT = "";

  public String getClientFilter() {
    if (USE_CLIENT_FILTER) {
      return CLIENT;
    }
    else {
      return null;
    }
  }

  @Override
  public boolean isNonDateFilterSpecified() {
    return super.isNonDateFilterSpecified() || USE_CLIENT_FILTER;
  }
}
