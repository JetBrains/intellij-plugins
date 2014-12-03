package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartTryStatement;
import org.jetbrains.annotations.Nullable;

public class DartWithTryCatchFinallySurrounder extends DartBlockStatementSurrounderBase {
  @Override
  public String getTemplateDescription() {
    return "try / catch / finally";
  }

  @Override
  protected String getTemplateText() {
    return "try {\n} catch (e) {\ncaret_here: print(e);\n} finally {\n}";
  }

  @Override
  @Nullable
  protected PsiElement findElementToDelete(PsiElement surrounder) {
    //noinspection ConstantConditions
    return ((DartTryStatement)surrounder).getOnPartList().get(0).getBlock().getStatements().getFirstChild(); // todo preselect print(e);
  }
}
