package org.osmorc.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.manifest.lang.ManifestLanguage;
import org.osmorc.manifest.lang.psi.Header;
import org.osmorc.manifest.lang.psi.HeaderValuePart;
import org.osmorc.manifest.lang.psi.ManifestFile;
import org.osmorc.util.OsmorcResourceBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class UnknownManifectPackageInspection extends LocalInspectionTool {

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return OsmorcResourceBundle.message("UnknownManifestPackageInspection.group-name");
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
    return OsmorcResourceBundle.message("UnknownManifestPackageInspection.display-name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "osmorcUnknownManifestPackage";
  }

  @NotNull
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {

      public void visitElement(PsiElement element) {
        if (OsmorcFacet.hasOsmorcFacet(element) && element instanceof HeaderValuePart) {
          for(PsiReference reference : element.getReferences()) {
            if(reference instanceof PsiPackageReference && reference.resolve() == null) {
              holder.registerProblemForReference(reference, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                                 OsmorcResourceBundle.message("UnknownManifestPackageInspection.problem.message.unknown-package", reference.getCanonicalText()));
            }
          }
          }
        }
    };
  }
}
