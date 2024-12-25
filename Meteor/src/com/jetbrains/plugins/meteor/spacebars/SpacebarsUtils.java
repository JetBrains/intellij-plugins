package com.jetbrains.plugins.meteor.spacebars;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SpacebarsUtils {

  /**
   * Returns simple HbPsiElementImpl element with inner text = name
   */
  public static @Nullable HbPsiElementImpl createMustacheTag(@NotNull Project project, @NotNull String name, boolean partial) {
    HbPsiFile text = createFileFromText(project, "{{" + (partial ? ">" : "") + name + "}}");

    PsiElement statement = text.getFirstChild();
    if (statement == null) return null;
    PsiElement mustache = statement.getFirstChild();
    if (mustache == null) return null;


    ASTNode mustacheName = mustache.getNode().findChildByType(HbTokenTypes.MUSTACHE_NAME);
    if (mustacheName == null) return null;
    ASTNode path = mustacheName.findChildByType(HbTokenTypes.PATH);
    if (path == null) return null;
    ASTNode id = path.findChildByType(HbTokenTypes.ID);
    if (id == null) return null;
    return (HbPsiElementImpl)id.getPsi();
  }

  private static @NotNull HbPsiFile createFileFromText(final Project project, final @NotNull String text) {
    return (HbPsiFile)PsiFileFactory.getInstance(project).createFileFromText("foo.spacebars", SpacebarsFileType.SPACEBARS_INSTANCE, text);
  }
}
