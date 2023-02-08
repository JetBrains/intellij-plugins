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
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.constant.StrutsConstantContributor;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.converters.values.BooleanValueConverter;
import com.intellij.util.xml.converters.values.NumberValueConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yann C&eacute;bron
 */
public abstract class StrutsConstantContributorBase implements StrutsConstantContributor {

  private static final Converter<String> BOOLEAN_CONVERTER = new BooleanValueConverter(false);

  private static final Converter<String> INTEGER_CONVERTER = new NumberValueConverter(Integer.class, false);

  /**
   * Returns the plugin's class name to determine the availability of this contributor.
   *
   * @return Class name.
   */
  @NonNls
  @NotNull
  protected abstract String getRequiredPluginClassName();

  @Override
  public boolean isAvailable(@NotNull final Module module) {
    return JavaPsiFacade.getInstance(module.getProject())
        .findClass(getRequiredPluginClassName(),
                   GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false)) != null;
  }

  protected static StrutsConstant addBooleanProperty(@NonNls final String propertyName) {
    return new StrutsConstant(propertyName, BOOLEAN_CONVERTER);
  }

  protected static StrutsConstant addIntegerProperty(@NonNls final String propertyName) {
    return new StrutsConstant(propertyName, INTEGER_CONVERTER);
  }

  protected static StrutsConstant addStringProperty(@NonNls final String propertyName) {
    return new StrutsConstant(propertyName, null);
  }

  protected static StrutsConstant addStringValuesProperty(@NonNls final String propertyName,
                                                          @NonNls final String... values) {
    return new StrutsConstant(propertyName, new StringValuesConverter(values));
  }

  protected static StrutsConstant addDelimitedStringValuesProperty(@NonNls final String propertyName) {
    return new StrutsConstant(propertyName, new DelimitedStringValuesConverter());
  }

  protected static StrutsConstant addClassWithShortcutProperty(@NonNls final String propertyName,
                                                               @NonNls @NotNull String baseClass,
                                                               final Pair<String, String>... shortcuts) {
    @NonNls final Map<String, String> shortCutToPsiClassMap = new HashMap<>();
    for (final Pair<String, String> shortcut : shortcuts) {
      shortCutToPsiClassMap.put(shortcut.first, shortcut.second);
    }

    return new StrutsConstant(propertyName, new ConstantValueClassConverter(baseClass, shortCutToPsiClassMap));
  }

  protected static StrutsConstant addStrutsPackage(@NonNls final String propertyName) {
    return new StrutsConstant(propertyName, new StrutsPackageConverter());
  }

  protected static StrutsConstant addResultTypeProperty(@NonNls final String propertyName) {
    return new StrutsConstant(propertyName, new ResultTypeConverter());
  }

}