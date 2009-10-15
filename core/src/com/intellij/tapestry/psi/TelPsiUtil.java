package com.intellij.tapestry.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.tapestry.lang.TelFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: 09.10.2009
 *         Time: 18:49:07
 */
public class TelPsiUtil {
  private TelPsiUtil() {
  }

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
}
