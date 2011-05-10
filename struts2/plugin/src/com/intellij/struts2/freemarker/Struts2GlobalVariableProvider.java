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

package com.intellij.struts2.freemarker;

import com.intellij.freemarker.psi.FtlType;
import com.intellij.freemarker.psi.files.FtlFile;
import com.intellij.freemarker.psi.files.FtlGlobalVariableProvider;
import com.intellij.freemarker.psi.files.FtlXmlNamespaceType;
import com.intellij.freemarker.psi.variables.FtlLightVariable;
import com.intellij.freemarker.psi.variables.FtlPsiType;
import com.intellij.freemarker.psi.variables.FtlVariable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.jsp.JspManager;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.Processor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public class Struts2GlobalVariableProvider extends FtlGlobalVariableProvider {

  @NotNull
  public List<? extends FtlVariable> getGlobalVariables(final FtlFile file) {
    final Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null) {
      return Collections.emptyList();
    }

    if (StrutsFacet.getInstance(module) == null) {
      return Collections.emptyList();
    }

    final List<FtlVariable> result = new ArrayList<FtlVariable>();
    result.add(new MyFtlLightVariable("stack", file, (FtlType) null));
    result.add(new MyFtlLightVariable("response", file, "javax.servlet.http.HttpServletResponse"));
    result.add(new MyFtlLightVariable("res", file, "javax.servlet.http.HttpServletResponse"));
    result.add(new MyFtlLightVariable("request", file, "javax.servlet.http.HttpServletRequest"));
    result.add(new MyFtlLightVariable("req", file, "javax.servlet.http.HttpServletRequest"));
    result.add(new MyFtlLightVariable("session", file, "javax.servlet.http.HttpSession"));
    result.add(new MyFtlLightVariable("application", file, "javax.servlet.ServletContext"));
    result.add(new MyFtlLightVariable("base", file, CommonClassNames.JAVA_LANG_STRING));

    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_STRUTS_UI_URI, StrutsConstants.TAGLIB_STRUTS_UI_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_PREFIX);

    final Processor<Action> processor = new Processor<Action>() {
      @Override
      public boolean process(final Action action) {
        final PsiClass actionClass = action.searchActionClass();
        if (actionClass != null) {
          for (final Result result1 : action.getResults()) {
            final ResultType resultType = result1.getEffectiveResultType();
            if (resultType != null &&
                FreeMarkerStrutsResultContributor.FREEMARKER.equals(resultType.getName().getStringValue())) {
              final PathReference reference = result1.getValue();
              final PsiElement target = reference == null ? null : reference.resolve();
              if (target != null &&
                  (file.getManager().areElementsEquivalent(file, target) ||
                   file.getManager().areElementsEquivalent(file.getOriginalFile(), target))) {
                final PsiClassType actionType =
                  JavaPsiFacade.getInstance(actionClass.getProject()).getElementFactory().createType(actionClass);
                final FtlPsiType ftlPsiType = FtlPsiType.wrap(actionType);
                result.add(new MyFtlLightVariable("", action.getXmlTag(), ftlPsiType));
                result.add(new MyFtlLightVariable("action", action.getXmlTag(), ftlPsiType));
                return false; // stop after first match
              }
            }
          }
        }

        return true;
      }
    };

    for (final StrutsModel model : StrutsManager.getInstance(file.getProject()).getAllModels(module)) {
      model.processActions(processor);
    }
    return result;
  }

  private static void installTaglibSupport(@NotNull final List<FtlVariable> result,
                                           @NotNull final Module module,
                                           @NotNull @NonNls final String taglibUri,
                                           @NotNull @NonNls final String taglibPrefix) {
    final XmlFile xmlFile = JspManager.getInstance(module.getProject()).getTldFileByUri(taglibUri, module, null);
    if (xmlFile == null) {
      return;
    }

    final XmlDocument document = xmlFile.getDocument();
    if (document == null) {
      return;
    }

    final XmlNSDescriptor descriptor = (XmlNSDescriptor) document.getMetaData();
    if (descriptor == null) {
      return;
    }

    PsiElement declaration = descriptor.getDeclaration();
    if (declaration == null) {
      declaration = xmlFile;
    }

    result.add(new MyFtlLightVariable(taglibPrefix, declaration, new FtlXmlNamespaceType(descriptor)));
  }


  private static class MyFtlLightVariable extends FtlLightVariable {

    private MyFtlLightVariable(@NotNull @NonNls final String name,
                               @NotNull final PsiElement parent,
                               @Nullable final FtlType type) {
      super(name, parent, type);
    }

    private MyFtlLightVariable(@NotNull @NonNls final String name,
                               @NotNull final PsiElement parent,
                               @NotNull @NonNls final String psiType) {
      super(name, parent, psiType);
    }

    @Override
    public Icon getIcon(final boolean open) {
      return StrutsIcons.STRUTS_VARIABLE_ICON;
    }
  }

}