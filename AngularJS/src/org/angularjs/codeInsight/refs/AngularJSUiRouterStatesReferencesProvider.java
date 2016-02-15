package org.angularjs.codeInsight.refs;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularUiRouterStatesIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Irina.Chernushina on 2/12/2016.
 */
public class AngularJSUiRouterStatesReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[] {new AngularJSUiRouterStateReference(((XmlAttributeValue)element))};
  }

  private static class AngularJSUiRouterStateReference extends AngularPolyReferenceBase<XmlAttributeValue> {
    public AngularJSUiRouterStateReference(XmlAttributeValue element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    private String getStateName() {
      final String text = StringUtil.unquoteString(getCanonicalText());
      final int idx = text.indexOf('(');
      if (idx >= 0) return text.substring(0, idx);
      return text;
    }

    @NotNull
    @Override
    protected ResolveResult[] resolveInner() {
      final String id = getStateName();
      if (StringUtil.isEmptyOrSpaces(id)) {
        return ResolveResult.EMPTY_ARRAY;
      }

      final List<ResolveResult> list = new ArrayList<ResolveResult>();
      AngularIndexUtil.multiResolve(myElement.getProject(), AngularUiRouterStatesIndex.KEY, id,
                                    new Processor<JSImplicitElement>() {
                                      @Override
                                      public boolean process(JSImplicitElement element) {
                                        list.add(new JSResolveResult(element));
                                        return true;
                                      }
                                    });
      return list.toArray(new ResolveResult[list.size()]);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      final Project project = getElement().getProject();

      final Collection<String> keys = AngularIndexUtil.getAllKeys(AngularUiRouterStatesIndex.KEY, project);
      final List<LookupElement> elements = new ArrayList<LookupElement>();
      for (String key : keys) {
        final LookupElementBuilder builder = LookupElementBuilder.create(key)
          .withTailText(" (angular-ui-router state)", true);
        final LookupElement item = PrioritizedLookupElement.withPriority(builder, JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY);
        elements.add(item);
      }

      return elements.toArray(new LookupElement[elements.size()]);
    }
  }
}
