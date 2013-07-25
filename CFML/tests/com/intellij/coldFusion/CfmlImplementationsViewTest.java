package com.intellij.coldFusion;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.codeInsight.hint.ImplementationViewComponent;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 4/24/12
 */
public class CfmlImplementationsViewTest extends CfmlCodeInsightFixtureTestCase {
  public void testQuickDefinitionViewForTagFunctions() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    PsiElement element =
      TargetElementUtilBase.findTargetElement(myFixture.getEditor(), TargetElementUtilBase.getInstance().getAllAccepted());
    assert element != null;
    final String newText = ImplementationViewComponent.getNewText(element.getNavigationElement());
    assertEquals(
      "<cffunction name=\"testIncludeFile\" hint=\"test including a file\" access=\"public\" returntype=\"void\" output=\"false\">\n" +
      "    <cfargument name=\"arg1\" type=\"numeric\">\n" +
      "    <cfargument name=\"arg2\" type=\"numeric\">\n" +
      "    <cfscript>\n" +
      "    </cfscript>\n" +
      "</cffunction>", newText);
  }


  @Override
  protected String getBasePath() {
    return "/implementationsView";
  }
}
