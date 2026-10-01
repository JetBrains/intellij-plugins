// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.FlexRenameHandler;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.HeadlessRenameProcessor;
import com.intellij.refactoring.rename.HeadlessRenamePsiElementProcessor;
import com.intellij.refactoring.rename.HeadlessRenameResult;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class FlexHeadlessRenameTest extends BasePlatformTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  public void testOverrideRenamesTheSuperMethod() {
    myFixture.configureByText("OverrideRenamesTheSuperMethod.js2", """
      package xxx {
        class YYY extends yyy.XXX {
          public override function te<caret>st() {
            if (false) test();
            var a:yyy.XXX
            a.test()
            this.test()
          }
        }
      }

      class YYY2 extends yyy.XXX {
          public function test() {
              super.test();
          }
      }

      package yyy {
        public class XXX {
          public function test() {
            if (true) test()
          }
        }
      }""");
    JSFunction override = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getCaretOffset()), JSFunction.class);
    assertNotNull(override);
    assertInstanceOf(HeadlessRenamePsiElementProcessor.processorOf(override), FlexRenameHandler.class);

    HeadlessRenameResult result = performRename(override, "test2");

    assertInstanceOf(result, HeadlessRenameResult.Applied.class);
    myFixture.checkResult("""
      package xxx {
        class YYY extends yyy.XXX {
          public override function test2() {
            if (false) test2();
            var a:yyy.XXX
            a.test2()
            this.test2()
          }
        }
      }

      class YYY2 extends yyy.XXX {
          public function test() {
              super.test2();
          }
      }

      package yyy {
        public class XXX {
          public function test2() {
            if (true) test2()
          }
        }
      }""");
  }

  private HeadlessRenameResult performRename(PsiElement element, String newName) {
    HeadlessRenameResult planned = HeadlessRenameProcessor.analyze(getProject(), element, newName);
    HeadlessRenameResult.Planned plan = assertInstanceOf(planned, HeadlessRenameResult.Planned.class);
    return plan.getPlan().apply();
  }
}
