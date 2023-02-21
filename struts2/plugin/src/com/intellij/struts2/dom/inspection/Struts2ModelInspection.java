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

package com.intellij.struts2.dom.inspection;

import com.intellij.codeInspection.options.OptPane;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.struts2.dom.params.ParamNameConverter;
import com.intellij.struts2.dom.params.ParamsElement;
import com.intellij.struts2.dom.struts.HasResultType;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.ActionMethodConverter;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter;
import com.intellij.struts2.dom.struts.impl.path.ResultTypeResolver;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.io.URLUtil;
import com.intellij.util.xml.*;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.intellij.codeInspection.options.OptPane.checkbox;
import static com.intellij.codeInspection.options.OptPane.pane;

/**
 * Default DOM-Model inspection for struts.xml files.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2ModelInspection extends BasicDomElementsInspection<StrutsRoot> {

  /**
   * @noinspection PublicField
   */
  public boolean ignoreExtendableClass;

  public Struts2ModelInspection() {
    super(StrutsRoot.class);
  }

  @Override
  public @NotNull OptPane getOptionsPane() {
    return pane(
      checkbox("ignoreExtendableClass", StrutsBundle.message("inspections.struts2.model.do.not.check.extendable.class")));
  }

  /**
   * Only inspect struts.xml files configured in fileset.
   *
   * @param strutsRootDomFileElement Root element of file to inspect.
   * @param holder                   Holder.
   */
  @Override
  public void checkFileElement(final @NotNull DomFileElement<StrutsRoot> strutsRootDomFileElement,
                               final @NotNull DomElementAnnotationHolder holder) {
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

  @Override
  protected boolean shouldCheckResolveProblems(final GenericDomValue value) {
    final Converter converter = value.getConverter();

    // we roll our own checking for "class" in Struts2ModelInspectionVisitor
    if (converter instanceof ExtendableClassConverter) {
      return false;
    }

    // hack for STRPL-85: suppress <param>-highlighting within <result> for certain result-types
    if (converter instanceof ParamNameConverter) {
      final Result result = DomUtil.getParentOfType(value, Result.class, false);
      if (result != null) {
        final ResultType resultType = result.getEffectiveResultType();
        if (resultType == null) {
          return false; // error
        }

        final String resultTypeValue = resultType.getName().getStringValue();
        if (resultTypeValue != null && ResultTypeResolver.isChainOrRedirectType(resultTypeValue)) {
          return false;
        }
      }
    }

    final String stringValue = value.getStringValue();

    // suppress <action> "method" when using wildcards
    if (converter instanceof ActionMethodConverter &&
        ConverterUtil.hasWildcardReference(stringValue)) {
      return false;
    }

    // suppress <result> path
    if (converter instanceof StrutsPathReferenceConverter) {

      if (stringValue == null) {
        return false;
      }

      // nested <param>-tags are present
      if (!((ParamsElement)value).getParams().isEmpty()) {
        return false;
      }

      // unsupported result-type
      final ResultType resultType = ((HasResultType)value).getEffectiveResultType();
      if (resultType == null) {
        return false;
      }

      if (!ResultTypeResolver.hasResultTypeContributor(resultType.getName().getStringValue())) {
        return false;
      }

      // suppress paths with wildcard reference
      if (ConverterUtil.hasWildcardReference(stringValue)) {
        final Action action = DomUtil.getParentOfType(value, Action.class, true);
        return action != null && !action.isWildcardMapping();
      }

      // "${actionProperty}"
      if (StringUtil.startsWith(stringValue, "${")) {
        return false;
      }

      // global URLs
      if (URLUtil.containsScheme(stringValue)) {
        return false;
      }
    }

    return true;
  }

  @Override
  protected void checkDomElement(final @NotNull DomElement element,
                                 final @NotNull DomElementAnnotationHolder holder,
                                 final @NotNull DomHighlightingHelper helper) {
    final int oldSize = holder.getSize();

    element.accept(new Struts2ModelInspectionVisitor(holder, ignoreExtendableClass));

    if (oldSize == holder.getSize()) {
      super.checkDomElement(element, holder, helper);
    }
  }

  @Override
  public String @NotNull [] getGroupPath() {
    return new String[]{StrutsBundle.message("inspections.group.path.name"), getGroupDisplayName()};
  }

  @Override
  @NotNull
  @NonNls
  public String getShortName() {
    return "Struts2ModelInspection";
  }
}
