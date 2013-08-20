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
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.ManifestConstants;
import org.osmorc.manifest.lang.psi.Clause;

/**
 * @author Vladislav.Soroka
 */
public class WrongImportPackageInspection extends LocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return OsmorcBundle.message("WrongImportPackageInspection.group-name");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return OsmorcBundle.message("WrongImportPackageInspection.display-name");
  }

  @NonNls
  @NotNull
  @Override
  public String getShortName() {
    return "osmorcWrongImportPackage";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      private final BundleManager myBundleManager = ServiceManager.getService(holder.getProject(), BundleManager.class);

      @Override
      public void visitElement(PsiElement element) {
        if (isImportPackageHeader(element) && OsmorcFacet.hasOsmorcFacet(element)) {
          Header header = (Header)element;
          for (HeaderValue value : header.getHeaderValues()) {
            HeaderValuePart valuePart = ((Clause)value).getValue();
            String packageSpec = value.getUnwrappedText();
            if (valuePart == null ||
                /* TODO add wildcard patterns support. May require update of the Manifest parser lib of the plugin (felix-utils.jar) to the newer version.*/
                packageSpec.indexOf('*') != -1 ||
                packageSpec.indexOf('?') != -1) {
              continue;
            }

            if (!valuePart.getText().isEmpty() && !myBundleManager.isProvided(packageSpec)) {
              holder.registerProblem(valuePart, OsmorcBundle.message(
                "WrongImportPackageInspection.problem.message.not-exported-package",
                valuePart.getUnwrappedText(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
            }
          }
        }
      }
    };
  }

  private static boolean isImportPackageHeader(PsiElement element) {
    return element instanceof Header && ManifestConstants.Headers.IMPORT_PACKAGE.equals(((Header)element).getName());
  }
}
