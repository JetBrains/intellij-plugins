package com.intellij.flex.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.CreateJSFunctionIntentionTestBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class CreateASFunctionIntentionTest extends CreateJSFunctionIntentionTestBase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getTestRootDisposable());
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + "/createfunction_as";
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testAll() {
    doTestAll();
  }

  public void testCreateConstructor() {
    doTestTwoFiles();
  }

  private void doTestTwoFiles() {
    myFixture.enableInspections(new JSValidateTypesInspection(), new JSCheckFunctionSignaturesInspection());
    String name = getTestName(false);
    String directory = "/" + name;
    String first = directory + "/" + name + ".as";
    String secondName = name + "_2.as";
    String second = directory + "/" + secondName;
    doTestForAndCheckLastFile(first, second);
  }

  @Override
  protected void performIntention(String testName, IntentionAction action) {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {
      XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
      if (testName.endsWith("8.mxml")) {
        xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
      }
      super.performIntention(testName, action);
    });
  }
}
