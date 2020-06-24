// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.application.options.editor.WebEditorOptions;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.project.Project;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.util.HtmlUtil;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class CreateAttributeQuickFix implements LocalQuickFix {

  private final String myAttributeName;

  public CreateAttributeQuickFix(String attributeName) {
    myAttributeName = attributeName;
  }

  @Override
  public @Nls @NotNull String getName() {
    return Angular2Bundle.message("angular.quickfix.template.create-attribute.name", myAttributeName);
  }

  @Override
  public @Nls @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.template.create-attribute.family");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final XmlTag tag = ((XmlAttribute)descriptor.getPsiElement()).getParent();
    if (tag == null || tag.getAttribute(myAttributeName) != null) {
      return;
    }
    final XmlAttributeDescriptor attributeDescriptor =
      doIfNotNull(tag.getDescriptor(), tagDescriptor -> tagDescriptor.getAttributeDescriptor(myAttributeName, tag));

    boolean insertQuotes = WebEditorOptions.getInstance().isInsertQuotesForAttributeValue()
                           && !(tag instanceof HtmlTag
                                && attributeDescriptor != null
                                && HtmlUtil.isShortNotationOfBooleanAttributePreferred()
                                && HtmlUtil.isBooleanAttribute(attributeDescriptor, tag));

    XmlAttribute attribute = tag.setAttribute(myAttributeName, insertQuotes ? "" : null);

    final XmlAttributeValue value = attribute == null ? null : attribute.getValueElement();
    if (value != null) {
      PsiNavigationSupport.getInstance().createNavigatable(
        project, attribute.getContainingFile().getVirtualFile(),
        value.getTextRange().getStartOffset() + 1
      ).navigate(true);
    }
  }
}
