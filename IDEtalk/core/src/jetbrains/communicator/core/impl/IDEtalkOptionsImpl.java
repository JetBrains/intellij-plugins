// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.IDEtalkOptions;

import java.util.prefs.Preferences;

/**
 * @author Kir
 */
public class IDEtalkOptionsImpl implements IDEtalkOptions {
  @Override
  public boolean isSet(String option) {
    return isSet(option, false);
  }

  @Override
  public boolean isSet(String option, boolean defaultValue) {
    return getPrefs().getBoolean(option, defaultValue);
  }

  @Override
  public double getNumber(String option, double defaultValue) {
    return getPrefs().getDouble(option, defaultValue);
  }

  @Override
  public void setNumber(String option, double value) {
    getPrefs().putDouble(option, value);
  }

  @Override
  public void setOption(String option, boolean value) {
    getPrefs().putBoolean(option, value);
  }

  private Preferences getPrefs() {
    return Preferences.userNodeForPackage(getClass());
  }
}
