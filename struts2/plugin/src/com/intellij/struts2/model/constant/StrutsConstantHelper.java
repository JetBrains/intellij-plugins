/*
 * Copyright 2011 The authors
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

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
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
public final class StrutsConstantHelper {
  private static final Function<String, String> DOT_PATH_FUNCTION = s -> "." + s;

  private StrutsConstantHelper() {
  }

  /**
   * Caches action extensions per file.
   */
  private static final Key<CachedValue<NotNullLazyValue<List<String>>>> KEY_ACTION_EXTENSIONS =
      Key.create("STRUTS2_ACTION_EXTENSIONS");

  /**
   * Returns the current action extension(s) ("{@code .action}").
   *
   * @param psiElement Invocation element.
   * @return empty list on configuration problems.
   */
  @NotNull
  public static List<String> getActionExtensions(@NotNull final PsiElement psiElement) {
    final PsiFile psiFile = psiElement.getContainingFile().getOriginalFile();

    CachedValue<NotNullLazyValue<List<String>>> extensions = psiFile.getUserData(KEY_ACTION_EXTENSIONS);
    if (extensions == null) {
      final Project project = psiElement.getProject();
      extensions = CachedValuesManager.getManager(project).createCachedValue(() -> {
        NotNullLazyValue<List<String>> lazyValue = NotNullLazyValue.atomicLazy(() -> {
          final List<String> extensions1 =
            ReadAction.compute(() -> StrutsConstantManager.getInstance(project)
              .getConvertedValue(psiFile,
                                 StrutsCoreConstantContributor.ACTION_EXTENSION));

          if (extensions1 == null) {
            return Collections.emptyList();
          }

          return ContainerUtil.map(extensions1, DOT_PATH_FUNCTION);
        });
        return CachedValueProvider.Result.create(lazyValue, PsiModificationTracker.MODIFICATION_COUNT);
      }, false);

      psiFile.putUserData(KEY_ACTION_EXTENSIONS, extensions);
    }

    return extensions.getValue().getValue();
  }
}