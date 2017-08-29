package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.hierarchy.call.JSCalleeMethodsTreeStructure;
import com.intellij.lang.javascript.hierarchy.call.JSCallerMethodsTreeStructure;
import com.intellij.lang.javascript.hierarchy.method.JSMethodHierarchyTreeStructure;
import com.intellij.lang.javascript.hierarchy.type.jsclass.JSSubtypesHierarchyTreeStructure;
import com.intellij.lang.javascript.hierarchy.type.jsclass.JSSupertypesHierarchyTreeStructure;
import com.intellij.lang.javascript.hierarchy.type.jsclass.JSTypeHierarchyTreeStructure;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexHierarchyTest extends HierarchyViewTestBase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getBasePath() {
    return "hierarchy";
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  private void doJSTypeHierarchyTest(final String hierarchyType, final String classFqn) throws Exception {
    doJSTypeHierarchyTest(hierarchyType, classFqn, getTestName(false) + ".as");
  }

  private void doJSTypeHierarchyTest(final String hierarchyType, final String classFqn, final String... fileNames) throws Exception {
    doHierarchyTest(() -> {
      final JSClass jsClass =
        (JSClass)JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
          .findClassByQName(classFqn, GlobalSearchScope.moduleScope(myModule));
      assert jsClass != null;
      if (TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE.equals(hierarchyType)) {
        return new JSTypeHierarchyTreeStructure(myProject, jsClass);
      }
      else if (TypeHierarchyBrowserBase.SUBTYPES_HIERARCHY_TYPE.equals(hierarchyType)) {
        return new JSSubtypesHierarchyTreeStructure(myProject, jsClass);
      }
      else if (TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE.equals(hierarchyType)) {
        return new JSSupertypesHierarchyTreeStructure(myProject, jsClass);
      }
      throw new IllegalArgumentException("Wrong hierarchy type: " + hierarchyType);
    }, fileNames);
  }

  private void doJSMethodHierarchyTest(final String classFqn,
                                       final String methodName,
                                       final boolean hideClassesWhereMethodNotImplemented,
                                       final String... fileNames) throws Exception {
    final HierarchyBrowserManager.State state = HierarchyBrowserManager.getInstance(myProject).getState();
    final boolean oldState = state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED;
    state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED = hideClassesWhereMethodNotImplemented;

    doHierarchyTest(() -> {
      final JSClass jsClass =
        (JSClass)JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
          .findClassByQName(classFqn, GlobalSearchScope.moduleScope(myModule));
      assert jsClass != null;
      final JSFunction jsFunction = jsClass.findFunctionByName(methodName);
      assert jsFunction != null;
      return new JSMethodHierarchyTreeStructure(myProject, jsFunction);
    }, fileNames);

    state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED = oldState;
  }

  private void doJSCallHierarchyTest(final String hierarchyType,
                                     final String classFqn,
                                     final String methodName,
                                     final String scope,
                                     final String... fileNames) throws Exception {
    doHierarchyTest(() -> {
      final JSClass jsClass =
        (JSClass)JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
          .findClassByQName(classFqn, GlobalSearchScope.moduleScope(myModule));
      assert jsClass != null;
      final JSFunction jsFunction = jsClass.findFunctionByName(methodName);
      assert jsFunction != null;
      if (CallHierarchyBrowserBase.CALLEE_TYPE.equals(hierarchyType)) {
        return new JSCalleeMethodsTreeStructure(myProject, jsFunction, scope);
      }
      else if (CallHierarchyBrowserBase.CALLER_TYPE.equals(hierarchyType)) {
        return new JSCallerMethodsTreeStructure(myProject, jsFunction, scope);
      }
      throw new IllegalArgumentException("Wrong hierarchy type: " + hierarchyType);
    }, fileNames);
  }

  public void testTypeHierarchy() throws Exception {
    doJSTypeHierarchyTest(TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE, "mypack.SomeClass");
  }

  public void testInterfacesHierarchy() throws Exception {
    final String testName = getTestName(false);
    doJSTypeHierarchyTest(TypeHierarchyBrowserBase.SUBTYPES_HIERARCHY_TYPE, "pack.Interface2", testName + ".as", testName + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testSupertypesHierarchy() throws Exception {
    final String testName = getTestName(false);
    doJSTypeHierarchyTest(TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE, "pack2.Class2", testName + ".as", testName + ".mxml");
  }

  private static final String COMMON_METHOD_HIERARCHY_RESOURCE_NAME = "MethodHierarchyCommon";

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testMethodHierarchyFromInterface() throws Exception {
    doJSMethodHierarchyTest("pack.Interface1", "foo", true, COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".as",
                            COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testMethodHierarchyFromMxml() throws Exception {
    doJSMethodHierarchyTest(COMMON_METHOD_HIERARCHY_RESOURCE_NAME, "foo", false, COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".as",
                            COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testMethodHierarchyFromAs() throws Exception {
    doJSMethodHierarchyTest("pack.Class2", "foo", true, COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".as",
                            COMMON_METHOD_HIERARCHY_RESOURCE_NAME + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testMethodCalleeHierarchy() throws Exception {
    final String testName = getTestName(false);
    doJSCallHierarchyTest(CallHierarchyBrowserBase.CALLEE_TYPE, "pack.subpack.Class1", "someFunction", CallHierarchyBrowserBase.SCOPE_ALL,
                          testName + ".as", testName + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testMethodCallerHierarchy() throws Exception {
    final String testName = getTestName(false);
    doJSCallHierarchyTest(CallHierarchyBrowserBase.CALLER_TYPE, "pack.Class1", "bar", CallHierarchyBrowserBase.SCOPE_PROJECT,
                          testName + ".as");
  }
}

