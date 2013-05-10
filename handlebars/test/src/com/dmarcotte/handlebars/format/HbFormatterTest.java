package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * Base class for Handlebars formatter tests.  Based on com.intellij.psi.formatter.java.AbstractJavaFormatterTest.
 */
public abstract class HbFormatterTest extends LightPlatformCodeInsightFixtureTestCase implements HbFormattingModelBuilderTest {
  private static final String TEST_DATA_PATH = new File(HbTestUtils.BASE_TEST_DATA_PATH, "formatter").getAbsolutePath();

  private FormatterTestSettings formatterTestSettings;

  protected HbFormatterTest() {
    PlatformTestCase.initPlatformLangPrefix();
  }

  @Override
  protected void setUp()
    throws Exception {
    super.setUp();

    formatterTestSettings = new FormatterTestSettings(getProject());
    formatterTestSettings.setUp();
  }

  @Override
  protected void tearDown()
    throws Exception {
    formatterTestSettings.tearDown();

    super.tearDown();
  }

  /**
   * Passes {@link com.intellij.testFramework.UsefulTestCase#getTestName(boolean)}
   * as a parameter to {@link #doFileBasedTest(java.lang.String)}
   *
   * @throws Exception
   */
  void doFileBasedTest() throws Exception {
    doFileBasedTest(getTestName(false) + ".hbs");
  }

  /**
   * Call this to run the formatter on a test file in the {@link #TEST_DATA_PATH} directory.
   * <p/>
   * The test will validate the results against a file of the same name with "_expected" appended.
   * (i.e. for fileNameBefore "TestFile.hbs", the formatter will be run on {@link #TEST_DATA_PATH}/TestFile.hbs
   * the test will look for {@link #TEST_DATA_PATH}/TestFile_expected.hbs to validate the results).
   *
   * @param fileNameBefore The name of the file to test (must have the '.hbs' extension).
   * @throws Exception
   */
  void doFileBasedTest(@NonNls String fileNameBefore) throws Exception {
    doFileBasedTest(fileNameBefore, HbLanguage.getDefaultTemplateLang());
  }

  /**
   * Specialization of {@link #doFileBasedTest(String)} which adds the option of specifying a templated language
   * other than {@link com.dmarcotte.handlebars.HbLanguage#getDefaultTemplateLang()}
   *
   * @param fileNameBefore           The name of the file to test
   * @param templateDataLanguageType The LanguageFileType of the templated file
   * @throws Exception
   */
  void doFileBasedTest(@NonNls String fileNameBefore, LanguageFileType templateDataLanguageType) throws Exception {
    doTextTest(loadFile(fileNameBefore), loadFile(fileNameBefore.replace(".hbs", "_expected.hbs")), templateDataLanguageType);
  }

  void doStringBasedTest(@NonNls final String text, @NonNls String textAfter) throws IncorrectOperationException {
    doTextTest(text, textAfter, HbLanguage.getDefaultTemplateLang());
  }

  private abstract static class FormatRunnableFactory {
    abstract Runnable createFormatRunnable(PsiFile file);
  }

  /**
   * This method runs both a full-file reformat on beforeText, and a line-by-line reformat.  Though the tests
   * would output slightly better errors if these were separate tests, enforcing that they are always both run
   * for any test defined is the easiest way to ensure that the line-by-line is not messed up by formatter changes
   *
   * @param beforeText               The text run the formatter on
   * @param textAfter                The expected result after running the formatter
   * @param templateDataLanguageType The templated language of the file
   * @throws IncorrectOperationException
   */
  void doTextTest(final String beforeText, String textAfter, LanguageFileType templateDataLanguageType) throws IncorrectOperationException {
    // define action to run "Reformat Code" on the whole "file" defined by beforeText
    FormatRunnableFactory fullFormatRunnableFactory = new FormatRunnableFactory() {
      @Override
      Runnable createFormatRunnable(final PsiFile file) {
        return new Runnable() {
          @Override
          public void run() {
            try {
              TextRange rangeToUse = file.getTextRange();
              CodeStyleManager styleManager = CodeStyleManager.getInstance(getProject());
              styleManager.reformatText(file, rangeToUse.getStartOffset(), rangeToUse.getEndOffset());
            }
            catch (IncorrectOperationException e) {
              assertTrue(e.getLocalizedMessage(), false);
            }
          }
        };
      }
    };

    // define action to run "Adjust line indent" on every line in the "file" defined by beforeText
    FormatRunnableFactory lineFormatRunnableFactory = new FormatRunnableFactory() {
      @Override
      Runnable createFormatRunnable(final PsiFile file) {
        return new Runnable() {
          @Override
          public void run() {
            try {
              final PsiDocumentManager manager = PsiDocumentManager.getInstance(getProject());
              final Document document = manager.getDocument(file);

              assert document != null;

              for (int lineNum = 0; lineNum < document.getLineCount(); lineNum++) {
                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
                int offset = document.getLineStartOffset(lineNum);
                @SuppressWarnings("deprecation") // if this breaks at some point, we should
                  // refactor to invoke AutoIndentLinesAction
                  // instead of doing the indent directly
                  boolean lineToBeIndented = codeStyleManager.isLineToBeIndented(file, offset);
                if (lineToBeIndented) {
                  codeStyleManager.adjustLineIndent(file, offset);
                }
              }
            }
            catch (IncorrectOperationException e) {
              assertTrue(e.getLocalizedMessage(), false);
            }
          }
        };
      }
    };

    doFormatterActionTest(fullFormatRunnableFactory, beforeText, textAfter, templateDataLanguageType);
    doFormatterActionTest(lineFormatRunnableFactory, beforeText, textAfter, templateDataLanguageType);
  }

  private void doFormatterActionTest(final FormatRunnableFactory formatAction,
                                     final String beforeText,
                                     String textAfter,
                                     LanguageFileType templateDataLanguageType) {
    PsiFile baseFile = myFixture.configureByText("A.hbs", beforeText);

    VirtualFile virtualFile = baseFile.getVirtualFile();
    assert virtualFile != null;
    TemplateDataLanguageMappings.getInstance(getProject()).setMapping(virtualFile, templateDataLanguageType.getLanguage());

    // fetch a fresh instance of the file -- the template data mapping creates a new instance,
    // which was causing problems in PsiFileImpl.isValid()
    final PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
    assert file != null;

    CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runWriteAction(formatAction.createFormatRunnable(file));
      }
    }, "", "");

    TemplateDataLanguageMappings.getInstance(getProject()).cleanupForNextTest();

    assertEquals("Reformat Code failed", prepareText(textAfter), prepareText(file.getText()));
    assertEquals("Reformat Code failed", prepareText(textAfter), prepareText(file.getText()));
  }

  private String prepareText(String actual) {
    if (actual.startsWith("\n")) {
      actual = actual.substring(1);
    }
    if (actual.startsWith("\n")) {
      actual = actual.substring(1);
    }

    // Strip trailing spaces
    final Document doc = EditorFactory.getInstance().createDocument(actual);
    CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            ((DocumentImpl)doc).stripTrailingSpaces();
          }
        });
      }
    }, "formatting", null);

    return doc.getText();
  }

  private static String loadFile(String name) throws Exception {
    String fullName = new File(TEST_DATA_PATH, name).getAbsolutePath();
    String text = FileUtil.loadFile(new File(fullName));
    text = StringUtil.convertLineSeparators(text);
    return text;
  }
}

