package org.angularjs.codeInsight;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSBitwiseOperatorUsageInspection;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection;
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection;
import org.angularjs.AngularTestUtil;
import org.angularjs.editor.AngularJSInjector;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class InjectionsTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private final TestFixture<String> testName = testNameFixture();

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "injections");
  }

  @Test
  public void testNgInitCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("ngInit.html", "ngInit.after.html", "angular.js");
  }

  @Test
  public void testNgInitResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngInit.resolve.html", "angular.js");
    checkVariableResolve("fri<caret>ends", "friends", JSDefinitionExpression.class);
  }

  @Test
  public void testNgRepeatImplicitCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatImplicit.html", "angular.js");
    myFixture.completeBasic();
    assertThat(myFixture.getLookupElementStrings()).contains("$index", "$first", "$middle", "$last", "$even", "$odd");
  }

  @Test
  public void testNgRepeatAfterDotCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatImplicitAfterDot.html", "angular.js");
    final List<String> variants = myFixture.getCompletionVariants("ngRepeatImplicitAfterDot.html");
    assertThat(variants).doesNotContain("person", "$index", "$first", "$middle", "$last", "$even", "$odd");
  }

  @Test
  public void testNgRepeatImplicitResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatImplicitType.html", "angular.js");
    final JSLocalImplicitElementImpl resolve = checkVariableResolve("ind<caret>ex", "$index", JSLocalImplicitElementImpl.class);
    assertInstanceOf(JSNumberType.class, ReadAction.compute(() -> resolve.getJSType()));
  }

  @Test
  public void testNgRepeatExplicitCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("ngRepeatExplicit.html", "\n", "ngRepeatExplicit.after.html", "angular.js");
  }

  @Test
  public void testInternalDefinitionCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("internalDefinition.html", "internalDefinition.after.html", "angular.js");
  }

  @Test
  public void testNgRepeatExplicitCompletionInScript() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("ngRepeatExplicitInScript.html", "\n", "ngRepeatExplicitInScript.after.html", "angular.js");
  }

  @Test
  public void testNgRepeatExplicitCompletionSameTag() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("ngRepeatExplicitSameTag.html", "\n", "ngRepeatExplicitSameTag.after.html", "angular.js");
  }

  @Test
  public void testNgRepeatExplicitResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatExplicit.resolve.html", "angular.js");
    checkVariableResolve("per<caret>son", "person", JSVariable.class);
  }

  @Test
  public void testInternalDefinitionResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("internalDefinition.after.html", "angular.js");
    checkVariableResolve("ord<caret>ered[", "ordered", JSDefinitionExpression.class);
  }

  @Test
  public void testNgRepeatExplicitResolveInScript() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatExplicitInScript.resolve.html", "angular.js");
    checkVariableResolve("per<caret>son", "person", JSVariable.class);
  }

  @Test
  public void testNgRepeatExplicitResolveSameTag() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatExplicitSameTag.resolve.html", "angular.js");
    checkVariableResolve("per<caret>son", "person", JSVariable.class);
  }

  @Test
  public void testNgRepeatExplicitKeyCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("ngRepeatExplicitHashKey.html", "\n", "ngRepeatExplicitHashKey.after.html", "angular.js");
  }

  @Test
  public void testNgRepeatExplicitKeyResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatExplicitHashKey.resolve.html", "angular.js");
    checkVariableResolve("ke<caret>y", "key", JSVariable.class);
  }

  @Test
  public void testNgRepeatExplicitValueCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("ngRepeatExplicitHashValue.html", "ngRepeatExplicitHashValue.after.html", "angular.js");
  }

  @Test
  public void testNgRepeatExplicitValueResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatExplicitHashValue.resolve.html", "angular.js");
    checkVariableResolve("val<caret>ue", "value", JSVariable.class);
  }

  @Test
  public void testNgRepeatEndCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletion("ngRepeatEnd.html", "ngRepeatEnd.after.html", "angular.js");
  }

  @Test
  public void testNgRepeatEndResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatEnd.resolve.html", "angular.js");
    checkVariableResolve("aNo<caret>te", "aNote", JSVariable.class);
  }

  @Test
  public void testNgControllerAliasCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("ngControllerAlias.html", "\n", "ngControllerAlias.after.html", "angular.js", "custom.js");
  }

  @Test
  public void testNgControllerAliasHighlighting() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngControllerAlias.highlight.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  @Test
  public void testNgControllerAliasResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngControllerAlias.resolve.html", "angular.js", "custom.js");
    checkVariableResolve("ap<caret>p", "app", JSDefinitionExpression.class);
  }

  @Test
  public void testEmmetBeforeInjection() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngController.emmet.html", "angular.js", "custom.js");
    JSTestUtils.runEmmetTemplate(myFixture);
    myFixture.checkResultByFile("ngController.emmet.after.html");
  }

  @Test
  public void testComment() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("comment.html", "angular.js");
  }

  @Test
  public void testCommentWithFollowingInterpolation() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles(testName.get() + ".html", "angular.js");
    ReadAction.run(() -> {
      assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    });
  }

  @Test
  public void testCustomStartDelimiterEmptyWhenTyping() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final String name = testName.get();
    myFixture.configureByFiles(name + ".html", "angular.js", name + ".js");
    ReadAction.run(() -> {
      assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
      assertBraces("{{", "]]");
    });
  }

  private void assertBraces(@NotNull String expectedStart, @NotNull String expectedEnd) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final Pair<String, String> braces = AngularJSInjector.BRACES_FACTORY.fun(myFixture.getFile());
    assertNotNull(braces);
    assertEquals(expectedStart, braces.getFirst());
    assertEquals(expectedEnd, braces.getSecond());
  }

  @Test
  public void testCustomEndDelimiterEmptyWhenTyping() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final String name = testName.get();
    myFixture.configureByFiles(name + ".html", "angular.js", name + ".js");
    ReadAction.run(() -> {
      assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
      assertBraces("[[", "}}");
    });
  }

  @Test
  public void testAngularInterpolationCrossesCommentArea() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles(testName.get() + ".html", "angular.js");
    ReadAction.run(() -> {
      assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    });
  }

  @Test
  public void testCustomDelimiters() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customDelimiters.html", "angular.js", "customDelimiters.js");
    ReadAction.run(() -> {
      assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
      assertBraces("[[", "]]");
    });
  }

  @Test
  public void testCustomDelimitersInline() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customDelimitersInline.html", "angular.js", "customDelimitersInline.js");
    ReadAction.run(() -> {
      assertEquals(HTMLLanguage.INSTANCE, myFixture.getFile().getLanguage());
      assertBraces("[[", "]]");
    });
  }

  @Test
  public void testCustomDelimitersDefaultIgnored() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customDelimitersDefaultIgnored.html", "angular.js", "customDelimitersDefaultIgnored.js");
    ReadAction.run(() -> {
      assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
      assertBraces("{{", "}}");
    });
  }

  @Test
  public void testDefaultDelimitersInJSX() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("defaultDelimiters.jsx", "angular.js");
    ReadAction.run(() -> {
      assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    });
  }

  @Test
  public void testNgRepeat() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("ngRepeatExplicit.jsx", "angular.js");
    ReadAction.run(() -> {
      assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    });
  }

  @Test
  public void testCustomDelimitersSameStartEnd() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("customDelimitersSameStartEnd.html", "angular.js", "customDelimitersSameStartEnd.js");
    ReadAction.run(() -> {
      assertEquals(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
      assertBraces("**", "**");
    });
  }

  @Test
  public void testNgNonBindable() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles(testName.get() + ".html", "angular.js");
    ReadAction.run(() -> {
      assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    });
  }

  @Test
  public void testNoQuotes() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("noQuotes.html", "angular.js");
  }

  @Test
  public void testBadExpression() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(BadExpressionStatementJSInspection.class);
    myFixture.configureByFiles("badExpression.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  @Test
  public void testCommaExpression() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(CommaExpressionJSInspection.class);
    myFixture.configureByFiles("commaExpression.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  private <T extends JSElement> T checkVariableResolve(final String signature, final String varName, final Class<T> varClass) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    return AngularTestUtil.checkVariableResolve(signature, varName, varClass, myFixture);
  }

  @Test
  public void testMessageFormat() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("messageFormat.html", "messageFormatController.js", "angular.js");
    myFixture.checkHighlighting();
  }

  @Test
  public void testElementOnly() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("select.html", "angular.js");
    ReadAction.run(() -> {
      assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    });
  }

  @Test
  public void testNoXml() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("noXml.xml", "angular.js");
    ReadAction.run(() -> {
      assertNotSame(AngularJSLanguage.INSTANCE, myFixture.getFile().getLanguage());
    });
  }

  @Test
  public void testBitwise() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.enableInspections(JSBitwiseOperatorUsageInspection.class);
    myFixture.configureByFiles("bitwise.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

}
