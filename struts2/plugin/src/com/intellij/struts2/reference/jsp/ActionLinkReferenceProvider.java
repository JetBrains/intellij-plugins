/*
 * Copyright 2008 The authors
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
package com.intellij.struts2.reference.jsp;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.javaee.web.CustomServletReferenceAdapter;
import com.intellij.javaee.web.ServletMappingInfo;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides links to Action-URLs in all places where Servlet-URLs are processed.
 *
 * @author Yann C&eacute;bron
 */
public class ActionLinkReferenceProvider extends CustomServletReferenceAdapter {

  protected PsiReference[] createReferences(@NotNull final PsiElement psiElement,
                                            final int offset,
                                            final String text,
                                            @Nullable final ServletMappingInfo info,
                                            final boolean soft) {
    final StrutsModel strutsModel = StrutsManager.getInstance(psiElement.getProject()).
        getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement));

    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final List<String> actionExtensions = StrutsConstantHelper.getActionExtensions(psiElement);
    if (actionExtensions.isEmpty()) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (StringUtil.indexOf(text, '/') != -1) {
      return new PsiReference[]{
          new ActionLinkPackageReference((XmlElement) psiElement, offset, text, soft, strutsModel, actionExtensions),
          new ActionLinkReference((XmlElement) psiElement, offset, text, soft, strutsModel, actionExtensions)
      };
    } else {
      return new PsiReference[]{
          new ActionLinkReference((XmlElement) psiElement, offset, text, soft, strutsModel, actionExtensions)
      };

    }
  }

  @Nullable
  public PathReference createWebPath(final String path,
                                     @NotNull final PsiElement psiElement,
                                     final ServletMappingInfo servletMappingInfo) {
    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    if (strutsManager.getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement)) == null) {
      return null;
    }

    return new PathReference(path, new PathReference.ConstFunction(StrutsIcons.ACTION)); /*{
TODO not needed so far ?!
   public PsiElement resolve() {
        return action.getXmlTag();
      }
    };*/
  }


  private static class ActionLinkReference extends PsiReferenceBase<XmlElement> implements EmptyResolveMessageProvider {

    private final StrutsModel strutsModel;
    private final List<String> actionExtensions;
    private final String fullActionPath;

    private ActionLinkReference(final XmlElement element,
                                final int offset,
                                final String text,
                                final boolean soft,
                                final StrutsModel strutsModel,
                                final List<String> actionExtensions) {
      super(element, new TextRange(offset, offset + text.length()), soft);
      this.strutsModel = strutsModel;
      this.actionExtensions = actionExtensions;

      fullActionPath = PathReference.trimPath(getValue());
      final int lastSlash = fullActionPath.lastIndexOf("/");

      // adapt TextRange to everything behind /packageName/
      if (lastSlash != -1) {
        setRangeInElement(TextRange.from(offset + lastSlash + 1, fullActionPath.length() - lastSlash - 1));
      }

      // reduce to action-name if full path given
      for (final String actionExtension : actionExtensions) {
        if (StringUtil.endsWith(fullActionPath, actionExtension)) {
          setRangeInElement(TextRange.from(getRangeInElement().getStartOffset(),
                                           getRangeInElement().getLength() - actionExtension.length()));
          break;
        }
      }
    }

    public PsiElement resolve() {
      final String ourActionExtension = ContainerUtil.find(actionExtensions, new Condition<String>() {
        public boolean value(final String s) {
          return StringUtil.endsWith(fullActionPath, s);
        }
      });
      if (ourActionExtension == null) {
        return null;
      }

      final String actionName = getActionName(fullActionPath, ourActionExtension);
      final String namespace = getNamespace(fullActionPath);
      final List<Action> actions = strutsModel.findActionsByName(actionName, namespace);
      if (actions.isEmpty()) {
        return null;
      }

      final Action myAction = actions.get(0);
      return myAction.getXmlTag();
    }

    @NotNull
    public Object[] getVariants() {
      final String namespace = getNamespace(fullActionPath);

      final String firstExtension = actionExtensions.get(0);

      final List<Action> actionList = strutsModel.getActionsForNamespace(namespace);
      final List<Object> variants = new ArrayList<Object>(actionList.size());
      for (final Action action : actionList) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          final Object variant = LookupValueFactory.createLookupValueWithHint(actionPath + firstExtension,
                                                                              StrutsIcons.ACTION,
                                                                              action.getNamespace());
          variants.add(variant);
        }
      }
      return ArrayUtil.toObjectArray(variants);
    }

    public String getUnresolvedMessagePattern() {
      return "Cannot resolve action ''" + getValue() + "''";
    }

    @NotNull
    private static String getActionName(final String fullActionPath,
                                        final String ourActionExtension) {
      final int slashIndex = fullActionPath.lastIndexOf("/");
      final int extensionIndex = fullActionPath.lastIndexOf(ourActionExtension);
      return fullActionPath.substring(slashIndex + 1, extensionIndex);
    }

  }

  /**
   * Extracts the namespace from the given action path.
   *
   * @param fullActionPath Full path.
   * @return Namespace.
   */
  @NotNull
  private static String getNamespace(final String fullActionPath) {

    final int lastSlash = fullActionPath.lastIndexOf('/');
    // no slash, use fake "root" for resolving "myAction.action"
    if (lastSlash == -1) {
      return "/";
    }

    // root-package
    if (lastSlash == 0) {
      return "/";
    }

    int firstSlash = fullActionPath.indexOf('/');
    return fullActionPath.substring(firstSlash, lastSlash);
  }


  /**
   * Provides reference to S2-package within action-path.
   */
  private static class ActionLinkPackageReference extends PsiReferenceBase<XmlElement> implements EmptyResolveMessageProvider {

    private final String namespace;
    private final List<StrutsPackage> allStrutsPackages;
    private final String fullActionPath;
    private final List<String> actionExtensions;

    private ActionLinkPackageReference(final XmlElement element,
                                       final int offset,
                                       final String text,
                                       final boolean soft,
                                       final StrutsModel strutsModel,
                                       final List<String> actionExtensions) {
      super(element, computeRange(offset, text), soft);
      this.actionExtensions = actionExtensions;

      fullActionPath = PathReference.trimPath(text);
      namespace = getNamespace(fullActionPath);

      allStrutsPackages = strutsModel.getStrutsPackages();
    }

    private static TextRange computeRange(int offset, String text) {
      int lastSlash = text.lastIndexOf('/');
      return new TextRange(offset, offset + (lastSlash == -1 ? text.length() : lastSlash));
    }

    public PsiElement resolve() {
      final String validExtension = ContainerUtil.find(actionExtensions, new Condition<String>() {
        public boolean value(final String s) {
          return StringUtil.endsWith(fullActionPath, s);
        }
      });
      if (validExtension == null) {
        return null;
      }

      for (final StrutsPackage strutsPackage : allStrutsPackages) {
        if (Comparing.equal(namespace, strutsPackage.searchNamespace())) {
          return strutsPackage.getXmlTag();
        }
      }

      return null;
    }

    @NotNull
    public Object[] getVariants() {
      return ContainerUtil.map2Array(allStrutsPackages, Object.class, new Function<StrutsPackage, Object>() {
        public Object fun(final StrutsPackage strutsPackage) {
          final String packageNamespace = strutsPackage.searchNamespace();
          return LookupValueFactory.createLookupValueWithHint(
              packageNamespace.length() != 1 ? packageNamespace + "/" : packageNamespace,
              StrutsIcons.PACKAGE, strutsPackage.getName().getStringValue());
        }
      });
    }

    public String getUnresolvedMessagePattern() {
      return "Cannot resolve Struts 2 package ''" + namespace + "''";
    }
  }

}