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

package com.intellij.struts2.dom.struts.strutspackage;

import com.intellij.ide.presentation.Presentation;
import com.intellij.jam.model.common.CommonDomModelElement;
import com.intellij.struts2.Struts2PresentationProvider;
import com.intellij.struts2.dom.StrutsDomConstants;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * {@code <package>}
 *
 * @author Yann C&eacute;bron
 */
@Namespace(StrutsDomConstants.STRUTS_NAMESPACE_KEY)
@Stubbed
@Presentation(typeName = "Struts Package", provider = Struts2PresentationProvider.class)
public interface StrutsPackage extends CommonDomModelElement {

  String DEFAULT_NAMESPACE = "/";

  @NameValue
  @Required
  @Stubbed
  GenericAttributeValue<String> getName();

  @Convert(StrutsPackageExtendsResolveConverter.class)
  @Stubbed
  GenericAttributeValue<List<StrutsPackage>> getExtends();

  GenericAttributeValue<Boolean> getAbstract();

  @Required(value = false, nonEmpty = true)
  @Stubbed
  GenericAttributeValue<String> getNamespace();

  GenericAttributeValue<Boolean> getStrictMethodInvocation();

  // externalReferenceResolver - deprecated

  /**
   * Gets the defined namespace or {@link #DEFAULT_NAMESPACE} if none defined.
   *
   * @return Namespace.
   */
  @NotNull
  String searchNamespace();

  // default-XXX tags ------------

  DefaultActionRef getDefaultActionRef();

  DefaultInterceptorRef getDefaultInterceptorRef();

  DefaultClassRef getDefaultClassRef();

  // global-XXX tags -------------

  @SubTag("global-results")
  GlobalResults getGlobalResults();

  @SubTag("global-exception-mappings")
  GlobalExceptionMappings getGlobalExceptionMappings();

  // --------------

  /**
   * not used directly.
   *
   * @return result-types element
   */
  @SubTag("result-types")
  ResultTypes getResultTypesElement();

  @PropertyAccessor({"resultTypesElement", "resultTypes"})
  List<ResultType> getResultTypes();

  /**
   * not used directly.
   *
   * @return interceptors element
   */
  @SubTag("interceptors")
  Interceptors getInterceptorsElement();

  @PropertyAccessor({"interceptorsElement", "interceptors"})
  List<Interceptor> getInterceptors();

  @PropertyAccessor({"interceptorsElement", "interceptorStacks"})
  List<InterceptorStack> getInterceptorStacks();

  @SubTagList(value = "action")
  List<Action> getActions();

  /**
   * Searches the {@code default-class-ref} element for this package, walking up the hierarchy until one is found.
   *
   * @return null if none was found.
   */
  @Nullable
  DefaultClassRef searchDefaultClassRef();

  /**
   * Searches the default {@code result-type} element for this package, walking up the hierarchy until one is found.
   *
   * @return null if no default defined.
   */
  @Nullable
  ResultType searchDefaultResultType();


}
