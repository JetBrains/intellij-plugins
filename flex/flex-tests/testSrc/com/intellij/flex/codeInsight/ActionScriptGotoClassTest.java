// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.ide.util.gotoByName.LanguageRef;
import com.intellij.lang.javascript.JSGotoByNameTestBase;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.navigation.NavigationItem;
import com.intellij.testFramework.TestFrameworkUtil;
import com.intellij.testFramework.TestIndexingModeSupporter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("NewClassNamingConvention")
public class ActionScriptGotoClassTest extends JSGotoByNameTestBase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JSTestUtils.initJSIndexes(getProject());
    myFixture.enableInspections(JSUnresolvedReferenceInspection.class);
  }

  public static @NotNull Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ActionScriptGotoClassTest.class);
    TestIndexingModeSupporter.addTest(ActionScriptGotoClassTest.class, new TestIndexingModeSupporter.FullIndexSuite(), suite);
    return TestFrameworkUtil.flattenSuite(suite);
  }

  public void testAsClass() {
    myFixture.addFileToProject("com/test/Foo.as", "package com.test { public class Foo {} }");
    checkGotoClass("Foo", true);
  }

  public void testMxmlClass() {
    myFixture.addFileToProject("com/test/Bar.mxml", """
      <?xml version="1.0" ?>
      <mx:Canvas xmlns:mx="http://www.adobe.com/2006/mxml">
      </mx:Canvas>""");
    checkGotoClass("Bar", true);
  }

  public void testMxmClass() {
    myFixture.addFileToProject("com/test/Bar.mxml", """
      <?xml version="1.0" ?>
      <mx:Canvas xmlns:mx="http://www.adobe.com/2006/mxml">
      </mx:Canvas>""");
    checkGotoClass("Bar", true);
  }

  private NavigationItem checkGotoClass(@NotNull String className, boolean exists) {
    GotoClassModel2 model = new GotoClassModel2(myFixture.getProject());
    model.setFilterItems(LanguageRef.forAllLanguages());
    return doCheckGoto(model, className, exists);
  }
}
