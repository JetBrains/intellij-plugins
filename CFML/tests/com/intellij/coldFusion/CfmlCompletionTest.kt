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
package com.intellij.coldFusion

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.coldFusion.UI.config.CfmlMappingsConfig
import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration
import com.intellij.coldFusion.model.CfmlLanguage
import com.intellij.javaee.ExternalResourceManagerEx
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.intellij.xml.util.XmlUtil
import junit.framework.TestCase
import org.jetbrains.annotations.NonNls
import java.util.*

class CfmlCompletionTest : JavaCodeInsightFixtureTestCase() {

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    CamelHumpMatcher.forceStartMatching(myFixture.testRootDisposable)
  }

  fun testAttributeCompletionAtFileEnd() {
    doTest()
  }

  fun testAttributeCompletionAtFileMiddle() {
    doTest()
  }

  fun testAttributeVariants() {
    doTestCompletionVariants("returntype", "returnformat", "roles")
  }

  fun testAccessorsCompletion() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent accessors=\"true\">\n" +
                                                             "\t<cfproperty name=\"Foo\" type=\"STRING\">\n" +
                                                             "</cfcomponent>")
    doTestCompletionVariants("getFoo()", "setFoo()", "Foo")
  }

  fun testPropertyGetterCompletion() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent>\n" +
                                                             "\t<cfproperty name=\"Foo\" getter=\"true\" type=\"STRING\">\n" +
                                                             "</cfcomponent>")
    doTestCompletionVariants("getFoo()", "Foo")
  }

  fun testPropertySetterCompletion() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent>\n" +
                                                             "\t<cfproperty name=\"Foo\" setter=\"true\" type=\"STRING\">\n" +
                                                             "</cfcomponent>")
    doTestCompletionVariants("setFoo()", "Foo")
  }

  fun testPropertyAccessorCompletionForPersistentComponent() {
    myFixture.addFileToProject("MyPropertyTagComponent.cfc", "<cfcomponent persistent=\"true\">\n" +
                                                             "\t<cfproperty name=\"foo\" type=\"STRING\">\n" +
                                                             "</cfcomponent>")
    doTestCompletionVariants("setFoo()", "getFoo()", "foo")
  }


  fun testMappedSuperClassCompletionInComponentTag() {
    doTest()
  }

  fun testMappedSuperClassCompletionInScriptComponent() {
    doTest()
  }

  fun testAttributeVariantsInPropertyTag() {
    doTest()
  }

  fun testAttributeVariantsInStartProperty() {
    doTestCompletionContainsVariants("name", "notnull")
  }

  fun testAttributeOrmTypeInProperty() {
    doTest()
  }

  fun testReturnTypeCompletion() {
    addOneComponentTo(myFixture)
    doTestCompletionContainsVariants("func1", "func2")
  }

  fun testReturnTypeCompletionInScriptFunctionDefinition() {
    doTestCompletionContainsVariants("String", "void", "Numeric")
  }

  fun testTagCompletionAtFileEnd() {
    doTestCompletionVariants("cffunction", "cffeed", "cffileupload", "cffinally", "cffile", "cfflush", "cfform", "cfformgroup",
                             "cfformitem", "cfftp")
  }

  fun testTagCompletionAfterAngleBracket() {
    val manager = ExternalResourceManagerEx.getInstanceEx()
    val doctype = manager.getDefaultHtmlDoctype(project)
    manager.setDefaultHtmlDoctype(XmlUtil.XHTML_URI, project)
    try {
      doTestCompletionContainsVariants("cffunction", "center", "cfset", "h1")
    }
    finally {
      manager.setDefaultHtmlDoctype(doctype, project)
    }
  }

  fun testTagCompletionAfterC() {
    val manager = ExternalResourceManagerEx.getInstanceEx()
    val doctype = manager.getDefaultHtmlDoctype(project)
    manager.setDefaultHtmlDoctype(XmlUtil.XHTML_URI, project)
    try {
      doTestCompletionContainsVariants("cffunction", "center", "cfset")
    }
    finally {
      manager.setDefaultHtmlDoctype(doctype, project)
    }
  }

  fun testTagCompletionAfterCf() {
    doTestCompletionContainsVariants("cffunction", "cfset")
  }

  fun testPropertyWordCompletionInScriptComponent() {
    doTest()
  }

  fun testSecondPropertyWordCompletionInScriptComponent() {
    doTestCompletionContainsVariants("property")
  }

  fun testNoPropertyWordCompletionInScriptComponent() {
    doTest()
  }

  fun testFunctionNamesCompletion() {
    doTestCompletionVariants("mid", "min", "mineFunc", "minute")
  }

  fun testVariableNamesCompletion() {
    doTestCompletionVariants("mid", "min", "mineVariable", "minute")
  }

  fun testVariableAndFunctionNamesCompletion() {
    doTestCompletionVariants("mineFunction", "mineVariable", "mid", "min", "minute")
  }

  fun testFunctionNameWithBracketsCompletion() {
    doTest()
  }

  fun testClassInstanceCompletion() {
    addJavaClassTo(myFixture)
    doTestCompletionVariants("VERSION", "init", "init")
  }

  fun testClassInstanceCompletionFromCreator() {
    addJavaClassTo(myFixture)
    doTestCompletionVariants("VERSION", "init", "init")
  }

  fun testClassInstanceCompletionAfterInit() {
    addJavaClassTo(myFixture)
    doTestCompletionVariants("VERSION", "add", "equals", "get", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait", "wait",
                             "wait")
  }

  fun testClassInstanceCompletionAfterInitFromCreator() {
    addJavaClassTo(myFixture)
    doTestCompletionVariants("VERSION", "add", "equals", "get", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait", "wait",
                             "wait")
  }

  fun testVariableFromCommentCompletion() {
    doTest()
    // doTestCompletionVariants("user");
  }

  fun testVariableFromCommentWithScopeCompletion() {
    doTest()
  }

  fun testScopedVariableCompletion() {
    doTestCompletionVariants("removeCachedQuery", "request.user", "request.some", "reque")
  }

  fun testAttributeValuesCompletion() {
    doTestCompletionVariants("cf_sql_bigint", "cf_sql_bit", "cf_sql_char", "cf_sql_blob", "cf_sql_clob", "cf_sql_date", "cf_sql_decimal",
                             "cf_sql_double", "cf_sql_float", "cf_sql_idstamp", "cf_sql_integer", "cf_sql_longvarchar", "cf_sql_money",
                             "cf_sql_money4", "cf_sql_numeric", "cf_sql_real", "cf_sql_refcursor", "cf_sql_smallint", "cf_sql_time",
                             "cf_sql_timestamp", "cf_sql_tinyint", "cf_sql_varchar")
  }


  fun testAttributeValuesForCustomTagCompletion() {
    doTest()
  }

  @Throws(Throwable::class)
  fun testAttributeCfloopArrayCompletionInCf8() {
    Util.runTestWithLanguageLevel({
                                    doTest()
                                    null
                                  }, CfmlLanguage.CF8, project)
  }

  fun testAttributeCompletionWhenTagUppercased() {
    doTest()
  }

  fun testTagFunctionArgumentsCompletion() {
    doTestCompletionVariants("arguments.some1", "arguments.some2")
  }

  fun testScriptComponentAttributesCompletion() {
    doTestCompletionVariants("extends", "embedded", "entityname")
  }

  fun testAutocompletePathToExpandPath() {
    addTwoComponents(myFixture)
    doTestCompletionVariants("MyComponent2.cfc", "MyComponent1.cfc", "autocompletePathToExpandPath.test.cfml")
  }

  fun testAutocompletePathToExpandPathInScript() {
    addTwoComponents(myFixture)
    doTestCompletionVariants("MyComponent2.cfc", "MyComponent1.cfc", "autocompletePathToExpandPathInScript.test.cfml")
  }

  fun testAutocompletePathToComponent() {
    addOneComponentTo(myFixture)
    doTest()
  }

  fun testAutocompletePathToComponentInPresentOfMapping() {
    addOneComponentTo(myFixture)

    val mappings = HashMap<String, String>()
    for (root in ProjectRootManager.getInstance(project).contentRoots) {
      val directoryName = root.presentableUrl + "/folder/subfolder"
      val fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName)
      if (fileByUrl != null) {
        mappings["/myfolder/subfolder"] = directoryName
      }
    }

    val defaultState = CfmlProjectConfiguration.getInstance(project).state
    val state = CfmlProjectConfiguration.State(CfmlMappingsConfig(mappings))
    try {
      CfmlProjectConfiguration.getInstance(project).loadState(state)
      doTest()
    }
    finally {
      CfmlProjectConfiguration.getInstance(project).loadState(defaultState!!)
    }
  }

  fun testAutocompletePathToIncludeTagInPresentOfMapping() {
    addOneComponentTo(myFixture)

    val mappings = HashMap<String, String>()
    for (root in ProjectRootManager.getInstance(project).contentRoots) {
      val directoryName = root.presentableUrl + "/folder/subfolder"
      val fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName)
      if (fileByUrl != null) {
        mappings["/myfolder/subfolder"] = directoryName
      }
    }

    val defaultState = CfmlProjectConfiguration.getInstance(project).state
    val state = CfmlProjectConfiguration.State(CfmlMappingsConfig(mappings))
    try {
      CfmlProjectConfiguration.getInstance(project).loadState(state)
      doTest()
    }
    finally {
      CfmlProjectConfiguration.getInstance(project).loadState(defaultState!!)
    }
  }

  fun testAutocompletePathToModuleTag() {
    addTwoComponents(myFixture)
    doTestCompletionContainsVariants("MyComponent1.cfc", "MyComponent2.cfc")
  }

  fun testNoCompletionToIncludeTagInPresentOfMapping() {
    addOneComponentTo(myFixture)

    val mappings = HashMap<String, String>()
    for (root in ProjectRootManager.getInstance(project).contentRoots) {
      val directoryName = root.presentableUrl + "/folder"
      val fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName)
      if (fileByUrl != null) {
        mappings["myfolder"] = directoryName
      }
    }

    val defaultState = CfmlProjectConfiguration.getInstance(project).state
    val state = CfmlProjectConfiguration.State(CfmlMappingsConfig())
    state.mapps = CfmlMappingsConfig(mappings)
    try {
      CfmlProjectConfiguration.getInstance(project).loadState(state)
      doTest()
    }
    finally {
      CfmlProjectConfiguration.getInstance(project).loadState(defaultState!!)
    }
  }

  fun testAutocompletePathToScriptIncludeInPresentOfMappingWithEnteredPath() {
    addOneComponentToDir(myFixture)

    val mappings = HashMap<String, String>()
    for (root in ProjectRootManager.getInstance(project).contentRoots) {
      val directoryName = root.presentableUrl + "/subfolder"
      val fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName)
      if (fileByUrl != null) {
        mappings["/lot"] = directoryName
        mappings["/fot"] = directoryName
      }
    }

    val defaultState = CfmlProjectConfiguration.getInstance(project).state
    val state = CfmlProjectConfiguration.State(CfmlMappingsConfig())
    state.mapps = CfmlMappingsConfig(mappings)
    try {
      CfmlProjectConfiguration.getInstance(project).loadState(state)
      doTestCompletionContainsVariants("lot", "fot")
    }
    finally {
      CfmlProjectConfiguration.getInstance(project).loadState(defaultState!!)
    }
  }

  fun testAutocompletePathToIncludeTagInPresentOfMappingWithFile() {
    addOneComponentToDir(myFixture)

    val mappings = HashMap<String, String>()
    for (root in ProjectRootManager.getInstance(project).contentRoots) {
      val directoryName = root.presentableUrl + "/subfolder"
      val fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName)
      if (fileByUrl != null) {
        mappings["/abc"] = directoryName
      }
    }

    val defaultState = CfmlProjectConfiguration.getInstance(project).state
    val state = CfmlProjectConfiguration.State(CfmlMappingsConfig(mappings))
    try {
      CfmlProjectConfiguration.getInstance(project).loadState(state)
      doTestCompletionContainsVariants("ComponentName.cfc")
    }
    finally {
      CfmlProjectConfiguration.getInstance(project).loadState(defaultState!!)
    }
  }


  fun testAutocompletePathToScriptIncludeInPresentOfMapping() {
    addOneComponentTo(myFixture)

    val mappings = HashMap<String, String>()
    for (root in ProjectRootManager.getInstance(project).contentRoots) {
      val directoryName = root.presentableUrl + "/folder/subfolder"
      val fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName)
      if (fileByUrl != null) {
        mappings["/myfolder/subfolder"] = directoryName
      }
    }

    val defaultState = CfmlProjectConfiguration.getInstance(project).state
    val state = CfmlProjectConfiguration.State(CfmlMappingsConfig(mappings))
    try {
      CfmlProjectConfiguration.getInstance(project).loadState(state)
      doTest()
    }
    finally {
      CfmlProjectConfiguration.getInstance(project).loadState(defaultState!!)
    }
  }

  fun testAutocompletePathToScriptIncludeInPresentOfMappingWithFile() {
    addOneComponentToDir(myFixture)

    val mappings = HashMap<String, String>()
    for (root in ProjectRootManager.getInstance(project).contentRoots) {
      val directoryName = root.presentableUrl + "/subfolder"
      val fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName)
      if (fileByUrl != null) {
        mappings["/subfolder"] = directoryName
      }
    }

    val defaultState = CfmlProjectConfiguration.getInstance(project).state
    val state = CfmlProjectConfiguration.State(CfmlMappingsConfig(mappings))
    try {
      CfmlProjectConfiguration.getInstance(project).loadState(state)
      doTestCompletionVariants("ComponentName.cfc")
    }
    finally {
      CfmlProjectConfiguration.getInstance(project).loadState(defaultState!!)
    }
  }

  fun testCompleteChainFunctionsCall() {
    addOneComponentToResolve(myFixture)
    doTestCompletionVariants("init", "foo")
  }

  fun testCompleteChainFunctionsCallAfterConstructorCall() {
    addOneComponentToResolve(myFixture)
    doTestCompletionVariants("init", "foo")
  }

  fun testCompleteSuperFunctionsInTagDefinedComponent() {
    addOneComponentTo(myFixture)
    doTestCompletionVariants("func1", "func2")
  }

  fun testCompleteSuperFunctionsInScriptDefinedComponent() {
    addOneComponentTo(myFixture)
    doTestCompletionVariants("this.func1", "this.func2")
  }

  fun testFunctionsNamesCompletionFromFollowingDeclarations() {
    doTestCompletionVariants("func1", "func2", "func3", "func4", "func5")
  }

  fun testCompletionArgumentsNames() {
    doTestCompletionVariants("argument1", "argument2", "argumentCollection")
  }

  fun testCompletionArgumentsNamesFromScriptFunction() {
    doTestCompletionVariants("argument1", "argument2", "argumentCollection")
  }

  fun testDoNotOfferDeclaredAttributes() {
    doTestCompletionVariants("hint", "default", "required", "type")
  }

  fun testCompleteAfterElvis() {
    doTest()
  }

  fun testArgumentCompletionAtCreateObject() {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice")
  }

  fun testArgumentCompletionAtCompleteCreateObject() {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice")
  }

  fun testTagArgumentCompletionAtCreateObject() {
    doTestCompletionContainsVariants("com", "component", "java", "corba", "webservice")
  }

  fun testComponentArgumentCompletionAtCreateObject() {
    addOneComponentTo(myFixture)
    doTest()
  }

  fun testComponentArgumentCompletionAtArgument() {
    addOneComponentTo(myFixture)
    doTest()
  }

  fun testComponentArgumentCompletionAtReturnType() {
    addOneComponentTo(myFixture)
    doTest()
  }

  fun testNoFunctionCompletionAtReturnType() {
    doTest()
  }

  fun testComponentArgumentCompletionAtReturnTypeInScript() {
    addOneComponentTo(myFixture)
    doTest()
  }

  fun testComponentArgumentCompletionAtArgumentInScript() {
    addOneComponentTo(myFixture)
    doTest()
  }

  fun testPredefinedVariablesForTagCompletion() {
    doTestCompletionContainsVariants("recordcount", "columnlist", "currentrow")
  }

  fun testPredefinedVariablesCompletion() {
    doTestCompletionContainsVariants("server.coldfusion.productversion", "server.coldfusion.rootdir")
  }

  fun testInsertPackageName() {
    addJavaClassToSubPackage(myFixture)
    doTest()
  }

  fun testVariableFromLocalScope() {
    doTest()
  }

  fun testArgumentCollection() {
    doTest()
  }

  fun testArgumentCollection2() {
    doTestCompletionVariants("argumentCollection", "argumentCollectionFoo")
  }

  fun testTagPropertyCompletion() {
    doTestCompletionVariants("prop1", "prop2")
  }

  fun testScopedPropertyCompletion() {
    doTestCompletionVariants("variables.prop1", "variables.prop2")
  }


  fun testThisScopedVariableCompletion() {
    doTest()
  }


  fun testPropertyCompletion() {
    doTestCompletionVariants("prop1", "prop2")
  }

  fun testStandardFunctionArgumentName() {
    doTestCompletionVariants("number1", "number2", "numberFormat")
  }

  fun testStandardFunctionArgumentName2() {
    doTest()
  }

  fun testCfml183() {
    doTest()
  }

  fun testCompleteNewArguments() {
    addOneComponentWithConstructorTo(myFixture)
    doTestCompletionVariants("arg1", "arg2", "argumentCollection")
  }

  fun testCompleteNew() {
    addTwoComponents(myFixture)
    doTestCompletionVariants("MyComponent1", "MyComponent2")
  }

  fun testCompleteStringedNew() {
    addTwoComponents(myFixture)
    doTestCompletionVariants("MyComponent1", "MyComponent2")
  }

  fun testOneVariantForVariables() {
    doTestCompletionVariants("variables.var1", "variables.var2")
  }

  fun testNoIntelliJIdeaRulezzSuggestion() {
    doTestCompletionVariants("fu1", "fu2")
  }

  fun testCompleteCfloopIndices() {
    doTestCompletionVariants("index1", "index2")
  }

  fun testGlobalVariablesCompletionInCfmlTagComponent() {
    doTest()
  }

  fun testGlobalVariablesCompletionInScriptComponent() {
    doTest("\n")
  }

  fun testScopedGlobalVariablesCompletionInCfmlTagComponent() {
    doTestCompletionContainsVariants("variables.injector")
  }

  private fun doTestCompletionContainsVariants(vararg items: String) {
    val inputDataFileName = Util.getInputDataFileName(getTestName(true))
    val variants = myFixture.getCompletionVariants(inputDataFileName)
    TestCase.assertTrue(variants!!.containsAll(Arrays.asList(*items)))
  }

  private fun doTestCompletionVariants(@NonNls vararg items: String) {
    val inputDataFileName = Util.getInputDataFileName(getTestName(true))
    myFixture.testCompletionVariants(inputDataFileName, *items)
  }

  private fun doTest(type: String = "") {
    val inputDataFileName = Util.getInputDataFileName(getTestName(true))
    val expectedResultFileName = Util.getExpectedDataFileName(getTestName(true))
    val input = arrayOf(inputDataFileName)
    myFixture.testCompletionTyping(input, type, expectedResultFileName)
  }

  override fun getTestDataPath(): String {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + basePath
  }

  override fun getBasePath(): String {
    return "/completion"
  }

  protected fun resolveReferenceAtCaret(): PsiElement? {
    return myFixture.getReferenceAtCaretPositionWithAssertion(Util.getInputDataFileName(getTestName(true))).resolve()
  }

  companion object {

    internal fun addJavaClassToSubPackage(fixture: JavaCodeInsightTestFixture) {
      fixture.addFileToProject("subPackage/MyClass.java", "package subPackage;\n" +
                                                          "\n" +
                                                          "public class MyClass {\n" +
                                                          "}")
    }

    internal fun addJavaClassTo(fixture: JavaCodeInsightTestFixture): PsiClass {
      return fixture.addClass("""import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class MyClass {
  private Collection<String> myCollection = new LinkedList<String>();
  public static final int VERSION = 1;
  public MyClass() {}
  public void add(String s) {
    myCollection.add(s);
  }
  public Collection<String> get() {
    return Collections.unmodifiableCollection(myCollection);
                                }
  public static foo() {}
}""")
    }

    internal fun addOneComponentWithConstructorTo(fixture: JavaCodeInsightTestFixture): PsiFile {
      return fixture.addFileToProject("ComponentWithConstructor.cfc",
                                      "<cfcomponent>\n" +
                                      "<cffunction name=\"init\">\n" +
                                      "    <cfargument name=\"arg1\">\n" +
                                      "    <cfargument name=\"arg2\">\n" +
                                      "</cffunction>" +
                                      "</cfcomponent>")
    }

    internal fun addOneComponentToResolve(fixture: JavaCodeInsightTestFixture): PsiFile {
      return fixture.addFileToProject("MyComponentToResolve.cfc", "<cfcomponent>\n" +
                                                                  "<cffunction name=\"init\">\n" +
                                                                  "    <cfreturn this>\n" +
                                                                  "</cffunction>\n" +
                                                                  "    <cffunction name=\"foo\">\n" +
                                                                  "</cffunction>\n" +
                                                                  "</cfcomponent>")
    }

    internal fun addTwoComponents(fixture: JavaCodeInsightTestFixture): PsiFile {
      fixture.addFileToProject("MyComponent2.cfc",
                               "<cfcomponent>\n" + "</cfcomponent>")

      return fixture.addFileToProject("MyComponent1.cfc",
                                      "<cfcomponent>\n" + "</cfcomponent>")
    }

    internal fun addOneComponentTo(fixture: JavaCodeInsightTestFixture): PsiFile {
      return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                      "<cfcomponent>\n" +
                                      "    <cffunction name=\"func1\">\n" +
                                      "    </cffunction>\n" +
                                      "\n" +
                                      "    <cffunction name=\"func2\">\n" +
                                      "    </cffunction>\n" +
                                      "</cfcomponent>")

    }

    internal fun addOneComponentToDir(fixture: JavaCodeInsightTestFixture): PsiFile {
      return fixture.addFileToProject("subfolder/ComponentName.cfc",
                                      "<cfcomponent>\n" +
                                      "    <cffunction name=\"func1\">\n" +
                                      "    </cffunction>\n" +
                                      "\n" +
                                      "    <cffunction name=\"func2\">\n" +
                                      "    </cffunction>\n" +
                                      "</cfcomponent>")
    }
  }
}
