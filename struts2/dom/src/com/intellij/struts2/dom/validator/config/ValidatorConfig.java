/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.dom.validator.config;

import com.intellij.ide.presentation.Presentation;
import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.StrutsDomConstants;
import com.intellij.util.xml.*;

/**
 * {@code validator} in validator-config.xml.
 *
 * @author Yann C&eacute;bron
 */
@Namespace(StrutsDomConstants.VALIDATOR_CONFIG_NAMESPACE_KEY)
@Presentation(typeName = "Validator", icon = "AllIcons.Actions.EditSource")
public interface ValidatorConfig extends DomElement {

  @NameValue
  @Required
  GenericAttributeValue<String> getName();

  @ExtendClass("com.opensymphony.xwork2.validator.Validator")
  @Attribute("class")
  @Required
  GenericAttributeValue<PsiClass> getValidatorClass();

}
