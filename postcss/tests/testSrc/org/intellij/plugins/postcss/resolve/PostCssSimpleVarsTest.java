package org.intellij.plugins.postcss.resolve;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/resolve")
public class PostCssSimpleVarsTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  private void doResolveTest(@NotNull String text, @NotNull String expectedTarget) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByText("foo.pcss", text);
    ReadAction.run(() -> {
      final PsiReference reference = TargetElementUtil.findReference(myFixture.getEditor());
      assertNotNull(reference, "reference not found");
      final PsiElement resolve = reference instanceof PsiMultiReference ? ((PsiMultiReference)reference).multiResolve(false)[0].getElement()
                                                                        : reference.resolve();
      assertNotNull(resolve, "reference not resolved");
      assertEquals(expectedTarget, resolve.getText());
    });
  }

  private void doTestFindUsages(@NotNull final String text, final int expectedUsageCount) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByText("foo.pcss", text);
    final int flags = TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED;
    final PsiElement element = ReadAction.computeBlocking(() -> TargetElementUtil.findTargetElement(myFixture.getEditor(), flags));
    assertEquals(expectedUsageCount, myFixture.findUsages(element).size());
  }

  @Test
  public void testResolvePropertyValue() {
    doResolveTest("""
                    $foo: 10px;
                    .header {
                        width: calc(4 * $<caret>foo);
                    }""",
                  "$foo: 10px;"
    );
  }

  @Test
  public void testResolveSelector() {
    doResolveTest("$foo: 20px;\n" +
                  "$foo<caret> { }",
                  "$foo: 20px;"
    );
  }

  @Test
  public void testResolvePropertyValueInterpolation() {
    doResolveTest("""
                    $foo: 10px;
                    .header {
                        width: calc(4 * $(<caret>foo));
                    }""",
                  "$foo: 10px;"
    );
  }

  @Test
  public void testResolveSelectorInterpolation() {
    doResolveTest("$foo: 20px;\n" +
                  "$(foo<caret>) { }",
                  "$foo: 20px;"
    );
  }

  @Test
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

  @Test
  public void testFindUsagesInOtherFiles() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
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

  @Test
  public void testRename() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
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
    EdtTestUtil.runInEdtAndWait(() -> myFixture.renameElementAtCaret("bar"));
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

  @Test
  public void testFormat() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
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
    WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> {
      CodeStyleManager.getInstance(myFixture.getProject()).reformat(myFixture.getFile());
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