// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.typescript.documentation.TypeScriptDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.xml.XmlAttributeDescriptor;
import one.util.streamex.StreamEx;
import org.angular2.Angular2Framework;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class Angular2DocumentationProvider extends AbstractDocumentationProvider {
  private final TypeScriptDocumentationProvider myTsProvider = new TypeScriptDocumentationProvider();

  @Override
  public @Nls @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
    return myTsProvider.generateDoc(element, originalElement);
  }

  @Nullable
  @Override
  public PsiElement getCustomDocumentationElement(@NotNull Editor editor,
                                                  @NotNull PsiFile file,
                                                  @Nullable PsiElement contextElement,
                                                  int targetOffset) {
    if (!Angular2Framework.getInstance().isContext(file)) {
      return null;
    }

    var element = contextElement;
    if (element instanceof XmlToken) {
      element = element.getParent();
    }

    if (element instanceof XmlAttribute) {
      XmlAttributeDescriptor descriptor = ((XmlAttribute)element).getDescriptor();
      if (!(descriptor instanceof Angular2AttributeDescriptor)) return null;
      return processAttribute((XmlAttribute)element, (Angular2AttributeDescriptor)descriptor);
    }

    if (element instanceof XmlTag) {
      element = resolveReference(element.getReference());
      return PsiTreeUtil.getContextOfType(element, JSClass.class);
    }

    return null;
  }

  private static PsiElement processAttribute(XmlAttribute element, Angular2AttributeDescriptor descriptor) {
    PsiElement targetElement = resolveReference(element.getReference());
    JSClass classElement = PsiTreeUtil.getContextOfType(targetElement, JSClass.class);

    Angular2Entity entity = Angular2EntitiesProvider.getEntity(classElement);
    if (!(entity instanceof Angular2Directive)) return null;
    Angular2Directive directive = (Angular2Directive)entity;

    Collection<PsiElement> sources = descriptor.getProperties(directive);

    for (var source : sources) {
      var comment = JSDocumentationUtils.findDocComment(source);
      if (comment == null) {
        continue;
      }

      if (comment instanceof JSDocComment) {
        var jsDoc = (JSDocComment)comment;
        var privateTag = StreamEx.of(jsDoc.getTags()).findFirst(tag -> "docs-private".equals(tag.getName()));
        if (privateTag.isPresent()) {
          continue;
        }
      }

      return source;
    }

    return classElement;
  }

  private static @Nullable PsiElement resolveReference(PsiReference reference) {
    if (reference == null) {
      return null;
    }
    else if (reference instanceof PsiPolyVariantReference) {
      var result = ArrayUtil.getFirstElement(((PsiPolyVariantReference)reference).multiResolve(false));
      return ObjectUtils.doIfNotNull(result, ResolveResult::getElement);
    }
    else {
      return reference.resolve();
    }
  }
}
