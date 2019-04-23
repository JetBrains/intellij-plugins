package org.intellij.plugins.postcss.resolve;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/resolve/simpleVars")
public class PostCssSimpleVarsTest extends PostCssFixtureTestCase {
  private void doResolveTest(@NotNull String text, @NotNull String expectedTarget) {
    myFixture.configureByText("foo.pcss", text);
    final PsiReference reference = TargetElementUtil.findReference(myFixture.getEditor());
    assertNotNull("reference not found", reference);
    final PsiElement resolve = reference instanceof PsiMultiReference ? ((PsiMultiReference)reference).multiResolve(false)[0].getElement()
                                                                      : reference.resolve();
    assertNotNull("reference not resolved", resolve);
    assertEquals(expectedTarget, resolve.getText());
  }

  private void doTestFindUsages(@NotNull final String text, final int expectedUsageCount) {
    myFixture.configureByText("foo.pcss", text);
    final int flags = TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED;
    final PsiElement element = TargetElementUtil.findTargetElement(myFixture.getEditor(), flags);
    assertEquals(expectedUsageCount, myFixture.findUsages(element).size());
  }

  public void testResolvePropertyValue() {
    doResolveTest("$foo: 10px;\n" +
                  ".header {\n" +
                  "    width: calc(4 * $<caret>foo);\n" +
                  "}",
                  "$foo: 10px;"
    );
  }

  public void testResolveSelector() {
    doResolveTest("$foo: 20px;\n" +
                  "$foo<caret> { }",
                  "$foo: 20px;"
    );
  }

  public void testFindUsages() {
    doTestFindUsages("$f<caret>oo: 10px;\n" +
                     "$foo {\n" +
                     "    width: calc(4 * $foo);\n" +
                     "    height: $foo);\n" +
                     "}",
                     3
    );
  }

  public void testRename() {
    myFixture.configureByText("foo.pcss",
                              "$foo: 10px;\n" +
                              "$foo {\n" +
                              "    width: calc(4 * $foo);\n" +
                              "    height: $fo<caret>o);\n" +
                              "}");
    myFixture.renameElementAtCaret("bar");
    myFixture.checkResult("$bar: 10px;\n" +
                          "$bar {\n" +
                          "    width: calc(4 * $bar);\n" +
                          "    height: $ba<caret>r);\n" +
                          "}");
  }

  public void testFormat() {
    myFixture.configureByText("foo.pcss",
                              "$dir: top; $blue: #056ef0;\n" +
                              "$column: 200px;\n" +
                              ".menu_link {}\n");
    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
      CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
    });
    myFixture.checkResult("$dir: top;\n" +
                          "$blue: #056ef0;\n" +
                          "$column: 200px;\n" +
                          "\n" +
                          ".menu_link {\n" +
                          "}\n");
  }
}