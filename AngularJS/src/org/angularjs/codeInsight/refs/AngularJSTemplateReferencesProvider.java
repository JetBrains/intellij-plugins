package org.angularjs.codeInsight.refs;

import com.intellij.javascript.JSFileReference;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.lang.typescript.modules.TypeScriptModuleFileReferenceSet.addParentPathContexts;

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
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
      final PsiElement element = getElement();
      final Project project = element.getProject();
      if (AngularIndexUtil.hasAngularJS2(project)) {
        final PsiFile file = element.getContainingFile().getOriginalFile();
        final TypeScriptConfig config = TypeScriptCompilerConfigUtil.getConfigForFile(project, file.getVirtualFile());
        final PsiDirectory directory = config != null ?
                                       PsiManager.getInstance(project).findDirectory(config.getConfigDirectory()) :
                                       null;

        String pathString = getPathString();

        Collection<PsiFileSystemItem> contexts = ContainerUtil.newLinkedHashSet();
        
        if (!pathString.startsWith(".") && addParentPathContexts(file, pathString, contexts)) {
          return contexts;
        }
        ContainerUtil.addAllNotNull(contexts, file.getContainingDirectory(), directory);
        return contexts;
      }

      return super.computeDefaultContexts();
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
      return new JSFileReference(text, index, range, this, ArrayUtil.EMPTY_STRING_ARRAY);
    }
  }
}
