// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.lang.resharper.ReSharperIntentionTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class Angular2BananaFixTest extends ReSharperIntentionTestCase {

  @NotNull
  @Override
  protected String getIntentionName() {
    return "Fix parentheses/brackets nesting";
  }

  @Override
  protected boolean isExcluded(@NotNull String testName) {
    return true;
  }

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static String findTestData(@NotNull Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass) + "Intentions/Angular2Html/QuickFixes";
  }
}
