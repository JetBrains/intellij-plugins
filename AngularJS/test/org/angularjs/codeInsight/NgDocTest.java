package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
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

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Konstantin.Ulitin
 */
@TestApplication
public class NgDocTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private final TestFixture<String> testNameUpper = testNameFixture(false);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "ngDoc");
  }

  @Test
  public void testModule15() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles(testNameUpper.get() + ".js");
    ReadAction.nonBlocking(() -> {
      PsiReference ref = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
      assertNotNull(ref);
      PsiElement resolve = ref.resolve();
      assertInstanceOf(JSImplicitElement.class, resolve);
    }).executeSynchronously();
  }
}
