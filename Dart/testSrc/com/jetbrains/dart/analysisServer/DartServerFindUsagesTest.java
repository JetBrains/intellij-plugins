// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.openapi.util.ProperTextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.findUsages.DartServerFindUsagesHandler;
import com.jetbrains.lang.dart.ide.findUsages.DartUsageTypeProvider;
import com.jetbrains.lang.dart.psi.impl.DartFileReference;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class DartServerFindUsagesTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @NotNull
  private Collection<UsageInfo> findUsages(@NotNull final SearchScope scope) {
    final PsiElement elementToSearch = getFile().findElementAt(getEditor().getCaretModel().getOffset());
    assertNotNull(elementToSearch);

    final FindUsagesManager manager = ((FindManagerImpl)FindManager.getInstance(getProject())).getFindUsagesManager();
    final FindUsagesHandler handler = manager.getFindUsagesHandler(elementToSearch, false);
    assertInstanceOf(handler, DartServerFindUsagesHandler.class);
    final CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<>();
    handler.processElementUsages(elementToSearch, processor, new FindUsagesOptions(scope));

    return processor.getResults();
  }

  private void checkUsages(@NotNull final SearchScope scope, final String @NotNull ... expected) {
    final String[] actualResult = ContainerUtil.map2Array(findUsages(scope), String.class, info -> {
      final PsiElement element = info.getElement();
      assertNotNull(element);
      final ProperTextRange range = info.getRangeInElement();
      assertNotNull(range);
      final int startOffset = element.getTextRange().getStartOffset() + range.getStartOffset();
      final int endOffset = element.getTextRange().getStartOffset() + range.getEndOffset();
      assert (startOffset >= 0);
      assert (endOffset > startOffset);
      final UsageType usageType = new DartUsageTypeProvider().getUsageType(element);
      return element.getClass().getSimpleName() + " in " + element.getContainingFile().getName() + "@" + startOffset + ":" + endOffset +
             (info.isDynamicUsage() ? " (dynamic usage)" : "") +
             (info.isNonCodeUsage() ? " (non-code usage)" : "") +
             (usageType != null ? " [" + usageType.toString() + "]" : " [<null> usage type]");
    });

    assertSameElements(actualResult, expected);
  }

  public void testBoolUsagesWithScope() {
    final PsiFile psiFile1 = myFixture.configureByText("file.dart", """
      /// [bool]
      <caret>bool foo() {
        var bool = #bool;
      }""");
    final PsiFile psiFile2 = myFixture.addFileToProject("file1.dart", "bool x;");

    myFixture.doHighlighting(); // warm up

    final String[] allProjectUsages = {"PsiCommentImpl in " + psiFile1.getName() + "@5:9 (non-code usage) [<null> usage type]",
      "DartReferenceExpressionImpl in " + psiFile1.getName() + "@11:15 [Value read]",
      "DartReferenceExpressionImpl in file1.dart@0:4 [Value read]"};

    checkUsages(new LocalSearchScope(psiFile1),
                "PsiCommentImpl in " + psiFile1.getName() + "@5:9 (non-code usage) [<null> usage type]",
                "DartReferenceExpressionImpl in " + psiFile1.getName() + "@11:15 [Value read]");
    checkUsages(new LocalSearchScope(psiFile2),
                "DartReferenceExpressionImpl in file1.dart@0:4 [Value read]");
    checkUsages(new LocalSearchScope(new PsiFile[]{psiFile1, psiFile2}), allProjectUsages);
    checkUsages(GlobalSearchScope.fileScope(getProject(), psiFile1.getVirtualFile()),
                "PsiCommentImpl in " + psiFile1.getName() + "@5:9 (non-code usage) [<null> usage type]",
                "DartReferenceExpressionImpl in " + psiFile1.getName() + "@11:15 [Value read]");
    checkUsages(GlobalSearchScope.fileScope(getProject(), psiFile2.getVirtualFile()),
                "DartReferenceExpressionImpl in file1.dart@0:4 [Value read]");
    checkUsages(GlobalSearchScope.filesScope(getProject(), Arrays.asList(psiFile1.getVirtualFile(), psiFile2.getVirtualFile())),
                allProjectUsages);
    checkUsages(GlobalSearchScope.projectScope(getProject()), allProjectUsages);
    final Collection<UsageInfo> usages = findUsages(GlobalSearchScope.allScope(getProject()));
    assertTrue(String.valueOf(usages.size()), usages.size() > 640);
  }

  public void testDynamicAndNonCodeUsage() {
    myFixture.configureByText("file.dart", """
      class Foo {
        /**
         * [bar] is awesome\s
         */
        var bar<caret>;
      }

      main () {
        Foo x;
        var y;
        x.bar;  // hard reference\s
        y.bar;  // potential usage\s
      }
      """);
    myFixture.doHighlighting(); // warm up
    checkUsages(GlobalSearchScope.projectScope(getProject()),
                "LeafPsiElement in " + getFile().getName() + "@24:27 (non-code usage) [<null> usage type]",
                "DartReferenceExpressionImpl in " + getFile().getName() + "@93:96 [Value read]",
                "DartReferenceExpressionImpl in " + getFile().getName() + "@122:125 (dynamic usage) [Value read]");
  }

  public void testCascadeReadUsage() {
    myFixture.configureByText("file.dart", """
      class Foo {
        int someInt;
      }

      bar (Foo foo) {
        foo
        ..someInt<caret>;
      }
      """);
    myFixture.doHighlighting(); // warm up
    checkUsages(GlobalSearchScope.projectScope(getProject()),
                "DartReferenceExpressionImpl in file.dart@56:63 [Value read]");
  }

  public void testCascadeWriteUsage() {
    myFixture.configureByText("file.dart", """
      class Foo {
        int someInt;
      }

      bar (Foo foo) {
        foo
        ..someInt<caret> = 1;
      }
      """);
    myFixture.doHighlighting(); // warm up
    checkUsages(GlobalSearchScope.projectScope(getProject()),
                "DartReferenceExpressionImpl in file.dart@56:63 [Value write]");
  }

  public void testFileUsage() {
    final PsiFile barFile = myFixture.configureByText("bar.dart", "");
    // it is important that foo.dart is not open in the editor
    myFixture.addFileToProject("foo.dart", "import '" + barFile.getName() + "';");
    myFixture.doHighlighting(); // warm up

    final FindUsagesManager manager = ((FindManagerImpl)FindManager.getInstance(getProject())).getFindUsagesManager();
    final FindUsagesHandler handler = manager.getFindUsagesHandler(getFile(), false);
    assertNotNull(handler);
    assertFalse(handler instanceof DartServerFindUsagesHandler);
    final Collection<PsiReference> usages = handler.findReferencesToHighlight(getFile(), GlobalSearchScope.allScope(getProject()));

    assertSize(1, usages);
    final PsiReference reference = usages.iterator().next();
    assertInstanceOf(reference, DartFileReference.class);
    assertEquals("foo.dart", reference.getElement().getContainingFile().getName());
    assertEquals("import '" + barFile.getName() + "';", reference.getElement().getParent().getText());
  }
}
