package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;

@TestApplication
public class CucumberPsiTreeListenerTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumber2ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture);

  @Test
  public void testCreationOfStepDefinition() {
    doTestCreation("treeListener", "I p<caret>ay 25", "@cucumber.api.java.en.When(\"^I pay (\\\\d+)$\")\npublic void i_pay(int amount) {}");
  }

  @Test
  public void testDeletionOfStepDefinition() {
    doTestDeletion("treeListener", "my change sh<caret>ould be 4", "my_change_should_be_");
  }

  private PsiClass getStepDefClass() {
    final PsiFile stepDefFile = cucumber.get().findPsiFileInTempDirBy("ShoppingStepdefs.java");
    final PsiJavaFile javaFile = (PsiJavaFile)stepDefFile;
    final PsiClass psiClass = ReadAction.computeBlocking(() -> PsiTreeUtil.getChildOfType(javaFile, PsiClass.class));
    assert psiClass != null;

    return psiClass;
  }

  private String createStepDefinition(@NotNull final String stepDef) {
    final PsiClass psiClass = getStepDefClass();
    final PsiFile psiFile = ReadAction.computeBlocking(() -> psiClass.getContainingFile());

    final Ref<String> createdMethodName = new Ref<>();

    WriteCommandAction.writeCommandAction(cucumber.get().getFixture().getProject(), psiFile).run(() -> {
      final PsiElementFactory factory = JavaPsiFacade.getInstance(cucumber.get().getFixture().getProject()).getElementFactory();
      final PsiMethod method = factory.createMethodFromText(stepDef, psiClass);
      psiClass.add(method);
      createdMethodName.set(method.getName());
    });

    return createdMethodName.get();
  }

  private void deleteStepDefinition(@NotNull final String stepDefName) {
    final PsiClass psiClass = getStepDefClass();
    final PsiFile psiFile = ReadAction.computeBlocking(() -> psiClass.getContainingFile());

    WriteCommandAction.writeCommandAction(cucumber.get().getFixture().getProject(), psiFile).run(() -> {
      for (PsiMethod method : psiClass.getAllMethods()) {
        if (method.getName().equals(stepDefName)) {
          method.delete();
          break;
        }
      }
    });
  }

  private void doTestCreation(@NotNull final String folder, @NotNull final String step, @NotNull final String stepDefinitionContent) {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init(folder);

    resolve.checkReference(step, null);
    final String stepDefinitionName = createStepDefinition(stepDefinitionContent);
    resolve.checkReference(step, stepDefinitionName);
  }

  private void doTestDeletion(@NotNull final String folder, @NotNull final String step, @NotNull final String stepDefinitionName) {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init(folder);

    resolve.checkReference(step, stepDefinitionName);
    deleteStepDefinition(stepDefinitionName);
    resolve.checkReference(step, null);
  }
}
