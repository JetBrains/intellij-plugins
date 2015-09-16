package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.List;

public class DartAnalysisServerHighlightingTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  protected String getBasePath() {
    return "/analysisServer/highlighting";
  }

  private void doHighlightingTest() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting();
  }

  private static void checkRegions(final List<? extends DartAnalysisServerService.PluginRegion> regions, final TextRange... ranges) {
    assertEquals("Incorrect regions amount", ranges.length, regions.size());
    int i = 0;
    for (DartAnalysisServerService.PluginRegion region : regions) {
      assertEquals("Mismatched region " + i, ranges[i++], TextRange.create(region.getOffset(), region.getOffset() + region.getLength()));
    }
  }

  private void undoAndUpdateHighlighting(@NotNull final VirtualFile file) {
    // to make sure that navigation and highlighting regions are reset we have to send new document contents both before Undo and after
    DartAnalysisServerService.getInstance().updateFilesContent();
    UndoManager.getInstance(getProject()).undo(FileEditorManager.getInstance(getProject()).getSelectedEditor(file));
    myFixture.doHighlighting();
    checkServerDataInitialState(file);
  }

  public void testErrorsHighlighting() throws Exception {
    doHighlightingTest();
  }

  public void testErrorsAfterEOF() {
    doHighlightingTest();
  }

  public void testErrorAtZeroOffset() {
    myFixture.addFileToProject("bar.dart", "part of x;");
    doHighlightingTest();
  }

  private void initServerDataTest() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByText(DartFileType.INSTANCE, "import 'dart:core';\n" +
                                                     "import 'dart:core';\n" +
                                                     "import 'dart:core';\n");
    myFixture.doHighlighting();
  }

  private static void checkServerDataInitialState(@NotNull final VirtualFile file) {
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    // references to 'dart:core'
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 39), TextRange.create(20, 26), TextRange.create(27, 38),
                 TextRange.create(40, 59), TextRange.create(40, 46), TextRange.create(47, 58));
  }

  public void testServerDataUpdateOnTyping() {
    // navigation region must be deleted when touched by editing and updated otherwise
    initServerDataTest();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();
    checkServerDataInitialState(file);

    // typing at the beginning of the region
    getEditor().getCaretModel().moveToOffset(27);
    myFixture.type('a');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(28, 39), TextRange.create(48, 59));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 40), TextRange.create(20, 26), TextRange.create(28, 39),
                 TextRange.create(41, 60), TextRange.create(41, 47), TextRange.create(48, 59));

    undoAndUpdateHighlighting(file);
    // typing in the middle of the region
    getEditor().getCaretModel().moveToOffset(29);
    myFixture.type('a');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(48, 59));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 40), TextRange.create(20, 26), TextRange.create(27, 39),
                 TextRange.create(41, 60), TextRange.create(41, 47), TextRange.create(48, 59));

    undoAndUpdateHighlighting(file);
    // typing at the end of the region
    getEditor().getCaretModel().moveToOffset(38);
    myFixture.type('a');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(48, 59));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 40), TextRange.create(20, 26), TextRange.create(27, 38),
                 TextRange.create(41, 60), TextRange.create(41, 47), TextRange.create(48, 59));
  }

  public void testServerDataUpdateOnPaste() {
    initServerDataTest();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();
    checkServerDataInitialState(file);

    CopyPasteManager.getInstance().setContents(new StringSelection("long text 012345678901234567890123456789"));

    // paste a lot at the beginning of the region
    getEditor().getCaretModel().moveToOffset(27);
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(67, 78), TextRange.create(87, 98));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 79), TextRange.create(20, 26), TextRange.create(67, 78),
                 TextRange.create(80, 99), TextRange.create(80, 86), TextRange.create(87, 98));

    undoAndUpdateHighlighting(file);
    // paste a lot in the middle of the region
    getEditor().getCaretModel().moveToOffset(29);
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(87, 98));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 79), TextRange.create(20, 26), TextRange.create(27, 78),
                 TextRange.create(80, 99), TextRange.create(80, 86), TextRange.create(87, 98));

    undoAndUpdateHighlighting(file);
    // paste a lot at the end of the region
    getEditor().getCaretModel().moveToOffset(38);
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(87, 98));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 79), TextRange.create(20, 26), TextRange.create(27, 38),
                 TextRange.create(80, 99), TextRange.create(80, 86), TextRange.create(87, 98));
  }

  public void testServerDataUpdateOnBackspace() {
    initServerDataTest();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();
    checkServerDataInitialState(file);

    // backspace at the beginning of the region
    getEditor().getCaretModel().moveToOffset(27);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(26, 37), TextRange.create(46, 57));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 38), TextRange.create(20, 26), TextRange.create(26, 37),
                 TextRange.create(39, 58), TextRange.create(39, 45), TextRange.create(46, 57));

    undoAndUpdateHighlighting(file);
    // backspace in the middle of the region
    getEditor().getCaretModel().moveToOffset(29);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(27, 38),*/ TextRange.create(46, 57));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 38), TextRange.create(20, 26), TextRange.create(27, 37),
                 TextRange.create(39, 58), TextRange.create(39, 45), TextRange.create(46, 57));

    undoAndUpdateHighlighting(file);
    // backspace at the end of the region
    getEditor().getCaretModel().moveToOffset(39);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(46, 57));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 38), TextRange.create(20, 26), TextRange.create(27, 38),
                 TextRange.create(39, 58), TextRange.create(39, 45), TextRange.create(46, 57));
  }

  public void testServerDataUpdateOnSelectionDelete() {
    initServerDataTest();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();
    checkServerDataInitialState(file);

    // delete exactly the region
    getEditor().getSelectionModel().setSelection(27, 38);
    getEditor().getCaretModel().moveToOffset(38);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(26, 37),*/ TextRange.create(47 - 11, 58 - 11));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 39 - 11), TextRange.create(20, 26), /*TextRange.create(27, 38),*/
                 TextRange.create(40 - 11, 59 - 11), TextRange.create(40 - 11, 46 - 11), TextRange.create(47 - 11, 58 - 11));

    undoAndUpdateHighlighting(file);
    // delete selection in the middle of the region
    getEditor().getSelectionModel().setSelection(29, 36);
    getEditor().getCaretModel().moveToOffset(36);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(47 - 7, 58 - 7));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 39 - 7), TextRange.create(20, 26), TextRange.create(27, 38 - 7),
                 TextRange.create(40 - 7, 59 - 7), TextRange.create(40 - 7, 46 - 7), TextRange.create(47 - 7, 58 - 7));

    undoAndUpdateHighlighting(file);
    // delete selection that includes region and selection start/end touch other regions
    getEditor().getSelectionModel().setSelection(18, 47);
    getEditor().getCaretModel().moveToOffset(47);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(47 - 29, 58 - 29));
    checkRegions(service.getHighlight(file),
                 /*TextRange.create(0, 19),*/ TextRange.create(0, 6), TextRange.create(7, 18),
                 /*TextRange.create(20, 39), TextRange.create(20, 26), TextRange.create(27, 38),*/
                 /*TextRange.create(40, 59), TextRange.create(40, 46),*/ TextRange.create(47 - 29, 58 - 29));

    undoAndUpdateHighlighting(file);
    // delete selection that has start in one region and end in another
    getEditor().getSelectionModel().setSelection(17, 28);
    getEditor().getCaretModel().moveToOffset(28);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), /*TextRange.create(7, 18), TextRange.create(28, 39),*/ TextRange.create(36, 47));
    checkRegions(service.getHighlight(file),
                 /*TextRange.create(0, 19),*/ TextRange.create(0, 6), /*TextRange.create(7, 18),*/
                 /*TextRange.create(20, 39), TextRange.create(20, 26), TextRange.create(27, 38),*/
                 TextRange.create(40 - 11, 59 - 11), TextRange.create(40 - 11, 46 - 11), TextRange.create(47 - 11, 58 - 11));
  }

  public void testNavigationTargetOffsetUpdated() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByText(DartFileType.INSTANCE, "var a = 1; var b = a;");
    myFixture.doHighlighting();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();

    final List<DartAnalysisServerService.PluginNavigationRegion> regions = service.getNavigation(file);
    checkRegions(regions, TextRange.create(4, 5), TextRange.create(15, 16), TextRange.create(19, 20));
    assertEquals(4, regions.get(2).getTargets().get(0).getOffset());

    getEditor().getCaretModel().moveToOffset(0);
    myFixture.type("foo \b");
    checkRegions(regions, TextRange.create(4 + 3, 5 + 3), TextRange.create(15 + 3, 16 + 3), TextRange.create(19 + 3, 20 + 3));
    assertEquals(4 + 3, regions.get(2).getTargets().get(0).getOffset());
  }

  public void testSyntaxHighlighting() throws Exception {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.checkHighlighting(true, true, true);
  }
}
