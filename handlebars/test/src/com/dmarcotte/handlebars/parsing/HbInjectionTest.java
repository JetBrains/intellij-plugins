// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.parsing;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbInjectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  @Test
  public void testHtmlSetContent() {
    CodeInsightTestFixture myFixture = fixture.get();
    String fileText = """
      <script type="text/x-handlebars-template">
          {{boo}}
          <label {{action "editTodo" on="doubleClick"}}>{{title}}</label>
      </script>""";
    PsiFile file = myFixture.configureByText("HtmlSetContent.html",
                                             fileText);
    XmlText text = ReadAction.computeBlocking(() -> {
      XmlTag tag = ((XmlFile)file).getRootTag();
      return tag.getValue().getTextElements()[0];
    });
    WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> text.setValue(text.getValue()));
    myFixture.checkResult(fileText);
  }
}
