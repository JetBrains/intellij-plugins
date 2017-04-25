package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.DataManager;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperProcessor;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.MultiFileTestCase;
import com.intellij.refactoring.util.DocCommentPolicy;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexExtractSuperTest extends MultiFileTestCase {

  @NotNull
  @Override
  protected String getTestRoot() {
    return "extractSuper/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  public void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    myDoCompare = true;
    JSTestUtils.disableFileHeadersInTemplates(getProject());
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  private void doTest(final JSExtractSuperProcessor.Mode mode, final boolean classNotInterface, final String sourceClassName,
                      final String extractedSuperName,
                      final int docCommentPolicy,
                      final String... members) throws Exception {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        FlexExtractSuperTest.this
          .performAction(classNotInterface, sourceClassName, extractedSuperName, docCommentPolicy, mode, members, new String[]{});
      }
    }, false);
  }

  private void doTestConflicts(final JSExtractSuperProcessor.Mode mode, final boolean classNotInterface, final String sourceClassName,
                               final String extractedSuperName,
                               final int docCommentPolicy,
                               final String[] members,
                               @NotNull final String[] conflicts) throws Exception {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        FlexExtractSuperTest.this
          .performAction(classNotInterface, sourceClassName, extractedSuperName, docCommentPolicy, mode, members, conflicts);
      }
    }, false);
  }

  private void performAction(boolean classNotInterface,
                             String from,
                             final String extractedSuperName,
                             int docCommentPolicy,
                             JSExtractSuperProcessor.Mode mode,
                             String[] members,
                             String[] conflicts) {
    JSClass sourceClass = JSTestUtils.findClassByQName(from, GlobalSearchScope.moduleScope(myModule));
    final List<JSMemberInfo> memberInfos = FlexPullUpTest.getMemberInfos(members, sourceClass, false);
    JSMemberInfo.sortByOffset(memberInfos);
    JSMemberInfo[] infosArray = JSMemberInfo.getSelected(memberInfos, sourceClass, Conditions.alwaysTrue());
    try {
      final PsiElement finalSourceClass = sourceClass;
      PsiDirectory dir =
        WriteCommandAction.runWriteCommandAction(null, new Computable<PsiDirectory>() {
          public PsiDirectory compute() {
            return CreateClassOrInterfaceFix.findOrCreateDirectory(StringUtil.getPackageName(extractedSuperName), finalSourceClass);
          }
        });
      new JSExtractSuperProcessor(sourceClass, infosArray, StringUtil.getShortName(extractedSuperName),
                                  StringUtil.getPackageName(extractedSuperName), docCommentPolicy, mode, classNotInterface, dir).run();
      assertEquals("Conflicts expected:\n" + StringUtil.join(conflicts, "\n"), 0, conflicts.length);
      myProject.getComponent(PostprocessReformattingAspect.class).doPostponedFormatting();
      FileDocumentManager.getInstance().saveAllDocuments();
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertNotNull("Conflicts not expected but found:" + e.getMessage(), conflicts);
      assertSameElements(e.getMessages(), conflicts);
      myDoCompare = false;
    }
  }

  public void testInterface() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "com.Foo", "bar.IFoo", DocCommentPolicy.COPY, "moved1",
           "moved2", "moved3", "moved4", "moved5", "MyInt");
  }

  public void testInterface2() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "SourceClass", "ISuper", DocCommentPolicy.COPY);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testUsages() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "SourceClass", "ISuper", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp",
           "IMoved");
  }

  public void testNoTurnRefs() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuper, false, "From", "ISuper", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp");
  }

  public void testConflicts1() throws Exception {
    String[] conflicts = new String[]{
      "Class AuxClass with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Interface AuxInterface with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Constant AuxConst with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)",
      "Function AuxFunc() with internal visibility won't be accessible from method movedMethod(AuxClass, AuxInterface, AuxClassPub, AuxFunc, AuxConst)"
    };
    doTestConflicts(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "From", "com.ISuper", DocCommentPolicy.COPY,
                    new String[]{"movedMethod"}, conflicts);
  }

  public void testInterfaceFromInterface() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuper, false, "IFrom", "ISuper", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp",
           "IMoved");
  }

  public void testExtractImplementationClass() throws Exception {
    // TODO FileDocumentManager.getInstance().saveDocument(document); in JSExtractSuperProcessor.renameOriginalClass seems to fix it
    doTest(JSExtractSuperProcessor.Mode.RenameImplementation, false, "com.From", "bar.FromImpl", DocCommentPolicy.COPY,
           "movedMethod",
           "movedProp",
           "movedProp2",
           "IMoved");
  }

  public void testConflicts2() throws Exception {
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
    doTestConflicts(JSExtractSuperProcessor.Mode.RenameImplementation, false, "From", "com.FromImpl", DocCommentPolicy.COPY,
                    new String[]{"movedMethod"}, conflicts);
  }

  public void testMxml1() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuper, false, "From", "com.IBar", DocCommentPolicy.COPY);
  }

  public void testMxmlTurnRefs() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "From", "com.IBar", DocCommentPolicy.COPY, "IFoo", "foo", "bar");
  }

  public void testMxmlExtractImpl() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.RenameImplementation, false, "From", "bar.FromImpl", DocCommentPolicy.COPY, "foo1", "foo2");
  }

  public void testExtractImplementationWithExplicitConstructor() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.RenameImplementation, false, "From", "FromImpl", DocCommentPolicy.COPY);
  }

  public void testSuperClass1() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, true, "com.From", "bar.NewClass", DocCommentPolicy.COPY, "v1", "v2", "foo1",
           "foo2", "MyInt1");
  }

  public void testSuperClass2() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuper, true, "Foo", "com.NewClass", DocCommentPolicy.COPY);
  }

  public void testInterface3() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuper, false, "com.Foo", "IFoo", DocCommentPolicy.COPY);
  }

  public void testFileLocal1() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuper, true, "From.as:Local1", "com.foo.Local1Base", DocCommentPolicy.COPY, "v", "foo");
  }

  public void testFileLocal2() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "From.as:Local1", "com.foo.ILocal1", DocCommentPolicy.COPY, "foo");
  }

  public void testFileLocal3() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.RenameImplementation, true, "From.as:Local1", "Local1Ex", DocCommentPolicy.COPY, "foo", "v");
  }

  public void testFileLocal4() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.RenameImplementation, false, "From.as:Local1", "Local1Ex", DocCommentPolicy.COPY, "foo");
  }

  public void testOverriddenParameter() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, true, "Foo", "FooBase", DocCommentPolicy.COPY, "foo");
  }

  public void testRefsInMovedMembers1() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, true, "From", "FromSuper", DocCommentPolicy.COPY, "foo", "bar");
  }

  public void testRefsInMovedMembers2() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "From", "IFrom", DocCommentPolicy.COPY, "foo");
  }

  public void testRefsInMovedMembers3() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.RenameImplementation, true, "From.as:From", "FromEx", DocCommentPolicy.COPY, "foo");
  }

  public void testSuperCall() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, true, "From", "FromSuper", DocCommentPolicy.COPY, "foo");
  }

  public void testHierarchyReturnType() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, false, "From", "IFrom", DocCommentPolicy.COPY, "moved");
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testForInStatement() throws Exception {
    doTest(JSExtractSuperProcessor.Mode.RenameImplementation, true, "From.as:Box", "BoxImpl", DocCommentPolicy.COPY, "sleep");
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testInheritanceFromSdk() throws Exception {
    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false);
    doTest(new PerformAction() {
      @Override
      public void performAction(final VirtualFile rootDir, final VirtualFile rootAfter) throws Exception {
        FlexTestUtils.modifyConfigs(myProject, editor -> {
          ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
          bc1.setName("1");
          FlexTestUtils.setSdk(bc1, sdk);
        });

        FlexExtractSuperTest.this.performAction(false, "skins.MyNewSkin1", "skins.IMySkin", DocCommentPolicy.COPY,
                                                JSExtractSuperProcessor.Mode.ExtractSuperTurnRefs, new String[]{"updateDisplayList"},
                                                ArrayUtil.EMPTY_STRING_ARRAY);
      }
    });
  }

  public void testActionAvailability1() throws Exception {
    checkActions(getTestName(false) + ".as");
  }

  public void testActionAvailability2() throws Exception {
    checkActions(getTestName(false) + ".mxml");
  }

  public void testActionAvailability3() throws Exception {
    checkActions(getTestName(false) + ".xml");
  }

  public void testActionAvailability4() throws Exception {
    checkActions(getTestName(false) + ".js");
  }

  private void checkActions(String filename) throws Exception {
    configureByFile(getTestRoot() + filename);
    LinkedHashMap<Integer, String> markers = JSTestUtils.extractPositionMarkers(myProject, getEditor().getDocument());
    int pos = 0;
    for (Map.Entry<Integer, String> entry : markers.entrySet()) {
      pos++;
      getEditor().getCaretModel().moveToOffset(entry.getKey());

      checkAction("ExtractInterface", entry.getValue().contains("interface"), pos);
      checkAction("ExtractSuperclass", entry.getValue().contains("class"), pos);
    }
  }

  private void checkAction(String actionId, boolean enabled, int pos) {
    AnAction action = ActionManager.getInstance().getAction(actionId);
    AnActionEvent e = new TestActionEvent(DataManager.getInstance().getDataContext(getEditor().getComponent()), action);
    action.beforeActionPerformedUpdate(e);
    assertEquals("Action " + actionId + " should be " + (enabled ? "enabled" : "disabled") + " at position " + pos, enabled,
                 e.getPresentation().isEnabled());
  }
}

