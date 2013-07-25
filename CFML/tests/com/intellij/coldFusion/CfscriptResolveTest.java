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

import com.intellij.coldFusion.UI.config.CfmlMappingsConfig;
import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.impl.*;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public class CfscriptResolveTest extends JavaCodeInsightFixtureTestCase {
  static PsiClass addJavaClassTo(JavaCodeInsightTestFixture fixture) throws IOException {
     return fixture.addClass("import java.util.Collection;\n" +
                              "import java.util.Collections;\n" +
                              "import java.util.LinkedList;\n" +
                              "\n" +
                              "public class MyClass {\n" +
                              "  private Collection<String> myCollection = new LinkedList<String>();\n" +
                              "    \n" +
                              "  public void add(String s) {\n" +
                              "    myCollection.add(s);\n" +
                              "  }\n" +
                              "            \n" +
                              "  public Collection<String> get() {\n" +
                              "    return Collections.unmodifiableCollection(myCollection);\n" +
                              "    \n" +
                              "  }\n" +
                              "}");
  }

  static PsiFile addComponentsTo(JavaCodeInsightTestFixture fixture) throws IOException {
    fixture.addFileToProject("ComponentWithConstructor.cfc",
                                    "<cfcomponent>\n" +
                                      "<cffunction name=\"init\">\n" +
                                      "    <cfargument name=\"arg1\">\n" +
                                      "    <cfargument name=\"arg2\">\n" +
                                      "</cffunction>" +
                                    "</cfcomponent>");
    fixture.addFileToProject("MyComponentName.cfc",
                                    "<cfcomponent>\n" +
                                    "    <cffunction name=\"func\">\n" +
                                    "    </cffunction>\n" +
                                    "</cfcomponent>");
    fixture.addFileToProject("MyInterfaceName.cfc",
                                    "<cfinterface>\n" +
                                    "    <cffunction name=\"func\">\n" +
                                    "    </cffunction>\n" +
                                    "</cfinterface>");
    fixture.addFileToProject("MyComponentToResolve.cfc", "<cfcomponent>\n" +
                                                         "<cffunction name=\"init\">\n" +
                                                         "    <cfreturn this>\n" +
                                                         "</cffunction>\n" +
                                                         "    <cffunction name=\"foo\">\n" +
                                                         "</cffunction>\n" +
                                                         "</cfcomponent>");
    return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                    "<cfcomponent name = \"ComponentName\" extends=\"folder.subfolder.OtherComponentName\">\n" +
                                    "    <cfproperty name=\"MyProperty\" getter=\"true\">\n"+
                                    "    <cffunction name=\"func1\">\n" +
                                    "    </cffunction>\n" +
                                    "\n" +
                                    "    <cffunction name=\"func2\" returnType=\"MyComponentName\">\n" +
                                    "    </cffunction>\n" +
                                    "</cfcomponent>");

  }

  static PsiFile addScriptComponentsTo(JavaCodeInsightTestFixture fixture) throws IOException {
    return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                    "component {\n" +
                                    "    function func1(){}\n" +
                                    "\n" +
                                    "    function func2() {}\n" +
                                    "}");

  }

  protected PsiElement resolveReferenceAtCaret() throws Throwable {
    return myFixture.getReferenceAtCaretPositionWithAssertion(Util.getInputDataFileName(getTestName(true))).resolve();
  }

  private void setDefaultState() {
    Map<String, String> mappings = new HashMap<String, String>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/folder/subfolder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("myfolder/subfolder", directoryName);
      }
    }

    CfmlProjectConfiguration.getInstance(getProject()).loadState(new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings)));
  }

  private void restoreDefaultState() {
    CfmlProjectConfiguration.getInstance(getProject()).loadState(new CfmlProjectConfiguration.State());
  }

  private static void assertAssignmentVariable(PsiElement element) {
    final PsiElement referenceExpression = element.getParent();
    assertInstanceOf(referenceExpression, CfmlReferenceExpression.class);
    final PsiElement assignmentExpression = referenceExpression.getParent();
    assertInstanceOf(assignmentExpression, CfmlAssignmentExpression.class);
  }

  @Override
  protected void tuneFixture(final JavaModuleFixtureBuilder moduleBuilder) {
      moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

    public void testInvokeResolveToTagFromTag() throws Throwable {
        doTest("fName");
    }

    public void testInvokeResolveToScriptFromTag() throws Throwable {
        doTest("fName");
    }

    public void testInvokeResolveToTagFromScript() throws Throwable {
        doTest("fName");
    }

    public void testInvokeResolveToScriptFromScript() throws Throwable {
        doTest("fName");
    }

    public void testResolveImplicitConstructor() throws Throwable {
        addJavaClassTo(myFixture);
        doTest("MyClass");
    }

    private PsiElement doTest(String resolveName) throws Throwable {
      PsiElement element = resolveReferenceAtCaret();
      PsiNamedElement var = assertInstanceOf(element, PsiNamedElement.class);

      final String name = var.getName();
      assertNotNull(name);
      assertEquals(resolveName.toLowerCase(), name.toLowerCase());
      return element;
    }

  @Override
  protected String getTestDataPath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
    protected String getBasePath() {
        return "/resolve";
    }

  public void testJavaClassInComments() throws Throwable {
      addJavaClassTo(myFixture);
      assertEquals("MyClass", assertInstanceOf(resolveReferenceAtCaret(), PsiClass.class).getName());
  }

  public void testJavaClassInCreateObject() throws Throwable {
    addJavaClassTo(myFixture);
    assertEquals("MyClass", assertInstanceOf(resolveReferenceAtCaret(), PsiClass.class).getName());
  }

  public void testJavaMethodResolve() throws Throwable {
    addJavaClassTo(myFixture);
    final PsiElement element = resolveReferenceAtCaret();
    assertEquals("add", assertInstanceOf(element, PsiMethod.class).getName());
  }

  public void testJavaMethodResolveCreatedFromComments() throws Throwable {
    addJavaClassTo(myFixture);
    final PsiElement element = resolveReferenceAtCaret();
    assertEquals("add", assertInstanceOf(element, PsiMethod.class).getName());
  }

  public void testDeeperJavaMethodsCall() throws Throwable {
    addJavaClassTo(myFixture);
    final PsiElement element = resolveReferenceAtCaret();
    assertEquals("iterator", assertInstanceOf(element, PsiMethod.class).getName());
  }

  public void testEa39634() throws Throwable {
    addJavaClassTo(myFixture);
    assertNull(resolveReferenceAtCaret());
  }

  public void testScriptFunctionParametersResolev() throws Throwable {
    final PsiElement element = doTest("parameter");
    assertInstanceOf(element, CfmlFunctionParameterImpl.class);
  }

  public void testTagFunctionParametersResolev() throws Throwable {
    final PsiElement element = doTest("arg");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testScopedParameterResolve() throws Throwable {
    final PsiElement element = doTest("arg");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testScopedParameterResolve2() throws Throwable {
    final PsiElement element = doTest("arg");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testScopedNamesDistinguishing() throws Throwable {
    final PsiElement element = doTest("name");
    assertAssignmentVariable(element);
    assertInstanceOf(element, CfmlVariable.class);
  }

  public void testResolveScopedVariablesInScript() throws Throwable {
    final PsiElement element = doTest("atest4");
    assertAssignmentVariable(element);
    assertInstanceOf(element, CfmlVariable.class);
    assertInstanceOf(element.getParent(), CfmlReferenceExpression.class);
    assertTrue(CfmlUtil.isSearchedScope(((CfmlReferenceExpression)element.getParent()).getScope().getText()));
  }

  public void testResolveFromReturn() throws Throwable {
    final PsiElement element = doTest("param");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testResolveToDefinitionWithVar() throws Throwable {
    final PsiElement element = doTest("variable");
    assertInstanceOf(element, CfmlVariable.class);
    assertAssignmentVariable(element);
    assertEquals(element.getParent().getParent().getText(),
                 "var variable = 10");
  }

  public void testResolveToFirstOccurence() throws Throwable {
    final PsiElement element = doTest("variable");
    assertInstanceOf(element, CfmlVariable.class);
    assertAssignmentVariable(element);
    assertEquals(element.getParent().getParent().getLastChild().getNode().getElementType(),
                 CfmlElementTypes.INTEGER_LITERAL);
  }

  public void testResolveToScriptFunctionParameter() throws Throwable {
    final PsiElement element = doTest("var2");
    assertInstanceOf(element, CfmlFunctionParameterImpl.class);
  }

  public void testRecursiveScriptFunctionResolve() throws Throwable {
    final PsiElement element = doTest("myFunction");
    assertInstanceOf(element, CfmlFunctionImpl.class);
  }

  public void testResolveJavaLoaderToComment() throws Throwable {
    final PsiElement element = doTest("myLoader");
    assertInstanceOf(element, CfmlImplicitVariable.class);
    assertInstanceOf(element.getParent(), PsiComment.class);
  }

  public void testResolveClassFromJavaLoaderCreateMethod() throws Throwable {
    addJavaClassTo(myFixture);
    assertEquals("MyClass", assertInstanceOf(resolveReferenceAtCaret(), PsiClass.class).getName());
  }

  public void testResolveAssignment() throws Throwable {
    // EA-36015
    addJavaClassTo(myFixture);
    assertNull(resolveReferenceAtCaret());
  }

  public void testResolveToNewFunctionSyntax() throws Throwable {
    assertEquals("someFunction", assertInstanceOf(resolveReferenceAtCaret(), CfmlFunctionImpl.class).getName());
  }

  public void testResolveParameterToNewFunctionSyntax() throws Throwable {
    assertEquals("param", assertInstanceOf(resolveReferenceAtCaret(), CfmlFunctionParameterImpl.class).getName());
  }

  public void testResolveScopedVariableToComment() throws Throwable {
    assertInstanceOf(assertInstanceOf(resolveReferenceAtCaret(), CfmlImplicitVariable.class).getParent(), PsiComment.class);
  }

  public void testResolveToNearestAssignment() throws Throwable {
    PsiElement parent = assertInstanceOf(assertInstanceOf(resolveReferenceAtCaret(), CfmlVariable.class).getParent(), CfmlReferenceExpression.class).getParent();
    assertInstanceOf(parent, CfmlAssignmentExpression.class);
    assertEquals(parent.getText(), "obj = 1");
  }

  public void testResolveComponentByImportAndPrefix() throws Throwable {
    myFixture.configureByFiles("mydir/MyComponentTest.cfc");
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyComponentTest");
  }

  public void testResolveComponentInCreateObject() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveComponentInCreateObjectWithoutFirstParameter() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveMultipleFunctionCall() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "foo");
  }

  public void testResolveFunctionCallAfterConstructorCall() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "foo");
  }


  public void testResolveScriptComponentInCreateObjectWithoutFirstParameter() throws Throwable {
    addScriptComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveFunctionDefinedLaterFromScript() throws Throwable {
    final PsiElement element = doTest("func2");
    assertEquals(assertInstanceOf(element, CfmlFunction.class).getName(), "func2");
  }

  public void testResolveFunctionDefinedLaterFromInvokeTag() throws Throwable {
    final PsiElement element = doTest("func2");
    assertEquals(assertInstanceOf(element, CfmlFunction.class).getName(), "func2");
  }

  public void testResolutionArgumentsNamesFromTag() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument1");
  }

  public void testResolutionArgumentsNamesFromScriptFunction() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument2");
  }

  public void testResolutionArgumentsNamesWithReferenceFromTag() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument1");
  }

  public void testResolutionArgumentsNamesWithReferenceFromScriptFunction() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument2");
  }

  public void testResolveArgumentFromScript() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "arg");
  }

  public void testResolveToCfsilent() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlVariable.class).getName(), "variable");
  }

  public void testResolveSuperMethod() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func1");
  }

  public void testThisToComponent() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunctionImpl.class).getName(), "MyFunction");
  }

  public void testThisFunction() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "MyFunction");
  }

  public void testResolveToPropertyTag() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveToPropertyFromScopedVariable() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "myProperty");
  }

  public void testResolveToProperty() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "myProperty");
  }

  public void testResolveImplicitlyDefinedSetter() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveImplicitlyDefinedGetter() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveImplicitGetterToSuper() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveToForIn() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlForImpl.Variable.class).getName(), "item");
  }

  public void testResovlePropertyToNameAttribute() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "prop");
  }

  public void testResolveSimpleNew() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyComponentName");
  }

  public void testResolveCompoundNew() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveStringedNew() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyComponentName");
  }

  public void testResolveScriptImport() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveTagImport() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveNewWithImport() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveNewWithMappings() throws Throwable {
    addComponentsTo(myFixture);
    setDefaultState();
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
    restoreDefaultState();
  }

  public void testResolveNewWithImportWithMappings() throws Throwable {
    addComponentsTo(myFixture);
    setDefaultState();
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
    restoreDefaultState();
  }

  public void testResolveNewArgumentToConstructorArgumentIfPresent() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "arg1");
  }

  public void testResolveStringedNewArgumentToConstructorArgumentIfPresent() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "arg1");
  }

  public void testResolveMethodAfterAssignmentWithNew() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func");
  }

  public void testResolveToTrulyDefinitionRatherThanToAssignment() throws Throwable {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "var1");
  }

  public void testResolveToInterface() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyInterfaceName");
  }

  public void testResolveForVariable() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlTagLoopImpl.Variable.class).getName(), "index1");
  }

  public void testResolveFunctionFromCreateObject() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func2");
  }

  public void testResolveFunctionFromFunctionReturnType() throws Throwable {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func");
  }

  public void testMethodResolveInComponentAfterSuper() throws Throwable {
    addComponentsTo(myFixture);
    PsiElement element = resolveReferenceAtCaret();
    assertInstanceOf(element, CfmlFunction.class);
    assertEquals("ComponentName", assertInstanceOf(element.getParent(), CfmlComponent.class).getName());
  }


}
