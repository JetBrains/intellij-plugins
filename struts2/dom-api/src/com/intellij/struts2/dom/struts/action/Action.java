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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.struts2.dom.params.ParamsElement;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorRef;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * {@code action}
 *
 * @author Yann C&eacute;bron
 */
@Stubbed
@Presentation(typeName = "Struts Action", icon = "com.intellij.struts2.Struts2Icons.Action")
public interface Action extends ParamsElement {

  /**
   * Default action name ("execute").
   */
  @NonNls
  String DEFAULT_ACTION_METHOD_NAME = "execute";

  @Referencing(value = ActionNameCustomReferenceConverter.class, soft = true)
  @Attribute(value = "name")
  @NameValue
  @Stubbed
  @Required(nonEmpty = true)
  @NotNull
  GenericAttributeValue<String> getName();

  @Attribute(value = "class")
  @Convert(ExtendableClassConverter.class)
  @ExtendClass(allowAbstract = false, allowInterface = false, instantiatable = false)
  @Stubbed
  GenericAttributeValue<PsiClass> getActionClass();

  @Attribute(value = "method")
  @Convert(ActionMethodConverter.class)
  GenericAttributeValue<PsiMethod> getMethod();


  // --------------------
  @SubTagList("result")
  @NotNull
  List<Result> getResults();

  @SubTagList("interceptor-ref")
  @NotNull
  List<InterceptorRef> getInterceptorRefs();

  @SubTagList("exception-mapping")
  @NotNull
  List<ExceptionMapping> getExceptionMappings();

  @SubTag(value = "allowed-methods")
  @Convert(AllowedMethodsConverter.class)
  @Nullable
  GenericDomValue<String> getAllowedMethods();

  // additional methods -----------------------------------

  /**
   * Returns whether this Action has a wildcard mapping.
   *
   * @return {@code true} if wildcard mapping.
   */
  boolean isWildcardMapping();

  /**
   * Does the given path match this action's path (including support for wildcards).
   *
   * @param path Path to check.
   * @return true if yes.
   */
  boolean matchesPath(@NotNull final String path);

  /**
   * Gets the enclosing package.
   *
   * @return Enclosing package.
   */
  @NotNull
  StrutsPackage getStrutsPackage();

  /**
   * Search the Action-class being used: <ol> <li>local "class" attribute</li> <li>default-class-ref of parent
   * package (search hierarchy upwards)</li> </ol>
   *
   * @return null if no matches.
   */
  @Nullable
  PsiClass searchActionClass();

  /**
   * Gets the defined method or the default method named {@link #DEFAULT_ACTION_METHOD_NAME}.
   *
   * @return null if nothing could be found.
   */
  @Nullable
  PsiMethod searchActionMethod();

  /**
   * Gets the namespace from enclosing {@link StrutsPackage}.
   *
   * @return Namespace identifier.
   */
  @NotNull
  String getNamespace();

  /**
   * Gets all methods suitable serving as action.
   *
   * @return List of methods (can be empty).
   */
  @NotNull
  List<PsiMethod> getActionMethods();

  /**
   * Finds the first suitable action-method with the given name.
   *
   * @param methodName Method name to search for.
   * @return {@code null} if no suitable method was found.
   */
  @Nullable
  PsiMethod findActionMethod(@Nullable final String methodName);

}
