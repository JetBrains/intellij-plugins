package com.intellij.flex.intentions;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSIntentionFixture;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSIntentionFixture.jsIntentionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class CreateASVariableIntentionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSIntentionFixture> js =
    jsIntentionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("") + "/createvariable_as")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), myFixture.getTestRootDisposable());
    myFixture.enableInspections(new JSUnresolvedReferenceInspection());
  }

  @Test
  public void testCreateVariableAS_() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_2() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_2Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS_2_2() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS_3() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_3Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS_3_2() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateVariableAS_3_3() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS_4() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCreateVariableAS_4Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS_5() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_5Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS_6() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_7() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_8() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_9() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_10() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_10_2() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_11() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_12() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_13() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_14() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_14_2() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_15() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_16() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_17() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_18() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_19() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_20() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_21() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_21Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS_22() {
    js.get().doCompositeNameBeforeAfterTest("js2", false);
  }

  @Test
  public void testCreateVariableAS_Mxml() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS__2() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }

  @Test
  public void testCreateVariableAS__3() {
    js.get().doCompositeNameBeforeAfterTest("mxml", false);
  }
}
