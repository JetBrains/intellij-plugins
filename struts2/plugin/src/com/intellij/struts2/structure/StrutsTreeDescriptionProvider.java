/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.structure;

import com.intellij.psi.PsiElement;
import com.intellij.struts2.dom.params.Param;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.constant.Constant;
import com.intellij.struts2.dom.struts.strutspackage.GlobalResult;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Returns additional "location" description for certain Struts elements.
 *
 * @author Yann C&eacute;bron
 */
public final class StrutsTreeDescriptionProvider {

  public static String getElementDescription(@NotNull final PsiElement psiElement) {
    final DomElement domElement = DomUtil.getDomElement(psiElement);
    if (domElement == null) {
      return null;
    }

    return getElementDescription(domElement);
  }

  @Nullable
  private static String getElementDescription(@NotNull final DomElement domElement) {
    if (domElement instanceof StrutsPackage) {
      return ((StrutsPackage) domElement).searchNamespace();
    }

    if (domElement instanceof Result) {
      return ((Result) domElement).getStringValue();
    }

    if (domElement instanceof Param) {
      return ((Param) domElement).getStringValue();
    }

    if (domElement instanceof Constant) {
      return ((Constant) domElement).getValue().getStringValue();
    }

    if (domElement instanceof GlobalResult) {
      return ((GlobalResult) domElement).getStringValue();
    }

    return ""; // empty text stops
  }

}