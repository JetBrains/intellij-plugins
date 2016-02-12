package org.angularjs.codeInsight.refs;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.completion.JSLookupPriority;
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
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.index.AngularUiRouterViewsIndex;
import org.angularjs.index.AngularViewDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    return new PsiReference[] {new AngularJSUiRouterViewReference(((JSProperty) element).getNameIdentifier())};
  }

  private static class AngularJSUiRouterViewReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
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
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
      return ResolveCache.getInstance(getElement().getProject()).resolveWithCaching(this, MyResolver.INSTANCE, false, false);
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
      final ResolveResult[] results = multiResolve(false);
      for (ResolveResult result : results) {
        if (getElement().getManager().areElementsEquivalent(result.getElement(), element)) {
          return true;
        }
      }
      return false;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      ResolveResult[] resolveResults = multiResolve(false);
      return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    public ResolveResult[] resolveInner() {
      final FileBasedIndex instance = FileBasedIndex.getInstance();
      final Project project = getElement().getProject();
      final String id = getViewName();
      Collection<VirtualFile> files = instance.getContainingFiles(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, id, GlobalSearchScope.allScope(project));
      if (StringUtil.isEmptyOrSpaces(id)) {
        // try to find templateUrl
        files = filterByTemplateUrl(files);
      }
      final List<ResolveResult> list = new ArrayList<ResolveResult>();
      for (VirtualFile file : files) {
        final List<AngularViewDefinition> values =
          instance.getValues(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, id, GlobalSearchScope.fileScope(project, file));
        for (AngularViewDefinition value : values) {
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

      final PsiElement object = myElement.getParent() instanceof JSProperty ? ((JSProperty) myElement.getParent()).getValue() : null;
      if (object instanceof JSObjectLiteralExpression) {
        final JSProperty templateUrl = ((JSObjectLiteralExpression)object).findProperty("templateUrl");
        if (templateUrl != null && templateUrl.getValue() != null) {
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
      final FileBasedIndex instance = FileBasedIndex.getInstance();
      final Project project = getElement().getProject();

      final Collection<String> keys = instance.getAllKeys(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, project);
      final List<LookupElement> elements = new ArrayList<LookupElement>();
      //boolean checkEmpty = false;
      for (String key : keys) {
        if (StringUtil.isEmptyOrSpaces(key)) {
          // no sense in adding empty strings into completion
          continue;
        }
        final LookupElementBuilder builder = LookupElementBuilder.create(key)
          .withTailText(" (angular-ui-router ui-view)", true);
        final LookupElement item = PrioritizedLookupElement.withPriority(builder, JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY);
        elements.add(item);
      }
      return elements.toArray(new LookupElement[elements.size()]);
    }

    private static class MyResolver implements ResolveCache.PolyVariantResolver<PsiPolyVariantReference> {
      private final static MyResolver INSTANCE = new MyResolver();

      @NotNull
      @Override
      public ResolveResult[] resolve(@NotNull PsiPolyVariantReference reference, boolean incompleteCode) {
        return ((AngularJSUiRouterViewReference) reference).resolveInner();
      }
    }
  }
}
