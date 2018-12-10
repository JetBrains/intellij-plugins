// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper;

import com.intellij.lang.javascript.resharper.JSReSharperSymbolResolveTestBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.TestDataPath;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;


@TestDataPath("$R#_SYMBOL_RESOLVE_TEST_ROOT/DependencyInjection")
public class AngularJSResolveTest extends JSReSharperSymbolResolveTestBase {

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static String findTestData(@NotNull Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass)
           + "/Resolve/"
           + StringUtil.trimStart(klass.getAnnotation(TestDataPath.class).value(),
                                  "$R#_SYMBOL_RESOLVE_TEST_ROOT");
  }
}
