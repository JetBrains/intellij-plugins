package com.intellij.flex.codeInsight;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSHierarchyFixture;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.hierarchy.call.JSCalleeMethodsTreeStructure;
import com.intellij.lang.javascript.hierarchy.call.JSCallerMethodsTreeStructure;
import com.intellij.lang.javascript.hierarchy.method.JSMethodHierarchyTreeStructure;
import com.intellij.lang.javascript.hierarchy.type.jsclass.JSSubtypesHierarchyTreeStructure;
import com.intellij.lang.javascript.hierarchy.type.jsclass.JSSupertypesHierarchyTreeStructure;
import com.intellij.lang.javascript.hierarchy.type.jsclass.JSTypeHierarchyTreeStructure;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSHierarchyFixture.jsHierarchyFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class FlexHierarchyTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSHierarchyFixture> hierarchy = jsHierarchyFixture(
    projectFixture,
    new JSFixtureOptions(FlexTestUtils.getTestDataPath("/hierarchy")).withModuleType(FlexModuleType.MODULE_TYPE_ID)
  );

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(hierarchy.get().getTestRootDisposable(), "");
  }

  private void setUpJdk() {
    JSHierarchyFixture js = hierarchy.get();
    FlexTestUtils.setupFlexSdk(js.getFixture().getModule(), js.getTestName(false), FlexHierarchyTest.class, js.getTestRootDisposable());
  }

  private void doJSTypeHierarchyTest(final String hierarchyType, final String classFqn) throws Exception {
    doJSTypeHierarchyTest(hierarchyType, classFqn, hierarchy.get().getTestName(false) + ".as");
  }

  private void doJSTypeHierarchyTest(final String hierarchyType, final String classFqn, final String... fileNames) throws Exception {
    JSHierarchyFixture js = hierarchy.get();
    js.doHierarchyTest(() -> {
      final JSClass jsClass =
        (JSClass)JSDialectSpecificHandlersFactory.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
          .findClassByQName(classFqn, GlobalSearchScope.moduleScope(js.getFixture().getModule()));
      assert jsClass != null;
      if (TypeHierarchyBrowserBase.getTypeHierarchyType().equals(hierarchyType)) {
        return new JSTypeHierarchyTreeStructure(js.getProject(), jsClass);
      }
      else if (TypeHierarchyBrowserBase.getSubtypesHierarchyType().equals(hierarchyType)) {
        return new JSSubtypesHierarchyTreeStructure(js.getProject(), jsClass);
      }
      else if (TypeHierarchyBrowserBase.getSupertypesHierarchyType().equals(hierarchyType)) {
        return new JSSupertypesHierarchyTreeStructure(js.getProject(), jsClass);
      }
      throw new IllegalArgumentException("Wrong hierarchy type: " + hierarchyType);
    }, fileNames);
  }

  private void doJSMethodHierarchyTest(final String classFqn,
                                       final String methodName,
                                       final boolean hideClassesWhereMethodNotImplemented,
                                       final String... fileNames) throws Exception {
    JSHierarchyFixture js = hierarchy.get();
    final HierarchyBrowserManager.State state = HierarchyBrowserManager.getInstance(js.getProject()).getState();
    final boolean oldState = state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED;
    state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED = hideClassesWhereMethodNotImplemented;

    js.doHierarchyTest(() -> {
      final JSClass jsClass =
        (JSClass)JSDialectSpecificHandlersFactory.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
          .findClassByQName(classFqn, GlobalSearchScope.moduleScope(js.getFixture().getModule()));
      assert jsClass != null;
      final JSFunction jsFunction = jsClass.findFunctionByName(methodName);
      assert jsFunction != null;
      return new JSMethodHierarchyTreeStructure(js.getProject(), jsFunction);
    }, fileNames);

    state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED = oldState;
  }

  private void doJSCallHierarchyTest(final String hierarchyType,
                                     final String classFqn,
                                     final String methodName,
                                     final String scope,
                                     final String... fileNames) throws Exception {
    JSHierarchyFixture js = hierarchy.get();
    js.doHierarchyTest(() -> {
      final JSClass jsClass =
        (JSClass)JSDialectSpecificHandlersFactory.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
          .findClassByQName(classFqn, GlobalSearchScope.moduleScope(js.getFixture().getModule()));
      assert jsClass != null;
      final JSFunction jsFunction = jsClass.findFunctionByName(methodName);
      assert jsFunction != null;
      if (CallHierarchyBrowserBase.getCalleeType().equals(hierarchyType)) {
        return new JSCalleeMethodsTreeStructure(js.getProject(), jsFunction, scope);
      }
      else if (CallHierarchyBrowserBase.getCallerType().equals(hierarchyType)) {
        return new JSCallerMethodsTreeStructure(js.getProject(), jsFunction, scope);
      }
      throw new IllegalArgumentException("Wrong hierarchy type: " + hierarchyType);
    }, fileNames);
  }

  @Test
  public void testTypeHierarchy() throws Exception {
    doJSTypeHierarchyTest(TypeHierarchyBrowserBase.getTypeHierarchyType(), "mypack.SomeClass");
  }

  @Test
  public void testInterfacesHierarchy() throws Exception {
    final String testName = hierarchy.get().getTestName(false);
    doJSTypeHierarchyTest(TypeHierarchyBrowserBase.getSubtypesHierarchyType(), "pack.Interface2", testName + ".as", testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testSupertypesHierarchy() throws Exception {
    setUpJdk();
    final String testName = hierarchy.get().getTestName(false);
    doJSTypeHierarchyTest(TypeHierarchyBrowserBase.getSupertypesHierarchyType(), "pack2.Class2", testName + ".as", testName + ".mxml");
  }

  private static final String COMMON_METHOD_HIERARCHY_RESOURCE_NAME = "MethodHierarchyCommon";

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMethodHierarchyFromInterface() throws Exception {
    doJSMethodHierarchyTest("pack.Interface1", "foo", true, COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".as",
                            COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMethodHierarchyFromMxml() throws Exception {
    doJSMethodHierarchyTest(COMMON_METHOD_HIERARCHY_RESOURCE_NAME, "foo", false, COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".as",
                            COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMethodHierarchyFromAs() throws Exception {
    doJSMethodHierarchyTest("pack.Class2", "foo", true, COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".as",
                            COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMethodCalleeHierarchy() throws Exception {
    setUpJdk();
    final String testName = hierarchy.get().getTestName(false);
    doJSCallHierarchyTest(CallHierarchyBrowserBase.getCalleeType(), "pack.subpack.Class1", "someFunction",
                          HierarchyBrowserBaseEx.SCOPE_ALL,
                          testName + ".as", testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMethodCallerHierarchy() throws Exception {
    final String testName = hierarchy.get().getTestName(false);
    doJSCallHierarchyTest(CallHierarchyBrowserBase.getCallerType(), "pack.Class1", "bar", HierarchyBrowserBaseEx.SCOPE_PROJECT,
                          testName + ".as");
  }
}

