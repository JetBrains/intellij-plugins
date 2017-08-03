/*
 * Copyright 2016 The authors
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

package com.intellij.struts2.dom;

import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Utility methods for DOM-Converters.
 *
 * @author Yann C&eacute;bron
 */
public final class ConverterUtil {

  // "{0}" through "{9}"
  private static final Pattern WILDCARD_PATTERN = Pattern.compile("\\{\\d}");

  private ConverterUtil() {
  }

  /**
   * Gets the StrutsModel for the current context (=file).
   *
   * @param context Invoking context.
   * @return {@code null} if no StrutsModel found by current file (e.g. not in any fileset).
   */
  @Nullable
  public static StrutsModel getStrutsModel(final ConvertContext context) {
    final XmlFile xmlFile = context.getFile();
    return StrutsManager.getInstance(xmlFile.getProject()).getModelByFile(xmlFile);
  }

  /**
   * Gets the StrutsModel for the current context (=file) or combined model for current module.
   *
   * @param context Invoking context.
   * @return {@code null} if no StrutsModel found in current file/module.
   */
  @Nullable
  public static StrutsModel getStrutsModelOrCombined(final ConvertContext context) {
    final StrutsModel modelByFile = getStrutsModel(context);
    if (modelByFile != null) {
      return modelByFile;
    }

    return StrutsManager.getInstance(context.getFile().getProject()).getCombinedModel(context.getModule());
  }

  /**
   * Returns the current (parent) {@code <package>} element.
   *
   * @param context Invoking context.
   * @return Parent package.
   */
  @NotNull
  public static StrutsPackage getCurrentStrutsPackage(final ConvertContext context) {
    final StrutsPackage strutsPackage = DomUtil.getParentOfType(context.getInvocationElement(),
                                                                StrutsPackage.class,
                                                                true);
    assert strutsPackage != null : context.getInvocationElement();
    return strutsPackage;
  }

  /**
   * Returns whether the value contains a wildcard-reference.
   *
   * @param value Value to check for wildcard pattern.
   * @return {@code true} if contains wildcard value.
   */
  public static boolean hasWildcardReference(@Nullable final String value) {
    return value != null && WILDCARD_PATTERN.matcher(value).find();
  }

}