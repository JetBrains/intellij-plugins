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

package com.intellij.struts2.model.constant.contributor;

import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.converters.DelimitedListConverter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Plain comma-separated String list w/o completion/resolving.
 *
 * @author Yann C&eacute;bron
 */
class DelimitedStringValuesConverter extends DelimitedListConverter<String> {

  DelimitedStringValuesConverter() {
    super(",");
  }

  @Override
  protected String convertString(@Nullable final String s, final ConvertContext convertContext) {
    return s;
  }

  @Override
  protected String toString(@Nullable final String s) {
    return s;
  }

  @Override
  protected Object[] getReferenceVariants(final ConvertContext convertContext,
                                          final GenericDomValue<? extends List<String>> listGenericDomValue) {
    return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
  }

  @Override
  protected PsiElement resolveReference(@Nullable final String s, final ConvertContext convertContext) {
    return convertContext.getReferenceXmlElement();
  }

  @Override
  protected String getUnresolvedMessage(final String s) {
    return "???"; // should never happen
  }

}