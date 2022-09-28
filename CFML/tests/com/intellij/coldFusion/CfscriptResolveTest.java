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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lera Nikolaenko
 */
public class CfscriptResolveTest extends JavaCodeInsightFixtureTestCase {
  static PsiClass addJavaClassTo(JavaCodeInsightTestFixture fixture) {
     return fixture.addClass("""
                               import java.util.Collection;
                               import java.util.Collections;
                               import java.util.LinkedList;

                               public class MyClass {
                                 private Collection<String> myCollection = new LinkedList<String>();
                                  \s
                                 public void add(String s) {
                                   myCollection.add(s);
                                 }
                                          \s
                                 public Collection<String> get() {
                                   return Collections.unmodifiableCollection(myCollection);
                                  \s
                                 }
                               }""");
  }

  static PsiFile addComponentsTo(JavaCodeInsightTestFixture fixture) {
    fixture.addFileToProject("ComponentWithConstructor.cfc",
                             """
                               <cfcomponent>
                               <cffunction name="init">
                                   <cfargument name="arg1">
                                   <cfargument name="arg2">
                               </cffunction></cfcomponent>""");
    fixture.addFileToProject("MyComponentName.cfc",
                             """
                               <cfcomponent>
                                   <cffunction name="func">
                                   </cffunction>
                               </cfcomponent>""");
    fixture.addFileToProject("MyInterfaceName.cfc",
                             """
                               <cfinterface>
                                   <cffunction name="func">
                                   </cffunction>
                               </cfinterface>""");
    fixture.addFileToProject("MyComponentToResolve.cfc", """
      <cfcomponent>
      <cffunction name="init">
          <cfreturn this>
      </cffunction>
          <cffunction name="foo">
      </cffunction>
      </cfcomponent>""");
    return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                    """
                                      <cfcomponent name = "ComponentName" extends="folder.subfolder.OtherComponentName">
                                          <cfproperty name="MyProperty" getter="true">
                                          <cffunction name="func1">
                                          </cffunction>

                                          <cffunction name="func2" returnType="MyComponentName">
                                          </cffunction>
                                      </cfcomponent>""");

  }

  static PsiFile addScriptComponentsTo(JavaCodeInsightTestFixture fixture) {
    return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                    """
                                      component {
                                          function func1(){}

                                          function func2() {}
                                      }""");

  }

  protected PsiElement resolveReferenceAtCaret() {
    return myFixture.getReferenceAtCaretPositionWithAssertion(Util.getInputDataFileName(getTestName(true))).resolve();
  }

  private void setDefaultState() {
    Map<String, String> mappings = new HashMap<>();
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

    public void testInvokeResolveToTagFromTag() {
        doTest("fName");
    }

    public void testInvokeResolveToScriptFromTag() {
        doTest("fName");
    }

    public void testInvokeResolveToTagFromScript() {
        doTest("fName");
    }

    public void testInvokeResolveToScriptFromScript() {
        doTest("fName");
    }

    public void testResolveImplicitConstructor() {
        addJavaClassTo(myFixture);
        doTest("MyClass");
    }

    private PsiElement doTest(String resolveName) {
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

  public void testJavaClassInComments() {
      addJavaClassTo(myFixture);
      assertEquals("MyClass", assertInstanceOf(resolveReferenceAtCaret(), PsiClass.class).getName());
  }

  public void testJavaClassInCreateObject() {
    addJavaClassTo(myFixture);
    assertEquals("MyClass", assertInstanceOf(resolveReferenceAtCaret(), PsiClass.class).getName());
  }

  public void testJavaMethodResolve() {
    addJavaClassTo(myFixture);
    final PsiElement element = resolveReferenceAtCaret();
    assertEquals("add", assertInstanceOf(element, PsiMethod.class).getName());
  }

  public void testJavaMethodResolveCreatedFromComments() {
    addJavaClassTo(myFixture);
    final PsiElement element = resolveReferenceAtCaret();
    assertEquals("add", assertInstanceOf(element, PsiMethod.class).getName());
  }

  public void testDeeperJavaMethodsCall() {
    addJavaClassTo(myFixture);
    final PsiElement element = resolveReferenceAtCaret();
    assertEquals("iterator", assertInstanceOf(element, PsiMethod.class).getName());
  }

  public void testEa39634() {
    addJavaClassTo(myFixture);
    assertNull(resolveReferenceAtCaret());
  }

  public void testScriptFunctionParametersResolev() {
    final PsiElement element = doTest("parameter");
    assertInstanceOf(element, CfmlFunctionParameterImpl.class);
  }

  public void testTagFunctionParametersResolev() {
    final PsiElement element = doTest("arg");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testScopedParameterResolve() {
    final PsiElement element = doTest("arg");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testScopedParameterResolve2() {
    final PsiElement element = doTest("arg");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testScopedNamesDistinguishing() {
    final PsiElement element = doTest("name");
    assertAssignmentVariable(element);
    assertInstanceOf(element, CfmlVariable.class);
  }

  public void testResolveScopedVariablesInScript() {
    final PsiElement element = doTest("atest4");
    assertAssignmentVariable(element);
    assertInstanceOf(element, CfmlVariable.class);
    assertInstanceOf(element.getParent(), CfmlReferenceExpression.class);
    assertTrue(CfmlUtil.isSearchedScope(((CfmlReferenceExpression)element.getParent()).getScope().getText()));
  }

  public void testResolveFromReturn() {
    final PsiElement element = doTest("param");
    assertInstanceOf(element, CfmlTagFunctionParameterImpl.class);
  }

  public void testResolveToDefinitionWithVar() {
    final PsiElement element = doTest("variable");
    assertInstanceOf(element, CfmlVariable.class);
    assertAssignmentVariable(element);
    assertEquals(element.getParent().getParent().getText(),
                 "var variable = 10");
  }

  public void testResolveToFirstOccurence() {
    final PsiElement element = doTest("variable");
    assertInstanceOf(element, CfmlVariable.class);
    assertAssignmentVariable(element);
    assertEquals(element.getParent().getParent().getLastChild().getNode().getElementType(),
                 CfmlElementTypes.INTEGER_LITERAL);
  }

  public void testResolveToScriptFunctionParameter() {
    final PsiElement element = doTest("var2");
    assertInstanceOf(element, CfmlFunctionParameterImpl.class);
  }

  public void testRecursiveScriptFunctionResolve() {
    final PsiElement element = doTest("myFunction");
    assertInstanceOf(element, CfmlFunctionImpl.class);
  }

  public void testResolveJavaLoaderToComment() {
    final PsiElement element = doTest("myLoader");
    assertInstanceOf(element, CfmlImplicitVariable.class);
    assertInstanceOf(element.getParent(), PsiComment.class);
  }

  public void testResolveClassFromJavaLoaderCreateMethod() {
    addJavaClassTo(myFixture);
    assertEquals("MyClass", assertInstanceOf(resolveReferenceAtCaret(), PsiClass.class).getName());
  }

  public void testResolveAssignment() {
    // EA-36015
    addJavaClassTo(myFixture);
    assertNull(resolveReferenceAtCaret());
  }

  public void testResolveToNewFunctionSyntax() {
    assertEquals("someFunction", assertInstanceOf(resolveReferenceAtCaret(), CfmlFunctionImpl.class).getName());
  }

  public void testResolveParameterToNewFunctionSyntax() {
    assertEquals("param", assertInstanceOf(resolveReferenceAtCaret(), CfmlFunctionParameterImpl.class).getName());
  }

  public void testResolveScopedVariableToComment() {
    assertInstanceOf(assertInstanceOf(resolveReferenceAtCaret(), CfmlImplicitVariable.class).getParent(), PsiComment.class);
  }

  public void testResolveToNearestAssignment() {
    PsiElement parent = assertInstanceOf(assertInstanceOf(resolveReferenceAtCaret(), CfmlVariable.class).getParent(), CfmlReferenceExpression.class).getParent();
    assertInstanceOf(parent, CfmlAssignmentExpression.class);
    assertEquals(parent.getText(), "obj = 1");
  }

  public void testResolveComponentByImportAndPrefix() {
    myFixture.configureByFiles("mydir/MyComponentTest.cfc");
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyComponentTest");
  }

  public void testResolveComponentInCreateObject() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveComponentInCreateObjectWithoutFirstParameter() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveMultipleFunctionCall() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "foo");
  }

  public void testResolveFunctionCallAfterConstructorCall() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "foo");
  }


  public void testResolveScriptComponentInCreateObjectWithoutFirstParameter() {
    addScriptComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveFunctionDefinedLaterFromScript() {
    final PsiElement element = doTest("func2");
    assertEquals(assertInstanceOf(element, CfmlFunction.class).getName(), "func2");
  }

  public void testResolveFunctionDefinedLaterFromInvokeTag() {
    final PsiElement element = doTest("func2");
    assertEquals(assertInstanceOf(element, CfmlFunction.class).getName(), "func2");
  }

  public void testResolutionArgumentsNamesFromTag() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument1");
  }

  public void testResolutionArgumentsNamesFromScriptFunction() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument2");
  }

  public void testResolutionArgumentsNamesWithReferenceFromTag() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument1");
  }

  public void testResolutionArgumentsNamesWithReferenceFromScriptFunction() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "argument2");
  }

  public void testResolveArgumentFromScript() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "arg");
  }

  public void testResolveToCfsilent() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlVariable.class).getName(), "variable");
  }

  public void testResolveSuperMethod() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func1");
  }

  public void testThisToComponent() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunctionImpl.class).getName(), "MyFunction");
  }

  public void testThisFunction() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "MyFunction");
  }

  public void testResolveToPropertyTag() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveToPropertyFromScopedVariable() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "myProperty");
  }

  public void testResolveToProperty() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "myProperty");
  }

  public void testResolveImplicitlyDefinedSetter() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveImplicitlyDefinedGetter() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveImplicitGetterToSuper() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "MyProperty");
  }

  public void testResolveToForIn() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlForImpl.Variable.class).getName(), "item");
  }

  public void testResovlePropertyToNameAttribute() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "prop");
  }

  public void testResolveSimpleNew() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyComponentName");
  }

  public void testResolveCompoundNew() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveStringedNew() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyComponentName");
  }

  public void testResolveScriptImport() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveTagImport() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveNewWithImport() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
  }

  public void testResolveNewWithMappings() {
    addComponentsTo(myFixture);
    setDefaultState();
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
    restoreDefaultState();
  }

  public void testResolveNewWithImportWithMappings() {
    addComponentsTo(myFixture);
    setDefaultState();
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "ComponentName");
    restoreDefaultState();
  }

  public void testResolveNewArgumentToConstructorArgumentIfPresent() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "arg1");
  }

  public void testResolveStringedNewArgumentToConstructorArgumentIfPresent() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlParameter.class).getName(), "arg1");
  }

  public void testResolveMethodAfterAssignmentWithNew() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func");
  }

  public void testResolveToTrulyDefinitionRatherThanToAssignment() {
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlProperty.class).getName(), "var1");
  }

  public void testResolveToInterface() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlComponent.class).getName(), "MyInterfaceName");
  }

  public void testResolveForVariable() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlTagLoopImpl.Variable.class).getName(), "index1");
  }

  public void testResolveFunctionFromCreateObject() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func2");
  }

  public void testResolveFunctionFromFunctionReturnType() {
    addComponentsTo(myFixture);
    assertEquals(assertInstanceOf(resolveReferenceAtCaret(), CfmlFunction.class).getName(), "func");
  }

  public void testMethodResolveInComponentAfterSuper() {
    addComponentsTo(myFixture);
    PsiElement element = resolveReferenceAtCaret();
    assertInstanceOf(element, CfmlFunction.class);
    assertEquals("ComponentName", assertInstanceOf(element.getParent(), CfmlComponent.class).getName());
  }


}
