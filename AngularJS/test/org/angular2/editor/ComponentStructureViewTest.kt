// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.lang.javascript.JSAbstractStructureViewTest;
import com.intellij.lang.javascript.StructureViewTestUtil;
import org.angularjs.AngularTestUtil;

public class ComponentStructureViewTest extends JSAbstractStructureViewTest {
  @Override
  protected String getBasePath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testStructure() {
    myFixture.configureByFiles("Structure.ts", "package.json");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }
}
