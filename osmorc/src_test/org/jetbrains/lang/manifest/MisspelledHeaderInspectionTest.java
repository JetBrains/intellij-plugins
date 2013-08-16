package org.jetbrains.lang.manifest;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.lang.manifest.highlighting.MisspelledHeaderInspection;

import java.util.List;

public class MisspelledHeaderInspectionTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new MisspelledHeaderInspection());
  }

  public void testNoProblem() {
    myFixture.configureByText(ManifestFileTypeFactory.MANIFEST, "Manifest-Version: 1.0\n");
    assertEquals(0, myFixture.getAvailableIntentions().size());
  }

  public void testFix() {
    myFixture.configureByText(ManifestFileTypeFactory.MANIFEST, "ManifestVersion: 1.0\n");
    List<IntentionAction> intentions = myFixture.filterAvailableIntentions("Change to");
    assertTrue(intentions.size() > 0);
    myFixture.launchAction(intentions.get(0));
    myFixture.checkResult("Manifest-Version: 1.0\n");
  }
}
