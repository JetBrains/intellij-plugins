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

  public void testNavigationDataUpdateOnTyping() {
    // navigation region must be deleted when touched by editing and updated otherwise
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByText(DartFileType.INSTANCE, "import 'dart:core';\n" +
                                                     "import 'dart:core';\n" +
                                                     "import 'dart:core';\n");
    // just to warm up, to make sure that highlighting and navigation data arrives
    myFixture.doHighlighting();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();

    // references to 'dart:core'
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    checkRegions(service.getHighlight(file),
                 TextRange.create(0, 19), TextRange.create(0, 6), TextRange.create(7, 18),
                 TextRange.create(20, 39), TextRange.create(20, 26), TextRange.create(27, 38),
                 TextRange.create(40, 59), TextRange.create(40, 46), TextRange.create(47, 58));

    // typing at the beginning of the region
    getEditor().getCaretModel().moveToOffset(27);
    myFixture.type('a');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(28, 39), TextRange.create(48, 59));

    undoAndUpdateHighlighting(file);
    // typing in the middle of the region
    getEditor().getCaretModel().moveToOffset(29);
    myFixture.type('a');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(48, 59));

    undoAndUpdateHighlighting(file);
    // typing at the end of the region
    getEditor().getCaretModel().moveToOffset(38);
    myFixture.type('a');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(48, 59));
  }

  public void testNavigationDataUpdateOnPaste() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByText(DartFileType.INSTANCE, "import 'dart:core';\n" +
                                                     "import 'dart:core';\n" +
                                                     "import 'dart:core';\n");
    myFixture.doHighlighting();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();

    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));

    CopyPasteManager.getInstance().setContents(new StringSelection("long text 012345678901234567890123456789"));

    // paste a lot at the beginning of the region
    getEditor().getCaretModel().moveToOffset(27);
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(67, 78), TextRange.create(87, 98));

    undoAndUpdateHighlighting(file);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    // paste a lot in the middle of the region
    getEditor().getCaretModel().moveToOffset(29);
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(87, 98));

    undoAndUpdateHighlighting(file);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    // paste a lot at the end of the region
    getEditor().getCaretModel().moveToOffset(38);
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(87, 98));
  }

  public void testNavigationDataUpdateOnBackspace() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByText(DartFileType.INSTANCE, "import 'dart:core';\n" +
                                                     "import 'dart:core';\n" +
                                                     "import 'dart:core';\n");
    myFixture.doHighlighting();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();

    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));

    // backspace in the middle of the region
    getEditor().getCaretModel().moveToOffset(29);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(46, 57));

    undoAndUpdateHighlighting(file);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    // backspace at the beginning of the region
    getEditor().getCaretModel().moveToOffset(27);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(26, 37), TextRange.create(46, 57));

    undoAndUpdateHighlighting(file);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    // backspace at the end of the region
    getEditor().getCaretModel().moveToOffset(38);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(27, 38),*/ TextRange.create(46, 57));
  }

  public void testNavigationDataUpdateOnSelectionDelete() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.configureByText(DartFileType.INSTANCE, "import 'dart:core';\n" +
                                                     "import 'dart:core';\n" +
                                                     "import 'dart:core';\n");
    myFixture.doHighlighting();

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = getFile().getVirtualFile();

    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));

    // delete exactly the region
    getEditor().getSelectionModel().setSelection(27, 38);
    getEditor().getCaretModel().moveToOffset(38);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(26, 37),*/ TextRange.create(36, 47));

    undoAndUpdateHighlighting(file);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    // delete selection in the middle of the region
    getEditor().getSelectionModel().setSelection(29, 36);
    getEditor().getCaretModel().moveToOffset(36);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(40, 51));

    undoAndUpdateHighlighting(file);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    // delete selection that includes region and selection start/end touch other regions
    getEditor().getSelectionModel().setSelection(18, 47);
    getEditor().getCaretModel().moveToOffset(47);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), /*TextRange.create(28, 39),*/ TextRange.create(18, 29));

    undoAndUpdateHighlighting(file);
    checkRegions(service.getNavigation(file), TextRange.create(7, 18), TextRange.create(27, 38), TextRange.create(47, 58));
    // delete selection that has start in one region and end in another
    getEditor().getSelectionModel().setSelection(17, 28);
    getEditor().getCaretModel().moveToOffset(28);
    myFixture.type('\b');
    checkRegions(service.getNavigation(file), /*TextRange.create(7, 18), TextRange.create(28, 39),*/ TextRange.create(36, 47));
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
    checkRegions(regions, TextRange.create(7, 8), TextRange.create(18, 19), TextRange.create(22, 23));
    assertEquals(7, regions.get(2).getTargets().get(0).getOffset());
  }
}
