// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType;
import com.intellij.lang.javascript.validation.ValidateTypesUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class FlexUnitSupport {

  private static final Logger LOG = Logger.getInstance(FlexUnitSupport.class.getName());

  @NotNull public final JSClass flexUnit1TestClass;
  @Nullable public final JSClass flunitTestClass;
  @Nullable private final JSClass flunitTestSuite;
  public final boolean flexUnit4Present;
  private final JSClass flexUnit1TestSuite;

  public static final String FLEX_UNIT_1_TESTCASE_CLASS = "flexunit.framework.TestCase";
  public static final String FLEX_UNIT_1_TESTSUITE_CLASS = "flexunit.framework.TestSuite";
  public static final String FLEX_UNIT_4_CORE_CLASS = "org.flexunit.runner.FlexUnitCore";
  public static final String FLUNIT_TESTCASE_CLASS = "net.digitalprimates.fluint.tests.TestCase";
  public static final String FLUNIT_TESTSUITE_CLASS = "net.digitalprimates.fluint.tests.TestSuite";
  public static final String TEST_ATTRIBUTE = "Test";
  static final String IGNORE_ATTRIBUTE = "Ignore";
  public static final String SUITE_ATTRIBUTE = "Suite";
  public static final String RUN_WITH_ATTRIBUTE = "RunWith";
  public static final String SUITE_RUNNER = "org.flexunit.runners.Suite";

  private FlexUnitSupport(@NotNull JSClass flexUnit1TestClass,
                          @NotNull JSClass flexUnit1TestSuiteClass,
                          @Nullable JSClass flunitTestClass,
                          @Nullable JSClass flunitTestSuite,
                          boolean flexUnit4Present) {
    this.flexUnit1TestClass = flexUnit1TestClass;
    this.flexUnit1TestSuite = flexUnit1TestSuiteClass;
    this.flunitTestClass = flunitTestClass;
    this.flunitTestSuite = flunitTestSuite;
    this.flexUnit4Present = flexUnit4Present;
  }

  @Nullable
  public static Pair<Module, FlexUnitSupport> getModuleAndSupport(@NotNull PsiElement context) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(context);
    final FlexUnitSupport support = getSupport(module);
    return support != null ? Pair.create(module, support) : null;
  }

  @Nullable
  public static FlexUnitSupport getSupport(@Nullable FlexBuildConfiguration bc, final Module module) {
    if (bc == null) return null;

    return getSupport(FlexUtils.getModuleWithDependenciesAndLibrariesScope(module, bc, true));
  }

  @Nullable
  public static FlexUnitSupport getSupport(@Nullable Module module) {
    if (module == null) return null;
    if (ModuleType.get(module) != FlexModuleType.getInstance() || FlexUtils.getSdkForActiveBC(module) == null) return null;

    return getSupport(GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
  }

  private static FlexUnitSupport getSupport(final GlobalSearchScope searchScope) {
    PsiElement flexUnit1TestClass = ActionScriptClassResolver.findClassByQNameStatic(FLEX_UNIT_1_TESTCASE_CLASS, searchScope);

    if (!(flexUnit1TestClass instanceof JSClass)) return null;

    PsiElement flexUnit1TestSuiteClass = ActionScriptClassResolver.findClassByQNameStatic(FLEX_UNIT_1_TESTSUITE_CLASS, searchScope);

    if (!(flexUnit1TestSuiteClass instanceof JSClass)) {
      LOG.warn(FLEX_UNIT_1_TESTCASE_CLASS + " class is present but " + FLEX_UNIT_1_TESTSUITE_CLASS + " is not");
      return null;
    }

    boolean flexUnit4Present = ActionScriptClassResolver.findClassByQNameStatic(FLEX_UNIT_4_CORE_CLASS, searchScope) instanceof JSClass;

    PsiElement flunitTestClass = flexUnit4Present ? ActionScriptClassResolver.findClassByQNameStatic(FLUNIT_TESTCASE_CLASS, searchScope) : null;

    PsiElement flunitTestSuiteClass =
      (flunitTestClass instanceof JSClass) ? (JSClass)ActionScriptClassResolver.findClassByQNameStatic(FLUNIT_TESTSUITE_CLASS, searchScope) : null;

    return new FlexUnitSupport((JSClass)flexUnit1TestClass, (JSClass)flexUnit1TestSuiteClass, (JSClass)flunitTestClass,
                               (JSClass)flunitTestSuiteClass, flexUnit4Present);
  }

  public boolean isFlexUnit1Subclass(JSClass clazz) {
    return JSInheritanceUtil.isParentClass(clazz, flexUnit1TestClass);
  }

  public boolean isFlexUnit1SuiteSubclass(JSClass clazz) {
    return JSInheritanceUtil.isParentClass(clazz, flexUnit1TestSuite);
  }

  public boolean isFlunitSubclass(JSClass clazz) {
    return flunitTestClass != null && JSInheritanceUtil.isParentClass(clazz, flunitTestClass);
  }

  public boolean isFlunitSuiteSubclass(JSClass clazz) {
    return flunitTestSuite != null && JSInheritanceUtil.isParentClass(clazz, flunitTestSuite);
  }

  public boolean isPotentialTestClass(@NotNull JSClass clazz) {
    if (isFlexUnit1Subclass(clazz) || isFlunitSubclass(clazz)) {
      return true;
    }

    for (JSFunction method : clazz.getFunctions()) {
      if (isPotentialTestMethod(method)) {
        return true;
      }
    }
    return false;
  }

  public boolean isPotentialTestMethod(JSFunction method) {
    if (method.getKind() == JSFunction.FunctionKind.CONSTRUCTOR) return false;

    PsiElement parent = method.getParent();
    if (parent instanceof JSClass && (isFlunitSubclass((JSClass)parent) || isFlexUnit1Subclass((JSClass)parent))) {
      if (method.getName() != null && method.getName().startsWith("test")) return true;
    }

    if (method.getAttributeList() != null &&
        method.getAttributeList().getAttributesByName(TEST_ATTRIBUTE).length > 0 &&
        method.getAttributeList().getAttributesByName(IGNORE_ATTRIBUTE).length == 0) {
      return true;
    }
    return false;
  }

  public boolean isTestClass(@NotNull JSClass clazz, boolean allowSuite) {
    if (clazz instanceof XmlBackedJSClassImpl) return false;
    if (clazz.getAttributeList() == null) return false;
    if (clazz.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) return false;

    if (allowSuite && isSuite(clazz)) return true;

    final boolean flexUnit1Subclass = isFlexUnit1Subclass(clazz);
    if (!flexUnit1Subclass && !flexUnit4Present) return false;

    if (getCustomRunner(clazz) == null) {
      for (JSFunction method : clazz.getFunctions()) {
        if (method.getKind() == JSFunction.FunctionKind.CONSTRUCTOR && ValidateTypesUtil.hasRequiredParameters(method)) return false;
      }
    }

    if (!flexUnit1Subclass && !isFlunitSubclass(clazz)) {
      boolean hasTests = false;
      for (JSFunction method : clazz.getFunctions()) {
        if (isTestMethod(method)) {
          hasTests = true;
          break;
        }
      }
      if (!hasTests) return false;
    }
    return true;
  }

  public boolean isSuite(JSClass clazz) {
    if (isFlexUnit1SuiteSubclass(clazz) || isFlunitSuiteSubclass(clazz)) return true;
    if (flexUnit4Present && clazz.getAttributeList() != null && clazz.getAttributeList().getAttributesByName(SUITE_ATTRIBUTE).length > 0) {
      return true;
    }
    return false;
  }

  /**
   * @return [RunWith] metadata default attribute value. Can be {@code null}, empty string or whatever.
   */
  @Nullable
  public static String getCustomRunner(JSClass clazz) {
    final JSAttribute[] attrs = clazz.getAttributeList().getAttributesByName(RUN_WITH_ATTRIBUTE);
    if (attrs.length == 0) return null;
    final JSAttributeNameValuePair attr = attrs[0].getValueByName(null);
    return attr == null ? null : attr.getSimpleValue();
  }

  public boolean isTestMethod(JSFunction method) {
    if (!(method.getParent() instanceof JSClass clazz) || method.getParent() instanceof XmlBackedJSClassImpl) return false;

    // FlexUnit 1: flexunit.framework.TestCase.getTestMethodNames()
    // Flunit: net.digitalprimates.fluint.tests.defaultFilterFunction()
    // FlexUnit 4: org.flexunit.runners.BlockFlexUnit4ClassRunner
    if (method.getName() == null) return false;

    if (flexUnit4Present && getCustomRunner(clazz) != null) return true;

    if (method.getAttributeList() == null) return false;
    if (method.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) return false;
    if (method.getKind() != JSFunction.FunctionKind.SIMPLE) return false;
    if (method.getAttributeList().hasModifier(JSAttributeList.ModifierType.STATIC)) return false;
    if (ValidateTypesUtil.hasRequiredParameters(method)) return false;

    if (isFlexUnit1Subclass(clazz)) {
      if (!method.getName().startsWith("test")) return false;
    }
    else if (isFlunitSubclass(clazz)) {
      if (!method.getName().startsWith("test") && method.getAttributeList().getAttributesByName(TEST_ATTRIBUTE).length == 0) return false;
    }
    else {
      if (!flexUnit4Present) return false;

      final JSType returnType = method.getReturnType();
      if (returnType != null && !(returnType instanceof JSVoidType)) return false;

      if (method.getAttributeList().getAttributesByName(IGNORE_ATTRIBUTE).length > 0) return false;
      if (method.getAttributeList().getAttributesByName(TEST_ATTRIBUTE).length == 0) return false;
    }
    return true;
  }

  public Collection<JSClass> getSuiteTestClasses(JSClass suiteClass) {
    if (!SUITE_RUNNER.equals(getCustomRunner(suiteClass))) return Collections.emptyList();

    Collection<JSClass> result = new ArrayList<>();
    for (JSField field : suiteClass.getFields()) {
      if (field.getAttributeList() == null) continue;
      if (field.getAttributeList().hasModifier(JSAttributeList.ModifierType.STATIC)) continue;
      if (field.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) continue;
      final PsiElement typeElement = field.getTypeElement();
      if (!(typeElement instanceof JSReferenceExpression)) continue;
      final PsiElement type = ((JSReferenceExpression)typeElement).resolve();
      if (!(type instanceof JSClass)) continue;
      result.add((JSClass)type);
    }
    return result;
  }
}
