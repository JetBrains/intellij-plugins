/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.inspection;

import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.validator.config.ValidatorsConfig;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann CŽbron
 */
public class ValidatorConfigModelInspection extends BasicDomElementsInspection<ValidatorsConfig> {

  public ValidatorConfigModelInspection() {
    super(ValidatorsConfig.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return StrutsBundle.message("inspections.groupdisplayname");
  }

  @NotNull
  public String getDisplayName() {
    return StrutsBundle.message("inspections.validatorconfig.model.displayname");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "ValidatorConfigModelInspection";
  }

}