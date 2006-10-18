/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.IDEtalkOptions;

import java.util.prefs.Preferences;

/**
 * @author Kir
 */
public class IDEtalkOptionsImpl implements IDEtalkOptions {
  public boolean isSet(String option) {
    return isSet(option, false);
  }

  public boolean isSet(String option, boolean defaultValue) {
    return getPrefs().getBoolean(option, defaultValue);
  }

  public double getNumber(String option, double defaultValue) {
    return getPrefs().getDouble(option, defaultValue);
  }

  public void setNumber(String option, double value) {
    getPrefs().putDouble(option, value);
  }

  public void setOption(String option, boolean value) {
    getPrefs().putBoolean(option, value);
  }

  private Preferences getPrefs() {
    return Preferences.userNodeForPackage(getClass());
  }
}
