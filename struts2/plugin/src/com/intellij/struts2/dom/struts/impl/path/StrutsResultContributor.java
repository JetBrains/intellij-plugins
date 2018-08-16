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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.paths.PathReferenceProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.struts2.dom.struts.HasResultType;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Function;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Provides references for {@code <result>/<global-result>}.
 * <p/>
 * Third party plugins can provide additional result types by providing a suitable subclass registered via extension
 * in their {@code plugin.xml}:
 * <p/>
 * <pre>
 *   &lt;extensions defaultExtensionNs="com.intellij">
 *     &lt;struts2.resultContributor implementation="[Name of your class]"/>
 *   &lt;/extensions>
 * </pre>
 *
 * @author Yann C&eacute;bron
 */
public abstract class StrutsResultContributor implements PathReferenceProvider {

  /**
   * Extension point name.
   */
  public static final ExtensionPointName<StrutsResultContributor> EP_NAME =
    new ExtensionPointName<>("com.intellij.struts2.resultContributor");

  /**
   * Returns whether this ResultContributor handles the given result type.
   *
   * @param resultType Result type.
   * @return {@code true} if yes.
   */
  protected abstract boolean matchesResultType(@NonNls @NotNull final String resultType);

  /**
   * Gets the current namespace for the given element.
   *
   * @param psiElement Current element.
   * @return {@code null} on XML errors or if {@link #matchesResultType(String)} returns {@code false}.
   */
  @Nullable
  protected final String getNamespace(@NotNull final PsiElement psiElement) {
    final DomElement resultElement = DomUtil.getDomElement(psiElement);
    if (resultElement == null) {
      return null; // XML syntax error
    }

    assert resultElement instanceof HasResultType : "not instance of HasResultType: " + resultElement +
                                                    ", text: " + psiElement.getText();

    final ResultType effectiveResultType = ((HasResultType) resultElement).getEffectiveResultType();
    if (effectiveResultType == null) {
      return null;
    }

    final String resultType = effectiveResultType.getName().getStringValue();
    if (resultType == null ||
        !matchesResultType(resultType)) {
      return null;
    }

    final StrutsPackage strutsPackage = resultElement.getParentOfType(StrutsPackage.class, true);
    if (strutsPackage == null) {
      return null; // XML syntax error
    }

    return strutsPackage.searchNamespace();
  }

  /**
   * Creates PathReference from resolve result.
   *
   * @param path       Path to resolve.
   * @param element    Context element.
   * @param staticIcon Static icon or {@code null} for resolve target's icon.
   * @return PathReference or {@code null} if no references.
   */
  @Nullable
  protected PathReference createDefaultPathReference(final String path,
                                                     final PsiElement element,
                                                     @Nullable final Icon staticIcon) {
    final ArrayList<PsiReference> list = new ArrayList<>(5);
    createReferences(element, list, true);
    if (list.isEmpty()) {
      return null;
    }

    final PsiElement target = list.get(list.size() - 1).resolve();
    if (target == null) {
      return null;
    }

    final Function<PathReference, Icon> iconFunction;
    if (staticIcon == null) {
      iconFunction = webPath -> target.getIcon(Iconable.ICON_FLAG_READ_STATUS);
    } else {
      iconFunction = new ConstantFunction<>(staticIcon);
    }

    return new PathReference(path, iconFunction) {
      @Override
      public PsiElement resolve() {
        return target;
      }
    };
  }
}