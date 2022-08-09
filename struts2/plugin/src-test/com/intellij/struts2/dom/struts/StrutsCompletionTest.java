/*
 * Copyright 2014 The authors
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

import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsCompletionTest extends StrutsLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "strutsXml/completion";
  }

  public void testCompletionVariantsResultName() {
    performCompletionVariantTest("struts-completionvariants-result_name.xml",
                                 "error", "input", "login", "success");
  }

  public void testCompletionVariantsResultType() {
    performCompletionVariantTest("struts-completionvariants-result_type.xml",
                                 "chain", "dispatcher", "freemarker", "httpheader");
  }

  public void testCompletionVariantsResultTypeExtendingPackage() {
    performCompletionVariantTest("struts-completionvariants-result_type-extending.xml",
                                 "chain", "chain2", "dispatcher", "freemarker", "httpheader", "velocity");
  }

  public void testCompletionVariantsPackageExtends() {
    performCompletionVariantTest("struts-completionvariants-package_extends.xml",
                                 "extendTest", "extendTest2");
  }

  /**
   * {@link  com.intellij.struts2.dom.struts.strutspackage.InterceptorRefResolveConverter}
   *
   */
  public void testCompletionVariantsInterceptorRef() {
    performCompletionVariantTest("struts-completionvariants-interceptor-ref.xml",
                                 "alias", "autowiring", "chain", "testInterceptorRefStack");
  }

  public void testCompletionVariantsInterceptorRefExtendsPackage() {
    performCompletionVariantTest("struts-completionvariants-interceptor-ref-extends.xml",
                                 "alias", "autowiring", "testInterceptorRefStack");
  }

  /**
   * {@link  com.intellij.struts2.dom.struts.action.ActionMethodConverter}
   *
   */
  public void testCompletionVariantsActionMethod() {
    myFixture.copyFileToProject("ActionClass.java");
    performCompletionVariantTest("struts-completionvariants-action_method.xml",
                                 "validActionMethod",
                                 "validActionMethodWithException",
                                 "getValidActionMethodNoUnderlyingField",
                                 "validActionMethodResult");
  }

  /**
   * Verify all core constants are present.
   */
  public void testCompletionVariantsConstantName() {
    final StrutsCoreConstantContributor coreConstantContributor = new StrutsCoreConstantContributor();
    final List<StrutsConstant> constants = coreConstantContributor.getStrutsConstantDefinitions(getModule());
    final String[] variants = ContainerUtil.map2Array(constants, String.class, strutsConstant -> strutsConstant.getName());

    performCompletionVariantTest("struts-completionvariants-constant_name.xml", variants);
  }
}