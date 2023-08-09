// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.DataManager;
import com.intellij.javascript.flex.refactoring.extractSuper.FlexExtractSuperProcessor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.LightPlatformMultiFileFixtureTestCase;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperMode;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.util.DocCommentPolicy;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlexExtractSuperTest extends LightPlatformMultiFileFixtureTestCase {

  @NotNull
  @Override
  protected String getTestRoot() {
    return "extractSuper/";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myDoCompare = true;
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  private void doTest(final JSExtractSuperMode mode, final boolean classNotInterface, final String sourceClassName,
                      final String extractedSuperName,
                      final int docCommentPolicy,
                      final String... members) {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) {
        FlexExtractSuperTest.this
          .performAction(classNotInterface, sourceClassName, extractedSuperName, docCommentPolicy, mode, members, new String[]{});
      }
    }, false);
  }

  private void doTestConflicts(final JSExtractSuperMode mode, final boolean classNotInterface, final String sourceClassName,
                               final String extractedSuperName,
                               final int docCommentPolicy,
                               final String[] members,
                               final String @NotNull [] conflicts) {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) {
        FlexExtractSuperTest.this
          .performAction(classNotInterface, sourceClassName, extractedSuperName, docCommentPolicy, mode, members, conflicts);
      }
    }, false);
  }

  private void performAction(boolean classNotInterface,
                             String from,
                             final String extractedSuperName,
                             int docCommentPolicy,
                             JSExtractSuperMode mode,
                             String[] members,
                             String[] conflicts) {
    JSClass sourceClass = JSTestUtils.findClassByQName(from, GlobalSearchScope.moduleScope(getModule()));
    final List<JSMemberInfo> memberInfos = FlexPullUpTest.getMemberInfos(members, sourceClass, false);
    JSMemberInfo.sortByOffset(memberInfos);
    JSMemberInfo[] infosArray = JSMemberInfo.getSelected(memberInfos, sourceClass, Conditions.alwaysTrue());
    try {
      final PsiElement finalSourceClass = sourceClass;
      PsiDirectory dir =
        WriteCommandAction.runWriteCommandAction(null,
                                                 (Computable<PsiDirectory>)() -> ActionScriptCreateClassOrInterfaceFix
                                                   .findOrCreateDirectory(StringUtil.getPackageName(extractedSuperName), finalSourceClass));
      new FlexExtractSuperProcessor(sourceClass, infosArray, StringUtil.getShortName(extractedSuperName),
                                    StringUtil.getPackageName(extractedSuperName), docCommentPolicy, mode, classNotInterface, dir).run();
      assertEquals("Conflicts expected:\n" + StringUtil.join(conflicts, "\n"), 0, conflicts.length);
      PostprocessReformattingAspect.getInstance(myFixture.getProject()).doPostponedFormatting();
      FileDocumentManager.getInstance().saveAllDocuments();
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertNotNull("Conflicts not expected but found:" + e.getMessage(), conflicts);
      assertSameElements(e.getMessages(), conflicts);
      myDoCompare = false;
    }
  }

  public void testInterface() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, false, "com.Foo", "bar.IFoo", DocCommentPolicy.COPY, "moved1",
           "moved2", "moved3", "moved4", "moved5", "MyInt");
  }

  public void testInterface2() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, false, "SourceClass", "ISuper", DocCommentPolicy.COPY);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testUsages() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, false, "SourceClass", "ISuper", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp",
           "IMoved");
  }

  public void testNoTurnRefs() {
    doTest(JSExtractSuperMode.ExtractSuper, false, "From", "ISuper", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp");
  }

  public void testConflicts1() {
    String[] conflicts = new String[]{
      "Class AuxClass with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Interface AuxInterface with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Constant AuxConst with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Function AuxFunc() with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)"
    };
    doTestConflicts(JSExtractSuperMode.ExtractSuperTurnRefs, false, "From", "com.ISuper", DocCommentPolicy.COPY,
                    new String[]{"movedMethod"}, conflicts);
  }

  public void testInterfaceFromInterface() {
    doTest(JSExtractSuperMode.ExtractSuper, false, "IFrom", "ISuper", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp",
           "IMoved");
  }

  public void testExtractImplementationClass() {
    // TODO FileDocumentManager.getInstance().saveDocument(document); in FlexExtractSuperProcessor.renameOriginalClass seems to fix it
    doTest(JSExtractSuperMode.RenameImplementation, false, "com.From", "bar.FromImpl", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp",
           "movedProp2",
           "IMoved");
  }

  public void testConflicts2() {
    String[] conflicts = new String[]{
      "Class AuxClass with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Class FromImpl with internal visibility won't be accessible from constructor Usage.Usage()",
      "Function AuxFunc() with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Function globalMethod() with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Interface AuxInterface with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Constant AuxConst with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Class FromImpl with internal visibility won't be accessible from constructor Usage.Usage()",
      "Method FromImpl.stat() with internal visibility won't be accessible from constructor Usage()"
    };
    doTestConflicts(JSExtractSuperMode.RenameImplementation, false, "From", "com.FromImpl", DocCommentPolicy.COPY,
                    new String[]{"movedMethod"}, conflicts);
  }

  public void testMxml1() {
    doTest(JSExtractSuperMode.ExtractSuper, false, "From", "com.IBar", DocCommentPolicy.COPY);
  }

  public void testMxmlTurnRefs() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, false, "From", "com.IBar", DocCommentPolicy.COPY, "IFoo", "foo", "bar");
  }

  public void testMxmlExtractImpl() {
    doTest(JSExtractSuperMode.RenameImplementation, false, "From", "bar.FromImpl", DocCommentPolicy.COPY, "foo1", "foo2");
  }

  public void testExtractImplementationWithExplicitConstructor() {
    doTest(JSExtractSuperMode.RenameImplementation, false, "From", "FromImpl", DocCommentPolicy.COPY);
  }

  public void testSuperClass1() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, true, "com.From", "bar.NewClass", DocCommentPolicy.COPY, "v1", "v2", "foo1",
           "foo2", "MyInt1");
  }

  public void testSuperClass2() {
    doTest(JSExtractSuperMode.ExtractSuper, true, "Foo", "com.NewClass", DocCommentPolicy.COPY);
  }

  public void testInterface3() {
    doTest(JSExtractSuperMode.ExtractSuper, false, "com.Foo", "IFoo", DocCommentPolicy.COPY);
  }

  public void testFileLocal1() {
    doTest(JSExtractSuperMode.ExtractSuper, true, "From.as:Local1", "com.foo.Local1Base", DocCommentPolicy.COPY, "v", "foo");
  }

  public void testFileLocal2() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, false, "From.as:Local1", "com.foo.ILocal1", DocCommentPolicy.COPY, "foo");
  }

  public void testFileLocal3() {
    doTest(JSExtractSuperMode.RenameImplementation, true, "From.as:Local1", "Local1Ex", DocCommentPolicy.COPY, "foo", "v");
  }

  public void testFileLocal4() {
    doTest(JSExtractSuperMode.RenameImplementation, false, "From.as:Local1", "Local1Ex", DocCommentPolicy.COPY, "foo");
  }

  public void testOverriddenParameter() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, true, "Foo", "FooBase", DocCommentPolicy.COPY, "foo");
  }

  public void testRefsInMovedMembers1() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, true, "From", "FromSuper", DocCommentPolicy.COPY, "foo", "bar");
  }

  public void testRefsInMovedMembers2() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, false, "From", "IFrom", DocCommentPolicy.COPY, "foo");
  }

  public void testRefsInMovedMembers3() {
    doTest(JSExtractSuperMode.RenameImplementation, true, "From.as:From", "FromEx", DocCommentPolicy.COPY, "foo");
  }

  public void testSuperCall() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, true, "From", "FromSuper", DocCommentPolicy.COPY, "foo");
  }

  public void testHierarchyReturnType() {
    doTest(JSExtractSuperMode.ExtractSuperTurnRefs, false, "From", "IFrom", DocCommentPolicy.COPY, "moved");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testForInStatement() {
    doTest(JSExtractSuperMode.RenameImplementation, true, "From.as:Box", "BoxImpl", DocCommentPolicy.COPY, "sleep");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testInheritanceFromSdk() {
    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, myFixture.getTestRootDisposable());
    doTest(new PerformAction() {
      @Override
      public void performAction(final VirtualFile rootDir, final VirtualFile rootAfter) {
        FlexTestUtils.modifyConfigs(myFixture.getProject(), editor -> {
          ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(getModule())[0];
          bc1.setName("1");
          FlexTestUtils.setSdk(bc1, sdk);
        });

        FlexExtractSuperTest.this.performAction(false, "skins.MyNewSkin1", "skins.IMySkin", DocCommentPolicy.COPY,
                                                JSExtractSuperMode.ExtractSuperTurnRefs, new String[]{"updateDisplayList"},
                                                ArrayUtilRt.EMPTY_STRING_ARRAY);
      }
    });
  }

  public void testActionAvailability1() {
    checkActions(getTestName(false) + ".as");
  }

  public void testActionAvailability2() {
    checkActions(getTestName(false) + ".mxml");
  }

  public void testActionAvailability3() {
    checkActions(getTestName(false) + ".xml");
  }

  private void checkActions(String filename) {
    myFixture.configureByFile(getTestRoot() + filename);
    LinkedHashMap<Integer, String> markers = JSTestUtils.extractPositionMarkers(myFixture.getProject(), myFixture.getEditor().getDocument());
    int pos = 0;
    for (Map.Entry<Integer, String> entry : markers.entrySet()) {
      pos++;
      myFixture.getEditor().getCaretModel().moveToOffset(entry.getKey());

      checkAction("ExtractInterface", entry.getValue().contains("interface"), pos);
      checkAction("ExtractSuperclass", entry.getValue().contains("class"), pos);
    }
  }

  private void checkAction(String actionId, boolean enabled, int pos) {
    AnAction action = ActionManager.getInstance().getAction(actionId);
    AnActionEvent e = TestActionEvent.createTestEvent(
      action, DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent()));
    // warm up injections
    InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), myFixture.getCaretOffset());
    ActionUtil.lastUpdateAndCheckDumb(action, e, false);
    assertEquals("Action " + actionId + " should be " + (enabled ? "enabled" : "disabled") + " at position " + pos, enabled,
                 e.getPresentation().isEnabled());
  }
}

