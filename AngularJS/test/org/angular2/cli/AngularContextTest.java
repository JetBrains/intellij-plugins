// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;

public class AngularContextTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularConfigTest.class) + "context";
  }

  public void testAngular9() {
    myFixture.enableInspections(HtmlUnknownTargetInspection.class);
    myFixture.copyDirectoryToProject("angular-9", ".");
    myFixture.configureFromTempProjectFile("src/app/app.component.html");
    myFixture.checkHighlighting(true, false, true);
    WriteAction.runAndWait(() -> {
      myFixture.getEditor().getDocument().setText("<img src=''/>");
      FileDocumentManager.getInstance().saveAllDocuments();
    });
    AngularTestUtil.moveToOffsetBySignature("<img src='<caret>'/>",myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "favicon.ico", "image.png");
  }
}
