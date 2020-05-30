/*
 * Copyright 2019 The authors
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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public abstract class ResultImpl implements Result {

  @Override
  @Nullable
  public PsiClass getParamsClass() {
    final ResultType type = getEffectiveResultType();
    return type != null ? type.getResultTypeClass().getValue() : null;
  }

  @Override
  @Nullable
  public ResultType getEffectiveResultType() {
    final GenericAttributeValue<ResultType> typeAttribute = getType();
    if (DomUtil.hasXml(typeAttribute)) {
      return typeAttribute.getValue();
    }

    final StrutsPackage strutsPackage = getParentOfType(StrutsPackage.class, true);
    return strutsPackage != null ? strutsPackage.searchDefaultResultType() : null;
  }

  @Override
  public String getNameOrDefault() {
    final String resultName = getName().getStringValue();
    return resultName != null ? resultName : Result.DEFAULT_NAME;
  }
}