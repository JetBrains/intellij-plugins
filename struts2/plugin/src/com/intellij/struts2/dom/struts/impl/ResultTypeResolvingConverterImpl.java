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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.action.ResultTypeResolvingConverter;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class ResultTypeResolvingConverterImpl extends ResultTypeResolvingConverter {

  @NotNull
  public Collection<? extends ResultType> getVariants(final ConvertContext context) {
    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);
    if (strutsModel == null) {
      return Collections.emptyList();
    }

    return getMergedResultTypes(strutsModel);
  }

  public ResultType fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (StringUtil.isEmpty(name)) {
      return null;
    }

    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);
    if (strutsModel == null) {
      return null;
    }

    final List<ResultType> allTypes = getMergedResultTypes(strutsModel);

    for (final ResultType allType : allTypes) {
      final String resultTypeName = allType.getName().getStringValue();
      if (resultTypeName != null && resultTypeName.equals(name)) {
        return allType;
      }
    }

    return null;
  }

  private static List<ResultType> getMergedResultTypes(@NotNull final StrutsModel strutsModel) {
    final List<ResultType> allTypes = new ArrayList<ResultType>();
    for (final StrutsPackage strutsPackage : strutsModel.getStrutsPackages()) {
      allTypes.addAll(strutsPackage.getResultTypes());
    }

    return allTypes;
  }

}