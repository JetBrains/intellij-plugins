package com.jetbrains.dart.analysisServer;

import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.util.ProperTextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.findUsages.DartServerFindUsagesHandler;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class DartServerFindUsagesTest extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @NotNull
  private Collection<UsageInfo> findUsages(@NotNull final SearchScope scope) {
    final PsiElement elementToSearch = getFile().findElementAt(getEditor().getCaretModel().getOffset());
    assertNotNull(elementToSearch);

    final DartServerFindUsagesHandler handler = new DartServerFindUsagesHandler(elementToSearch);
    CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<UsageInfo>();
    handler.processElementUsages(elementToSearch, processor, new FindUsagesOptions(scope));

    return processor.getResults();
  }

  private void checkUsages(@NotNull final SearchScope scope, @NotNull final String... expected) {
    final String[] actualResult = ContainerUtil.map2Array(findUsages(scope), String.class, new Function<UsageInfo, String>() {
      @Override
      public String fun(UsageInfo info) {
        final PsiElement element = info.getElement();
        assertNotNull(element);
        final ProperTextRange range = info.getRangeInElement();
        assertNotNull(range);
        final int startOffset = element.getTextRange().getStartOffset() + range.getStartOffset();
        final int endOffset = element.getTextRange().getStartOffset() + range.getEndOffset();
        return element.getClass().getSimpleName() + " in " + element.getContainingFile().getName() + "@" + startOffset + ":" + endOffset +
               (info.isDynamicUsage() ? " (dynamic usage)" : "") +
               (info.isNonCodeUsage() ? " (non-code usage)" : "");
      }
    });

    assertSameElements(actualResult, expected);
  }

  public void testBoolUsagesWithScope() {
    final PsiFile psiFile1 = myFixture.configureByText("file.dart", "/// [bool]\n" +
                                                                    "<caret>bool foo() {\n" +
                                                                    "  var bool = #bool;\n" +
                                                                    "}");
    final PsiFile psiFile2 = myFixture.addFileToProject("file1.dart", "bool x;");

    myFixture.doHighlighting(); // warm up

    final String[] allProjectUsages = {"PsiCommentImpl in file0.dart@5:9 (non-code usage)",
      "DartReferenceExpressionImpl in file0.dart@11:15",
      "DartReferenceExpressionImpl in file1.dart@0:4"};

    checkUsages(new LocalSearchScope(psiFile1),
                "PsiCommentImpl in file0.dart@5:9 (non-code usage)",
                "DartReferenceExpressionImpl in file0.dart@11:15");
    checkUsages(new LocalSearchScope(psiFile2),
                "DartReferenceExpressionImpl in file1.dart@0:4");
    checkUsages(new LocalSearchScope(new PsiFile[]{psiFile1, psiFile2}), allProjectUsages);
    checkUsages(new GlobalSearchScope.FilesScope(getProject(), Collections.singletonList(psiFile1.getVirtualFile())),
                "PsiCommentImpl in file0.dart@5:9 (non-code usage)",
                "DartReferenceExpressionImpl in file0.dart@11:15");
    checkUsages(new GlobalSearchScope.FilesScope(getProject(), Collections.singletonList(psiFile2.getVirtualFile())),
                "DartReferenceExpressionImpl in file1.dart@0:4");
    checkUsages(new GlobalSearchScope.FilesScope(getProject(), Arrays.asList(psiFile1.getVirtualFile(), psiFile2.getVirtualFile())),
                allProjectUsages);
    checkUsages(GlobalSearchScope.projectScope(getProject()), allProjectUsages);
    final Collection<UsageInfo> usages = findUsages(GlobalSearchScope.allScope(getProject()));
    assertTrue(String.valueOf(usages.size()), usages.size() > 620);
  }

  public void testDynamicAndNonCodeUsage() {
    myFixture.configureByText("file.dart", "class Foo {\n" +
                                           "  /**\n" +
                                           "   * [bar] is awesome \n" +
                                           "   */\n" +
                                           "  var bar<caret>;\n" +
                                           "}\n" +
                                           "\n" +
                                           "main () {\n" +
                                           "  Foo x;\n" +
                                           "  var y;\n" +
                                           "  x.bar;  // hard reference \n" +
                                           "  y.bar;  // potential usage \n" +
                                           "}\n");
    myFixture.doHighlighting(); // warm up
    checkUsages(GlobalSearchScope.projectScope(getProject()),
                "LeafPsiElement in file0.dart@24:27 (non-code usage)",
                "DartReferenceExpressionImpl in file0.dart@93:96",
                "DartReferenceExpressionImpl in file0.dart@122:125 (dynamic usage)");
  }
}
