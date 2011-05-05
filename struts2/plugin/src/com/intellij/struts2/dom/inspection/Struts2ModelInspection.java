/*
 * Copyright 2011 The authors
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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.struts2.dom.params.ParamNameNestedConverter;
import com.intellij.struts2.dom.params.ParamsElement;
import com.intellij.struts2.dom.struts.HasResultType;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter;
import com.intellij.struts2.dom.struts.impl.path.ResultTypeResolver;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.xml.*;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Default DOM-Model inspection for struts.xml files.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2ModelInspection extends BasicDomElementsInspection<StrutsRoot> {

  public Struts2ModelInspection() {
    super(StrutsRoot.class);
  }

  /**
   * Only inspect struts.xml files configured in fileset.
   *
   * @param strutsRootDomFileElement Root element of file to inspect.
   * @param holder                   Holder.
   */
  public void checkFileElement(final DomFileElement<StrutsRoot> strutsRootDomFileElement,
                               final DomElementAnnotationHolder holder) {
    final Module module = strutsRootDomFileElement.getModule();
    if (module == null) {
      return;
    }

    final XmlFile xmlFile = strutsRootDomFileElement.getFile();
    final VirtualFile virtualFile = xmlFile.getVirtualFile();

    final Set<StrutsFileSet> fileSets = StrutsManager.getInstance(xmlFile.getProject()).getAllConfigFileSets(module);
    for (final StrutsFileSet strutsFileSet : fileSets) {
      if (strutsFileSet.hasFile(virtualFile)) {
        super.checkFileElement(strutsRootDomFileElement, holder);
        break;
      }
    }
  }

  protected boolean shouldCheckResolveProblems(final GenericDomValue value) {
    final Converter converter = value.getConverter();

    // we roll our own checking for "class" in Struts2ModelInspectionVisitor
    if (converter instanceof ExtendableClassConverter) {
      return false;
    }

    // hack for STRPL-85: suppress <param>-highlighting within <result> for certain result-types
    if (converter instanceof ParamNameNestedConverter) {
      final Result result = DomUtil.getParentOfType(value, Result.class, false);
      if (result != null) {
        final ResultType resultType = result.getEffectiveResultType();
        if (resultType == null) {
          return false; // error
        }

        if (ResultTypeResolver.isChainOrRedirectType(resultType.getName().getStringValue())) {
          return false;
        }
      }
    }

    final String stringValue = value.getStringValue();

    // suppress <result> path
    if (converter instanceof StrutsPathReferenceConverter) {

      // global URLs
      if (stringValue != null &&
          stringValue.contains("://")) {
        return false;
      }

      // nested <param>-tags are present
      if (!((ParamsElement) value).getParams().isEmpty()) {
        return false;
      }

      // unsupported result-type
      final ResultType resultType = ((HasResultType) value).getEffectiveResultType();
      if (resultType == null) {
        return false;
      }

      if (!ResultTypeResolver.hasResultTypeContributor(resultType.getName().getStringValue())) {
         return false;
      }
    }

    // hack for suppressing wildcard-resolving
    return stringValue == null || !StringUtil.containsChar(stringValue, '{') ;
  }

  protected void checkDomElement(final DomElement element,
                                 final DomElementAnnotationHolder holder,
                                 final DomHighlightingHelper helper) {
    final int oldSize = holder.getSize();

    element.accept(new Struts2ModelInspectionVisitor(holder));

    if (oldSize == holder.getSize()) {
      super.checkDomElement(element, holder, helper);
    }
  }

  @NotNull
  public String getGroupDisplayName() {
    return StrutsBundle.message("inspections.groupdisplayname");
  }

  @NotNull
  @Override
  public String[] getGroupPath() {
    return new String[]{StrutsBundle.message("inspections.grouppathname"), getGroupDisplayName()};
  }

  @NotNull
  public String getDisplayName() {
    return StrutsBundle.message("inspections.struts2.model.displayname");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "Struts2ModelInspection";
  }

}
