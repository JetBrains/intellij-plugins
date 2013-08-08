package org.osmorc.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.ManifestConstants;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.Header;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class WrongImportPackageInspection extends LocalInspectionTool {

  private BundleManager bundleManager;

  private static boolean isImportPackageHeader(PsiElement element) {
    if (element instanceof Header &&
        ManifestConstants.Headers.IMPORT_PACKAGE.equals(Header.class.cast(element).getName())) {
      return true;
    }
    else {
      return false;
    }
  }

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return OsmorcBundle.message("WrongImportPackageInspection.group-name");
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
    return OsmorcBundle.message("WrongImportPackageInspection.display-name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "osmorcWrongImportPackage";
  }

  @NotNull
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {

      public void visitElement(PsiElement element) {
        if (OsmorcFacet.hasOsmorcFacet(element) && isImportPackageHeader(element)) {
          Header header = (Header)element;
          for (Clause clause : header.getClauses()) {
            HeaderValuePart valuePart = clause.getValue();
            String packageSpec = clause.getClauseText();
            if (valuePart != null && !valuePart.getText().isEmpty() && !isProvided(element, packageSpec)) {
              holder.registerProblem(valuePart, OsmorcBundle.message(
                "WrongImportPackageInspection.problem.message.not-exported-package",
                valuePart.getUnwrappedText(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
            }
          }
        }
      }
    };
  }

  private boolean isProvided(PsiElement element, String packageSpec) {
    if(packageSpec != null && !packageSpec.contains("*")) {
      Set<ManifestHolder> manifestHolders = getBundleManager(element).getBundleCache().whoProvides(packageSpec);
      return !manifestHolders.isEmpty();
    } else {
      // TODO add '*' patterns support. May require update of the Manifest parser lib of the plugin (felix-utils.jar) to the newer version.
      return true;
    }
  }

  private BundleManager getBundleManager(PsiElement element) {
    if (bundleManager == null) {
      bundleManager = ServiceManager.getService(element.getProject(), BundleManager.class);
    }
    return bundleManager;
  }
}
