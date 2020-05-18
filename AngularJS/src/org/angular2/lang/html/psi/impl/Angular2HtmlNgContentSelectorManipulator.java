// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.XmlElementFactoryImpl.quoteValue;

public class Angular2HtmlNgContentSelectorManipulator
  extends AbstractElementManipulator<Angular2HtmlNgContentSelector> {

  private static final Logger LOG = Logger.getInstance(Angular2HtmlNgContentSelectorManipulator.class);


  @Override
  public @Nullable Angular2HtmlNgContentSelector handleContentChange(@NotNull Angular2HtmlNgContentSelector element,
                                                                     @NotNull TextRange range,
                                                                     String newContent) throws IncorrectOperationException {
    if (!FileModificationService.getInstance().preparePsiElementsForWrite(element)) return element;
    String newSelector = range.replace(element.getText(), newContent);
    XmlAttribute newSelectAttribute = createNgContentSelectAttribute(element.getProject(), newSelector);
    Angular2HtmlNgContentSelector newSelectorElement = PsiTreeUtil.findChildOfType(
      newSelectAttribute.getValueElement(),
      Angular2HtmlNgContentSelector.class);
    LOG.assertTrue(newSelectorElement != null, newSelectAttribute.getParent().getText());
    return (Angular2HtmlNgContentSelector)element.replace(newSelectorElement);
  }

  private static @NotNull XmlAttribute createNgContentSelectAttribute(@NotNull Project project, @NotNull String value) {
    String quotedValue = quoteValue(value);

    XmlTag tag = XmlElementFactory.getInstance(project).createTagFromText(
      "<ng-content select=" + quotedValue + "></ng-content>",
      Angular2HtmlLanguage.INSTANCE);

    XmlAttribute[] attributes = tag.getAttributes();
    LOG.assertTrue(attributes.length == 1, tag.getText());
    return attributes[0];
  }
}
