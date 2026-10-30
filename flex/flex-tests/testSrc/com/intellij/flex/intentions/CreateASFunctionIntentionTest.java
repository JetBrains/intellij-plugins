// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.intentions;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.CreateJSFunctionIntentionFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.openapi.project.Project;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSIntentionFixture.createJSFunctionIntentionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class CreateASFunctionIntentionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<CreateJSFunctionIntentionFixture> js =
    createJSFunctionIntentionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("") + "/createfunction_as")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().setIntentionWrapper((testName, perform) -> {
      JSTestUtils.testWithTempCodeStyleSettings(js.get().getProject(), settings -> {
        XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
        if (testName.endsWith("8.mxml")) {
          xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
        }
        perform.run();
      });
    });
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), myFixture.getTestRootDisposable());
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateFunctionAS() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }
  
  @Test
  public void testCreateFunctionAS_2() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateFunctionAS_2Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateFunctionAS_2_2() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateFunctionAS_3() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateFunctionAS_3Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateFunctionAS_4() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_4Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateFunctionAS_5() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateFunctionAS_5Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateFunctionAS_6() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateFunctionAS_6Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateFunctionAS_7() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateFunctionAS_7_2() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_8() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateFunctionAS_8Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateFunctionAS_9() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_10() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_11() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_12() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_13() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_14() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_15() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_16() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_17() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_18() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_19() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_19_2() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_20() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_21() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_22() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateFunctionAS_Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateConstructor() {
    doTestTwoFiles();
  }

  private void doTestTwoFiles() {
    js.get().getFixture().enableInspections(new JSValidateTypesInspection(), new JSCheckFunctionSignaturesInspection());
    String name = js.get().getTestName(false);
    String first = name + '/' + name + ".as";
    String second = name + '/' + name + "_2.as";
    js.get().doTestForAndCheckLastFile(first, second);
  }
}