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

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
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

  private static final Function<String, String> DOT_PATH_FUNCTION = new Function<String, String>() {
    public String fun(final String s) {
      return "." + s;
    }
  };

  private StrutsConstantHelper() {
  }

  /**
   * Caches action extensions per file.
   */
  private static final Key<CachedValue<List<String>>> KEY_EXTENSIONS = Key.create("STRUTS2_ACTION_EXTENSIONS");

  /**
   * Returns the current action extension(s) ("{@code .action}").
   *
   * @param psiElement Invocation element.
   * @return empty list on configuration problems.
   */
  @NotNull
  public static List<String> getActionExtensions(final PsiElement psiElement) {
    final PsiFile psiFile = psiElement.getContainingFile();

    CachedValue<List<String>> extensions = psiFile.getUserData(KEY_EXTENSIONS);
    if (extensions == null) {
      extensions = psiElement.getManager().getCachedValuesManager().createCachedValue(
          new CachedValueProvider<List<String>>() {
            public Result<List<String>> compute() {
              final StrutsConstantManager constantManager = StrutsConstantManager.getInstance(psiElement.getProject());

              final String actionExtension = constantManager.getConvertedValue(psiElement,
                                                                               StrutsCoreConstantContributor.ACTION_EXTENSION);

              final List<String> myExtensions;
              if (actionExtension == null) {
                myExtensions = Collections.emptyList();
              } else {
                myExtensions = ContainerUtil.map(StringUtil.split(actionExtension, ","), DOT_PATH_FUNCTION);
              }

              return Result.create(myExtensions, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
            }
          }, false);

      psiFile.putUserData(KEY_EXTENSIONS, extensions);
    }

    return extensions.getValue();
  }

}