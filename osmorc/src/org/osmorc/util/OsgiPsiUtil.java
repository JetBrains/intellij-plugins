package org.osmorc.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.ManifestFileTypeFactory;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.ManifestFile;

public class OsgiPsiUtil {
  private OsgiPsiUtil() { }

  @NotNull
  public static Header createHeader(@NotNull Project project, @NotNull String headerName, @NotNull String valueText) {
    String text = String.format("%s: %s\n", headerName, valueText);
    PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("DUMMY.MF", ManifestFileTypeFactory.MANIFEST, text);
    Header header = ((ManifestFile)file).getHeader(headerName);
    if (header == null) {
      throw new IncorrectOperationException("Bad header: '" + text + "'");
    }
    return header;
  }

  @NotNull
  public static TextRange trimRange(@NotNull PsiElement element) {
    return trimRange(element, new TextRange(0, element.getTextLength()));
  }

  @NotNull
  public static TextRange trimRange(@NotNull PsiElement element, @NotNull TextRange range) {
    String text = element.getText();
    String substring = range.substring(text).trim();
    int start = text.indexOf(substring);
    int end = start + substring.length();
    return new TextRange(start, end);
  }

  public static boolean isHeader(@Nullable PsiElement element, @NotNull String headerName) {
    return element instanceof Header && headerName.equals(((Header)element).getName());
  }
}
