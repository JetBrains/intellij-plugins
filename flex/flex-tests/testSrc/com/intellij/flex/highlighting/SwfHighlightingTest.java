package com.intellij.flex.highlighting;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.EditorInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class SwfHighlightingTest extends JSDaemonAnalyzerTestCase {

  protected Runnable myAfterCommitRunnable = null;

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    myAfterCommitRunnable = null;
  }

  @Override
  protected void tearDown() throws Exception {
    myAfterCommitRunnable = null;
    super.tearDown();
  }

  @Override
  protected String getBasePath() {
    return "";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(FlexHighlightingTest.BASE_PATH);
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  @Override
  protected void doCommitModel(@NotNull final ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    FlexTestUtils.setupFlexLib(myProject, getClass(), getTestName(false));
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  @NotNull
  @Override
  protected List<HighlightInfo> doHighlighting() {
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();

    TIntArrayList toIgnore = new TIntArrayList();
    toIgnore.add(Pass.EXTERNAL_TOOLS);
    toIgnore.add(Pass.LOCAL_INSPECTIONS);
    toIgnore.add(Pass.WHOLE_FILE_LOCAL_INSPECTIONS);
    toIgnore.add(Pass.POPUP_HINTS);
    return CodeInsightTestFixtureImpl.instantiateAndRun(getFile(), getEditor(), toIgnore.toNativeArray(), false);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithLineMarkers})
  public void testLineMarkersInSwf() throws Exception {
    final String testName = getTestName(false);
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "lib", getTestDataPath() + getBasePath() + "/", testName + ".swc", null, null);
    configureByFile("/" + testName + ".as"); // actual test data is in library.swf; this file is here just because we need any file
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + ".swc");
    vFile = JarFileSystem.getInstance().getJarRootForLocalFile(vFile).findChild("library.swf");
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, vFile, 0), false);
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

    myFile = myPsiManager.findFile(vFile);
    ((EditorImpl)myEditor).setCaretActive();

    vFile = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + ".as");
    final String verificationText = StreamUtil.convertSeparators(VfsUtilCore.loadText(vFile));
    checkHighlighting(new ExpectedHighlightingData(new DocumentImpl(verificationText), false, false, true, myFile));
  }

  public void testProtectSwf() {
    configureByFiles((String)null);
    VirtualFile vFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + getTestName(false) + ".swf");
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, vFile, 0), false);
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

    myFile = myPsiManager.findFile(vFile);
    ((EditorImpl)myEditor).setCaretActive();
    assertFalse(FileDocumentManager.getInstance().requestWriting(myEditor.getDocument(), myProject));
  }

  @NotNull
  @Override
  protected List<Editor> openEditorsAndActivateLast(@NotNull Map<VirtualFile, EditorInfo> editorInfos) {
    // we're not going to open any editors in configureByFiles()
    return Collections.emptyList();
  }


  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
}
