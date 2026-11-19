package org.angularjs.codeInsight;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class FiltersTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "filters");
  }

  @Test
  public void testFilterCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("filter.html", "angular.js", "custom.js");
    final List<String> variants = myFixture.getCompletionVariants("filter.html");
    assertThat(variants).contains("lowercase", "json", "filta");
  }

  @Test
  public void testFilterBuiltinResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("filterBuiltin.resolve.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("js<caret>on", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("angular.js", resolve.getContainingFile().getName());
    });
  }

  @Test
  public void testFilterCustomResolve() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("filterCustom.resolve.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("fil<caret>ta", myFixture.getFile());
    ReadAction.run(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertNotNull(resolve);
      assertEquals("custom.js", resolve.getContainingFile().getName());
      assertEquals("\"filta\"", resolve.getParent().getText());
    });
  }

}
