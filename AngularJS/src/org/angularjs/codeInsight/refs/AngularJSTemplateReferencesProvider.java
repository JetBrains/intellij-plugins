package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTemplateReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return ArrayUtil.mergeArrays(new Angular2SoftFileReferenceSet(element).getAllReferences(),
                                 new PsiReference[] {new AngularJSTemplateCacheReference((JSLiteralExpression)element)});
  }

  static class Angular2SoftFileReferenceSet extends SoftFileReferenceSet {
    public Angular2SoftFileReferenceSet(PsiElement element) {
      super(element);
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> getDefaultContexts() {
      final PsiElement element = getElement();
      final Project project = element.getProject();
      if (AngularIndexUtil.hasAngularJS2(project)) {
        final PsiFile file = element.getContainingFile().getOriginalFile();
        final TypeScriptConfig config = TypeScriptCompilerConfigUtil.getConfigForFile(project, file.getVirtualFile());
        if (config != null) {
          final PsiDirectory directory = PsiManager.getInstance(project).findDirectory(config.getConfigFile().getParent());
          if (directory != null) {
            return Collections.<PsiFileSystemItem>singleton(directory);
          }
        }
      }

      final Collection<PsiFileSystemItem> current = lookupByTailPathMatch(element);
      if (current != null) return current;
      return super.getDefaultContexts();
    }

    // example: in angular-ui-router sample project lies under /sample, it does not start from project root
    // in order for templateUrl worked for paths like 'app/contacts/contacts.detail.html' (when this subpath is actually under /sample),
    // we need to provide corresponding context
    @Nullable
    private static Collection<PsiFileSystemItem> lookupByTailPathMatch(PsiElement element) {
      final String referenceText = StringUtil.unquoteString(element.getText());
      final String normalized = referenceText.replace('\\', '/');
      final String[] parts = normalized.split("/");
      final PsiFile[] byName =
        FilenameIndex.getFilesByName(element.getProject(), parts[parts.length - 1], GlobalSearchScope.projectScope(element.getProject()));
      for (PsiFile file : byName) {
        PsiFileSystemItem current = file;
        boolean found = true;
        for (int i = parts.length - 1; i >= 0; i--) {
          if (current == null || !parts[i].equals(current.getName())) {
            found = false;
            break;
          }
          current = current.getParent();
        }
        if (found) return Collections.singleton(current);
      }
      return null;
    }
  }
}
