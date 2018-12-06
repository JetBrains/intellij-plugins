// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.lang.resharper.ReSharperIntentionTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;

public class Angular2BananaFixTest extends ReSharperIntentionTestCase {

  @NotNull
  @Override
  protected String getIntentionName() {
    return "Fix parentheses/brackets nesting";
  }

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static String findTestData(@NotNull Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass) + "Intentions/Angular2Html/QuickFixes";
  }

  @Override
  protected void doSingleTest(String suffix, String path) throws Exception {
    try {
      super.doSingleTest(suffix, path);
    }
    catch (AssertionError | RuntimeException assertionError) {
      Assume.assumeTrue("This test is ignored", false); // causes test to be ignored
    }
    Assert.fail("Test is ignored but passed successfully");
  }
}
