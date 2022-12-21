// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.impl.util.table.CssDescriptorsUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;

import static org.angular2.lang.html.parser.Angular2AttributeNameParser.*;

public class Angular2CssClassInAttributeReferenceProvider extends PsiReferenceProvider {

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (!(element instanceof XmlAttribute)) {
      return PsiReference.EMPTY_ARRAY;
    }
    String attributeName = ((XmlAttribute)element).getName();
    AttributeInfo info = parse(attributeName, ((XmlAttribute)element).getParent());
    if (!(info instanceof PropertyBindingInfo)
        || !(((PropertyBindingInfo)info).bindingType == PropertyBindingType.CLASS)) {
      return PsiReference.EMPTY_ARRAY;
    }
    String className = info.name;
    int offset = attributeName.lastIndexOf(className);
    if (offset < 0) {
      return PsiReference.EMPTY_ARRAY;
    }
    CssElementDescriptorProvider descriptorProvider = CssDescriptorsUtil.findDescriptorProvider(element);
    assert descriptorProvider != null;
    return new PsiReference[]{descriptorProvider.getStyleReference(
      element, offset, offset + className.length(), true)};
  }
}
