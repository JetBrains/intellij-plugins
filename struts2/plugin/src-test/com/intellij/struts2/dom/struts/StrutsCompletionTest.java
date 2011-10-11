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

import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsCompletionTest extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "strutsXmlCompletion";
  }

  @SkipStrutsLibrary
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

  @SkipStrutsLibrary
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
  @HasJavaSources
  @SkipStrutsLibrary
  public void testCompletionVariantsActionMethod() throws Throwable {
    performCompletionVariantTest("struts-completionvariants-action_method.xml",
                                 "validActionMethod",
                                 "validActionMethodWithException",
                                 "getValidActionMethodNoUnderlyingField",
                                 "validActionMethodResult");
  }

  /**
   * Verify all core constants are present.
   */
  public void testCompletionVariantsConstantName() throws Throwable {
    final StrutsCoreConstantContributor coreConstantContributor = new StrutsCoreConstantContributor();
    final List<StrutsConstant> constants = coreConstantContributor.getStrutsConstantDefinitions(myModule);
    final String[] variants = ContainerUtil.map2Array(constants, String.class, new Function<StrutsConstant, String>() {
      @Override
      public String fun(final StrutsConstant strutsConstant) {
        return strutsConstant.getName();
      }
    });

    performCompletionVariantTest("struts-completionvariants-constant_name.xml", variants);
  }

}