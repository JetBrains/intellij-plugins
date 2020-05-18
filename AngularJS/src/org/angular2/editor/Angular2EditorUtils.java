// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.codeInsight.tags.Angular2TagDescriptor;
import org.angular2.entities.Angular2Directive;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.intellij.psi.xml.XmlTokenType.XML_NAME;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2InjectionUtils.getElementAtCaretFromContext;

public class Angular2EditorUtils {

  static @NotNull List<Angular2Directive> getDirectivesAtCaret(@NotNull DataContext context) {
    PsiElement element = getElementAtCaretFromContext(context);
    List<Angular2Directive> directives = Collections.emptyList();
    if (element != null && element.getNode().getElementType() == XML_NAME) {
      element = element.getParent();
    }
    if (element instanceof XmlAttribute) {
      Angular2AttributeDescriptor descriptor = tryCast(((XmlAttribute)element).getDescriptor(), Angular2AttributeDescriptor.class);
      if (descriptor != null) {
        directives = descriptor.getSourceDirectives();
      }
    }
    else if (element instanceof XmlTag) {
      Angular2TagDescriptor descriptor = tryCast(((XmlTag)element).getDescriptor(), Angular2TagDescriptor.class);
      if (descriptor != null) {
        directives = descriptor.getSourceDirectives();
      }
    }
    return directives;
  }
}
