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
 *
 */

package com.intellij.struts2.dom.struts.impl.path;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.paths.PathReferenceProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides references for &lt;result&gt;.
 *
 * @author Yann C&eacute;bron
 */
public abstract class StrutsResultContributor implements PathReferenceProvider {

  /**
   * Register extension points with this name to include additional result types.
   */
  public static final ExtensionPointName<StrutsResultContributor> EP_NAME =
          new ExtensionPointName<StrutsResultContributor>("com.intellij.struts2.resultContributor");

  /**
   * Override to limit to certain known result-type names.
   *
   * @param resultType Result type.
   * @return true.
   */
  public boolean matchesResultType(@NonNls @Nullable final String resultType) {
    return true;
  }

  /**
   * Gets the current namespace for the given element.
   *
   * @param psiElement Current element.
   * @return null on XML errors or if {@link #matchesResultType(String)} returns <code>false</code>.
   */
  @Nullable
  protected final String getNamespace(@NotNull final PsiElement psiElement) {
    final DomElement resultElement = DomUtil.getDomElement(psiElement);
    if (resultElement == null) {
      return null; // XML syntax error
    }

    final StrutsPackage strutsPackage = resultElement.getParentOfType(StrutsPackage.class, true);
    if (strutsPackage == null) {
      return null; // XML syntax error
    }

    final XmlTag resultTag = resultElement.getXmlTag();
    if (resultTag == null) {
      return null; // XML syntax error
    }

    final String resultType = resultTag.getAttributeValue("type");
    if (!matchesResultType(resultType)) {
      return null;
    }

    return strutsPackage.searchNamespace();
  }

}