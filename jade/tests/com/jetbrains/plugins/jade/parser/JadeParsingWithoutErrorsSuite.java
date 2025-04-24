// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.parser;

import com.intellij.idea.IgnoreJUnit3;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.TestDataFile;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ThrowableRunnable;
import com.jetbrains.plugins.jade.JadeBaseParsingTestCase;
import com.jetbrains.plugins.jade.JadeTestUtil;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

@IgnoreJUnit3(reason = "meant to be run manually")
public class JadeParsingWithoutErrorsSuite extends TestSuite {
  private static final long TIMEOUT = 60;

  public JadeParsingWithoutErrorsSuite() throws IOException {
    File sourceRoot = null;

    final List<File> oldTemp = FileUtil.findFilesOrDirsByMask(Pattern.compile("pug-test.*"), new File(FileUtilRt.getTempDirectory()));
    for (File tmpDir : oldTemp) {
      if (!FileUtil.findFilesByMask(Pattern.compile(".*\\.pug"), tmpDir).isEmpty()) {
        sourceRoot = tmpDir;
        break;
      }
    }

    if (sourceRoot == null) {
      sourceRoot = fetchJadeSourcesToTmp();
    }

    Assert.assertNotNull(sourceRoot);
    File[] children = sourceRoot.listFiles((dir, name) -> name.endsWith(".pug"));

    for (File child : children) {
      addTest(new JadeLightParserTest(child));
    }
  }

  private static File fetchJadeSourcesToTmp() throws IOException {
    final File sourceRoot = FileUtil.createTempDirectory("pug-test", null, false);

    final String pathToScript = JadeTestUtil.getContribPath() +
                                "/jade/tests/com/jetbrains/plugins/jade/parser/fetch_jade.sh";

    final Future<?> future = ApplicationManager.getApplication().executeOnPooledThread(() -> {
      Process exec;
      try {
        exec = Runtime.getRuntime().exec(new String[]{"/bin/sh", pathToScript}, ArrayUtilRt.EMPTY_STRING_ARRAY, sourceRoot);
      }
      catch (IOException e) {
        e.printStackTrace();
        return;
      }

      try {
        exec.waitFor();
      }
      catch (InterruptedException ignore) {
      }
    });

    try {
      future.get(TIMEOUT, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      throw new RuntimeException("Interrupted?!", e);
    }
    catch (java.util.concurrent.ExecutionException e) {
      throw new RuntimeException(e);
    }
    catch (TimeoutException e) {
      throw new RuntimeException("Cancelled by timeout", e);
    }
    finally {
      future.cancel(true);
    }
    return sourceRoot;
  }

  public static Test suite() throws IOException {
    return new JadeParsingWithoutErrorsSuite();
  }

  @SuppressWarnings({"JUnitTestCaseWithNoTests", "JUnitMalformedDeclaration"})
  private static class JadeLightParserTest extends JadeBaseParsingTestCase {
    private final File myFile;
    private final String myFileName;

    JadeLightParserTest(File file) {
      super();
      myFile = file;
      myFileName = file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    @NotNull
    @Override
    protected String getTestName(boolean lowercaseFirstLetter) {
      return myFileName;
    }

    @Override
    public String getName() {
      return myFileName;
    }

    @Override
    public void runTestRunnable(@NotNull ThrowableRunnable<Throwable> testRunnable) {
      try {
        setUp();
        doTest(false);
        tearDown();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected void doTest(boolean checkResult) {
      try {
        String text = FileUtil.loadFile(myFile, CharsetToolkit.UTF8, true).trim();
        PsiFile psiFile = createPsiFile(getTestName(true), text);
        ensureParsed(psiFile);
        assertEquals("light virtual file text mismatch", text, ((LightVirtualFile)psiFile.getVirtualFile()).getContent().toString());
        assertEquals("virtual file text mismatch", text, LoadTextUtil.loadText(psiFile.getVirtualFile()));
        //noinspection ConstantConditions
        assertEquals("doc text mismatch", text, psiFile.getViewProvider().getDocument().getText());
        assertEquals("psi text mismatch", text, psiFile.getText());
        checkResult("", psiFile);
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }

    @Override
    protected void checkResult(@NotNull @NonNls @TestDataFile String targetDataName, @NotNull final PsiFile file) {
      file.accept(new PsiElementVisitor() {
        @Override
        public void visitElement(@NotNull PsiElement element) {
          assertNonErrorElement(element);
          element.acceptChildren(this);
        }

        private void assertNonErrorElement(PsiElement element) {
          if (element instanceof PsiErrorElement) {
            final String message = "Element: " + element.getText() + ": " + ((PsiErrorElement)element).getErrorDescription();
            // always failing
            assertSameLinesWithFile(myFile.getAbsolutePath(), message);
          }
        }
      });
    }
  }

}

