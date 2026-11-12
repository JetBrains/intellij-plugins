package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.file.HbFileType;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@TestApplication
public class HbsEmmetTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  @Test
  public void testSimpleTags() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByText(HbFileType.INSTANCE, "div>span<caret>");
    WriteCommandAction.runWriteCommandAction(
      myFixture.getProject(),
      () -> {
        TemplateManager.getInstance(myFixture.getProject()).startTemplate(myFixture.getEditor(), TemplateSettings.TAB_CHAR);
      });

    checkResult();
  }

  @Test
  public void testSimpleTagsWithHtmlSubstitutor(@TestDisposable Disposable disposable) {
    CodeInsightTestFixture myFixture = fixture.get();
    HbTestUtils.setOpenHtmlAsHandlebars(true, myFixture.getProject(), disposable);
    final PsiFile file = myFixture.configureByText("test.html", "div>span<caret>");
    assertInstanceOf(HbPsiFile.class, file);
    WriteCommandAction.runWriteCommandAction(
      myFixture.getProject(),
      () -> {
        TemplateManager.getInstance(myFixture.getProject()).startTemplate(myFixture.getEditor(), TemplateSettings.TAB_CHAR);
      });

    checkResult();
  }

  private void checkResult() {
    EdtTestUtil.runInEdtAndWait(() -> {
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
    });

    fixture.get().checkResult("<div><span></span></div>");
  }
}
