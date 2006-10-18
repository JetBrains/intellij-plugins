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

package jetbrains.communicator.core;

import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 */
public interface IDEtalkOptions {
  String HIDE_ALL = "HIDE_ALL_KEY";
  String TIMEOUT_AWAY_MIN = "TIMEOUT_AWAY_MIN";
  String TIMEOUT_XA_MIN = "TIMEOUT_XA_MIN";
  @NonNls
  String OPTION_HIDE_OFFLINE = "OPTION_HIDE_OFFLINE_USERS";

  boolean isSet(String option);
  boolean isSet(String option, boolean defaultValue);
  void setOption(String option, boolean value);

  double getNumber(String option, double defaultValue);
  void setNumber(String option, double value);
}
