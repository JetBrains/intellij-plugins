// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.parser;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.lang.dtd.DTDParserDefinition;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.ParsingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.plugins.jade.JadeParserDefinition;
import com.jetbrains.plugins.jade.JadeTestUtil;
import com.jetbrains.plugins.jade.js.JavascriptInJadeParserDefinition;
import org.coffeescript.lang.parser.CoffeeScriptParserDefinition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Requires network access to GitHub; run manually")
@TestApplication
public class JadeParsingWithoutErrorsSuite {
  private static final long TIMEOUT = 60;

  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, JadeTestUtil.getBaseTestDataPath(), "jade", true,
                   new JadeParserDefinition(), new JavascriptInJadeParserDefinition(), new JavascriptParserDefinition(),
                   new XMLParserDefinition(), new DTDParserDefinition(), new CSSParserDefinition(), new CoffeeScriptParserDefinition());

  @BeforeEach
  void setUp() {
    CodeStyleSettings settings = CodeStyle.createTestSettings();
    CodeStyleSettingsManager.getInstance(projectFixture.get()).setTemporarySettings(settings);
  }

  @AfterEach
  void tearDown() {
    CodeStyleSettingsManager.getInstance(projectFixture.get()).dropTemporarySettings();
  }

  static List<Named<File>> pugFiles() throws IOException {
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

    assertNotNull(sourceRoot);
    File[] children = sourceRoot.listFiles((dir, name) -> name.endsWith(".pug"));
    return Arrays.stream(children).map(child -> Named.of(getFileName(child), child)).toList();
  }

  private static String getFileName(File file) {
    return file.getName().substring(0, file.getName().lastIndexOf("."));
  }

  private static File fetchJadeSourcesToTmp() throws IOException {
    final File sourceRoot = FileUtil.createTempDirectory("pug-test", null, false);

    final String pathToScript = JadeTestUtil.getContribPath() +
                                "/jade/testResources/com/jetbrains/plugins/jade/parser/fetch_jade.sh";

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

  @ParameterizedTest(name = "{0}")
  @MethodSource("pugFiles")
  void testParsingWithoutErrors(File file) throws IOException {
    String text = FileUtil.loadFile(file, CharsetToolkit.UTF8, true).trim();
    PsiFile psiFile = parsingFixture.get().createPsiFile(getFileName(file), text);
    ReadAction.runBlocking(() -> {
      ParsingTestUtil.ensureParsed(psiFile);
      assertEquals(text, ((LightVirtualFile)psiFile.getVirtualFile()).getContent().toString(), "light virtual file text mismatch");
      assertEquals(text, LoadTextUtil.loadText(psiFile.getVirtualFile()), "virtual file text mismatch");
      //noinspection ConstantConditions
      assertEquals(text, psiFile.getViewProvider().getDocument().getText(), "doc text mismatch");
      assertEquals(text, psiFile.getText(), "psi text mismatch");
      checkNoErrorElements(file, psiFile);
    });
  }

  private static void checkNoErrorElements(File pugFile, PsiFile file) {
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
          PlatformTestUtil.assertSameLinesWithFile(pugFile.getAbsolutePath(), message);
        }
      }
    });
  }
}
