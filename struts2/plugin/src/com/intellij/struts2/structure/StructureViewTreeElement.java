/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.structure;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementVisitor;
import com.intellij.util.xml.DomElementsNavigationManager;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationsManager;
import com.intellij.util.xml.highlighting.DomElementProblemDescriptor;
import com.intellij.util.xml.highlighting.DomElementsProblemsHolder;
import com.intellij.util.xml.structure.DomStructureTreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents one node for the structure view.
 *
 * @author Yann C&eacute;bron
 */
class StructureViewTreeElement extends DomStructureTreeElement implements ColoredItemPresentation {

  StructureViewTreeElement(@NotNull final DomElement domElement) {
    super(domElement,
          BaseStructureViewBuilderProvider.ALWAYS_SHOW,
          DomElementsNavigationManager.getManager(DomUtil.getFile(domElement).getProject()).
              getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME));
  }

  /**
   * Highlight invalid elements with red underwave.
   *
   * @return null if no errors.
   */
  @Nullable
  @Override
  public TextAttributesKey getTextAttributesKey() {
    final DomElement element = getElement();
    if (!element.isValid()) {
      return null;
    }

    final XmlTag tag = element.getXmlTag();
    if (tag == null) {
      return null;
    }

    final DomElementsProblemsHolder holder = DomElementAnnotationsManager.getInstance(tag.getProject())
                                                                         .getCachedProblemHolder(element);

    final List<DomElementProblemDescriptor> problems = holder.getProblems(element, true, HighlightSeverity.ERROR);
    if (!problems.isEmpty()) {
      return CodeInsightColors.ERRORS_ATTRIBUTES;
    }

    return null;
  }


  @Override
  public TreeElement @NotNull [] getChildren() {
    final DomElement element = getElement();
    if (!element.isValid()) {
      return EMPTY_ARRAY;
    }

    final List<TreeElement> result = new SmartList<>();
    DomUtil.acceptAvailableChildren(element, new DomElementVisitor() {
      @Override
      public void visitDomElement(final DomElement domElement) {
        result.add(new StructureViewTreeElement(domElement));
      }
    });

    return result.toArray(TreeElement.EMPTY_ARRAY);
  }

  /**
   * Add some extra text behind element presentation.
   *
   * @return null if no extra text is provided for the current element.
   */
  @Override
  @Nullable
  public String getLocationString() {
    final XmlElement xmlElement = getElement().getXmlElement();
    assert xmlElement != null : getElement();
    return StrutsTreeDescriptionProvider.getElementDescription(xmlElement);
  }
}