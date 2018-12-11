// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper;

import com.intellij.lang.javascript.resharper.JSReSharperSymbolResolveTestBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;


@TestDataPath("$R#_SYMBOL_RESOLVE_TEST_ROOT/DependencyInjection")
public class AngularJSResolveTest extends JSReSharperSymbolResolveTestBase {

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static String findTestData(@NotNull Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass)
           + "/Resolve/"
           + StringUtil.trimStart(klass.getAnnotation(TestDataPath.class).value(),
                                  "$R#_SYMBOL_RESOLVE_TEST_ROOT");
  }

  private static final Set<String> IGNORED_TESTS = ContainerUtil.newHashSet(
    "InjectBuiltinServices.js",
    "InjectBuiltinServicesByStringLiteral.js",
    "InjectByStringLiteral.js",
    "InjectFunctionConstant.js",
    "InjectFunctionValue.js",
    "InjectIntoAnimation.js",
    "InjectIntoConfig.js",
    "InjectIntoDirective.js",
    "InjectIntoFactory.js",
    "InjectIntoFilter.js",
    "InjectIntoProvider.js",
    "InjectIntoRun.js",
    "InjectIntoService.js",
    "InjectNumberConstant.js",
    "InjectProvider.js",
    "InjectProviderValue.js",
    "InjectStringConstant.js",
    "InjectStringValue.js"
  );

  @Override
  protected boolean isExcluded() {
    return IGNORED_TESTS.contains(myTestFile);
  }
}
