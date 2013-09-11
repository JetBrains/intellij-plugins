package org.osmorc.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.util.OsgiPsiUtil;

/**
 * @author Vladislav.Soroka
 */
public class UnknownManifestPackageInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {

      public void visitElement(final PsiElement element) {
        if (element instanceof HeaderValuePart && OsmorcFacet.hasOsmorcFacet(element) && !element.getText().isEmpty()) {
          PsiReference[] references = element.getReferences();
          for (int i = 0; i < references.length; i++) {
            PsiReference reference = references[i];
            if (reference.getCanonicalText().equals("*") && i == references.length - 1) {
              break;
            }
            if (reference.resolve() == null) {
              TextRange range = OsgiPsiUtil.trimRange(element, reference.getRangeInElement());
              holder.registerProblem(element, range, OsmorcBundle.message("UnknownManifestPackage.message"));
            }
          }
        }
      }
    };
  }
}
