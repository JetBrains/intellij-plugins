// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.parsing;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class HbInjectionTest extends BasePlatformTestCase {
  public void testHtmlSetContent() {
    String fileText = """
      <script type="text/x-handlebars-template">
          {{boo}}
          <label {{action "editTodo" on="doubleClick"}}>{{title}}</label>
      </script>""";
    PsiFile file = myFixture.configureByText(getTestName(false) + ".html",
                                             fileText);
    XmlTag tag = ((XmlFile)file).getRootTag();
    XmlText text = tag.getValue().getTextElements()[0];
    WriteCommandAction.runWriteCommandAction(getProject(), () -> text.setValue(text.getValue()));
    myFixture.checkResult(fileText);
  }
}
