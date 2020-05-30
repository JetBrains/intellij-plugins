// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
