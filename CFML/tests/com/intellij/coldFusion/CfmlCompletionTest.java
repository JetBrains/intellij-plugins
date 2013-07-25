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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lera Nikolaenko
 * Date: 04.12.2008
 */
public class CfmlCompletionTest extends JavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    CamelHumpMatcher.forceStartMatching(getTestRootDisposable());
  }

  static void addJavaClassToSubPackage(JavaCodeInsightTestFixture fixture) throws IOException {
    fixture.addFileToProject("subPackage/MyClass.java", "package subPackage;\n" +
                                                        "\n" +
                                                        "public class MyClass {\n" +
                                                        "}");
  }

  static PsiClass addJavaClassTo(JavaCodeInsightTestFixture fixture) throws IOException {
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

  static PsiFile addOneComponentWithConstructorTo(JavaCodeInsightTestFixture fixture) throws IOException {
    return fixture.addFileToProject("ComponentWithConstructor.cfc",
                                    "<cfcomponent>\n" +
                                    "<cffunction name=\"init\">\n" +
                                    "    <cfargument name=\"arg1\">\n" +
                                    "    <cfargument name=\"arg2\">\n" +
                                    "</cffunction>" +
                                    "</cfcomponent>");
  }

  static PsiFile addOneComponentToResolve(JavaCodeInsightTestFixture fixture) throws IOException {
    return fixture.addFileToProject("MyComponentToResolve.cfc", "<cfcomponent>\n" +
                                                                "<cffunction name=\"init\">\n" +
                                                                "    <cfreturn this>\n" +
                                                                "</cffunction>\n" +
                                                                "    <cffunction name=\"foo\">\n" +
                                                                "</cffunction>\n" +
                                                                "</cfcomponent>");
  }

  static PsiFile addTwoComponents(JavaCodeInsightTestFixture fixture) throws IOException {
    fixture.addFileToProject("MyComponent2.cfc",
                             "<cfcomponent>\n" +
                             "</cfcomponent>");

    return fixture.addFileToProject("MyComponent1.cfc",
                                    "<cfcomponent>\n" +
                                    "</cfcomponent>");
  }

  static PsiFile addOneComponentTo(JavaCodeInsightTestFixture fixture) throws IOException {
    return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                    "<cfcomponent>\n" +
                                    "    <cffunction name=\"func1\">\n" +
                                    "    </cffunction>\n" +
                                    "\n" +
                                    "    <cffunction name=\"func2\">\n" +
                                    "    </cffunction>\n" +
                                    "</cfcomponent>");

  }

  static PsiFile addOneComponentToDir(JavaCodeInsightTestFixture fixture) throws IOException {
    return fixture.addFileToProject("subfolder/ComponentName.cfc",
                                    "<cfcomponent>\n" +
                                    "    <cffunction name=\"func1\">\n" +
                                    "    </cffunction>\n" +
                                    "\n" +
                                    "    <cffunction name=\"func2\">\n" +
                                    "    </cffunction>\n" +
                                    "</cfcomponent>");
  }

  public void testAttributeCompletionAtFileEnd() throws Throwable {
    doTest();
  }

  public void testAttributeCompletionAtFileMiddle() throws Throwable {
    doTest();
  }

  public void testAttributeVariants() throws Throwable {
    doTestCompletionVariants("returntype", "returnformat", "roles");
  }

  public void testAccessorsCompletion() throws Throwable {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent accessors=\"true\">\n" +
                                                             "\t<cfproperty name=\"Foo\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("getFoo()", "setFoo()", "Foo");
  }

  public void testPropertyGetterCompletion() throws Throwable {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent>\n" +
                                                             "\t<cfproperty name=\"Foo\" getter=\"true\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("getFoo()", "Foo");
  }

  public void testPropertySetterCompletion() throws Throwable {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent>\n" +
                                                             "\t<cfproperty name=\"Foo\" setter=\"true\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("setFoo()", "Foo");
  }

  public void testPropertyAccessorCompletionForPersistentComponent() throws Throwable {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent persistent=\"true\">\n" +
                                                             "\t<cfproperty name=\"foo\" type=\"STRING\">\n" +
                                                             "</cfcomponent>");
    doTestCompletionVariants("setFoo()", "getFoo()", "foo");
  }


  public void testMappedSuperClassCompletionInComponentTag() throws Throwable {
    doTest();
  }

  public void testMappedSuperClassCompletionInScriptComponent() throws Throwable {
    doTest();
  }

  public void testAttributeVariantsInPropertyTag() throws Throwable {
    doTest();
  }

  public void testAttributeVariantsInStartProperty() throws Throwable {
    doTestCompletionContainsVariants("name", "notnull");
  }

  public void testAttributeOrmTypeInProperty() throws Throwable {
    doTest();
  }

  public void testReturnTypeCompletion() throws Throwable {
    addOneComponentTo(myFixture);
    doTestCompletionContainsVariants("func1", "func2");
  }

  public void testReturnTypeCompletionInScriptFunctionDefinition() throws Throwable {
    doTestCompletionContainsVariants("String", "void", "Numeric");
  }

  public void testTagCompletionAtFileEnd() throws Throwable {
    doTestCompletionVariants("cffunction", "cffeed", "cffileupload", "cffinally", "cffile", "cfflush", "cfform", "cfformgroup",
                             "cfformitem", "cfftp");
  }

  public void testTagCompletionAfterAngleBracket() throws Throwable {
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

  public void testTagCompletionAfterC() throws Throwable {
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

  public void testTagCompletionAfterCf() throws Throwable {
    doTestCompletionContainsVariants("cffunction", "cfset");
  }

  public void testPropertyWordCompletionInScriptComponent() throws Throwable {
    doTest();
  }

  public void testSecondPropertyWordCompletionInScriptComponent() throws Throwable {
    doTestCompletionContainsVariants("property");
  }

  public void testNoPropertyWordCompletionInScriptComponent() throws Throwable {
    doTest();
  }

  public void testFunctionNamesCompletion() throws Throwable {
    doTestCompletionVariants("mid", "min", "mineFunc", "minute");
  }

  public void testVariableNamesCompletion() throws Throwable {
    doTestCompletionVariants("mid", "min", "mineVariable", "minute");
  }

  public void testVariableAndFunctionNamesCompletion() throws Throwable {
    doTestCompletionVariants("mineFunction", "mineVariable", "mid", "min", "minute");
  }

  public void testFunctionNameWithBracketsCompletion() throws Throwable {
    doTest();
  }

  public void testClassInstanceCompletion() throws Throwable {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "init", "init");
  }

  public void testClassInstanceCompletionFromCreator() throws Throwable {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "init", "init");
  }

  public void testClassInstanceCompletionAfterInit() throws Throwable {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "add", "equals", "get", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait", "wait",
                             "wait");
  }

  public void testClassInstanceCompletionAfterInitFromCreator() throws Throwable {
    addJavaClassTo(myFixture);
    doTestCompletionVariants("VERSION", "add", "equals", "get", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait", "wait",
                             "wait");
  }

  public void testVariableFromCommentCompletion() throws Throwable {
    doTest();
    // doTestCompletionVariants("user");
  }

  public void testVariableFromCommentWithScopeCompletion() throws Throwable {
    doTest();
  }

  public void testScopedVariableCompletion() throws Throwable {
    doTestCompletionVariants("request.user", "request.some", "reque");
  }

  public void testAttributeValuesCompletion() throws Throwable {
    doTestCompletionVariants("cf_sql_bigint", "cf_sql_bit", "cf_sql_char", "cf_sql_blob", "cf_sql_clob", "cf_sql_date", "cf_sql_decimal",
                             "cf_sql_double", "cf_sql_float", "cf_sql_idstamp", "cf_sql_integer", "cf_sql_longvarchar", "cf_sql_money",
                             "cf_sql_money4", "cf_sql_numeric", "cf_sql_real", "cf_sql_refcursor", "cf_sql_smallint", "cf_sql_time",
                             "cf_sql_timestamp", "cf_sql_tinyint", "cf_sql_varchar");
  }


  public void testAttributeValuesForCustomTagCompletion() throws Throwable {
    doTest();
  }

  public void testAttributeCfloopArrayCompletionInCf8() throws Throwable {
    CfmlProjectConfiguration.State currentState = new CfmlProjectConfiguration.State();
    try {
      currentState.setLanguageLevel(CfmlLanguage.CF8);
      CfmlProjectConfiguration.getInstance(getProject()).loadState(currentState);
      doTest();
    }
    finally {
      currentState.setLanguageLevel(CfmlLanguage.CF9);
      CfmlProjectConfiguration.getInstance(getProject()).loadState(currentState);
    }
  }

  public void testAttributeCompletionWhenTagUppercased() throws Throwable {
    doTest();
  }

  public void testTagFunctionArgumentsCompletion() throws Throwable {
    doTestCompletionVariants("arguments.some1", "arguments.some2");
  }

  public void testScriptComponentAttributesCompletion() throws Throwable {
    doTestCompletionVariants("extends", "embedded", "entityname");
  }

  public void testAutocompletePathToExpandPath() throws Throwable {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent2.cfc", "MyComponent1.cfc", "autocompletePathToExpandPath.test.cfml");
  }

  public void testAutocompletePathToExpandPathInScript() throws Throwable {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent2.cfc", "MyComponent1.cfc", "autocompletePathToExpandPathInScript.test.cfml");
  }

  public void testAutocompletePathToComponent() throws Throwable {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testAutocompletePathToComponentInPresentOfMapping() throws Throwable {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<String, String>();
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

  public void testAutocompletePathToIncludeTagInPresentOfMapping() throws Throwable {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<String, String>();
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

  public void testAutocompletePathToModuleTag() throws Throwable {
    addTwoComponents(myFixture);
    doTestCompletionContainsVariants("MyComponent1.cfc","MyComponent2.cfc");
  }

  public void testNoCompletionToIncludeTagInPresentOfMapping() throws Throwable {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<String, String>();
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

  public void testAutocompletePathToScriptIncludeInPresentOfMappingWithEnteredPath() throws Throwable {
    addOneComponentToDir(myFixture);

    Map<String, String> mappings = new HashMap<String, String>();
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

  public void testAutocompletePathToIncludeTagInPresentOfMappingWithFile() throws Throwable {
    addOneComponentToDir(myFixture);

    Map<String, String> mappings = new HashMap<String, String>();
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


  public void testAutocompletePathToScriptIncludeInPresentOfMapping() throws Throwable {
    addOneComponentTo(myFixture);

    Map<String, String> mappings = new HashMap<String, String>();
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

  public void testAutocompletePathToScriptIncludeInPresentOfMappingWithFile() throws Throwable {
    addOneComponentToDir(myFixture);

    Map<String, String> mappings = new HashMap<String, String>();
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

  public void testCompleteChainFunctionsCall() throws Throwable {
    addOneComponentToResolve(myFixture);
    doTestCompletionVariants("init", "foo");
  }

  public void testCompleteChainFunctionsCallAfterConstructorCall() throws Throwable {
    addOneComponentToResolve(myFixture);
    doTestCompletionVariants("init", "foo");
  }

  public void testCompleteSuperFunctionsInTagDefinedComponent() throws Throwable {
    addOneComponentTo(myFixture);
    doTestCompletionVariants("func1", "func2");
  }

  public void testCompleteSuperFunctionsInScriptDefinedComponent() throws Throwable {
    addOneComponentTo(myFixture);
    doTestCompletionVariants("this.func1", "this.func2");
  }

  public void testFunctionsNamesCompletionFromFollowingDeclarations() throws Throwable {
    doTestCompletionVariants("func1", "func2", "func3", "func4", "func5");
  }

  public void testCompletionArgumentsNames() throws Throwable {
    doTestCompletionVariants("argument1", "argument2", "argumentCollection");
  }

  public void testCompletionArgumentsNamesFromScriptFunction() throws Throwable {
    doTestCompletionVariants("argument1", "argument2", "argumentCollection");
  }

  public void testDoNotOfferDeclaredAttributes() throws Throwable {
    doTestCompletionVariants("hint", "default", "required", "type");
  }

  public void testArgumentCompletionAtCreateObject() throws Throwable {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice");
  }

  public void testArgumentCompletionAtCompleteCreateObject() throws Throwable {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice");
  }

  public void testTagArgumentCompletionAtCreateObject() throws Throwable {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice");
  }

  public void testComponentArgumentCompletionAtCreateObject() throws Throwable {
    addOneComponentTo(myFixture);
    doTest();
  }

  public void testPredefinedVariablesForTagCompletion() throws Throwable {
    doTestCompletionContainsVariants("recordcount", "columnlist", "currentrow");
  }

  public void testPredefinedVariablesCompletion() throws Throwable {
    doTestCompletionContainsVariants("server.coldfusion.productversion", "server.coldfusion.rootdir");
  }

  public void testInsertPackageName() throws Throwable {
    addJavaClassToSubPackage(myFixture);
    doTest();
  }

  public void testVariableFromLocalScope() throws Throwable {
    doTest();
  }

  public void testArgumentCollection() throws Throwable {
    doTest();
  }

  public void testArgumentCollection2() throws Throwable {
    doTestCompletionVariants("argumentCollection", "argumentCollectionFoo");
  }

  public void testTagPropertyCompletion() throws Throwable {
    doTestCompletionVariants("prop1", "prop2");
  }

  public void testScopedPropertyCompletion() throws Throwable {
    doTestCompletionVariants("variables.prop1", "variables.prop2");
  }


  public void testThisScopedVariableCompletion() throws Throwable {
    doTest();
  }


  public void testPropertyCompletion() throws Throwable {
    doTestCompletionVariants("prop1", "prop2");
  }

  public void testStandardFunctionArgumentName() throws Throwable {
    doTestCompletionVariants("number1", "number2", "numberFormat");
  }

  public void testStandardFunctionArgumentName2() throws Throwable {
    doTest();
  }

  public void testCfml183() throws Throwable {
    doTest();
  }

  public void testCompleteNewArguments() throws Throwable {
    addOneComponentWithConstructorTo(myFixture);
    doTestCompletionVariants("arg1", "arg2", "argumentCollection");
  }

  public void testCompleteNew() throws Throwable {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent1", "MyComponent2");
  }

  public void testCompleteStringedNew() throws Throwable {
    addTwoComponents(myFixture);
    doTestCompletionVariants("MyComponent1", "MyComponent2");
  }

  public void testOneVariantForVariables() throws Throwable {
    doTestCompletionVariants("variables.var1", "variables.var2");
  }

  public void testNoIntelliJIdeaRulezzSuggestion() throws Throwable {
    doTestCompletionVariants("fu1", "fu2");
  }

  public void testCompleteCfloopIndices() throws Throwable {
    doTestCompletionVariants("index1", "index2");
  }

  public void testGlobalVariablesCompletionInCfmlTagComponent() throws Throwable {
    doTest();
  }

  public void testGlobalVariablesCompletionInScriptComponent() throws Throwable {
    doTest("\n");
  }

  public void testScopedGlobalVariablesCompletionInCfmlTagComponent() throws Throwable {
    doTestCompletionContainsVariants("variables.injector");
  }

  private void doTestCompletionContainsVariants(String... items) {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    List<String> variants = myFixture.getCompletionVariants(inputDataFileName);
    assertTrue(variants.containsAll(Arrays.asList(items)));
  }

  private void doTestCompletionVariants(@NonNls String... items) throws Throwable {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    myFixture.testCompletionVariants(inputDataFileName, items);
  }

  private void doTest() throws Throwable {
    doTest("");
  }

  private void doTest(final String type) throws Throwable {
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

  protected PsiElement resolveReferenceAtCaret() throws Throwable {
    return myFixture.getReferenceAtCaretPositionWithAssertion(Util.getInputDataFileName(getTestName(true))).resolve();
  }
}
