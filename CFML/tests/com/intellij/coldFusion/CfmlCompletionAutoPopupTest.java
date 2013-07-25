package com.intellij.coldFusion;

/**
 * @author Nadya Zabrodina
 */
class CfmlCompletionAutoPopupTest extends CfmlCodeInsightFixtureTestCase {

  public void testAutopopupBasics() throws Throwable {
    myFixture.configureByText("a.cfml", "<cfinclude template=\"folder<caret>\">");

    myFixture.addFileToProject("folder/subfolder/b.cfml", "");
    myFixture.addFileToProject("folder/subfolder2/b.cfml", "");
    myFixture.type('/');
    assertSameElements(myFixture.getLookupElementStrings(), "subfolder", "subfolder2");
  }
}
