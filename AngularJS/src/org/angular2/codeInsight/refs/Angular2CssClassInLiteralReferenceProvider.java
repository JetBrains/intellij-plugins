// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.impl.util.table.CssDescriptorsUtil;
import com.intellij.psi.css.util.CssResolveUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2CssClassInLiteralReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    String text = null;
    if (element instanceof JSLiteralExpression) {
      text = ((JSLiteralExpression)element).getStringValue();
    }
    else if (element.getNode().getElementType() == JSTokenTypes.STRING_LITERAL) {
      text = StringUtil.unquoteString(element.getText());
    }
    if (text == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    List<PsiReference> result = new SmartList<>();
    CssResolveUtil.consumeClassNames(text, element, (token, range) -> {
      CssElementDescriptorProvider descriptorProvider = CssDescriptorsUtil.findDescriptorProvider(element);
      assert descriptorProvider != null;
      range = range.shiftRight(1);
      result.add(descriptorProvider.getStyleReference(
        element, range.getStartOffset(), range.getEndOffset(), true));
    });
    return result.toArray(PsiReference.EMPTY_ARRAY);
  }
}
