package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class CucumberPsiTreeListenerTest extends BaseCucumberJavaResolveTest {
  public void testCreationOfStepDefinition() {
    doTestCreation("treeListener", "I p<caret>ay 25", "@cucumber.api.java.en.When(\"^I pay (\\\\d+)$\")\npublic void i_pay(int amount) {}");
  }

  public void testDeletionOfStepDefinition() {
    doTestDeletion("treeListener", "my change sh<caret>ould be 4", "my_change_should_be_");
  }

  private PsiClass getStepDefClass() {
    final PsiFile stepDefFile = findPsiFileInTempDirBy("ShoppingStepdefs.java");
    final PsiJavaFile javaFile = (PsiJavaFile)stepDefFile;
    final PsiClass psiClass = PsiTreeUtil.getChildOfType(javaFile, PsiClass.class);
    assert psiClass != null;

    return psiClass;
  }

  private String createStepDefinition(@NotNull final String stepDef) {
    final PsiClass psiClass = getStepDefClass();
    final PsiFile psiFile = psiClass.getContainingFile();

    final Ref<String> createdMethodName = new Ref<>();

    WriteCommandAction.writeCommandAction(getProject(), psiFile).run(() -> {
      final PsiElementFactory factory = JavaPsiFacade.getInstance(getProject()).getElementFactory();
      final PsiMethod method = factory.createMethodFromText(stepDef, psiClass);
      psiClass.add(method);
      createdMethodName.set(method.getName());
    });

    return createdMethodName.get();
  }

  private void deleteStepDefinition(@NotNull final String stepDefName) {
    final PsiClass psiClass = getStepDefClass();
    final PsiFile psiFile = psiClass.getContainingFile();

    WriteCommandAction.writeCommandAction(getProject(), psiFile).run(() -> {
      for (PsiMethod method : psiClass.getAllMethods()) {
        if (method.getName().equals(stepDefName)) {
          method.delete();
          break;
        }
      }
    });
  }

  private void doTestCreation(@NotNull final String folder, @NotNull final String step, @NotNull final String stepDefinitionContent) {
    init(folder);

    checkReference(step, null);
    final String stepDefinitionName = createStepDefinition(stepDefinitionContent);
    checkReference(step, stepDefinitionName);
  }

  private void doTestDeletion(@NotNull final String folder, @NotNull final String step, @NotNull final String stepDefinitionName) {
    init(folder);

    checkReference(step, stepDefinitionName);
    deleteStepDefinition(stepDefinitionName);
    checkReference(step, null);
  }
}
