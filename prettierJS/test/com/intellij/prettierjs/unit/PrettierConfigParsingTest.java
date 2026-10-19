// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit;

import com.google.gson.Gson;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.transformer.TextPresentationTransformer;
import com.intellij.openapi.vfs.transformer.TextPresentationTransformers;
import com.intellij.prettierjs.PrettierConfig;
import com.intellij.prettierjs.PrettierJSTestUtil;
import com.intellij.prettierjs.PrettierUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class PrettierConfigParsingTest extends BasePlatformTestCase {

  public void testDefaultConfigs() {
    doTest(PrettierConfig.DEFAULT, "package.json", """
      {
        "devDependencies":{
          "prettier":"latest"
        },
        "prettier": {}
      }""");
    doTest(PrettierConfig.DEFAULT, ".prettierrc.json", "{}");
    doTest(PrettierConfig.DEFAULT, ".prettierrc.yml", "#comment");
  }

  public void testNoConfigIfPackageNotInDependencies() {
    doTest(null, "package.json", "{}");
  }

  public void testJsonConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true,
                              3, PrettierConfig.TrailingCommaOption.all, true,
                              LineSeparator.CRLF, true),
           ".prettierrc.json",
           """
             {
               "semi": false,
               "bracketSpacing": false,
               "jsxBracketSameLine": true,
               "printWidth": 120,
               "singleQuote": true,
               "tabWidth": 3,
               "useTabs": true,
               "trailingComma": "all",
               "parser": "babylon",
               "endOfLine": "crlf",
               "vueIndentScriptAndStyle": true
             }""");
  }

  public void testJsonWithAutoLineSeparator() {
    PrettierConfig parsed = doParse(".prettierrc.json",
                                    """
                                      {
                                        "printWidth": 113,
                                        "endOfLine": "auto"
                                      }""");
    assertEquals(113, parsed.printWidth);
    assertNull(parsed.lineSeparator);
  }

  public void testPackageJsonConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true,
                              3, PrettierConfig.TrailingCommaOption.all, true, null, false),
           "package.json",
           """
             {
               "devDependencies": {
                 "prettier": "latest"
               },
               "prettier": {
                 "semi": false,
                 "bracketSpacing": false,
                 "jsxBracketSameLine": true,
                 "printWidth": 120,
                 "singleQuote": true,
                 "tabWidth": 3,
                 "useTabs": true,
                 "trailingComma": "all",
                 "parser": "babylon"
               }
             }""");
  }

  public void testYamlConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true, 3, PrettierConfig.TrailingCommaOption.es5, true,
                              LineSeparator.CRLF, true),
           ".prettierrc.yml",
           """
             semi: false
             bracketSpacing: false
             jsxBracketSameLine: true
             printWidth: 120
             singleQuote: true
             tabWidth: 3
             useTabs: true
             trailingComma: es5
             parser: babylon
             endOfLine: crlf
             vueIndentScriptAndStyle: true"""
    );
  }

  public void testCancelledReadDoesNotCacheConfig() {
    var config = myFixture.addFileToProject(".prettierrc.json", "{\"tabWidth\": 3}").getVirtualFile();
    // The parser loads the text through the transformer only when the file has no cached document.
    assertNull(FileDocumentManager.getInstance().getCachedDocument(config));
    var cancelNextLoad = new AtomicBoolean(true);
    ApplicationManager.getApplication().getService(TextPresentationTransformers.class).addExplicitExtension(
      config.getFileType(),
      new TextPresentationTransformer() {
        @Override
        public @NotNull CharSequence fromPersistent(@NotNull CharSequence text, @NotNull VirtualFile virtualFile) {
          if (virtualFile.equals(config) && cancelNextLoad.getAndSet(false)) {
            throw new ProcessCanceledException();
          }
          return text;
        }

        @Override
        public @NotNull CharSequence toPersistent(@NotNull CharSequence text, @NotNull VirtualFile virtualFile) {
          return text;
        }
      },
      getTestRootDisposable());

    assertThrows(ProcessCanceledException.class, () -> PrettierUtil.parseConfig(getProject(), config));
    var parsed = PrettierUtil.parseConfig(getProject(), config);
    assertNotNull(parsed);
    assertEquals(3, parsed.tabWidth);
  }

  private static void assertSameConfig(PrettierConfig expected, PrettierConfig actual) {
    if (expected == null || actual == null) {
      assertEquals(expected, actual);
      return;
    }
    Gson gson = JSLanguageServiceQueue.SharedGson.GSON;
    assertEquals(gson.toJson(expected), gson.toJson(actual));
  }

  private void doTest(PrettierConfig expected, String fileName, String fileContent) {
    assertSameConfig(expected, doParse(fileName, fileContent));
  }

  private PrettierConfig doParse(String fileName, String fileContent) {
    PsiFile psiFile = myFixture.configureByText(fileName, fileContent);
    return PrettierUtil.parseConfig(psiFile.getProject(), psiFile.getVirtualFile());
  }
}
