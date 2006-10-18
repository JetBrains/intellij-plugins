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
