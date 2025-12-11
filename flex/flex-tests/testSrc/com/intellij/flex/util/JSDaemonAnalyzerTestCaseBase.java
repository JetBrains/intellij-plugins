// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.util;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.EditorInfo;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.preview.IntentionPreviewPopupUpdateProcessor;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.platform.testFramework.core.FileComparisonFailedError;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil;
import com.intellij.testFramework.*;
import com.intellij.testFramework.common.EditorCaretTestUtil;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;

import static com.intellij.lang.javascript.JSTestUtils.configureRecursionAssertions;

/**
 * @author Konstantin.Ulitin
 */
public abstract class JSDaemonAnalyzerTestCaseBase extends HeavyPlatformTestCase {
  protected Editor myEditor;
  protected PsiFile myFile;
  protected PsiManagerEx myPsiManager;

  protected final Collection<Module> myModulesToDispose = new ArrayList<>();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final LocalInspectionTool[] tools = configureLocalInspectionTools();

    InspectionsKt.configureInspections(tools, getProject(), getTestRootDisposable());

    DaemonCodeAnalyzerImpl daemonCodeAnalyzer = (DaemonCodeAnalyzerImpl)DaemonCodeAnalyzer.getInstance(getProject());
    daemonCodeAnalyzer.prepareForTest();
    DaemonCodeAnalyzerSettings.getInstance().setImportHintEnabled(false);

    myPsiManager = PsiManagerEx.getInstanceEx(myProject);
    myModulesToDispose.clear();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (!myModulesToDispose.isEmpty()) {
        WriteAction.run(() -> {
          ModuleManager moduleManager = ModuleManager.getInstance(myProject);
          for (Module module : myModulesToDispose) {
            try {
              String moduleName = module.getName();
              if (moduleManager.findModuleByName(moduleName) != null) {
                moduleManager.disposeModule(module);
              }
            }
            catch (Throwable e) {
              addSuppressedException(e);
            }
          }
        });
      }

