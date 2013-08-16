package org.jetbrains.lang.manifest;

import com.intellij.codeInsight.completion.LightCompletionTestCase;

public class ManifestCompletionTest extends LightCompletionTestCase {
  public void testHeaderNameCompletionVariants() throws Exception {
    configureFromFileText("MANIFEST.MF", "Specification-V<caret>\n");
    complete();
    assertContainsItems("Specification-Vendor", "Specification-Version");
    assertNotContainItems("Specification-Title");
  }

  public void testHeaderNameEnterCompletion() throws Exception {
    configureFromFileText("MANIFEST.MF", "Specification-V<caret>\n");
    complete();
    assertContainsItems("Specification-Vendor");
    selectItem(myItems[0], '\n');
    checkResultByText("Specification-Vendor: <caret>\n");
  }

  public void testHeaderNameColonCompletion() throws Exception {
    configureFromFileText("MANIFEST.MF", "Specification-V<caret>\n");
    complete();
    assertContainsItems("Specification-Vendor");
    selectItem(myItems[0], ':');
    checkResultByText("Specification-Vendor: <caret>\n");
  }

  public void testHeaderNameSpaceCompletion() throws Exception {
    configureFromFileText("MANIFEST.MF", "Specification-V<caret>\n");
    complete();
    assertContainsItems("Specification-Vendor");
    selectItem(myItems[0], ' ');
    checkResultByText("Specification-Vendor: <caret>\n");
  }
}
