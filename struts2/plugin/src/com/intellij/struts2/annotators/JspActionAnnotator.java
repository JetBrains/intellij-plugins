/*
 * Copyright 2010 The authors
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
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Annotates custom tags with "action" attribute.
 *
 * @author Yann C&eacute;bron
 */
public class JspActionAnnotator implements Annotator {

  @NonNls
  private static final String ACTION_ATTRIBUTE_NAME = "action";

  @NonNls
  private static final String[] TAGS_WITH_ACTION_ATTRIBUTE = new String[]{"action", "form", "reset", "submit", "url"};

  private static final NullableFunction<Action, PsiMethod> ACTION_METHOD_FUNCTION = new NullableFunction<Action, PsiMethod>() {
    public PsiMethod fun(final Action action) {
      return action.searchActionMethod();
    }
  };

  public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder annotationHolder) {
    if (!(psiElement instanceof XmlTag)) {
      return;
    }

    // short exit when Struts 2 facet not present
    if (StrutsFacet.getInstance(psiElement) == null) {
      return;
    }

    final XmlTag xmlTag = (XmlTag) psiElement;

    // any of our tags?
    final String tagName = xmlTag.getLocalName();
    if (Arrays.binarySearch(TAGS_WITH_ACTION_ATTRIBUTE, tagName) < 0) {
      return;
    }

    // determine Struts 2 taglib prefix
    final String uiTaglibPrefix = getUITaglibPrefix(xmlTag);
    if (uiTaglibPrefix == null ||
        !Comparing.equal(xmlTag.getNamespacePrefix(), uiTaglibPrefix)) {
      return;
    }

    // special case for <action>
    final String actionPath = Comparing.equal(tagName, ACTION_ATTRIBUTE_NAME) ?
                              xmlTag.getAttributeValue("name") :
                              xmlTag.getAttributeValue(ACTION_ATTRIBUTE_NAME);
    if (actionPath == null) {
      return;
    }

    final StrutsModel strutsModel = StrutsManager.getInstance(psiElement.getProject())
      .getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement));
    if (strutsModel == null) {
      return;
    }

    final String namespace = xmlTag.getAttributeValue("namespace");
    final List<Action> actions = strutsModel.findActionsByName(actionPath, namespace);
    if (actions.isEmpty()) {
      return;
    }

    // resolve to action method should be exactly 1
    NavigationGutterIconBuilder.create(StrutsIcons.ACTION_CLASS_ICON).
      setTooltipText(StrutsBundle.message("annotators.jsp.goto.action.method")).
      setEmptyPopupText(StrutsBundle.message("annotators.jsp.goto.action.method.notfound")).
      setTargets(new NotNullLazyValue<Collection<? extends PsiElement>>() {
        @NotNull
        protected Collection<PsiMethod> compute() {
          return ContainerUtil.mapNotNull(actions, ACTION_METHOD_FUNCTION);
        }
      }).install(annotationHolder, xmlTag);
  }

  // TODO cache in JspFile's UserData?
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
    return ContainerUtil.find(knownTaglibPrefixes, new Condition<String>() {
      public boolean value(final String s) {
        final String namespaceByPrefix = rootTag.getNamespaceByPrefix(s);
        final XmlNSDescriptor descriptor = rootTag.getNSDescriptor(namespaceByPrefix, true);
        if (descriptor instanceof TldDescriptor) {
          final String uri = ((TldDescriptor) descriptor).getUri(); // URI is optional in TLD!
          return Comparing.equal(uri, StrutsConstants.TAGLIB_STRUTS_UI_URI);
        }
        return false;
      }
    });
  }

}
