/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.codeInsight.hint.DefaultImplementationTextSelectioner;
import com.intellij.coldFusion.model.psi.CfmlAttribute;
import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 4/23/12
 */
public class CfmlImplementationTextSelectioner extends DefaultImplementationTextSelectioner {

  @Override
  public int getTextEndOffset(@NotNull PsiElement element) {

    PsiElement parent = element.getParent();
    if (parent instanceof CfmlAttribute) {
      parent = parent.getParent();
    }
    if (parent instanceof CfmlFunction || parent instanceof CfmlTag) {
      return parent.getTextRange().getEndOffset();
    }
    return super.getTextEndOffset(element);
  }
}
