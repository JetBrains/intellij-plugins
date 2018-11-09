/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.model.constant.contributor;

import com.intellij.openapi.module.Module;
import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.jam.convention.StrutsConventionConstants;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Provides Convention-plugin specific configuration properties.
 * <a href="http://cwiki.apache.org/WW/convention-plugin.html#ConventionPlugin-Configurationreference">List of properties</a>.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConventionPluginConstantContributor extends StrutsConstantContributorBase {

  @NonNls
  private static final List<StrutsConstant> CONSTANTS = Arrays.asList(
      addBooleanProperty("struts.convention.action.alwaysMapExecute"),
      addDelimitedStringValuesProperty("struts.convention.action.includeJars"),
      addStringProperty("struts.convention.action.packages"),
      addStringProperty("struts.convention.result.path"),
      addBooleanProperty("struts.convention.result.flatLayout"),
      addStringProperty("struts.convention.action.suffix"),
      addBooleanProperty("struts.convention.action.disableScanning"),
      addBooleanProperty("struts.convention.action.mapAllMatches"),
      addBooleanProperty("struts.convention.action.checkImplementsAction"),
      addStrutsPackage("struts.convention.default.parent.package"),
      addBooleanProperty("struts.convention.action.name.lowercase"),
      addStringProperty("struts.convention.action.name.separator"),
      addDelimitedStringValuesProperty("struts.convention.package.locators"),
      addBooleanProperty("struts.convention.package.locators.disable"),
      addDelimitedStringValuesProperty("struts.convention.exclude.packages"),
      addStringProperty("struts.convention.package.locators.basePackage"),
      addResultTypeProperty("struts.convention.relative.result.types"),
      addBooleanProperty("struts.convention.redirect.to.slash"),
      addBooleanProperty("struts.convention.classLoader.excludeParent"),
      addBooleanProperty("struts.convention.classes.reload"),
      addBooleanProperty("struts.convention.action.eagerLoading")
  );

  @NotNull
  @Override
  protected String getRequiredPluginClassName() {
    return StrutsConventionConstants.CONVENTIONS_SERVICE;
  }

  @Override
  @NotNull
  public List<StrutsConstant> getStrutsConstantDefinitions(@NotNull final Module module) {
    return CONSTANTS;
  }

}