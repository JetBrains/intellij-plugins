package org.jetbrains.lang.manifest;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.lang.manifest.highlighting.MissingFinalNewlineInspection;

public class MissingFinalNewlineInspectionTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new MissingFinalNewlineInspection());
  }

  public void testEmptyFile() {
    myFixture.configureByText(ManifestFileTypeFactory.MANIFEST, "");
    assertEquals(0, myFixture.getAvailableIntentions().size());
  }

  public void testNoProblem() {
    myFixture.configureByText(ManifestFileTypeFactory.MANIFEST, "Manifest-Version: 1.0\n");
    assertEquals(0, myFixture.getAvailableIntentions().size());
  }

  public void testFix() {
    myFixture.configureByText(ManifestFileTypeFactory.MANIFEST, "Manifest-Version: 1.0");
    IntentionAction intention = myFixture.findSingleIntention(ManifestBundle.message("inspection.newline.fix"));
    myFixture.launchAction(intention);
    myFixture.checkResult("Manifest-Version: 1.0\n");
  }
}
