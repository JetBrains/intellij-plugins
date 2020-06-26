package com.intellij.tapestry.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiType;
import com.intellij.tapestry.lang.TelFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public final class TelPsiUtil {
  private TelPsiUtil() {
  }

  @NonNls
  private static final String NULL_TYPE_NAME = "???";

  @NotNull
  public static TelReferenceExpression parseReference(@NonNls final String text, final Project project) {
    PsiElement expression = parseFtlFile("${" + text + "}", project);
    final PsiElement interpolation = expression.getFirstChild();
    final PsiElement elStart = interpolation.getFirstChild();
    return (TelReferenceExpression)elStart.getNextSibling();
  }

  @NotNull
  public static PsiElement parseFtlFile(@NonNls final String text, final Project project) {
    PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("dummy.tel", TelFileType.INSTANCE, text);
    return file.getFirstChild();
  }

  @NotNull
  public static String getPresentableText(@Nullable final PsiType psiType) {
    return psiType == null ? NULL_TYPE_NAME : psiType.getPresentableText();
  }
}
