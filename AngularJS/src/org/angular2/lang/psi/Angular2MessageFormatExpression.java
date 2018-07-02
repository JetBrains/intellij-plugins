// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.psi;

import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.parser.Angular2ElementTypes;
import org.angular2.lang.parser.Angular2MessageFormatParser;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Irina.Chernushina on 12/2/2015.
 */
public class Angular2MessageFormatExpression extends JSExpressionImpl {
  private static final Logger LOG = Logger.getInstance("#org.angular2.lang.psi.Angular2MessageFormatExpression");

  public Angular2MessageFormatExpression(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitMessageFormatExpression(this);
    } else {
      super.accept(visitor);
    }
  }

  public Angular2MessageFormatParser.ExtensionType getExtensionType() {
    final Ref<PsiElement> ref = new Ref<>();
    PsiTreeUtil.processElements(this, new PsiElementProcessor() {
      @Override
      public boolean execute(@NotNull PsiElement element) {
        final boolean isExpressionName = element.getNode().getElementType() == Angular2ElementTypes.MESSAGE_FORMAT_EXPRESSION_NAME;
        ref.set(element);
        return !isExpressionName;
      }
    });
    final PsiElement typeElement = getExtensionTypeElement();
    if (typeElement == null) return null;
    try {
      return Angular2MessageFormatParser.ExtensionType.valueOf(typeElement.getText());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public PsiElement getExtensionTypeElement() {
    final Ref<PsiElement> ref = new Ref<>();
    PsiTreeUtil.processElements(this, new PsiElementProcessor() {
      @Override
      public boolean execute(@NotNull PsiElement element) {
        final boolean isExpressionName = element.getNode().getElementType() == Angular2ElementTypes.MESSAGE_FORMAT_EXPRESSION_NAME;
        ref.set(element);
        return !isExpressionName;
      }
    });
    if (ref.isNull()) {
      LOG.error("Angular2 message format expression does not have name");
      return null;
    }
    else {
      return ref.get();
    }
  }

  public PsiElement[] getOptions() {
    return ((CompositeElement)getNode()).getChildrenAsPsiElements(Angular2ElementTypes.MESSAGE_FORMAT_OPTION, PsiElement.ARRAY_FACTORY);
  }

  public List<PsiElement> getSelectionKeywordElements() {
    if (!(getNode() instanceof CompositeElement)) return Collections.emptyList();
    final PsiElement[] selectionsKeywords = ((CompositeElement)getNode()).getChildrenAsPsiElements(
      Angular2ElementTypes.MESSAGE_FORMAT_SELECTION_KEYWORD, PsiElement.ARRAY_FACTORY);
    return ContainerUtil.filter(selectionsKeywords,
                                element -> element.getNode().getElementType() == Angular2ElementTypes.MESSAGE_FORMAT_SELECTION_KEYWORD);
  }
}
