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

package com.intellij.struts2.model.constant;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Base class for testing {@link StrutsConstantManager}.
 *
 * @author Yann C&eacute;bron
 */
public abstract class StrutsConstantManagerTestCase extends BasicLightHighlightingTestCase {

  <T> void performResolveTest(@NotNull final VirtualFile invokingFile,
                              @NotNull final StrutsConstantKey<T> strutsConstantKey,
                              @Nullable @NonNls final T value) {
    final StrutsConstantManager constantManager = StrutsConstantManager.getInstance(getProject());

    final PsiFile invokingPsiFile = PsiManager.getInstance(getProject()).findFile(invokingFile);
    assert invokingPsiFile != null : invokingFile.getPath();

    final T constantValue = constantManager.getConvertedValue(invokingPsiFile, strutsConstantKey);
    assertThat(constantValue, is(value));
  }
}