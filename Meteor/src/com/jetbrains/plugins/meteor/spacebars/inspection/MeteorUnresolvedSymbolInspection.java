package com.jetbrains.plugins.meteor.spacebars.inspection;


import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsLanguageDialect;
import com.jetbrains.plugins.meteor.spacebars.templates.helpers.MeteorMustacheTagPsiReference;
import com.jetbrains.plugins.meteor.spacebars.templates.helpers.MeteorSpacebarsReferenceContributor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class MeteorUnresolvedSymbolInspection extends LocalInspectionTool {
  public static final TokenSet OPEN_TOKENS = TokenSet.create(HbTokenTypes.OPEN, HbTokenTypes.OPEN_PARTIAL);
  private static final Set<String> ignoredParentTags = ContainerUtil.newHashSet("polymer-element");
  public static final String THIS_KEYWORD = "this";

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {

      @Override
      public void visitElement(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null || !file.getLanguage().is(SpacebarsLanguageDialect.INSTANCE)) {
          super.visitElement(element);
          return;
        }
        if (MeteorSpacebarsReferenceContributor.possibleMustacheBlockTag().accepts(element) ||
            MeteorSpacebarsReferenceContributor.possibleMustacheTag(OPEN_TOKENS).accepts(element)) {
          super.visitElement(element);

          if (element instanceof HbPsiElementImpl) {
            if (THIS_KEYWORD.equals(element.getText())) {
              return;
            }
          }

          PsiReference[] references = element.getReferences();
          for (PsiReference reference : references) {
            if (reference instanceof MeteorMustacheTagPsiReference &&
                ((MeteorMustacheTagPsiReference)reference).multiResolve(false, true).length == 0 &&
                !hasIgnoredParentTag(element)) {


              holder.registerProblem(element, MeteorBundle.message("meteor.inspection.unresolved"));
              return;
            }
          }
        }
      }
    };
  }

  private static boolean hasIgnoredParentTag(@NotNull PsiElement element) {
    PsiElement place = MeteorMustacheTagPsiReference.getTagForPlace(element);
    if (place == null || place instanceof PsiFile) return false;

    XmlTag parent = (XmlTag)PsiTreeUtil.findFirstParent(place, element1 -> element1 instanceof XmlTag && ignoredParentTags.contains(((XmlTag)element1).getName()));
    return parent != null;
  }
}
