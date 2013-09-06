package org.osmorc.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;

/**
 * @author Vladislav.Soroka
 */
public class UnknownManifestPackageInspection extends LocalInspectionTool {
  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return OsmorcBundle.message("UnknownManifestPackageInspection.group-name");
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return OsmorcBundle.message("UnknownManifestPackageInspection.display-name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "osmorcUnknownManifestPackage";
  }

  @NotNull
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {

      public void visitElement(final PsiElement element) {
        if (OsmorcFacet.hasOsmorcFacet(element) && element instanceof HeaderValuePart && !element.getText().isEmpty()) {
          PsiReference[] references = element.getReferences();
          for (int i = 0; i < references.length; i++) {
            PsiReference reference = references[i];
            if (reference.getCanonicalText().equals("*") && i == references.length - 1) {
              break;
            }
            if (reference instanceof PsiPackageReference && reference.resolve() == null) {
              holder.registerProblemForReference(
                reference, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                OsmorcBundle.message("UnknownManifestPackageInspection.problem.message.unknown-package", reference.getCanonicalText()));
            }
          }
        }
      }
    };
  }
}
