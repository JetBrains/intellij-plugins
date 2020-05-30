// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.xml.XmlElement;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlPropertyBindingImpl extends Angular2HtmlPropertyBindingBase implements Angular2HtmlPropertyBinding {

  public Angular2HtmlPropertyBindingImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull PsiReferenceService.Hints hints) {
    if (getBindingType() == PropertyBindingType.CLASS) {
      if (hints.offsetInElement != null) {
        XmlElement nameElement = getNameElement();
        if (nameElement == null || hints.offsetInElement > nameElement.getStartOffsetInParent() + nameElement.getTextLength()) {
          return PsiReference.EMPTY_ARRAY;
        }
      }
      return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
    return super.getReferences(hints);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitPropertyBinding(this);
    }
    else if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor)visitor).visitXmlAttribute(this);
    }
    else {
      visitor.visitElement(this);
    }
  }
}
