package org.angularjs.codeInsight;

import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class NgRepeatTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "ngRepeat");
  }

  @Test
  public void testNgInit() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("ngInit.html", "angular.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 3);
    assertEquals("age", variants.get(0));
    assertEquals("gender", variants.get(1));
    assertEquals("name", variants.get(2));
  }

  @Test
  public void testControllerWithAlias() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("controllerWithAlias.html", "angular.js", "custom.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("firstName", variants.get(0));
    assertEquals("lastName", variants.get(1));
  }

  @Test
  public void testControllerWithAliasNoVar() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("controllerWithAlias.html", "angular.js", "customNoVar.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("firstName", variants.get(0));
    assertEquals("lastName", variants.get(1));
  }

  @Test
  public void testControllerWithoutAlias() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("controllerWithoutAlias.html", "angular.js", "custom.js");
    assertNotNull(variants);
    assertTrue(variants.size() >= 2);
    assertEquals("firstName", variants.get(0));
    assertEquals("lastName", variants.get(1));
  }

  @Test
  public void testUnusedHighlighting() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class);
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class);
    myFixture.configureByFiles("unusedHighlighting.html", "angular.js", "customObj.js");
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testInspection() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(JSUnresolvedReferenceInspection.class);
    myFixture.disableInspections();
    myFixture.configureByFiles("controllerWithAlias.inspect.html", "angular.js", "customNoVar.js");
    myFixture.checkHighlighting();
  }
}
