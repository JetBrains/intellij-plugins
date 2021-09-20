// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.editor;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.lang.javascript.JSLiveTemplatesTestBase;
import org.angularjs.AngularTestUtil;

public class AngularLiveTemplateTest extends JSLiveTemplatesTestBase {
  @Override
  protected String getBasePath() {
    return "";
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testFactory() {
    doTest("ngmfa", "js", "AngularJS");
  }

  public void testClassMemberContext() {
    myFixture.configureByText("a.js", "class C {\n<caret>\n}");
    Template template = TemplateSettings.getInstance().getTemplate("ngmfa", "AngularJS");
    checkTemplateApplicability(template, false);
  }
}
