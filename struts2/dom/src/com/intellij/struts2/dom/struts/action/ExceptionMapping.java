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

package com.intellij.struts2.dom.struts.action;

import com.intellij.ide.presentation.Presentation;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.struts2.Struts2PresentationProvider;
import com.intellij.util.xml.*;

/**
 * @author Yann C&eacute;bron
 */
@Presentation(typeName = "Exception Mapping", provider = Struts2PresentationProvider.class)
public interface ExceptionMapping extends DomElement /* ParamsElement TODO ??! */ {

  // @NameValue  TODO ??
  GenericAttributeValue<String> getName();

  @Attribute("exception")
  @ExtendClass(value = CommonClassNames.JAVA_LANG_EXCEPTION, instantiatable = false, allowInterface = false)
  @Required
  GenericAttributeValue<PsiClass> getExceptionClass();

  @Convert(ExceptionMappingResultResolveConverter.class)
  @Required
  GenericAttributeValue<Result> getResult();

}