// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator;

import jetbrains.communicator.core.IDEtalkOptions;
import jetbrains.communicator.core.Pico;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 */
public class OptionFlag {

  public static final OptionFlag HIDE_ALL_KEY = new OptionFlag(false, IDEtalkOptions.HIDE_ALL);
  public static final OptionFlag OPTION_HIDE_OFFLINE_USERS = new OptionFlag(false, IDEtalkOptions.OPTION_HIDE_OFFLINE);

  private final boolean myEnabledByDefault;
  private final String myName;

  public OptionFlag(boolean enabledByDefault, @NonNls String name) {
    myEnabledByDefault = enabledByDefault;
    myName = name;
  }

  public String toString() {
    return myName;
  }

  public boolean isEnabledByDefault() {
    return myEnabledByDefault;
  }

  public boolean isSet() {
    return Pico.getOptions().isSet(toString(), isEnabledByDefault());
  }

  public void change(boolean value) {
    Pico.getOptions().setOption(toString(), value);
  }

}
