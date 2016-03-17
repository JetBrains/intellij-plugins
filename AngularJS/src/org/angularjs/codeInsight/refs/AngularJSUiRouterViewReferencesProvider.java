package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSQualifiedNameImpl;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.index.AngularNamedItemDefinition;
import org.angularjs.index.AngularUiRouterViewsIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Irina.Chernushina on 2/11/2016.
 */
public class AngularJSUiRouterViewReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    final PsiElement identifier = element instanceof JSProperty ? ((JSProperty)element).getNameIdentifier() : element;
    return new PsiReference[] {new AngularJSUiRouterViewReference(identifier)};
  }

  private static class AngularJSUiRouterViewReference extends AngularPolyReferenceBase<PsiElement> {
    public AngularJSUiRouterViewReference(PsiElement element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    private String getViewName() {
      final String text = StringUtil.unquoteString(getCanonicalText());
      final int idx = text.indexOf('@');
      if (idx >= 0) return text.substring(0, idx);
      return text;
    }

    @NotNull
    public ResolveResult[] resolveInner() {
      final FileBasedIndex instance = FileBasedIndex.getInstance();
      final Project project = getElement().getProject();
      final String id = getViewName();
      Collection<VirtualFile> files =
        instance.getContainingFiles(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, id, GlobalSearchScope.allScope(project));
      if (StringUtil.isEmptyOrSpaces(id)) {
        // try to find templateUrl
        files = filterByTemplateUrl(files);
      }
      final List<ResolveResult> list = new ArrayList<ResolveResult>();
      for (VirtualFile file : files) {
        final List<AngularNamedItemDefinition> values =
          instance.getValues(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, id, GlobalSearchScope.fileScope(project, file));
        for (AngularNamedItemDefinition value : values) {
          JSQualifiedNameImpl qName = JSQualifiedNameImpl.fromQualifiedName(id);
          JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(qName, null);
          final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
          if (psiFile != null) {
            list.add(new JSResolveResult(new JSOffsetBasedImplicitElement(elementBuilder, (int)value.getStartOffset(), psiFile)));
          }
        }
      }
      return list.toArray(new ResolveResult[list.size()]);
    }

    private Collection<VirtualFile> filterByTemplateUrl(Collection<VirtualFile> files) {
      if (files.isEmpty()) return files;

      final PsiElement object = myElement.getParent() instanceof JSProperty ? ((JSProperty)myElement.getParent()).getValue() : null;
      if (object instanceof JSObjectLiteralExpression) {
        final JSProperty templateUrl = ((JSObjectLiteralExpression)object).findProperty("templateUrl");
        if (templateUrl != null && templateUrl.getValue() != null && templateUrl.getValue() instanceof JSLiteralExpression
            && ((JSLiteralExpression)templateUrl.getValue()).isQuotedLiteral()) {
          String templateUrlText = StringUtil.unquoteString(templateUrl.getValue().getText());
          if (!StringUtil.isEmptyOrSpaces(templateUrlText)) {
            templateUrlText = templateUrlText.trim().replace('\\', '/');
            final String finalTemplateUrlText = templateUrlText;
            files = ContainerUtil.filter(files, new Condition<VirtualFile>() {
              @Override
              public boolean value(VirtualFile file) {
                final String path = file.getPath();
                return path.endsWith(finalTemplateUrlText);
              }
            });
          }
        }
      }
      return files;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
