/*
 * Copyright 2008 The authors
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

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

/**
 * @author Yann CŽbron
 */
public class StrutsCompletionTestCase extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected String getTestDataLocation() {
    return "strutsXmlCompletion";
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addStrutsJars(moduleBuilder);
  }


  public void testCompletionVariantsResultName() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-result_name.xml",
                                 "error", "input", "login", "success");
  }

  public void testCompletionVariantsResultType() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-result_type.xml",
                                 "chain", "dispatcher", "freemarker", "httpheader");
  }

  public void testCompletionVariantsResultTypeExtendingPackage() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-result_type-extending.xml",
                                 "chain", "dispatcher", "freemarker", "httpheader", "velocity");
  }

  public void testCompletionVariantsPackageExtends() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-package_extends.xml",
                                 "extendTest", "extendTest2");
  }

  /**
   * {@link  com.intellij.struts2.dom.struts.strutspackage.InterceptorRefResolveConverter}
   *
   * @throws Throwable On any errors.
   */
  public void testCompletionVariantsInterceptorRef() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-interceptor-ref.xml",
                                 "alias", "autowiring", "chain", "testInterceptorRefStack");
  }

  /**
   * {@link  com.intellij.struts2.dom.struts.action.ActionMethodConverter}
   *
   * @throws Throwable On any errors.
   */
  public void testCompletionVariantsActionMethod() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-action_method.xml",
                                 "validActionMethod",
                                 "validActionMethodWithException",
                                 "getValidActionMethodNoUnderlyingField",
                                 "validActionMethodResult");
  }

}