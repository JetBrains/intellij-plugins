package org.osmorc.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.osgi.framework.Constants;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.util.OsgiPsiUtil;

/**
 * @author Vladislav.Soroka
 */
public class WrongImportPackageInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      private final BundleManager myBundleManager = BundleManager.getInstance(holder.getProject());

      @Override
      public void visitElement(PsiElement element) {
        if (OsgiPsiUtil.isHeader(element, Constants.IMPORT_PACKAGE) && OsmorcFacet.hasOsmorcFacet(element)) {
          for (HeaderValue value : ((Header)element).getHeaderValues()) {
            String packageSpec = value.getUnwrappedText();
            if (!StringUtil.isEmptyOrSpaces(packageSpec) && !myBundleManager.isProvided(packageSpec)) {
              TextRange range = OsgiPsiUtil.trimRange(value);
              holder.registerProblem(value, range, OsmorcBundle.message("WrongImportPackageInspection.message"));
            }
          }
        }
      }
    };
  }
}
