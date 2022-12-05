// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSCompletionInTextFieldTest;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class FlexCompletionInTextFieldBase extends BaseJSCompletionInTextFieldTest {

  protected static final String BASE_PATH = "/js2_completion/";

  static final String[] DEFALUT_VALUES = { "true", "false", "null", "NaN", "Infinity" };

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Override
  protected String getExtension() {
    return "js2";
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
  }

  protected void checkTextFieldCompletion(JSExpressionCodeFragment fragment,
                                          String[] included,
                                          String[] excluded,
                                          @Nullable String choose,
                                          String file) {
    doTestTextFieldFromFile(fragment, file);
    assertContains(myFixture.getLookupElements(), true, included);
    assertContains(myFixture.getLookupElements(), false, excluded);
    if (choose != null) {
      boolean found = false;
      for (LookupElement item : myFixture.getLookupElements()) {
        if (choose.equals(item.getLookupString())) {
          myFixture.getLookup().setCurrentItem(item);
          myFixture.type('\n');
          found = true;
          break;
        }
      }
      assertTrue("Item '" + choose + "' not found in lookup", found);
      myFixture.checkResultByFile(getTestName(false) + "_after.txt");
    }
  }

  static void assertContains(LookupElement[] items, boolean contains, String... expected) {
    Collection<String> c = ContainerUtil.newHashSet(expected);
    for (LookupElement item : items) {
      final String s = item.getLookupString();
      final boolean removed = c.remove(s);
      if (!contains) {
        assertTrue("'" + s + "' is not expected to be part of completion list", !removed);
      }
    }
    if (contains) {
      assertTrue("Items [" + toString(c, ",") + "] are expected to be part of completion list", c.isEmpty());
    }
  }

  protected JSClass createFakeClass() {
    return JSPsiImplUtils.findClass((JSFile)JSChangeUtil
      .createJSTreeFromText(getProject(), "package {class Foo { function a() {}} }", JavaScriptSupportLoader.ECMA_SCRIPT_L4)
      .getPsi().getContainingFile());
  }
}
