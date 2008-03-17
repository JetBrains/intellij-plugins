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

package com.intellij.struts2.annotators;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.jsp.impl.TldDescriptor;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsp.JspFileViewProvider;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.reference.ReferenceFilters;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Icons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Annotates custom tags with "action" attribute.
 *
 * @author Yann CŽbron
 */
public class JspActionAnnotator implements Annotator {

  public static final LayeredIcon ACTION_CLASS_ICON = new LayeredIcon(2);

  private static final String[] TAGS_WITH_ACTION_ATTRIBUTE = new String[]{"action", "form", "url"};

  static {
    ACTION_CLASS_ICON.setIcon(Icons.CLASS_ICON, 0);
    ACTION_CLASS_ICON.setIcon(StrutsIcons.ACTION_SMALL, 1, 0, StrutsIcons.SMALL_ICON_Y_OFFSET);
  }

  public void annotate(final PsiElement psiElement, final AnnotationHolder annotationHolder) {
    if (!(psiElement instanceof XmlTag)) {
      return;
    }

    // short exit when Struts 2 facet not present
    if (StrutsFacet.getInstance(psiElement) == null) {
      return;
    }

    // short exit when Struts 2 taglib not declared
    final XmlTag xmlTag = (XmlTag) psiElement;
    final String uiTaglibPrefix = getUITaglibPrefix(xmlTag);
    if (uiTaglibPrefix == null) {
      return;
    }

    // match tag-prefix/name
    if (xmlTag.getNamespacePrefix().equals(uiTaglibPrefix) &&
        Arrays.binarySearch(TAGS_WITH_ACTION_ATTRIBUTE, xmlTag.getLocalName()) > -1) {

      // special case for <action> 
      final String actionPath = xmlTag.getLocalName().equals("action") ? xmlTag.getAttributeValue("name") :
                                xmlTag.getAttributeValue("action");
      if (actionPath == null) {
        return;
      }

      final StrutsModel strutsModel = StrutsManager.getInstance(psiElement.getProject()).getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement));
      if (strutsModel == null) {
        return;
      }
      final String namespace = xmlTag.getAttributeValue("namespace");
      final List<Action> actions = strutsModel.findActionsByName(actionPath, namespace);
      if (!actions.isEmpty()) {

        // resolve to action method should be exactly 0||1
        final List<PsiMethod> navigationTargets = new ArrayList<PsiMethod>(actions.size());
        for (final Action action : actions) {
          ContainerUtil.addIfNotNull(action.searchActionMethod(), navigationTargets);
        }

        if (!navigationTargets.isEmpty()) {
          NavigationGutterIconBuilder.create(ACTION_CLASS_ICON).
            setTooltipText("Go To Action method").
            setTargets(navigationTargets).
            install(annotationHolder, xmlTag);
        }
      }
    }
  }

  @Nullable
  private static String getUITaglibPrefix(final XmlTag xmlTag) {
    final PsiFile containingFile = xmlTag.getContainingFile();
    if (!(containingFile instanceof JspFile)) {
      return null;
    }

    final JspFile jspFile = (JspFile) containingFile;
    final XmlDocument document = jspFile.getDocument();
    if (document == null) {
      return null;
    }

    final XmlTag rootTag = document.getRootTag();
    if (rootTag == null) {
      return null;
    }

    final Set<String> knownTaglibPrefixes = ((JspFileViewProvider) jspFile.getViewProvider()).getKnownTaglibPrefixes();
    for (final String prefix : knownTaglibPrefixes) {
      final String namespaceByPrefix = rootTag.getNamespaceByPrefix(prefix);
      final XmlNSDescriptor descriptor = rootTag.getNSDescriptor(namespaceByPrefix, true);
      if (descriptor != null && descriptor instanceof TldDescriptor) {
        final String uri = ((TldDescriptor) descriptor).getUri();
        if (uri != null && uri.equals(ReferenceFilters.TAGLIB_STRUTS_UI_URI)) {  // URI is optional in TLD!
          return prefix;
        }
      }
    }
    return null;
  }

}
