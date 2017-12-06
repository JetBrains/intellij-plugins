/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion;

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.coldFusion.UI.config.CfmlMappingsConfig;
import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlCompletionTest extends JavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    CamelHumpMatcher.forceStartMatching(myFixture.getTestRootDisposable());
  }

  static void addJavaClassToSubPackage(JavaCodeInsightTestFixture fixture) {
    fixture.addFileToProject("subPackage/MyClass.java", "package subPackage;\n" +
                                                        "\n" +
                                                        "public class MyClass {\n" +
                                                        "}");
  }

  static PsiClass addJavaClassTo(JavaCodeInsightTestFixture fixture) {
    return fixture.addClass("import java.util.Collection;\n" +
                            "import java.util.Collections;\n" +
                            "import java.util.LinkedList;\n" +
                            "\n" +
                            "public class MyClass {\n" +
                            "  private Collection<String> myCollection = new LinkedList<String>();\n" +
                            "  public static final int VERSION = 1;\n" +
                            "  public MyClass() {}\n" +
                            "  public void add(String s) {\n" +
                            "    myCollection.add(s);\n" +
                            "  }\n" +
                            "  public Collection<String> get() {\n" +
                            "    return Collections.unmodifiableCollection(myCollection);\n" +
                            "    \n" +
                            "  }\n" +
                            "  public static foo() {}\n" +
                            "}");
  }

  static PsiFile addOneComponentWithConstructorTo(JavaCodeInsightTestFixture fixture) {
    return fixture.addFileToProject("ComponentWithConstructor.cfc",
                                    "<cfcomponent>\n" +
                                    "<cffunction name=\"init\">\n" +
                                    "    <cfargument name=\"arg1\">\n" +
                                    "    <cfargument name=\"arg2\">\n" +
                                    "</cffunction>" +
                                    "</cfcomponent>");
  }

  static PsiFile addOneComponentToResolve(JavaCodeInsightTestFixture fixture) {
    return fixture.addFileToProject("MyComponentToResolve.cfc", "<cfcomponent>\n" +
                                                                "<cffunction name=\"init\">\n" +
                                                                "    <cfreturn this>\n" +
                                                                "</cffunction>\n" +
                                                                "    <cffunction name=\"foo\">\n" +
                                                                "</cffunction>\n" +
                                                                "</cfcomponent>");
  }

  static PsiFile addTwoComponents(JavaCodeInsightTestFixture fixture) {
    fixture.addFileToProject("MyComponent2.cfc",
                             "<cfcomponent>\n" +
                             "</cfcomponent>");

    return fixture.addFileToProject("MyComponent1.cfc",
                                    "<cfcomponent>\n" +
                                    "</cfcomponent>");
  }

  static PsiFile addOneComponentTo(JavaCodeInsightTestFixture fixture) {
    return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                    "<cfcomponent>\n" +
                                    "    <cffunction name=\"func1\">\n" +
                                    "    </cffunction>\n" +
                                    "\n" +
                                    "    <cffunction name=\"func2\">\n" +
                                    "    </cffunction>\n" +
                                    "</cfcomponent>");

  }

  static PsiFile addOneComponentToDir(JavaCodeInsightTestFixture fixture) {
    return fixture.addFileToProject("subfolder/ComponentName.cfc",
                                    "<cfcomponent>\n" +
                                    "    <cffunction name=\"func1\">\n" +
                                    "    </cffunction>\n" +
                                    "\n" +
                                    "    <cffunction name=\"func2\">\n" +
                                    "    </cffunction>\n" +
                                    "</cfcomponent>");
  }

  public void testAttributeCompletionAtFileEnd() {
    doTest();
  }

  public void testAttributeCompletionAtFileMiddle() {
    doTest();
  }

  public void testAttributeVariants() {
    doTestCompletionVariants("returntype", "returnformat", "roles");
  }

  public void testAccessorsCompletion() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent accessors=\"true\">\n" +
                                                             "\t<cfproperty name=\"Foo\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("getFoo()", "setFoo()", "Foo");
  }

  public void testPropertyGetterCompletion() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent>\n" +
                                                             "\t<cfproperty name=\"Foo\" getter=\"true\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("getFoo()", "Foo");
  }

  public void testPropertySetterCompletion() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent>\n" +
                                                             "\t<cfproperty name=\"Foo\" setter=\"true\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("setFoo()", "Foo");
  }

  public void testPropertyAccessorCompletionForPersistentComponent() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent persistent=\"true\">\n" +
                                                             "\t<cfproperty name=\"foo\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("setFoo()", "getFoo()", "foo");
  }


  public void testMappedSuperClassCompletionInComponentTag() {
    doTest();
  }

  public void testMappedSuperClassCompletionInScriptComponent() {
    doTest();
  }

  public void testAttributeVariantsInPropertyTag() {
    doTest();
  }

  public void testAttributeVariantsInStartProperty() {
    doTestCompletionContainsVariants("name", "notnull");
  }

  public void testAttributeOrmTypeInProperty() {
    doTest();
  }

  public void testReturnTypeCompletion() {
    addOneComponentTo(myFixture);
    doTestCompletionContainsVariants("func1", "func2");
  }

  public void testReturnTypeCompletionInScriptFunctionDefinition() {
    doTestCompletionContainsVariants("String", "void", "Numeric");
  }

  public void testTagCompletionAtFileEnd() {
    doTestCompletionVariants("cffunction", "cffeed", "cffileupload", "cffinally", "cffile", "cfflush", "cfform", "cfformgroup",
                             "cfformitem", "cfftp");
  }

  public void testTagCompletionAfterAngleBracket() {
    final ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
    final String doctype = manager.getDefaultHtmlDoctype(getProject());
    manager.setDefaultHtmlDoctype(XmlUtil.XHTML_URI, getProject());
    try {
      doTestCompletionContainsVariants("cffunction", "center", "cfset", "h1");
    }
    finally {
      manager.setDefaultHtmlDoctype(doctype, getProject());
    }
  }

  public void testTagCompletionAfterC() {
    final ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
    final String doctype = manager.getDefaultHtmlDoctype(getProject());
    manager.setDefaultHtmlDoctype(XmlUtil.XHTML_URI, getProject());
    try {
      doTestCompletionContainsVariants("cffunction", "center", "cfset");
    }
    finally {
      manager.setDefaultHtmlDoctype(doctype, getProject());
    }
  }

  public void testTagCompletionAfterCf() {
    doTestCompletionContainsVariants("cffunction", "cfset");
  }

  public void testPropertyWordCompletionInScriptComponent() {
    doTest();
  }

  public void testSecondPropertyWordCompletionInScriptComponent() {
    doTestCompletionContainsVariants("property");
  }

  public void testNoPropertyWordCompletionInScriptComponent() {
    doTest();
  }

  public void testFunctionNamesCompletion() {
    doTestCompletionVariants("mid", "min", "mineFunc", "minute");
  }

  public void testVariableNamesCompletion() {
    doTestCompletionVariants("mid", "min", "mineVariable", "minute");
  }

  public void testVariableAndFunctionNamesCompletion() {
    doTestCompletionVariants("mineFunction", "mineVariable", "mid", "min", "minute");
  }

  public void testFunctionNameWithBracketsCompletion() {
    doTest();
  }

  public void testClassInstanceCompletion() {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "init", "init");
  }

  public void testClassInstanceCompletionFromCreator() {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "init", "init");
  }

  public void testClassInstanceCompletionAfterInit() {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "add", "equals", "get", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait", "wait",
                             "wait");
  }

  public void testClassInstanceCompletionAfterInitFromCreator() {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "add", "equals", "get", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait", "wait",
                             "wait");
  }

  public void testVariableFromCommentCompletion() {
    doTest();
    // doTestCompletionVariants("user");
  }

  public void testVariableFromCommentWithScopeCompletion() {
    doTest();
  }

  public void testScopedVariableCompletion() {
    doTestCompletionVariants("removeCachedQuery", "request.user", "request.some", "reque");
  }

  public void testAttributeValuesCompletion() {
    doTestCompletionVariants("cf_sql_bigint", "cf_sql_bit", "cf_sql_char", "cf_sql_blob", "cf_sql_clob", "cf_sql_date", "cf_sql_decimal",
                             "cf_sql_double", "cf_sql_float", "cf_sql_idstamp", "cf_sql_integer", "cf_sql_longvarchar", "cf_sql_money",
                             "cf_sql_money4", "cf_sql_numeric", "cf_sql_real", "cf_sql_refcursor", "cf_sql_smallint", "cf_sql_time",
                             "cf_sql_timestamp", "cf_sql_tinyint", "cf_sql_varchar");
  }


  public void testAttributeValuesForCustomTagCompletion() {
    doTest();
  }

  public void testAttributeCfloopArrayCompletionInCf8() throws Throwable {
    Util.runTestWithLanguageLevel(() -> {
      doTest();
      return null;
    }, CfmlLanguage.CF8, getProject());
  }

  public void testAttributeCompletionWhenTagUppercased() {
    doTest();
  }

  public void testTagFunctionArgumentsCompletion() {
    doTestCompletionVariants("arguments.some1", "arguments.some2");
  }

  public void testScriptComponentAttributesCompletion() {
    doTestCompletionVariants("extends", "embedded", "entityname");
  }

  public void testAutocompletePathToExpandPath() {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent2.cfc", "MyComponent1.cfc", "autocompletePathToExpandPath.test.cfml");
  }

  public void testAutocompletePathToExpandPathInScript() {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent2.cfc", "MyComponent1.cfc", "autocompletePathToExpandPathInScript.test.cfml");
  }

  public void testAutocompletePathToComponent() {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testAutocompletePathToComponentInPresentOfMapping() {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/folder/subfolder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("/myfolder/subfolder", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      doTest();
    } finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }

  public void testAutocompletePathToIncludeTagInPresentOfMapping() {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/folder/subfolder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("/myfolder/subfolder", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      doTest();
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }

  public void testAutocompletePathToModuleTag() {
    addTwoComponents(myFixture);
    doTestCompletionContainsVariants("MyComponent1.cfc","MyComponent2.cfc");
  }

  public void testNoCompletionToIncludeTagInPresentOfMapping() {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/folder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("myfolder", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig());
    state.setMapps(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      doTest();
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }

  public void testAutocompletePathToScriptIncludeInPresentOfMappingWithEnteredPath() {
    addOneComponentToDir(myFixture);

    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/subfolder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("/lot", directoryName);
        mappings.put("/fot", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig());
    state.setMapps(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      doTestCompletionContainsVariants("lot", "fot");
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }

  public void testAutocompletePathToIncludeTagInPresentOfMappingWithFile() {
    addOneComponentToDir(myFixture);

    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/subfolder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("/abc", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      doTestCompletionContainsVariants("ComponentName.cfc");
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }


  public void testAutocompletePathToScriptIncludeInPresentOfMapping() {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/folder/subfolder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("/myfolder/subfolder", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      doTest();
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }

  public void testAutocompletePathToScriptIncludeInPresentOfMappingWithFile() {
    addOneComponentToDir(myFixture);

    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/subfolder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("/subfolder", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      doTestCompletionVariants("ComponentName.cfc");
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }

  public void testCompleteChainFunctionsCall() {
    addOneComponentToResolve(myFixture);
    doTestCompletionVariants("init", "foo");
  }

  public void testCompleteChainFunctionsCallAfterConstructorCall() {
    addOneComponentToResolve(myFixture);
    doTestCompletionVariants("init", "foo");
  }

  public void testCompleteSuperFunctionsInTagDefinedComponent() {
    addOneComponentTo(myFixture);
    doTestCompletionVariants("func1", "func2");
  }

  public void testCompleteSuperFunctionsInScriptDefinedComponent() {
    addOneComponentTo(myFixture);
    doTestCompletionVariants("this.func1", "this.func2");
  }

  public void testFunctionsNamesCompletionFromFollowingDeclarations() {
    doTestCompletionVariants("func1", "func2", "func3", "func4", "func5");
  }

  public void testCompletionArgumentsNames() {
    doTestCompletionVariants("argument1", "argument2", "argumentCollection");
  }

  public void testCompletionArgumentsNamesFromScriptFunction() {
    doTestCompletionVariants("argument1", "argument2", "argumentCollection");
  }

  public void testDoNotOfferDeclaredAttributes() {
    doTestCompletionVariants("hint", "default", "required", "type");
  }

  public void testCompleteAfterElvis() {
    doTest();
  }

  public void testArgumentCompletionAtCreateObject() {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice");
  }

  public void testArgumentCompletionAtCompleteCreateObject() {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice");
  }

  public void testTagArgumentCompletionAtCreateObject() {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice");
  }

  public void testComponentArgumentCompletionAtCreateObject() {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testComponentArgumentCompletionAtArgument() {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testComponentArgumentCompletionAtReturnType() {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testNoFunctionCompletionAtReturnType() {
    doTest();
  }

  public void testComponentArgumentCompletionAtReturnTypeInScript() {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testComponentArgumentCompletionAtArgumentInScript() {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testPredefinedVariablesForTagCompletion() {
    doTestCompletionContainsVariants("recordcount", "columnlist", "currentrow");
  }

  public void testPredefinedVariablesCompletion() {
    doTestCompletionContainsVariants("server.coldfusion.productversion", "server.coldfusion.rootdir");
  }

  public void testInsertPackageName() {
    addJavaClassToSubPackage(myFixture);
    doTest();
  }

  public void testVariableFromLocalScope() {
    doTest();
  }

  public void testArgumentCollection() {
    doTest();
  }

  public void testArgumentCollection2() {
    doTestCompletionVariants("argumentCollection", "argumentCollectionFoo");
  }

  public void testTagPropertyCompletion() {
    doTestCompletionVariants("prop1", "prop2");
  }

  public void testScopedPropertyCompletion() {
    doTestCompletionVariants("variables.prop1", "variables.prop2");
  }


  public void testThisScopedVariableCompletion() {
    doTest();
  }


  public void testPropertyCompletion() {
    doTestCompletionVariants("prop1", "prop2");
  }

  public void testStandardFunctionArgumentName() {
    doTestCompletionVariants("number1", "number2", "numberFormat");
  }

  public void testStandardFunctionArgumentName2() {
    doTest();
  }

  public void testCfml183() {
    doTest();
  }

  public void testCompleteNewArguments() {
    addOneComponentWithConstructorTo(myFixture);
    doTestCompletionVariants("arg1", "arg2", "argumentCollection");
  }

  public void testCompleteNew() {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent1", "MyComponent2");
  }

  public void testCompleteStringedNew() {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent1", "MyComponent2");
  }

  public void testOneVariantForVariables() {
    doTestCompletionVariants("variables.var1", "variables.var2");
  }

  public void testNoIntelliJIdeaRulezzSuggestion() {
    doTestCompletionVariants("fu1", "fu2");
  }

  public void testCompleteCfloopIndices() {
    doTestCompletionVariants("index1", "index2");
  }

  public void testGlobalVariablesCompletionInCfmlTagComponent() {
    doTest();
  }

  public void testGlobalVariablesCompletionInScriptComponent() {
    doTest("\n");
  }

  public void testScopedGlobalVariablesCompletionInCfmlTagComponent() {
    doTestCompletionContainsVariants("variables.injector");
  }

  private void doTestCompletionContainsVariants(String... items) {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    List<String> variants = myFixture.getCompletionVariants(inputDataFileName);
    assertTrue(variants.containsAll(Arrays.asList(items)));
  }

  private void doTestCompletionVariants(@NonNls String... items) {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    myFixture.testCompletionVariants(inputDataFileName, items);
  }

  private void doTest() {
    doTest("");
  }

  private void doTest(final String type) {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    String expectedResultFileName = Util.getExpectedDataFileName(getTestName(true));
    String[] input = {inputDataFileName};
    myFixture.testCompletionTyping(input, type, expectedResultFileName);
  }

  @Override
  protected String getTestDataPath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected String getBasePath() {
    return "/completion";
  }

  protected PsiElement resolveReferenceAtCaret() {
    return myFixture.getReferenceAtCaretPositionWithAssertion(Util.getInputDataFileName(getTestName(true))).resolve();
  }
}
