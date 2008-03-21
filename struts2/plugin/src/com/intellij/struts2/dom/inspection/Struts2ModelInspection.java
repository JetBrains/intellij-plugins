/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.inspection;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.ActionClassConverter;
import com.intellij.util.xml.*;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Default DOM-Model inspection for struts.xml files.
 *
 * @author Yann CŽbron
 */
public class Struts2ModelInspection extends BasicDomElementsInspection<StrutsRoot> {

  public Struts2ModelInspection() {
    super(StrutsRoot.class);
  }

  protected boolean shouldCheckResolveProblems(final GenericDomValue value) {
    // we roll our own checking for <action> class in checkDomElement()
    if (value.getConverter() instanceof ActionClassConverter) {
      return false;
    }

    // TODO hack for suppresing wildcard-resolving
    final String stringValue = value.getStringValue();
    return stringValue == null || stringValue.indexOf('{') < 0;
  }

  protected void checkDomElement(final DomElement element,
                                 final DomElementAnnotationHolder holder,
                                 final DomHighlightingHelper helper) {
    super.checkDomElement(element, holder, helper);

    if (element instanceof GenericAttributeValue) {
      final GenericAttributeValue genericDomValue = (GenericAttributeValue) element;
      if (genericDomValue.getConverter() instanceof ActionClassConverter) {
        final XmlElement xmlElement = DomUtil.getValueElement(genericDomValue);
        if (xmlElement == null) return;

        final PsiReference[] psiReferences = xmlElement.getReferences();

        for (final PsiReference psiReference : psiReferences) {
          final PsiElement resolveElement = psiReference.resolve();
          if (resolveElement != null &&
              resolveElement instanceof PsiClass) {
            return;
          }
        }

        final String referenceTypes = StringUtil.join(genericDomValue.getUserData(ActionClassConverter.REFERENCES_TYPES),
                                                      "|");
        holder.createProblem(genericDomValue,
                             HighlightSeverity.ERROR,
                             "Cannot resolve " + referenceTypes + " '" + genericDomValue.getStringValue() + "'");
      }
    }
  }

  @NotNull
  public String getGroupDisplayName() {
    return "Struts 2";
  }

  @NotNull
  public String getDisplayName() {
    return "Struts 2 Model Inspection";
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "Struts2ModelInspection";
  }

}