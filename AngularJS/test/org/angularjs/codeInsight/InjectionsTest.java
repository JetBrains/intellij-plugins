package org.angularjs.codeInsight;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.javascript.inspections.JSBitwiseOperatorUsageInspection;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection;
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection;
import org.angularjs.AngularTestUtil;
import org.angularjs.editor.AngularJSInjector;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class InjectionsTest extends BasePlatformTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  public void testNgInitCompletion() {
    myFixture.testCompletion("ngInit.html", "ngInit.after.html", "angular.js");
  }

  public void testNgInitResolve() {
    myFixture.configureByFiles("ngInit.resolve.html", "angular.js");
    checkVariableResolve("fri<caret>ends", "friends", JSDefinitionExpression.class);
  }

  public void testNgRepeatImplicitCompletion() {
    myFixture.configureByFiles("ngRepeatImplicit.html", "angular.js");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "$index", "$first", "$middle", "$last", "$even", "$odd");
  }

  public void testNgRepeatAfterDotCompletion() {
    myFixture.configureByFiles("ngRepeatImplicitAfterDot.html", "angular.js");
    final List<String> variants = myFixture.getCompletionVariants("ngRepeatImplicitAfterDot.html");
    assertDoesntContain(variants, "person", "$index", "$first", "$middle", "$last", "$even", "$odd");
  }

  public void testNgRepeatImplicitResolve() {
    myFixture.configureByFiles("ngRepeatImplicitType.html", "angular.js");
    final JSLocalImplicitElementImpl resolve = checkVariableResolve("ind<caret>ex", "$index", JSLocalImplicitElementImpl.class);
    assertInstanceOf(resolve.getJSType(), JSNumberType.class);
  }

  public void testNgRepeatExplicitCompletion() {
    myFixture.testCompletionTyping("ngRepeatExplicit.html", "\n", "ngRepeatExplicit.after.html", "angular.js");
  }

  public void testInternalDefinitionCompletion() {
    myFixture.testCompletion("internalDefinition.html", "internalDefinition.after.html", "angular.js");
  }

  public void testNgRepeatExplicitCompletionInScript() {
    myFixture.testCompletionTyping("ngRepeatExplicitInScript.html", "\n", "ngRepeatExplicitInScript.after.html", "angular.js");
  }

  public void testNgRepeatExplicitCompletionSameTag() {
    myFixture.testCompletionTyping("ngRepeatExplicitSameTag.html", "\n", "ngRepeatExplicitSameTag.after.html", "angular.js");
  }

  public void testNgRepeatExplicitResolve() {
    myFixture.configureByFiles("ngRepeatExplicit.resolve.html", "angular.js");
    checkVariableResolve("per<caret>son", "person", JSVariable.class);
  }

  public void testInternalDefinitionResolve() {
    myFixture.configureByFiles("internalDefinition.after.html", "angular.js");
    checkVariableResolve("ord<caret>ered[", "ordered", JSDefinitionExpression.class);
  }

  public void testNgRepeatExplicitResolveInScript() {
    myFixture.configureByFiles("ngRepeatExplicitInScript.resolve.html", "angular.js");
    checkVariableResolve("per<caret>son", "person", JSVariable.class);
  }

  public void testNgRepeatExplicitResolveSameTag() {
    myFixture.configureByFiles("ngRepeatExplicitSameTag.resolve.html", "angular.js");
    checkVariableResolve("per<caret>son", "person", JSVariable.class);
  }

  public void testNgRepeatExplicitKeyCompletion() {
    myFixture.testCompletionTyping("ngRepeatExplicitHashKey.html", "\n", "ngRepeatExplicitHashKey.after.html", "angular.js");
  }

  public void testNgRepeatExplicitKeyResolve() {
    myFixture.configureByFiles("ngRepeatExplicitHashKey.resolve.html", "angular.js");
    checkVariableResolve("ke<caret>y", "key", JSVariable.class);
  }

  public void testNgRepeatExplicitValueCompletion() {
    myFixture.testCompletion("ngRepeatExplicitHashValue.html", "ngRepeatExplicitHashValue.after.html", "angular.js");
  }

  public void testNgRepeatExplicitValueResolve() {
    myFixture.configureByFiles("ngRepeatExplicitHashValue.resolve.html", "angular.js");
    checkVariableResolve("val<caret>ue", "value", JSVariable.class);
  }

  public void testNgRepeatEndCompletion() {
    myFixture.testCompletion("ngRepeatEnd.html", "ngRepeatEnd.after.html", "angular.js");
  }

  public void testNgRepeatEndResolve() {
    myFixture.configureByFiles("ngRepeatEnd.resolve.html", "angular.js");
    checkVariableResolve("aNo<caret>te", "aNote", JSVariable.class);
  }

  public void testNgControllerAliasCompletion() {
    myFixture.testCompletionTyping("ngControllerAlias.html", "\n", "ngControllerAlias.after.html", "angular.js", "custom.js");
  }

  public void testNgControllerAliasHighlighting() {
    myFixture.configureByFiles("ngControllerAlias.highlight.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  public void testNgControllerAliasResolve() {
    myFixture.configureByFiles("ngControllerAlias.resolve.html", "angular.js", "custom.js");
    checkVariableResolve("ap<caret>p", "app", JSDefinitionExpression.class);
  }

  public void testEmmetBeforeInjection() {
    myFixture.configureByFiles("ngController.emmet.html", "angular.js", "custom.js");
    myFixture.type('\t');
    myFixture.checkResultByFile("ngController.emmet.after.html");
  }

  public void testComment() {
    myFixture.configureByFiles("comment.html", "angular.js");
  }

  public void testCommentWithFollowingInterpolation() {
    myFixture.configureByFiles(getTestName(true) + ".html", "angular.js");
    assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }

  public void testCustomStartDelimiterEmptyWhenTyping() {
    final String name = getTestName(true);
    myFixture.configureByFiles(name + ".html", "angular.js", name + ".js");
    assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    assertBraces("{{", "]]");
  }

  private void assertBraces(@NotNull String expectedStart, @NotNull String expectedEnd) {
    final Pair<String, String> braces = AngularJSInjector.BRACES_FACTORY.fun(myFixture.getFile());
    Assert.assertNotNull(braces);
    Assert.assertEquals(expectedStart, braces.getFirst());
    Assert.assertEquals(expectedEnd, braces.getSecond());
  }

  public void testCustomEndDelimiterEmptyWhenTyping() {
    final String name = getTestName(true);
    myFixture.configureByFiles(name + ".html", "angular.js", name + ".js");
    assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    assertBraces("[[", "}}");
  }

  public void testAngularInterpolationCrossesCommentArea() {
    myFixture.configureByFiles(getTestName(true) + ".html", "angular.js");
    assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }

  public void testCustomDelimiters() {
    myFixture.configureByFiles("customDelimiters.html", "angular.js", "customDelimiters.js");
    assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    assertBraces("[[", "]]");
  }

  public void testCustomDelimitersInline() {
    myFixture.configureByFiles("customDelimitersInline.html", "angular.js", "customDelimitersInline.js");
    assertEquals(HTMLLanguage.INSTANCE, myFixture.getFile().getLanguage());
    assertBraces("[[", "]]");
  }

  public void testCustomDelimitersDefaultIgnored() {
    myFixture.configureByFiles("customDelimitersDefaultIgnored.html", "angular.js", "customDelimitersDefaultIgnored.js");
    assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    assertBraces("{{", "}}");
  }

  public void testDefaultDelimitersInJSX() {
    myFixture.configureByFiles("defaultDelimiters.jsx", "angular.js");
    assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }

  public void testNgRepeat() {
    myFixture.configureByFiles("ngRepeatExplicit.jsx", "angular.js");
    assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }

  public void testCustomDelimitersSameStartEnd() {
    myFixture.configureByFiles("customDelimitersSameStartEnd.html", "angular.js", "customDelimitersSameStartEnd.js");
    assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    assertBraces("**", "**");
  }

  public void testNgNonBindable() {
    myFixture.configureByFiles(getTestName(true) + ".html", "angular.js");
    assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }

  public void testNoQuotes() {
    myFixture.configureByFiles("noQuotes.html", "angular.js");
  }

  public void testBadExpression() {
    myFixture.enableInspections(BadExpressionStatementJSInspection.class);
    myFixture.configureByFiles("badExpression.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  public void testCommaExpression() {
    myFixture.enableInspections(CommaExpressionJSInspection.class);
    myFixture.configureByFiles("commaExpression.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  private <T extends JSElement> T checkVariableResolve(final String signature, final String varName, final Class<T> varClass) {
    return AngularTestUtil.checkVariableResolve(signature, varName, varClass, myFixture);
  }

  public void testMessageFormat() {
    myFixture.configureByFiles("messageFormat.html", "messageFormatController.js", "angular.js");
    myFixture.checkHighlighting();
  }

  public void testElementOnly() {
    myFixture.configureByFiles("select.html", "angular.js");
    assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }

  public void testNoXml() {
    myFixture.configureByFiles("noXml.xml", "angular.js");
    assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
  }

  public void testBitwise() {
    myFixture.enableInspections(JSBitwiseOperatorUsageInspection.class);
    myFixture.configureByFiles("bitwise.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

}
