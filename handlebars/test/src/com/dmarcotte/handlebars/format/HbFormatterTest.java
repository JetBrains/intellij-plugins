package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.generation.AutoIndentLinesHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * Base class for Handlebars formatter tests.  Based on com.intellij.psi.formatter.java.AbstractJavaFormatterTest.
 */
public abstract class HbFormatterTest extends BasePlatformTestCase implements HbFormattingModelBuilderTest {
  private static final String TEST_DATA_PATH = new File(HbTestUtils.BASE_TEST_DATA_PATH, "formatter").getAbsolutePath();

  protected FormatterTestSettings formatterTestSettings;

  @Override
  protected void setUp()
    throws Exception {
    super.setUp();

    formatterTestSettings = new FormatterTestSettings(CodeStyle.getSettings(getProject()));
    formatterTestSettings.setUp();
  }

  @Override
  protected void tearDown()
    throws Exception {
    try {
      formatterTestSettings.tearDown();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  /**
   * Passes {@link com.intellij.testFramework.UsefulTestCase#getTestName(boolean)}
   * as a parameter to {@link #doFileBasedTest(java.lang.String)}
   *
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
   */
  void doFileBasedTest(@NonNls String fileNameBefore, LanguageFileType templateDataLanguageType) throws Exception {
    String extension = FileUtilRt.getExtension(fileNameBefore);
    doTextTest(loadFile(fileNameBefore),
               loadFile(fileNameBefore.replace("." + extension, "_expected." + extension)),
               extension,
               templateDataLanguageType);
  }

  void doStringBasedTest(@NonNls final String text, @NonNls String textAfter) throws IncorrectOperationException {
    doTextTest(text, textAfter, "hbs", HbLanguage.getDefaultTemplateLang());
  }

  /**
   * This method runs both a full-file reformat on beforeText, and a line-by-line reformat.  Though the tests
   * would output slightly better errors if these were separate tests, enforcing that they are always both run
   * for any test defined is the easiest way to ensure that the line-by-line is not messed up by formatter changes
   *
   * @param beforeText               The text run the formatter on
   * @param textAfter                The expected result after running the formatter
   * @param templateDataLanguageType The templated language of the file
   */
  void doTextTest(final String beforeText, String textAfter, String extension, LanguageFileType templateDataLanguageType) throws IncorrectOperationException {
    // define action to run "Reformat Code" on the whole "file" defined by beforeText
    Runnable fullFormatRunnableFactory = () -> {
          TextRange rangeToUse = myFixture.getFile().getTextRange();
          CodeStyleManager styleManager = CodeStyleManager.getInstance(getProject());
          styleManager.reformatText(myFixture.getFile(), rangeToUse.getStartOffset(), rangeToUse.getEndOffset());
        };

    // define action to run "Adjust line indent" on every line in the "file" defined by beforeText
    Runnable lineFormatRunnableFactory = () -> {
          Editor editor = myFixture.getEditor();
          editor.getSelectionModel().setSelection(0, editor.getDocument().getTextLength());
          new AutoIndentLinesHandler().invoke(myFixture.getProject(), editor, myFixture.getFile());
        };

    doFormatterActionTest(fullFormatRunnableFactory, beforeText, textAfter, extension, templateDataLanguageType);
    doFormatterActionTest(lineFormatRunnableFactory, beforeText, textAfter, extension, templateDataLanguageType);
  }

  private void doFormatterActionTest(final Runnable formatAction,
                                     final String beforeText,
                                     String textAfter,
                                     String extension,
                                     LanguageFileType templateDataLanguageType) {
    PsiFile baseFile = myFixture.configureByText("A." + extension, beforeText);

    VirtualFile virtualFile = baseFile.getVirtualFile();
    assert virtualFile != null;
    TemplateDataLanguageMappings.getInstance(getProject()).setMapping(virtualFile, templateDataLanguageType.getLanguage());

    // fetch a fresh instance of the file -- the template data mapping creates a new instance,
    // which was causing problems in PsiFileImpl.isValid()
    final PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
    assert file != null;

    WriteCommandAction.runWriteCommandAction(getProject(), formatAction);
    TemplateDataLanguageMappings.getInstance(getProject()).cleanupForNextTest();
    assertEquals("Reformat Code failed", prepareText(textAfter), prepareText(file.getText()));
  }

  private String prepareText(String actual) {
    actual = StringUtil.trimStart(actual, "\n");
    actual = StringUtil.trimStart(actual, "\n");

    // Strip trailing spaces
    final Document doc = EditorFactory.getInstance().createDocument(actual);
    CommandProcessor.getInstance().executeCommand(getProject(), () -> ApplicationManager.getApplication().runWriteAction(() -> {
      ((DocumentImpl)doc).stripTrailingSpaces(getProject());
    }), "formatting", null);

    return doc.getText();
  }

  private static String loadFile(String name) throws Exception {
    String fullName = new File(TEST_DATA_PATH, name).getAbsolutePath();
    String text = FileUtil.loadFile(new File(fullName));
    text = StringUtil.convertLineSeparators(text);
    return text;
  }
}

