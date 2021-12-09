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

package com.intellij.struts2.dom.struts;

import com.intellij.ide.presentation.Presentation;
import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.util.xml.*;

/**
 * {@code bean}
 *
 * @author Yann C&eacute;bron
 */
@Presentation(typeName = "Bean", icon = "JavaUltimateIcons.Javaee.Ejb")
public interface Bean extends DomElement {

  @NameValue(unique = false)
    // TODO must be unique within same "type"-elements
//  @Required TODO not for static=true
  @Required(value = false, nonEmpty = true)
  GenericAttributeValue<String> getName();

  @Attribute(value = "type")
  @ExtendClass(instantiatable = false, allowAbstract = true, allowInterface = true)
  GenericAttributeValue<PsiClass> getBeanType();

  @Attribute(value = "class")
  @ExtendClass(instantiatable = false, allowAbstract = false, allowInterface = false)
  @Convert(ExtendableClassConverter.class)
  GenericAttributeValue<PsiClass> getBeanClass();

  @Attribute(value = "scope")
  GenericAttributeValue<BeanScope> getScope();

  @Attribute(value = "static")
  GenericAttributeValue<Boolean> getStatic();

  @Attribute(value = "optional")
  GenericAttributeValue<Boolean> getOptional();
}
