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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.struts.strutspackage.GlobalResult;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public abstract class GlobalResultImpl implements GlobalResult {

  @Override
  @Nullable
  public PsiClass getParamsClass() {
    final ResultType resultType = getType().getValue();
    return resultType != null ? resultType.getResultTypeClass().getValue() : null;
  }

  @Override
  @Nullable
  public ResultType getEffectiveResultType() {
    final ResultType resultType = getType().getValue();
    if (resultType != null) {
      return resultType;
    }

    final StrutsPackage strutsPackage = getParentOfType(StrutsPackage.class, true);
    return strutsPackage != null ? strutsPackage.searchDefaultResultType() : null;
  }

}