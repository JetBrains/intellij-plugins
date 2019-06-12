/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.model.constant.contributor;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.converters.DelimitedListConverter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Resolve to comma-separated list of result-types.
 *
 * @author Yann C&eacute;bron
 */
class ResultTypeConverter extends DelimitedListConverter<ResultType> {

  private static final Function<StrutsPackage, Collection<? extends ResultType>> RESULT_TYPE_GETTER =
    strutsPackage -> strutsPackage.getResultTypes();

  ResultTypeConverter() {
    super(",");
  }

  @Override
  protected ResultType convertString(@Nullable final String s, final ConvertContext convertContext) {
    if (StringUtil.isEmpty(s)) {
      return null;
    }

    final StrutsModel strutsModel = ConverterUtil.getStrutsModelOrCombined(convertContext);
    if (strutsModel == null) {
      return null;
    }

    final List<ResultType> resultTypes = ContainerUtil.concat(strutsModel.getStrutsPackages(),
                                                              RESULT_TYPE_GETTER);

    return ContainerUtil.find(resultTypes, resultType -> Comparing.strEqual(s, resultType.getName().getStringValue()));
  }

  @Override
  protected String toString(@Nullable final ResultType resultType) {
    return resultType != null ? resultType.getName().getStringValue() : null;
  }

  @Override
  protected Object[] getReferenceVariants(final ConvertContext convertContext,
                                          final GenericDomValue<? extends List<ResultType>> listGenericDomValue) {
    final StrutsModel strutsModel = ConverterUtil.getStrutsModelOrCombined(convertContext);
    if (strutsModel == null) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    return ElementPresentationManager.getInstance()
        .createVariants(ContainerUtil.concat(strutsModel.getStrutsPackages(), RESULT_TYPE_GETTER));
  }

  @Override
  protected PsiElement resolveReference(@Nullable final ResultType resultType, final ConvertContext convertContext) {
    return resultType != null ? resultType.getXmlTag() : null;
  }

  @Override
  protected String getUnresolvedMessage(final String s) {
    return "Cannot resolve result-type '" + s + "'";
  }

}
