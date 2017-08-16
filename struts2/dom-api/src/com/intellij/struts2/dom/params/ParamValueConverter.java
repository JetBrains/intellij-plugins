/*
 * Copyright 2017 The authors
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
package com.intellij.struts2.dom.params;

import com.intellij.psi.PsiType;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.WrappingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 * @see ParamValueConvertersRegistry
 */
public class ParamValueConverter extends WrappingConverter {

  @Nullable
  @Override
  public Converter getConverter(@NotNull GenericDomValue domElement) {
    final Param param = domElement.getParentOfType(Param.class, false);
    if (param == null) {
      return null;
    }

    // skip values containing expressions
    final String text = domElement.getRawText();
    if (text == null || text.contains("${")) {
      return null;
    }

    final List<BeanProperty> beanProperties = param.getName().getValue();
    if (beanProperties == null || beanProperties.isEmpty()) {
      return null;
    }

    final BeanProperty property = beanProperties.get(beanProperties.size() - 1);
    final PsiType type = property.getPropertyType();
    return ParamValueConvertersRegistry.getInstance().getConverter(domElement, type);
  }
}