      if (myProject != null) {
        FileEditorManagerEx.getInstanceEx(myProject).closeAllFiles();
      }
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      myModulesToDispose.clear();
      myPsiManager = null;
      myEditor = null;
      myFile = null;
      super.tearDown();
    }
  }

  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return LocalInspectionTool.EMPTY_ARRAY;
  }

  public static String getTestDataPathStatic() {
    return JSTestUtils.getTestDataPath();
  }

  protected abstract String getBasePath();

  /**
   * @return file extension without "."
   */
  protected abstract String getExtension();

  protected Collection<HighlightInfo> defaultTest() {
    return defaultTest(useAssertOnRecursionPreventionByDefault());
  }

  protected Collection<HighlightInfo> defaultTestForTwoFiles() {
    return defaultTestForTwoFiles(useAssertOnRecursionPreventionByDefault());
  }

  protected Collection<HighlightInfo> defaultTest(boolean preventRecursion) {
    return doTestWithExplicitAssertOnRecursion(preventRecursion, true, getTestName(false) + "." + getExtension());
  }

  protected boolean useAssertOnRecursionPreventionByDefault() {
    return false;
  }

  protected Collection<HighlightInfo> defaultTestForNFiles(boolean preventRecursion, int fileCount) {
    String[] fileNames = new String[fileCount];
    String testName = getTestName(false);
    String ext = getExtension();
    fileNames[0] = testName + "." + ext;
    for (int i = 1; i < fileCount; i++) {
      fileNames[i] = testName + "_" + (i+1) + "." + ext;
    }

    return doTestWithExplicitAssertOnRecursion(preventRecursion, true, fileNames);
  }

  protected Collection<HighlightInfo> defaultTestForTwoFiles(boolean preventRecursion) {
    return defaultTestForNFiles(preventRecursion, 2);
  }

  protected Collection<HighlightInfo> doTestFor(boolean checkWeakWarnings, @NonNls String... fileNames) {
    return doTestWithExplicitAssertOnRecursion(useAssertOnRecursionPreventionByDefault(), checkWeakWarnings, fileNames);
  }

  protected Collection<HighlightInfo> doTestWithExplicitAssertOnRecursion(boolean assertOnRecursion,
                                                                          boolean checkWeakWarnings,
                                                                          @NonNls String... fileNames) {
    if (assertOnRecursion) {
      configureRecursionAssertions(this);
    }
    return doTestFor(checkWeakWarnings, null, fileNames);
  }


  protected Collection<HighlightInfo> doTestFor(boolean checkWeakWarnings, @Nullable Runnable action, @NonNls String... fileNames) {
    return doTestFor(checkWeakWarnings, null, action, fileNames);
  }

  protected Collection<HighlightInfo> doTestFor(boolean checkWeakWarnings,
                                                File projectRoot,
                                                @Nullable final Runnable action,
                                                @NonNls String... fileNames) {
    return doTestFor(checkWeakWarnings, projectRoot, highlightInfos -> {
      if (action != null) action.run();
      return null;
    }, fileNames);
  }

  protected Collection<HighlightInfo> doTestFor(boolean checkWeakWarnings,
                                                File root,
                                                Function<? super Collection<HighlightInfo>, Void> function,
                                                String... fileNames)  {
    final VirtualFile[] files = new VirtualFile[fileNames != null && fileNames.length != 0 ? fileNames.length : 1];
    if (fileNames == null || fileNames.length == 0) fileNames = new String[]{getTestName(false) + "." + getExtension()};
    for (int i = 0; i < files.length; ++i) {
      files[i] = findVirtualFile(getBasePath() + "/" + fileNames[i]);
    }
    try {
      configureByFiles(root, files);
    } catch (IOException e) {
      addSuppressedException(e);
    }
    JSTestUtils.buildJSFileGists(getProject());
    boolean hasInfos = JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithLineMarkers) ||
                       JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithInfos);
    return doDoTest(!JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithoutWarnings), hasInfos,
                    checkWeakWarnings);
  }

  protected void doTest(@NonNls @NotNull String filePath, boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings)
    throws Exception {
    configureByFile(filePath);
    doDoTest(checkWarnings, checkInfos, checkWeakWarnings);
  }

  protected void doTest(@NonNls @NotNull String filePath, boolean checkWarnings, boolean checkInfos) throws Exception {
    doTest(filePath, checkWarnings, checkInfos, false);
  }

  protected void doTest(@NonNls @NotNull String filePath, @NonNls String projectRoot, boolean checkWarnings, boolean checkInfos)
    throws Exception {
    configureByFile(filePath, projectRoot);
    doDoTest(checkWarnings, checkInfos);
  }

  @NotNull
  protected String getTestDataPath() {
    return getTestDataPathStatic();
  }

  protected Collection<HighlightInfo> doSimpleHighlightingWithInvokeFixAndCheckResult(String fixName, String... files) throws Exception {
    return doHighlightingWithInvokeFixAndCheckResult(fixName, getExtension(), files);
  }

  protected Collection<HighlightInfo> doHighlightingWithInvokeFixAndCheckResult(String fixName, String ext, String... files)
    throws Exception {
    if (files.length == 0) files = new String[]{getTestName(false) + "." + ext};
    final Collection<HighlightInfo> infoCollection = doTestFor(true, files);
    findAndInvokeActionWithExpectedCheck(fixName, ext, infoCollection);
    return infoCollection;
  }

  protected void findAndInvokeActionWithExpectedCheck(String fixName, String ext, Collection<HighlightInfo> infoCollection)
    throws Exception {
    String afterFilePath = getBasePath() + "/" + getTestName(false) + "_after." + ext;
    if (checkPreview()) {
      IntentionAction action = ContainerUtil.find(
        getIntentionActions(infoCollection, myEditor, myFile),
        a -> a.getText().equals(fixName)
      );
      Assert.assertNotNull(action);
      String previewText  = IntentionPreviewPopupUpdateProcessor.getPreviewText(getProject(), action, myFile, myEditor);
      String expectedText = getFileTextWithoutCaretAndSelection(afterFilePath);
      Assert.assertEquals(expectedText, previewText);
    }
    findAndInvokeIntentionAction(infoCollection, fixName, myEditor, myFile);
    checkResultByFile(afterFilePath);
  }

  protected boolean checkPreview() {
    return false;
  }

  protected boolean isAddDirToContentRoot() {
    return true;
  }

  protected boolean isAddDirToSource() {
    return true;
  }

  protected boolean clearModelBeforeConfiguring() {
    return false;
  }

  protected boolean isAddDirToTests() {
    return false;
  }

  protected void sourceRootAdded(final VirtualFile dir) {
  }

  private String getFileTextWithoutCaretAndSelection(String filePath) throws IOException {
      String expectedText = VfsUtilCore.loadText(findVirtualFile(filePath));
      expectedText = StringUtil.convertLineSeparators(expectedText);
      Document document = EditorFactory.getInstance().createDocument(expectedText);
      EditorTestUtil.extractCaretAndSelectionMarkers(document);
      return document.getText();
  }

  protected @NotNull VirtualFile findVirtualFile(@NotNull String filePath) {
    String absolutePath = getTestDataPath() + filePath;
    VfsRootAccess.allowRootAccess(getTestRootDisposable(), absolutePath);
    return VfsTestUtil.findFileByCaseSensitivePath(absolutePath);
  }

  protected static @Nullable IntentionAction findIntentionAction(@NotNull Collection<? extends HighlightInfo> infos,
                                                                 @NotNull String intentionActionName,
                                                                 @NotNull Editor editor,
                                                                 @NotNull PsiFile psiFile) {
    List<IntentionAction> actions = getIntentionActions(infos, editor, psiFile);
    return findActionWithText(actions, intentionActionName);
  }

  protected static void findAndInvokeIntentionAction(@NotNull Collection<? extends HighlightInfo> infos,
                                                     @NotNull String intentionActionName,
                                                     @NotNull Editor editor,
                                                     @NotNull PsiFile psiFile) {
    List<IntentionAction> actions = getIntentionActions(infos, editor, psiFile);
    IntentionAction intentionAction = findActionWithText(actions, intentionActionName);

    if (intentionAction == null) {
      fail("Could not find action '" + intentionActionName +
           "'.\nAvailable actions: [" + StringUtil.join(ContainerUtil.map(actions, c -> c.getText()), ", ") + "]\n" +
           "HighlightInfos: [" + StringUtil.join(infos, ", ") + "]");
    }
    CodeInsightTestFixtureImpl.invokeIntention(intentionAction, psiFile, editor);
  }

  protected static @NotNull @Unmodifiable List<IntentionAction> getIntentionActions(@NotNull Collection<? extends HighlightInfo> infos,
                                                                                    @NotNull Editor editor,
                                                                                    @NotNull PsiFile psiFile) {
    List<IntentionAction> actions = getAvailableActions(editor, psiFile);

    final List<IntentionAction> quickFixActions = new ArrayList<>();
    for (HighlightInfo info : infos) {
      info.findRegisteredQuickFix((descriptor, range) -> {
        IntentionAction action = descriptor.getAction();
        if (!actions.contains(action) && action.isAvailable(psiFile.getProject(), editor, psiFile)) {
          quickFixActions.add(action);
        }
        return null;
      });
    }
    return ContainerUtil.concat(actions, quickFixActions);
  }

  protected @Unmodifiable List<IntentionAction> getAvailableActions() {
    doHighlighting();
    return getAvailableActions(getEditor(), getFile());
  }

  public static @NotNull @Unmodifiable List<IntentionAction> getAvailableActions(@NotNull Editor editor, @NotNull PsiFile file) {
    return CodeInsightTestFixtureImpl.getAvailableIntentions(editor, file);
  }

  public static IntentionAction findActionWithText(@NotNull List<? extends IntentionAction> actions, @NotNull String text) {
    for (IntentionAction action : actions) {
      if (text.equals(action.getText())) {
        return action;
      }
    }
    return null;
  }

  protected void checkResultByFile(@NotNull String filePath) throws Exception {
    checkResultByFile(filePath, false);
  }

  protected void checkResultByFile(final @NotNull String filePath, final boolean stripTrailingSpaces) throws Exception {
    WriteCommandAction.writeCommandAction(getProject()).run(() -> {
      PostprocessReformattingAspect.getInstance(getProject()).doPostponedFormatting();
      if (stripTrailingSpaces) {
        ((DocumentImpl)myEditor.getDocument()).stripTrailingSpaces(getProject());
      }

      PsiDocumentManager.getInstance(myProject).commitAllDocuments();

      VirtualFile vFile = findVirtualFile(filePath);

      VfsTestUtil.assertFilePathEndsWithCaseSensitivePath(vFile, filePath);
      String expectedText;
      try {
        expectedText = VfsUtilCore.loadText(vFile);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }

      expectedText = StringUtil.convertLineSeparators(expectedText);
      Document document = EditorFactory.getInstance().createDocument(expectedText);

      EditorCaretTestUtil.CaretAndSelectionState caretState = EditorTestUtil.extractCaretAndSelectionMarkers(document);

      expectedText = document.getText();
      if (stripTrailingSpaces) {
        Document document1 = EditorFactory.getInstance().createDocument(expectedText);
        ((DocumentImpl)document1).stripTrailingSpaces(getProject());
        expectedText = document1.getText();
      }

      myEditor = InjectedLanguageEditorUtil.getTopLevelEditor(myEditor);
      myFile = PsiDocumentManager.getInstance(getProject()).getPsiFile(myEditor.getDocument());

      String actualText = StringUtil.convertLineSeparators(myFile.getText());
      if (!Objects.equals(expectedText, actualText)) {
        throw new FileComparisonFailedError("Text mismatch in file " + filePath, expectedText, actualText, vFile.getPath());
      }

      EditorTestUtil.verifyCaretAndSelectionState(myEditor, caretState);
    });
  }

  protected void configureByExistingFile(final @NotNull VirtualFile virtualFile) {
    myFile = null;
    myEditor = null;

    final Editor editor = createEditor(virtualFile);

    final Document document = editor.getDocument();
    final EditorInfo editorInfo = new EditorInfo(document.getText());

    final String newFileText = editorInfo.getNewFileText();
    ApplicationManager.getApplication().runWriteAction(() -> {
      if (!document.getText().equals(newFileText)) {
        document.setText(newFileText);
      }

      PsiFile file = myPsiManager.findFile(virtualFile);
      if (myFile == null) myFile = file;

      if (myEditor == null) myEditor = editor;

      editorInfo.applyToEditor(editor);
    });


    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    IndexingTestUtil.waitUntilIndexesAreReady(getProject());
  }

  /**
   * @param files the first file will be loaded in editor
   */
  protected VirtualFile configureByFiles(@Nullable String projectRoot, String @NotNull ... files) {
    if (files.length == 0) return null;
    final VirtualFile[] vFiles = new VirtualFile[files.length];
    for (int i = 0; i < files.length; i++) {
      vFiles[i] = findVirtualFile(files[i]);
      if (vFiles[i] != null) {
        VfsTestUtil.assertFilePathEndsWithCaseSensitivePath(vFiles[i], files[i]);
      }
    }

    File projectFile = projectRoot == null ? null : new File(getTestDataPath() + projectRoot);

    try {
      return configureByFiles(projectFile, vFiles);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected VirtualFile configureByFiles(@Nullable File rawProjectRoot, VirtualFile @NotNull ... vFiles) throws IOException {
    myFile = null;
    myEditor = null;

    VirtualFile toDir = createVirtualDirectoryForContentFile();

    Map<VirtualFile, EditorInfo> editorInfos = WriteAction.compute(() -> {
      try {
        final ModuleRootManager rootManager = ModuleRootManager.getInstance(myModule);
        final ModifiableRootModel rootModel = rootManager.getModifiableModel();
        if (clearModelBeforeConfiguring()) {
          rootModel.clear();
        }

        // auxiliary files should be copied first
        VirtualFile[] reversed = ArrayUtil.reverseArray(vFiles);
        Map<VirtualFile, EditorInfo> editorInfos1;
        if (rawProjectRoot != null) {
          FileUtil.copyDir(rawProjectRoot, toDir.toNioPath().toFile());
          File projectRoot = rawProjectRoot.getCanonicalFile();
          VirtualFile aNull = Objects.requireNonNull(LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectRoot));
          editorInfos1 = copyFilesFillingEditorInfos(aNull, toDir, ContainerUtil.map2Array(reversed, String.class, s -> {
            return s.getPath().substring(projectRoot.getPath().length());
          }));

          toDir.refresh(false, true);
        }
        else {
          editorInfos1 = new LinkedHashMap<>();
          for (VirtualFile vFile : reversed) {
            VirtualFile parent = vFile.getParent();
            assert parent.isDirectory() : parent;
            editorInfos1.putAll(copyFilesFillingEditorInfos(parent, toDir, vFile.getName()));
          }
        }

        boolean sourceRootAdded = false;
        if (isAddDirToContentRoot()) {
          final ContentEntry contentEntry = rootModel.addContentEntry(toDir);
          if (isAddDirToSource()) {
            sourceRootAdded = true;
            contentEntry.addSourceFolder(toDir, isAddDirToTests());
          }
        }
        doCommitModel(rootModel);
        if (sourceRootAdded) {
          sourceRootAdded(toDir);
        }

        return editorInfos1;
      }
      catch (IOException e) {
        LOG.error(e);
        return null;
      }
    });
    IndexingTestUtil.waitUntilIndexesAreReady(myProject);

    if (editorInfos != null) {
      List<Editor> list = openEditors(editorInfos);
      setActiveEditor(ContainerUtil.getLastItem(list));
    }

    return toDir;
  }

  protected void configureByFile(String filePath) throws Exception {
    configureByFile(filePath, null);
  }

  protected VirtualFile configureByFile(@NotNull String filePath, @Nullable String projectRoot) throws Exception {
    VirtualFile vFile = findVirtualFile(filePath);
    File projectFile = projectRoot == null ? null : new File(getTestDataPath() + projectRoot);

    return configureByFile(vFile, projectFile);
  }

  protected VirtualFile configureByFile(@NotNull VirtualFile vFile, File projectRoot) throws IOException {
    return configureByFiles(projectRoot, vFile);
  }

  protected @NotNull Map<VirtualFile, EditorInfo> copyFilesFillingEditorInfos(@NotNull VirtualFile fromDir,
                                                                              @NotNull VirtualFile toDir,
                                                                              String @NotNull ... relativePaths) throws IOException {
    Map<VirtualFile, EditorInfo> editorInfos = new LinkedHashMap<>();

    List<OutputStream> streamsToClose = new ArrayList<>();

    for (String relativePath : relativePaths) {
      relativePath = StringUtil.trimStart(relativePath, "/");
      final VirtualFile fromFile = fromDir.findFileByRelativePath(relativePath);
      assertNotNull(fromDir.getPath() + "/" + relativePath, fromFile);
      VirtualFile toFile = toDir.findFileByRelativePath(relativePath);
      if (toFile == null) {
        final File file = new File(toDir.getPath(), relativePath);
        FileUtil.createIfDoesntExist(file);
        toFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        assertNotNull(file.getCanonicalPath(), toFile);
      }
      toFile.putUserData(VfsTestUtil.TEST_DATA_FILE_PATH, FileUtil.toSystemDependentName(fromFile.getPath()));
      editorInfos.put(toFile, copyContent(fromFile, toFile, streamsToClose));
    }

    for (int i = streamsToClose.size() - 1; i >= 0; --i) {
      streamsToClose.get(i).close();
    }
    return editorInfos;
  }

  private EditorInfo copyContent(@NotNull VirtualFile from, @NotNull VirtualFile to, @NotNull List<? super OutputStream> streamsToClose)
    throws IOException {
    byte[] content = from.getFileType().isBinary() ? from.contentsToByteArray() : null;
    final String fileText = from.getFileType().isBinary() ? null : StringUtil.convertLineSeparators(VfsUtilCore.loadText(from));

    EditorInfo editorInfo = fileText == null ? null : new EditorInfo(fileText);
    String newFileText = fileText == null ? null : editorInfo.getNewFileText();
    doWrite(newFileText, to, content, streamsToClose);
    return editorInfo;
  }

  private void doWrite(final String newFileText,
                       @NotNull VirtualFile newVFile,
                       byte[] content,
                       @NotNull List<? super OutputStream> streamsToClose) throws IOException {
    if (newFileText == null) {
      final OutputStream outputStream = newVFile.getOutputStream(this, -1, -1);
      outputStream.write(content);
      streamsToClose.add(outputStream);
    }
    else {
      setFileText(newVFile, newFileText);
    }
  }

  protected final @Unmodifiable @NotNull List<Editor> openEditors(@NotNull Map<VirtualFile, EditorInfo> editorInfos) {
    return ContainerUtil.map(editorInfos.keySet(), newVFile -> {
      PsiFile file = myPsiManager.findFile(newVFile);
      if (myFile == null) myFile = file;

      Editor editor = createEditor(newVFile);
      if (myEditor == null) myEditor = editor;

      EditorInfo editorInfo = editorInfos.get(newVFile);
      if (editorInfo != null) {
        editorInfo.applyToEditor(editor);
      }
      return editor;
    });
  }

  protected Editor createEditor(@NotNull VirtualFile file) {
    final FileEditorManager instance = FileEditorManager.getInstance(myProject);

    if (file.getFileType().isBinary()) return null;
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    Editor editor = instance.openTextEditor(new OpenFileDescriptor(myProject, file, 0), false);
    ((EditorImpl)editor).setCaretActive();
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    DaemonCodeAnalyzerEx.getInstanceEx(getProject()).restart("JavaCodeInsightTestCase.createEditor " + file);

    return editor;
  }

  protected final void setActiveEditor(@NotNull Editor editor) {
    myEditor = editor;
    myFile = getPsiFile(editor.getDocument());
  }

  protected @NotNull VirtualFile createVirtualDirectoryForContentFile() {
    return getTempDir().createVirtualDir();
  }

  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    rootModel.commit();
    IndexingTestUtil.waitUntilIndexesAreReady(getProject());
  }

  protected @Unmodifiable Collection<HighlightInfo> doDoTest(final boolean checkWarnings,
                                                             final boolean checkInfos,
                                                             final boolean checkWeakWarnings) {
    return ContainerUtil.filter(
      checkHighlighting(new ExpectedHighlightingData(myEditor.getDocument(), checkWarnings, checkWeakWarnings, checkInfos)),
      info -> info.getSeverity() == HighlightSeverity.INFORMATION && checkInfos ||
              info.getSeverity() == HighlightSeverity.WARNING && checkWarnings ||
              info.getSeverity() == HighlightSeverity.WEAK_WARNING && checkWeakWarnings ||
              info.getSeverity().compareTo(HighlightSeverity.WARNING) > 0);
  }

  protected @NotNull @Unmodifiable Collection<HighlightInfo> doDoTest(boolean checkWarnings, boolean checkInfos) {
    return doDoTest(checkWarnings, checkInfos, false);
  }

  protected @NotNull Collection<HighlightInfo> checkHighlighting(final @NotNull ExpectedHighlightingData data) {
    data.init();
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();

    VirtualFileFilter virtualFileFilter = JSTestUtils.createJSFileTreeAccessFilter(this::getFile, this);
    PsiManagerEx.getInstanceEx(getProject()).setAssertOnFileLoadingFilter(virtualFileFilter, getTestRootDisposable());

    try {
      Collection<HighlightInfo> infos = doHighlighting();

      String text = myEditor.getDocument().getText();
      doCheckResult(data, infos, text);
      return infos;
    }
    finally {
      PsiManagerEx.getInstanceEx(getProject()).setAssertOnFileLoadingFilter(VirtualFileFilter.NONE, getTestRootDisposable());
    }
  }

  private static @NotNull @Unmodifiable List<HighlightInfo> filter(@NotNull List<? extends HighlightInfo> infos,
                                                                   @NotNull HighlightSeverity minSeverity) {
    return ContainerUtil.filter(infos, info -> info.getSeverity().compareTo(minSeverity) >= 0);
  }

  protected final @NotNull @Unmodifiable List<HighlightInfo> doHighlighting(@NotNull HighlightSeverity minSeverity) {
    return filter(doHighlighting(), minSeverity);
  }

  protected final @NotNull @Unmodifiable List<HighlightInfo> doHighlighting() {
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();

    IntList toIgnore = new IntArrayList();
    if (!doTestLineMarkers()) {
      toIgnore.add(Pass.LINE_MARKERS);
      toIgnore.add(Pass.SLOW_LINE_MARKERS);
    }

    if (!doExternalValidation()) {
      toIgnore.add(Pass.EXTERNAL_TOOLS);
    }
    if (forceExternalValidation()) {
      toIgnore.add(Pass.LINE_MARKERS);
      toIgnore.add(Pass.SLOW_LINE_MARKERS);
      toIgnore.add(Pass.LOCAL_INSPECTIONS);
      toIgnore.add(Pass.POPUP_HINTS);
      toIgnore.add(Pass.UPDATE_ALL);
    }

    List<HighlightInfo> infos = CodeInsightTestFixtureImpl.instantiateAndRun(getFile(), getEditor(), toIgnore.toIntArray(), false, true);

    Document document = getDocument(getFile());
    DaemonCodeAnalyzerEx daemonCodeAnalyzer = DaemonCodeAnalyzerEx.getInstanceEx(myProject);
    daemonCodeAnalyzer.getFileStatusMap().assertAllDirtyScopesAreNull(document);

    return infos;
  }

  public Document getDocument(@NotNull PsiFile file) {
    return PsiDocumentManager.getInstance(getProject()).getDocument(file);
  }

  public Document getDocument(@NotNull VirtualFile file) {
    return FileDocumentManager.getInstance().getDocument(file);
  }

  public PsiFile getFile() {
    return myFile;
  }

  public Editor getEditor() {
    return myEditor;
  }

  protected boolean doTestLineMarkers() {
    return false;
  }

  protected boolean doExternalValidation() {
    return true;
  }

  protected boolean forceExternalValidation() {
    return false;
  }

  protected void doCheckResult(@NotNull ExpectedHighlightingData data,
                               @NotNull Collection<? extends HighlightInfo> infos,
                               @NotNull String text) {
    PsiFile psiFile = getFile();
    ActionUtil.underModalProgress(myProject, "", () -> {
      //line marker tooltips are called in BGT in production
      data.checkLineMarkers(psiFile, DaemonCodeAnalyzerImpl.getLineMarkers(getDocument(psiFile), getProject()), text);
      return null;
    });
    data.checkResult(psiFile, infos, text);
  }

  protected final void enableInspectionTool(@NotNull InspectionProfileEntry tool) {
    InspectionsKt.enableInspectionTool(getProject(), tool, getTestRootDisposable());
  }

  protected void enableInspectionTools(InspectionProfileEntry @NotNull ... tools) {
    InspectionsKt.enableInspectionTools(getProject(), getTestRootDisposable(), tools);
  }

  protected void disableInspectionTool(@NotNull String shortName) {
    InspectionProfileImpl profile = InspectionProjectProfileManager.getInstance(getProject()).getCurrentProfile();
    if (profile.getInspectionTool(shortName, getProject()) != null) {
      profile.setToolEnabled(shortName, false);
    }
  }

  protected final @NotNull com.intellij.openapi.module.Module createModuleFromTestData(@NotNull String dirInTestData,
                                                                                       @NotNull String newModuleFileName,
                                                                                       @NotNull ModuleType<?> moduleType,
                                                                                       boolean addSourceRoot)
    throws IOException {
    VirtualFile moduleDir = getTempDir().createVirtualDir();
    FileUtil.copyDir(new File(dirInTestData), moduleDir.toNioPath().toFile());
    moduleDir.refresh(false, true);
    Module module = createModule(moduleDir.toNioPath().resolve(newModuleFileName), moduleType);
    if (addSourceRoot) {
      PsiTestUtil.addSourceContentToRoots(module, moduleDir);
    }
    else {
      PsiTestUtil.addContentRoot(module, moduleDir);
    }
    return module;
  }

  protected @NotNull Module createModule(@NotNull Path moduleFile, @NotNull ModuleType<?> moduleType) {
    Module module = WriteAction.compute(() -> ModuleManager.getInstance(myProject).newModule(moduleFile, moduleType.getId()));
    myModulesToDispose.add(module);
    IndexingTestUtil.waitUntilIndexesAreReady(getProject());
    return module;
  }
}
