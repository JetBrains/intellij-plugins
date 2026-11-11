// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.ide.util.gotoByName.LanguageRef;
import com.intellij.lang.javascript.JSGotoByNameFixture;
import com.intellij.lang.javascript.JSGotoByNameFixture.IndexingModesTest;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestIndexingModeSupporter.IndexingMode;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import static com.intellij.lang.javascript.JSGotoByNameFixture.jsGotoByNameFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptGotoClassTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSGotoByNameFixture> gotoFixture = jsGotoByNameFixture(projectFixture);

  @BeforeEach
  void setUp() {
    JSTestUtils.initJSIndexes(gotoFixture.get().getProject());
    gotoFixture.get().getFixture().enableInspections(JSUnresolvedReferenceInspection.class);
  }

  @IndexingModesTest
  public void testAsClass(IndexingMode mode) {
    gotoFixture.get().inMode(mode).addFileToProject("com/test/Foo.as", "package com.test { public class Foo {} }");
    checkGotoClass("Foo", true);
  }

  @IndexingModesTest
  public void testMxmlClass(IndexingMode mode) {
    gotoFixture.get().inMode(mode).addFileToProject("com/test/Bar.mxml", """
      <?xml version="1.0" ?>
      <mx:Canvas xmlns:mx="http://www.adobe.com/2006/mxml">
      </mx:Canvas>""");
    checkGotoClass("Bar", true);
  }

  @IndexingModesTest
  public void testMxmClass(IndexingMode mode) {
    gotoFixture.get().inMode(mode).addFileToProject("com/test/Bar.mxml", """
      <?xml version="1.0" ?>
      <mx:Canvas xmlns:mx="http://www.adobe.com/2006/mxml">
      </mx:Canvas>""");
    checkGotoClass("Bar", true);
  }

  private NavigationItem checkGotoClass(@NotNull String className, boolean exists) {
    GotoClassModel2 model = new GotoClassModel2(gotoFixture.get().getProject());
    model.setFilterItems(LanguageRef.forAllLanguages());
    return gotoFixture.get().doCheckGoto(model, className, exists);
  }
}
