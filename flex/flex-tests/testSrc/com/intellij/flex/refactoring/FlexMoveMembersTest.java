// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.refactoring.moveMembers.ActionScriptMoveMembersDialog;
import com.intellij.javascript.flex.refactoring.moveMembers.ActionScriptMoveMembersProcessor;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMoveMembersOptions;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.MultiFileTestCase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FlexMoveMembersTest extends MultiFileTestCase {

  private static final String VISIBILITY_AS_IS = null;

  private static final String[] ALL_MEMBERS = {};

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");

    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "moveMembers/";
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
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  private void doTest(final String sourceClassName, final String targetClassName, final String visibility, final String memberName) {
    doTest(sourceClassName, targetClassName, visibility, new String[]{memberName});
  }

  private void doTest(final String sourceClassName, final String targetClassName, final String visibility, final String[] memberNames) {
    try {
      doTestImpl(sourceClassName, targetClassName, visibility, memberNames);
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      fail("Conflicts: " + toString(e.getMessages(), "\n"));
    }
  }

  private void doTestImpl(final String sourceClassName, final String targetClassName, final String visibility, final String[] memberNames) {
    doTest((rootDir, rootAfter) -> performAction(sourceClassName, targetClassName, visibility, memberNames));
  }

  private void performAction(String sourceClassName, final String targetClassName, final String visibility, final String[] memberNames) {
    final JSClassResolver resolver =
      JSDialectSpecificHandlersFactory.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    final JSClass sourceClass = (JSClass)resolver.findClassByQName(sourceClassName, ActionScriptMoveMembersDialog.getScope(myProject));
    assertNotNull("Class " + sourceClassName + " not found", sourceClass);
    JSClass targetClass = (JSClass)resolver.findClassByQName(targetClassName, ActionScriptMoveMembersDialog.getScope(myProject));
    assertNotNull("Class " + targetClassName + " not found", targetClass);

    List<JSMemberInfo> memberInfos = new ArrayList<>();
    JSMemberInfo.extractStaticMembers(sourceClass, memberInfos,
                                      member -> memberNames == ALL_MEMBERS || ArrayUtil.contains(member.getName(), memberNames));

    List<JSMemberInfo> sortedInfos = JSMemberInfo.sortByOffset(memberInfos);
    sortedInfos.forEach(memberInfo -> memberInfo.setChecked(true));

    new ActionScriptMoveMembersProcessor(myProject, null, sourceClass, ActionScriptMoveMembersDialog.getScope(myProject), new JSMoveMembersOptions() {

      @Override
      public JSAttributeListOwner[] getSelectedMembers() {
        final JSMemberInfo[] selected = JSMemberInfo.getSelected(sortedInfos, sourceClass, Conditions.alwaysTrue());
        JSAttributeListOwner[] result = new JSAttributeListOwner[selected.length];
        for (int i = 0; i < result.length; i++) {
          result[i] = selected[i].getMember();
        }
        return result;
      }

      @Override
      public String getTargetClassName() {
        return targetClassName;
      }

      @Override
      public String getMemberVisibility() {
        return visibility;
      }
    }).run();

    PostprocessReformattingAspect.getInstance(myProject).doPostponedFormatting();
    FileDocumentManager.getInstance().saveAllDocuments();
  }

  private void doTestConflicts(final String sourceClassName,
                               final String targetClassName,
                               final String visibility,
                               final String[] memberNames,
                               String[] expectedConflicts) {
    try {
      doTestImpl(sourceClassName, targetClassName, visibility, memberNames);
      fail("conflicts expected");
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertSameElements(e.getMessages(), expectedConflicts);
    }
  }

  public void testSamePackage() {
    doTest("foo.From", "foo.To", null, "func");
  }

  public void testVar() {
    doTest("From", "To", VISIBILITY_AS_IS, "aVar");
  }

  public void testVar1Of2() {
    doTest("From", "To", VISIBILITY_AS_IS, "var1");
  }

  public void testVar2Of2() {
    doTest("From", "To", VISIBILITY_AS_IS, "var2");
  }

  public void test2Vars() {
    doTest("From", "To", VISIBILITY_AS_IS, new String[]{"var1", "var2"});
  }

  public void testBackAndForth() {
    doTest((rootDir, rootAfter) -> {
      performAction("From", "To", VISIBILITY_AS_IS, new String[]{"foo"});
      performAction("To", "From", VISIBILITY_AS_IS, new String[]{"foo"});
    });
  }

  public void testImports() {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, "foo");
  }

  public void testMultiple() {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testEscalateVisibility1() {
    doTest("a.From", "b.To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testEscalateVisibility2() {
    doTest("From", "To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testEscalateVisibility3() {
    doTest("From", "To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testEscalateVisibility4() {
    doTest("a.From", "b.To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testFromMxml1() {
    doTest("From", "foo.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testImports2() {
    doTest("a.From", "a.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testImports3() {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, new String[]{"foo", "bar"});
  }

  public void testImports4() {
    doTest("a.From", "b.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testConflicts1() {
    String[] conflicts = {"Field From.foo with private visibility won't be accessible from method move1()",
      "Method move1() with private visibility in the target class is not accessible from field From.foo",
      "Constant field move2 with protected visibility in the target class is not accessible from field From.bar",
      "Field From.zzz with internal visibility won't be accessible from method move1()",
      "Inner class Aux won't be accessible from method move1()",
      "Constant field move3 with internal visibility in the target class is not accessible from field From.zzz",
      "Field From.bar with protected visibility won't be accessible from method move1()"};
    doTestConflicts("a.From", "b.To", VISIBILITY_AS_IS, new String[]{"move1", "move2", "move3"}, conflicts);
  }

  public void testConflicts2() {
    String[] conflicts = {"Class To already contains a field bar", "Class To already contains a method foo()"};
    doTestConflicts("From", "To", VISIBILITY_AS_IS, new String[]{"foo", "bar", "zz"}, conflicts);
  }

  public void testConflicts3() {
    String[] conflicts = {"Method From.move2() with protected visibility won't be accessible from method move()"};
    doTestConflicts("From", "a.To", VISIBILITY_AS_IS, new String[]{"move"}, conflicts);
  }

  public void testConflicts4() {
    String[] conflicts =
      {"Method move() with protected visibility in the target class is not accessible from method From.move2()",
        "Class Class1 with internal visibility won't be accessible from method move()"};
    doTestConflicts("From", "a.To", VISIBILITY_AS_IS, new String[]{"move"}, conflicts);
  }

  public void testConflicts5() {
    String[] conflicts = {"Field a with internal visibility in the target class is not accessible from method From.foo()"};
    doTestConflicts("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS, conflicts);
  }

  public void testProperty() {
    doTest("From", "To", VISIBILITY_AS_IS, new String[]{"readwrite", "read", "write"});
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImportInMxml() {
    doTest("From", "foo.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImportInMxml2() {
    doTest("bar.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImports5() {
    doTest("From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImportInMxml3() {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testSimple() {
    doTest("From", "com.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testDequalify() {
    doTest("From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testReferenceToAS3() {
    doTest("From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testConstructorCall() {
    doTest("foo.From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testNamespaces() {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testNamespaces2() {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  public void testConflictsNs() {
    String[] conflicts = {"Namespace MyNs with internal visibility won't be accessible from field v",
      "Namespace MyNs with internal visibility won't be accessible from method foo()",
      "Namespace MyNs2 with internal visibility won't be accessible from method foo()"};
    doTestConflicts("foo.From", "bar.To", VISIBILITY_AS_IS, ALL_MEMBERS, conflicts);
  }

  public void testNamespaces3() {
    doTest("foo.From", "bar.To", VISIBILITY_AS_IS, new String[]{"foo", "bar"});
  }

  public void testNamespaces4() {
    doTest("From", "To", JSAttributeList.AccessType.PUBLIC.name(), new String[]{"foo", "bar", "ZZ"});
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testVector() {
    doTest("foo.From", "To", VISIBILITY_AS_IS, ALL_MEMBERS);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testEmbeddedImage() {
    doTest("From", "To", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }

  public void testToInnerClass() {
    doTest("C", "Ggg", JSVisibilityUtil.ESCALATE_VISIBILITY, ALL_MEMBERS);
  }
}
