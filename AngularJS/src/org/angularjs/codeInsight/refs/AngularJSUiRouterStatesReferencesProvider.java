package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularUiRouterStatesIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AngularJSUiRouterStatesReferencesProvider extends PsiReferenceProvider {
  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new AngularJSUiRouterStateReference(((XmlAttributeValue)element))};
  }

  private static class AngularJSUiRouterStateReference extends CachingPolyReferenceBase<XmlAttributeValue> {
    AngularJSUiRouterStateReference(XmlAttributeValue element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    private String getStateName() {
      final String text = StringUtil.unquoteString(getCanonicalText());
      final int idx = text.indexOf('(');
      if (idx >= 0) return text.substring(0, idx);
      return text;
    }

    @Override
    protected ResolveResult @NotNull [] resolveInner() {
      final String id = getStateName();
      if (StringUtil.isEmptyOrSpaces(id)) {
        return ResolveResult.EMPTY_ARRAY;
      }

      final List<ResolveResult> list = new ArrayList<>();
      AngularIndexUtil.multiResolve(myElement.getProject(), AngularUiRouterStatesIndex.KEY, id,
                                    element -> {
                                      list.add(new JSResolveResult(element));
                                      return true;
                                    });
      return list.toArray(ResolveResult.EMPTY_ARRAY);
    }

    @Override
    public boolean isSoft() {
      return true;
    }
  }
}
