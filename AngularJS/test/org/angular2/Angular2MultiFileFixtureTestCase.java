// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.javascript.LightPlatformMultiFileFixtureTestCase;
import org.angularjs.AngularTestUtil;

public abstract class Angular2MultiFileFixtureTestCase extends LightPlatformMultiFileFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AngularTestUtil.enableAstLoadingFilter(this);
  }
}
