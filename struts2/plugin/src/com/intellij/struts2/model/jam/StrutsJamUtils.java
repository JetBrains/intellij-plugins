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

package com.intellij.struts2.model.jam;

import com.intellij.jam.JamAttributeElement;
import com.intellij.psi.PsiElement;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for working with JAM.
 *
 * @author Yann C&eacute;bron
 */
public final class StrutsJamUtils {

  private StrutsJamUtils() {
  }

  /**
   * Gets the model for the given attribute element.
   *
   * @param attributeElement Attribute element.
   * @return {@code null} if no StrutsModel could be determined.
   */
  @Nullable
  public static StrutsModel getStrutsModel(final JamAttributeElement attributeElement) {
    final PsiElement value = attributeElement.getPsiElement();
    if (value == null) {
      return null;
    }

    final StrutsManager instance = StrutsManager.getInstance(value.getProject());
    return instance.getCombinedModel(value);
  }

}