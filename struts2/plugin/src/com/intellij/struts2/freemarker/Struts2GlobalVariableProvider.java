/*
 * Copyright 2014 The authors
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
import com.intellij.javaee.web.WebCommonClassNames;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.jsp.JspManager;
import com.intellij.psi.util.PsiTypesUtil;
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

final class Struts2GlobalVariableProvider extends FtlGlobalVariableProvider {
  @Override
  @NotNull
  public List<? extends FtlVariable> getGlobalVariables(final FtlFile file) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module == null) {
      return Collections.emptyList();
    }

    if (StrutsFacet.getInstance(module) == null) {
      return Collections.emptyList();
    }

    final List<FtlVariable> result = new ArrayList<>();
    result.add(new MyFtlLightVariable("stack", file, (FtlType) null));
    result.add(new MyFtlLightVariable("response", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_RESPONSE));
    result.add(new MyFtlLightVariable("res", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_RESPONSE));
    result.add(new MyFtlLightVariable("request", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_REQUEST));
    result.add(new MyFtlLightVariable("req", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_REQUEST));
    result.add(new MyFtlLightVariable("session", file, WebCommonClassNames.JAVAX_HTTP_SESSION));
    result.add(new MyFtlLightVariable("application", file, WebCommonClassNames.JAVAX_SERVLET_CONTEXT));
    result.add(new MyFtlLightVariable("base", file, CommonClassNames.JAVA_LANG_STRING));

    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_STRUTS_UI_URI, StrutsConstants.TAGLIB_STRUTS_UI_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_CHART_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_CHART_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_TREE_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_TREE_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_GRID_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_GRID_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_MOBILE_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_MOBILE_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_BOOTSTRAP_PLUGIN_URI, StrutsConstants.TAGLIB_BOOTSTRAP_PLUGIN_PREFIX);

    final Processor<Action> processor = action -> {
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
              final PsiClassType actionType = PsiTypesUtil.getClassType(actionClass);
              final FtlPsiType ftlPsiType = FtlPsiType.wrap(actionType);
              result.add(new MyFtlLightVariable("", action.getXmlTag(), ftlPsiType));
              result.add(new MyFtlLightVariable("action", action.getXmlTag(), ftlPsiType));
              return false; // stop after first match
            }
          }
        }
      }

      return true;
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


  private static final class MyFtlLightVariable extends FtlLightVariable {

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
      return StrutsIcons.STRUTS_VARIABLE;
    }
  }

}