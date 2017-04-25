package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.quickFix.LightQuickFixTestCase;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class ActionScriptStubsTest extends ActionScriptDaemonAnalyzerTestCase {
  protected List<PsiFile> myPsiFiles = new ArrayList<>();

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    myPsiFiles.clear();
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
  protected boolean enableJSIndex() {
    return false; // otherwise all the files will be parsed when building
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  private void doTest(@Nullable final ThrowableRunnable<Exception> runnable, String... files) throws Exception {
    Runnable r = runnable != null ? () -> {
      try {
        runnable.run();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    } : null;
    doTestFor(true, r, files);
    assertNotParsed(myPsiFiles.subList(1, myPsiFiles.size())); // the first one was parsed during highlighting

    // we need to go though files open in editors
    assertNotParsed(ContainerUtil.mapNotNull(FileEditorManager.getInstance(myProject).getOpenFiles(), virtualFile -> {
      if (Comparing.equal(virtualFile, myFile.getVirtualFile())) {
        return null; // this one is opened in editor
      }
      Document document = ((TextEditor)FileEditorManager.getInstance(myProject).getSelectedEditor(virtualFile)).getEditor().getDocument();
      return PsiDocumentManager.getInstance(myProject).getPsiFile(document);
    }));
  }

  @Override
  protected VirtualFile configureByFiles(final File projectRoot, @NotNull final VirtualFile[] vFiles) throws IOException {
    VirtualFile result = super.configureByFiles(projectRoot, vFiles);
    for (VirtualFile vFile : vFiles) {
      // to have new instance of PsiFile created that will replace original that was parsed in order
      // to build stubs, this one should not be parsed and should get stub tree loaded from index
      PsiFile file = myPsiManager.findFile(vFile);
      myPsiFiles.add(file);
    }

    assertNotParsed(myPsiFiles);
    return result;
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkersIncludes1() throws Exception {
    doTest(null, getTestName(false) + ".as", getTestName(false) + "_2.as", getTestName(false) + "_3.as", getTestName(false) + "_4.as");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testCreateVariable() throws Exception {
    doTest(() -> {
      final IntentionAction action =
        LightQuickFixTestCase
          .findActionWithText(LightQuickFixTestCase.getAvailableActions(myEditor, myFile), "Create Field 'myfield'");
      CommandProcessor.getInstance().executeCommand(getProject(), () -> action.invoke(myProject, myEditor, myFile), "Create field", null);
      checkResultByFile(getBasePath() + "/" + getTestName(false) + "_after.as");
    }, getTestName(false) + ".as", "restparam.swc");
  }

  // yole: I don't know why we care whether a user-initiated operation such as "override methods" expands stubs or not
  public void _testNoParseOnMethodOverride() throws Exception {
    doTest(() -> {
      PlatformTestUtil.invokeNamedAction("OverrideMethods");
      checkResultByFile(getBasePath() + "/" + getTestName(false) + "_after.as");
    }, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  // kostya: I don't know why we care whether a user-initiated operation such as "delegate methods" expands stubs or not
  public void _testNoParseOnMethodDelegate() throws Exception {
    doTest(() -> {
      PlatformTestUtil.invokeNamedAction("DelegateMethods");
      checkResultByFile(getBasePath() + "/" + getTestName(false) + "_after.as");
    }, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  private static void assertNotParsed(Collection<PsiFile> psiFiles) {
    for (PsiFile file : psiFiles) {
      if (file instanceof PsiFileImpl) {
        assertNull("File should not be parsed before stubs test", ((PsiFileImpl)file).getTreeElement());
      }
    }
  }
}
