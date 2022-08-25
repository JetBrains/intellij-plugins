/*
 * Copyright 2018 The authors
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

package com.intellij.lang.ognl.psi.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.ui.IconManager;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
abstract class OgnlReferenceExpressionBase extends OgnlExpressionImpl {

  protected OgnlReferenceExpressionBase(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public ItemPresentation getPresentation() {
    return new PresentationData(getIdentifier().getText(),
                                null,
                                IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Parameter),
                                null);
  }

  private PsiElement getIdentifier() {
    return findNotNullChildByType(OgnlTypes.IDENTIFIER);
  }

  @Override
  public PsiReference getReference() {
    PsiElement lastIdentifier = findLastChildByType(OgnlTypes.IDENTIFIER);
    int length = lastIdentifier == null ? getTextLength() :
                 lastIdentifier.getStartOffsetInParent() + lastIdentifier.getTextLength();
    return new PsiReferenceBase<PsiElement>(this, TextRange.from(0, length)) {

      @Override
      public PsiElement resolve() {
        return myElement;
      }

      @Override
      public Object @NotNull [] getVariants() {
        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
/*
  TODO
        return new LookupElement[]{
            LookupElementBuilder.create("root")
                                .setIcon(PlatformIcons.PARAMETER_ICON)
                                .setTypeText(CommonClassNames.JAVA_LANG_OBJECT),
            LookupElementBuilder.create("context")
                                .setIcon(PlatformIcons.PARAMETER_ICON)
                                .setTypeText("Map<String,Object>")};
*/
      }
    };
  }
}
