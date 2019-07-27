// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;

public abstract class Angular2CodeInsightFixtureTestCase extends BasePlatformTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AngularTestUtil.enableAstLoadingFilter(this);
  }
}
