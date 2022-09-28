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
    doResolveTest("""
                    $foo: 10px;
                    .header {
                        width: calc(4 * $<caret>foo);
                    }""",
                  "$foo: 10px;"
    );
  }

  public void testResolveSelector() {
    doResolveTest("$foo: 20px;\n" +
                  "$foo<caret> { }",
                  "$foo: 20px;"
    );
  }

  public void testResolvePropertyValueInterpolation() {
    doResolveTest("""
                    $foo: 10px;
                    .header {
                        width: calc(4 * $(<caret>foo));
                    }""",
                  "$foo: 10px;"
    );
  }

  public void testResolveSelectorInterpolation() {
    doResolveTest("$foo: 20px;\n" +
                  "$(foo<caret>) { }",
                  "$foo: 20px;"
    );
  }

  public void testFindUsages() {
    doTestFindUsages("""
                       $f<caret>oo: 10px;
                       $foo $(foo) {
                           width: calc(4 * $foo);
                           height: $foo;
                           width: calc(4 * $(foo));
                           height: $(foo);
                           height: a$(foo);
                           height: $(foo)b;
                       }""",
                     8
    );
  }

  public void testFindUsagesInOtherFiles() {
    // 4 usages in one.pcss
    myFixture.addFileToProject("one.pcss",
                               """
                                 $foo: 0;
                                 .bar {
                                   x: $foo $foo $(foo) $(foo);
                                 }""");
    // 8 usages in two.pcss.
    myFixture.addFileToProject("two.pcss",
                               """
                                 @import 'one.pcss';
                                 .bar {
                                   x: $foo $foo $foo $foo $foo $foo $foo $foo;
                                 }""");
    // three.pcss doesn't import one.pcss, so no usages here
    myFixture.addFileToProject("three.pcss",
                               """
                                 .bar {
                                   x: $foo $foo $foo $foo $(foo) $(foo) $(foo) $(foo);
                                   x: $foo $foo $foo $foo $(foo) $(foo) $(foo) $(foo);
                                 }""");

    // 6 usages in this file
    doTestFindUsages("""
                       @import 'one.pcss';
                       .bar {
                         x: $<caret>foo $foo $foo $(foo) $(foo) $(foo);
                       }""",
                     18
    );
  }

  public void testRename() {
    myFixture.configureByText("foo.pcss",
                              """
                                $foo: 10px;
                                $foo, $(foo) {
                                    width: calc(4 * $foo);
                                    height: $fo<caret>o;
                                    width: calc(4 * $(foo));
                                    height: $(foo);
                                    height: a$(foo);
                                    height: $(foo)b;
                                }""");
    myFixture.renameElementAtCaret("bar");
    myFixture.checkResult("""
                            $bar: 10px;
                            $bar, $(bar) {
                                width: calc(4 * $bar);
                                height: $ba<caret>r;
                                width: calc(4 * $(bar));
                                height: $(bar);
                                height: a$(bar);
                                height: $(bar)b;
                            }""");
  }

  public void testFormat() {
    myFixture.configureByText("foo.pcss",
                              """
                                $dir: top; $blue: #056ef0;
                                $column: 200px;
                                .menu_link, a-$(dir) {
                                    width: calc(4 * $(foo));
                                    height: $(foo);
                                    margin-$(dir): 0;
                                    height: a$(foo);
                                    height: $(foo)b;
                                }
                                """);
    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
      CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
    });
    myFixture.checkResult("""
                            $dir: top;
                            $blue: #056ef0;
                            $column: 200px;

                            .menu_link, a-$(dir) {
                                width: calc(4 * $(foo));
                                height: $(foo);
                                margin-$(dir): 0;
                                height: a$(foo);
                                height: $(foo)b;
                            }
                            """);
  }
}