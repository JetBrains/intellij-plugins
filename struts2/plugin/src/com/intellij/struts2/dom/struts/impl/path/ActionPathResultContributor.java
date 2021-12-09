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

package com.intellij.struts2.dom.struts.impl.path;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides paths to "/XYZ.action".
 * <p/>
 * TODO integrate in DispatchPathResultContributor
 *
 * @author Yann C&eacute;bron
 */
public class ActionPathResultContributor extends StrutsResultContributor {

  @Override
  public boolean matchesResultType(@NotNull @NonNls final String resultType) {
    return ResultTypeResolver.isDispatchType(resultType);
  }

  @Override
  public boolean createReferences(@NotNull final PsiElement psiElement,
                                  final @NotNull List<PsiReference> references,
                                  final boolean soft) {
    final StrutsModel model = StrutsManager.getInstance(psiElement.getProject())
                                           .getModelByFile((XmlFile) psiElement.getContainingFile());
    if (model == null) {
      return false;
    }

    final String currentPackage = getNamespace(psiElement);
    if (currentPackage == null) {
      return false;
    }

    final TextRange rangeInElement = ElementManipulators.getValueTextRange(psiElement);
    final String fullPath = psiElement.getText();
    final String trimmedPath = rangeInElement.substring(fullPath);
    final TextRange trimmedPathRange = TextRange.from(rangeInElement.getStartOffset(),
                                                      PathReference.trimPath(trimmedPath).length());

    final PsiReference actionReference = new ActionPathReference((XmlTag) psiElement,
                                                                 trimmedPathRange,
                                                                 currentPackage,
                                                                 model);

    references.add(actionReference);
    return false;
  }

  @Override
  @Nullable
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    return createDefaultPathReference(path, element, Struts2Icons.Action);
  }


  private static final class ActionPathReference extends PsiReferenceBase<XmlTag> {

    private final String currentPackage;
    private final StrutsModel model;

    private ActionPathReference(final XmlTag psiElement,
                                final TextRange textRange,
                                final String currentPackage,
                                final StrutsModel model) {
      super(psiElement, textRange, true);
      this.currentPackage = currentPackage;
      this.model = model;
    }

    private List<String> getActionExtensions() {
      return StrutsConstantHelper.getActionExtensions(myElement);
    }

    @Override
    public PsiElement resolve() {
      final String path = getCanonicalText();

      int extensionIndex = -1;
      for (final String actionExtension : getActionExtensions()) {
        extensionIndex = path.lastIndexOf(actionExtension);
        if (extensionIndex != -1) {
          break;
        }
      }
      if (extensionIndex == -1) {
        return null;
      }

      // use given namespace or current if none given
      final int namespacePrefixIndex = path.lastIndexOf("/");
      final String namespace;
      if (namespacePrefixIndex != -1) {
        namespace = path.substring(0, namespacePrefixIndex);
      } else {
        namespace = currentPackage;
      }

      // "/XX/" behind ".extension" --> not parseable
      if (namespacePrefixIndex > extensionIndex) {
        return null;
      }

      final String strippedPath = path.substring(namespacePrefixIndex != -1 ? namespacePrefixIndex + 1 : 0,
                                                 extensionIndex);
      final List<Action> actions = model.findActionsByName(strippedPath, namespace);
      if (actions.size() == 1) {
        final Action action = actions.get(0);
        return action.getXmlTag();
      }

      return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
      final List<String> extensions = getActionExtensions();
      if (extensions.isEmpty()) {
        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
      }

      final String firstExtension = extensions.get(0);

      final List<Action> allActions = model.getActionsForNamespace(null);
      final List<LookupElementBuilder> variants = new ArrayList<>(allActions.size());
      for (final Action action : allActions) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          final boolean isInCurrentPackage = Objects.equals(action.getNamespace(), currentPackage);

          // prepend package-name if not default ("/") or "current" package
          final String actionNamespace = action.getNamespace();
          final String fullPath;
          if (!Objects.equals(actionNamespace, StrutsPackage.DEFAULT_NAMESPACE) &&
              !isInCurrentPackage) {
            fullPath = actionNamespace + "/" + actionPath + firstExtension;
          } else {
            fullPath = actionPath + firstExtension;
          }

          final LookupElementBuilder builder = LookupElementBuilder.create(action.getXmlTag(), fullPath)
                                                                   .withBoldness(isInCurrentPackage)
                                                                   .withIcon(Struts2Icons.Action)
                                                                   .withTypeText(action.getNamespace());
          variants.add(builder);
        }
      }

      return ArrayUtil.toObjectArray(variants);
    }
  }

}
