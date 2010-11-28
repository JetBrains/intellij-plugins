/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
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
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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
    if (module == null) return Collections.emptyList();

    if (StrutsFacet.getInstance(module) == null) return Collections.emptyList();

    List<FtlVariable> result = new ArrayList<FtlVariable>();
    result.add(new FtlLightVariable("stack", file, (FtlType)null));
    result.add(new FtlLightVariable("action", file, (FtlType)null));
    result.add(new FtlLightVariable("response", file, "javax.servlet.http.HttpServletResponse"));
    result.add(new FtlLightVariable("res", file, "javax.servlet.http.HttpServletResponse"));
    result.add(new FtlLightVariable("request", file, "javax.servlet.http.HttpServletRequest"));
    result.add(new FtlLightVariable("req", file, "javax.servlet.http.HttpServletRequest"));
    result.add(new FtlLightVariable("session", file, "javax.servlet.http.HttpSession"));
    result.add(new FtlLightVariable("application", file, "javax.servlet.ServletContext"));
    result.add(new FtlLightVariable("base", file, CommonClassNames.JAVA_LANG_STRING));

    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_STRUTS_UI_URI, StrutsConstants.TAGLIB_STRUTS_UI_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_PLUGIN_PREFIX);
    installTaglibSupport(result, module,
                         StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI, StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_PREFIX);

    for (final StrutsModel model : StrutsManager.getInstance(file.getProject()).getAllModels(module)) {
      for (final StrutsPackage strutsPackage : model.getStrutsPackages()) {
        for (final Action action : strutsPackage.getActions()) {
          final PsiClass actionClass = action.getActionClass().getValue();
          if (actionClass != null) {
            final PsiClassType actionType =
              JavaPsiFacade.getInstance(actionClass.getProject()).getElementFactory().createType(actionClass);
            for (final Result result1 : action.getResults()) {
              if ("freemarker".equals(result1.getType().getStringValue())) {
                final PathReference reference = result1.getValue();
                final PsiElement target = reference == null ? null : reference.resolve();
                if (target != null &&
                    (file.getManager().areElementsEquivalent(file, target) ||
                     file.getManager().areElementsEquivalent(file.getOriginalFile(), target))) {
                  result.add(new FtlLightVariable("", action.getXmlTag(), FtlPsiType.wrap(actionType)));
                  break;
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  private static void installTaglibSupport(@NotNull final List<FtlVariable> result,
                                           @NotNull final Module module,
                                           @NotNull @NonNls final String taglibUri,
                                           @NotNull @NonNls final String taglibPrefix) {
    final XmlFile xmlFile = JspManager.getInstance(module.getProject()).getTldFileByUri(taglibUri, module, null);
    if (xmlFile != null) {
      final XmlDocument document = xmlFile.getDocument();
      if (document != null) {
        final XmlNSDescriptor descriptor = (XmlNSDescriptor)document.getMetaData();
        if (descriptor != null) {
          PsiElement declaration = descriptor.getDeclaration();
          if (declaration == null) declaration = xmlFile;
          result.add(new FtlLightVariable(taglibPrefix, declaration, new FtlXmlNamespaceType(descriptor)));
        }
      }
    }
  }

}
