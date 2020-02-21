// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.refactoring.memberPushDown.JSPushDownProcessor;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.MultiFileTestCase;
import com.intellij.refactoring.util.DocCommentPolicy;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class FlexPushDownTest extends MultiFileTestCase {
  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "pushDown/";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), getTestRootDisposable());
  }

  private void doTestPushDown(final String from, final int docCommentPolicy, final boolean makeAbstract, final String... toPushDown) {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) {
        FlexPushDownTest.this.performAction(from, docCommentPolicy, makeAbstract, toPushDown);
      }
    }, false);
  }

  private void performAction(String from, int docCommentPolicy, boolean makeAbstract, final String[] toPushDown) {
    final JSClass sourceClass =
      (JSClass)JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
        .findClassByQName(from, GlobalSearchScope.projectScope(getProject()));
    assertNotNull("source class not found: " + from, sourceClass);
    assertTrue(sourceClass.getQualifiedName() + " has no inheritors",
               !JSInheritanceUtil.findDirectSubClasses(sourceClass, false).isEmpty());

    final List<JSMemberInfo> memberInfos = FlexPullUpTest.getMemberInfos(toPushDown, sourceClass, makeAbstract);

    JSMemberInfo[] infosArray = JSMemberInfo.getSelected(memberInfos, sourceClass, Conditions.alwaysTrue());
    new JSPushDownProcessor(myProject, infosArray, sourceClass, docCommentPolicy) {
      @Override
      protected UsageInfo @NotNull [] findUsages() {
        // ensure stable order
        final UsageInfo[] usages = super.findUsages();
        Arrays.sort(usages,
                    (o1, o2) -> ((JSClass)o1.getElement()).getQualifiedName().compareTo(((JSClass)o2.getElement()).getQualifiedName()));
        return usages;
      }
    }.run();
    PostprocessReformattingAspect.getInstance(myProject).doPostponedFormatting();
    FileDocumentManager.getInstance().saveAllDocuments();
  }

  private void doTestConflicts(final String from, String[] expectedConflicts, final String... toPushDown) {
    try {
      doTestPushDown(from, DocCommentPolicy.MOVE, false, toPushDown);
      fail("conflicts expected : " + StringUtil.join(expectedConflicts, ";"));
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertSameElements(e.getMessages(), expectedConflicts);
    }
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testClass() {
    doTestPushDown("Super", DocCommentPolicy.MOVE, false, "foo", "bar", "t", "uu", "v");
  }

  public void testSuperMethodCall() {
    doTestPushDown("Super", DocCommentPolicy.MOVE, false, "foo");
  }

  public void testImplements() {
    doTestPushDown("Super", DocCommentPolicy.MOVE, false, "IFoo");
  }

  public void testInterface() {
    doTestPushDown("ISuper", DocCommentPolicy.MOVE, false, "foo");
  }

  public void testAbstractize1() {
    doTestPushDown("ISuper", DocCommentPolicy.ASIS, true, "foo");
  }

  public void testAbstractize2() {
    doTestPushDown("ISuper", DocCommentPolicy.COPY, true, "foo");
  }

  public void testFromMxml() {
    doTestPushDown("Super", DocCommentPolicy.COPY, false, "foo", "IFoo", "v1", "v3", "v4", "instance");
  }

  public void testConflicts1() {
    String[] conflicts =
      new String[]{"Property prop uses field v, which is pushed down", "Constructor Super() uses method foo(), which is pushed down",
        "Method Super.i() with internal visibility won't be accessible from method foo()",
        "Method Super.prv() with private visibility won't be accessible from method foo()",
        "Class Aux1 with internal visibility won't be accessible from method foo()",
        "Inner class Aux2 won't be accessible from method foo()"};
    doTestConflicts("Super", conflicts, "foo", "v");
  }

  public void testProperty() {
    doTestPushDown("Super", DocCommentPolicy.ASIS, false, "opacity", "_opacity");
  }

  public void testConflicts2() {
    String[] conflicts =
      new String[]{"Method Interface1.foo() is already overridden in class Impl. Method will not be pushed down to that class."};
    doTestConflicts("Interface1", conflicts, "foo");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testVector() {
    doTestPushDown("foo.From", DocCommentPolicy.ASIS, false, "foo");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testVector2() {
    doTestPushDown("IFoo", DocCommentPolicy.ASIS, false, "abc");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testVector3() {
    doTestPushDown("IFoo", DocCommentPolicy.ASIS, false, "abc");
  }
}
