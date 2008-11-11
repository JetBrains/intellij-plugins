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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.struts2.dom.ParamsElement;
import com.intellij.struts2.dom.ParamsNameConverter;
import com.intellij.struts2.dom.struts.Bean;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter;
import com.intellij.struts2.dom.struts.impl.path.ResultTypeResolver;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.strutspackage.DefaultClassRef;
import com.intellij.struts2.dom.struts.strutspackage.Interceptor;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
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
    // we roll our own checking for "class" in S2DomModelVisitor#visitAction()
    if (value.getConverter() instanceof ExtendableClassConverter) {
      return false;
    }

    // hack for STRPL-85: suppress <param>-highlighting within <result> when pointing to Actions
    if (value.getConverter() instanceof ParamsNameConverter) {
      final Result result = DomUtil.getParentOfType(value, Result.class, false);
      if (result != null) {
        if (ResultTypeResolver.isChainOrRedirectType(result.getType().getStringValue())) {
          return false;
        }
      }
    }

    final String stringValue = value.getStringValue();

    // suppress <result> path
    if (value.getConverter() instanceof StrutsPathReferenceConverter) {

      // global URLs
      if (stringValue != null &&
          stringValue.contains("://")) {
        return false;
      }

      // nested <param>-tags are present
      if (!((ParamsElement) value).getParams().isEmpty()) {
        return false;
      }
    }

    // hack for suppressing wildcard-resolving
    return stringValue == null || stringValue.indexOf('{') < 0;
  }

  protected void checkDomElement(final DomElement element,
                                 final DomElementAnnotationHolder holder,
                                 final DomHighlightingHelper helper) {
    super.checkDomElement(element, holder, helper);

    final Struts2DomModelVisitor visitor = new Struts2DomModelVisitor(holder);
    element.accept(visitor);
  }

  @NotNull
  public String getGroupDisplayName() {
    return StrutsBundle.message("inspections.groupdisplayname");
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

  /**
   * Provides extended highlighting for various elements.
   */
  private static class Struts2DomModelVisitor implements DomElementVisitor {

    private final DomElementAnnotationHolder holder;

    Struts2DomModelVisitor(final DomElementAnnotationHolder holder) {
      this.holder = holder;
    }

    public void visitDomElement(final DomElement element) {
    }

    public void visitAction(final Action action) {
      checkExtendableClassConverter(action.getActionClass());
    }

    public void visitBean(final Bean bean) {
      checkExtendableClassConverter(bean.getBeanClass());
    }

    public void visitDefaultClassRef(final DefaultClassRef defaultClassRef) {
      checkExtendableClassConverter(defaultClassRef.getDefaultClass());
    }

    public void visitInterceptor(final Interceptor interceptor) {
      checkExtendableClassConverter(interceptor.getInterceptorClass());
    }

    public void visitResultType(final ResultType resultType) {
      checkExtendableClassConverter(resultType.getResultTypeClass());
    }

    public void visitStrutsPackage(final StrutsPackage strutsPackage) {
      final String namespace = strutsPackage.getNamespace().getStringValue();
      if (namespace != null && !namespace.startsWith("/")) {
        holder.createProblem(strutsPackage.getNamespace(),
                             StrutsBundle.message("dom.strutspackage.must.start.with.slash"));
      }
    }

    private void checkExtendableClassConverter(final GenericAttributeValue clazzAttributeValue) {
      assert clazzAttributeValue.getConverter() instanceof ExtendableClassConverter :
          "wrong converter " + clazzAttributeValue.getParent();

      final XmlElement xmlElement = DomUtil.getValueElement(clazzAttributeValue);
      if (xmlElement == null) {
        return;
      }

      final PsiReference[] psiReferences = xmlElement.getReferences();
      for (final PsiReference psiReference : psiReferences) {
        final PsiElement resolveElement = psiReference.resolve();
        if (resolveElement != null &&
            resolveElement instanceof PsiClass) {
          return;
        }
      }

      final String[] referenceTypesUserData = clazzAttributeValue.getUserData(ExtendableClassConverter.REFERENCES_TYPES);
      final String referenceTypes = referenceTypesUserData != null ?
                                    StringUtil.join(referenceTypesUserData, "|") :
                                    StrutsBundle.message("dom.extendable.class.converter.type.class");
      holder.createProblem(clazzAttributeValue, HighlightSeverity.ERROR,
                           StrutsBundle.message("dom.extendable.class.converter.cannot.resolve",
                                                referenceTypes,
                                                clazzAttributeValue.getStringValue()));
    }

  }

}
