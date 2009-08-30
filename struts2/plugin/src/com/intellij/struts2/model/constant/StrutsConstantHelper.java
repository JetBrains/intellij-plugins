/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.model.constant;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Provides convenience access methods for commonly used constants.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantHelper {

  private StrutsConstantHelper() {
  }

  /**
   * Returns the current action extension(s) ("{@code .action}").
   *
   * @param psiElement Invocation element.
   * @return empty list on configuration problems.
   */
  @NotNull
  public static List<String> getActionExtensions(final PsiElement psiElement) {
    final StrutsConstantManager constantManager = StrutsConstantManager.getInstance(psiElement.getProject());

    final String actionExtension = constantManager.getConvertedValue(psiElement,
                                                                     StrutsCoreConstantContributor.ACTION_EXTENSION);

    if (actionExtension == null) {
      return Collections.emptyList();
    }

    return ContainerUtil.map(StringUtil.split(actionExtension, ","), new Function<String, String>() {
      public String fun(final String s) {
        return "." + s;
      }
    });
  }

}