/*
 * Copyright 2015 The authors
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

package com.intellij.struts2.dom.struts.constant;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.model.constant.StrutsConstantKey;
import com.intellij.struts2.model.constant.StrutsConstantManager;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class ConstantValueConverterImpl extends ConstantValueConverter {

  @Override
  @Nullable
  public Converter<?> getConverter(@NotNull final GenericDomValue domElement) {
    final Constant constant = (Constant) domElement.getParent();
    assert constant != null;

    final String constantName = constant.getName().getStringValue();
    if (StringUtil.isEmpty(constantName)) {
      return null;
    }

    final XmlTag xmlTag = domElement.getXmlTag();
    final StrutsConstantManager constantManager = StrutsConstantManager.getInstance(xmlTag.getProject());

    return constantManager.findConverter(xmlTag, StrutsConstantKey.create(constantName));
  }

}