package org.jetbrains.plugins.cucumber.java.completion;


import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider;
import com.intellij.platform.backend.documentation.DocumentationData;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.impl.ImplKt;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CompletionTester;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.io.File;

public class CucumberJavaCompletionTest extends BasePlatformTestCase {
  private CompletionTester myCompletionTester;

  @Override
  protected void tearDown() throws Exception {
    myCompletionTester = null;
    super.tearDown();
  }

  public void testStepWithRegExGroups() throws Throwable {
    doTestVariants();
  }

  public void testStepWithRegex() throws Throwable {
    doTestVariants();
  }

  public void testStepWithQuestionMark() throws Throwable {
    doTestVariants();
  }

  public void testStepWithInterpolation() throws Throwable {
    doTestVariants();
  }

  public void testStepWithGroupInsideGroup() throws Throwable {
    doTestVariants();
  }

  public void testStepWithNumberStartingWithDot() throws Throwable {
    doTestVariants();
  }

  public void testWordOrder() throws Throwable {
    doTestVariants();
  }

  public void testCompletionForNonCapturingTokens() throws Throwable {
    doTestVariants();
  }

  public void testCompletionForOrGroup() throws Throwable {
    doTestVariants();
  }

  public void testCompletionForInt() throws Throwable {
    doTestVariants();
  }

  public void testNoCompletionInTable() throws Throwable {
    doTestVariants();
  }

  private void doTestVariants() throws Throwable {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myCompletionTester.doTestVariantsInner(getTestName(true) + File.separator + getTestName(true) + ".feature", GherkinFileType.INSTANCE);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "completion" + File.separator;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCompletionTester = new CompletionTester(myFixture);
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
  }


  /// Test for IDEA-383824.
  public void testStepCompletionDocumentation() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile(getTestName(true) + File.separator + getTestName(true) + ".feature");

    LookupElement[] lookupElements = myFixture.completeBasic();
    assertNotNull(lookupElements);

    LookupElement stepLookup = ContainerUtil.find(lookupElements, e -> e.getLookupString().contains("I am logged"));
    assertNotNull(stepLookup);

    IdeDocumentationTargetProvider targetProvider = IdeDocumentationTargetProvider.getInstance(getProject());
    var targets = targetProvider.documentationTargets(myFixture.getEditor(), myFixture.getFile(), stepLookup);
    assertFalse("Expected at least one documentation target for completion item", targets.isEmpty());

    DocumentationTarget target = targets.getFirst();
    DocumentationData documentationData = ImplKt.computeDocumentationBlocking(target.createPointer());
    assertNotNull("Documentation result should not be null for completion item", documentationData);

    String html = documentationData.getHtml();
    assertNotNull("Documentation HTML should not be null", html);
    assertTrue("Documentation HTML should contain the Javadoc of the step's method", html.contains("This step logs in a user"));
  }
}
