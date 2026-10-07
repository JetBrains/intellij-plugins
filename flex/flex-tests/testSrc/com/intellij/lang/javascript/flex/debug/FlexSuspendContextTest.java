// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class FlexSuspendContextTest extends BasePlatformTestCase {
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addFileToProject("ui/buttons/Button.as", """
      package ui.buttons {
      public class Button {
        public function set PrefixMap(map:Object):void {}
      }
      }""");
    myFixture.addFileToProject("lib/controls/Button.as", """
      package lib.controls {
      public class Button {}
      }""");
    myFixture.addFileToProject("app/views/CellView.as", """
      package app.views {
      import ui.buttons.Button;
      public class CellView extends Button {
        public function CellView() {
          PrefixMap = {};
        }
      }
      }""");
  }

  public void testInheritedMethod() {
    assertEquals("ui.buttons", getPackage(
      "#0   this = [Object 1, class='app.views::CellView'].Button/set PrefixMap(map=[Object 2, class='Object']) at Button.as#3:3"));
  }

  public void testOwnMethod() {
    assertEquals("app.views", getPackage("#0   this = [Object 1, class='app.views::CellView'].CellView() at CellView.as#4:5"));
  }

  public void testMethodClassWithPackage() {
    assertEquals("lib.controls", getPackage("#0   this = [Object 1, class='app.views::CellView'].lib.controls::Button/foo() at Button.as#3:3"));
  }

  public void testStaticContext() {
    assertEquals("app.views", getPackage("#0   this = [Object 1, class='app.views::CellView$'].CellView$/foo() at CellView.as#4:5"));
  }

  public void testUnknownMethodClass() {
    assertEquals("app.views", getPackage("#0   this = [Object 1, class='app.views::CellView'].Unknown/foo() at Unknown.as#4:5"));
  }

  public void testNoThis() {
    assertNull(getPackage("#0   FlexSprite() at FlexSprite.as:59"));
  }

  private String getPackage(String frameText) {
    return FlexSuspendContext.getPackageFromFrameText(getProject(), getModule(), frameText);
  }
}
