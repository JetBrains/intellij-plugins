package com.intellij.javascript.flex.completion;

import com.intellij.lang.javascript.completion.*;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptCompletionKeywordsContributor extends JSCompletionKeywordsContributor {
  private static final @NonNls String[] accessModifiers = {"public", "private", "protected", "internal"};

  @Override
  public boolean process(@NotNull KeywordCompletionConsumer consumer, @NotNull PsiElement context) {
    if (JSCompletionContributor.getInstance().isDoingSmartCodeCompleteAction()) return false;
    final PsiElement parent = context.getParent();
    final PsiElement grandParent = parent != null ? parent.getParent() : null;
    final PsiElement grandGrandParent = grandParent != null ? grandParent.getParent() : null;
    if (parent instanceof JSReferenceExpression &&
        ((JSReferenceExpression)parent).getQualifier() == null &&
        (JSResolveUtil.isExprInTypeContext((JSReferenceExpression)parent) ||
         grandParent instanceof JSExpressionStatement && (JSResolveUtil.isPlaceWhereNsCanBe(grandParent) ||
                                                          grandGrandParent instanceof JSFile && grandGrandParent.getContext() == null) ||
         grandParent instanceof JSAttributeList ||
         parent instanceof JSAttributeNameValuePair
        )
      ) {
      if (!(grandParent instanceof JSImportStatement) &&
          (grandParent instanceof JSAttributeList || JSResolveUtil.isPlaceWhereNsCanBe(grandParent) ||
           grandGrandParent instanceof JSFile) &&
          (!(grandParent instanceof JSFunction) || ((JSFunction)grandParent).getReturnTypeElement() != parent)
        ) {
        consumer.consume(JSLookupPriority.SMART_KEYWORDS_PRIORITY, true, accessModifiers);
        consumer.consume(JSLookupPriority.SMART_KEYWORDS_PRIORITY, true, "class", "function", "interface",
                         "namespace", "package",
                         "extends", "implements", "import", "override", "static", "dynamic", "var", "const", "use",
                         "final");
      }
      return false;
    }
    if (JSResolveUtil.isInPlaceWhereTypeCanBeDuringCompletion(parent) && JSResolveUtil.isPlaceWhereNsCanBe(grandParent)
      ) {
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, false, JSKeywordsCompletionProvider.TYPE_LITERAL_VALUES);
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, false, "function");
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, true, accessModifiers);
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, true, "extends", "implements", "include",
                       "import", "static",
                       "override", "namespace", "class", "interface", "var", "const", "use");
      return false;
    }
    return true;
  }

  @Override
  public void appendSpecificKeywords(@NotNull KeywordCompletionConsumer consumer,
                                     @NotNull PsiElement context,
                                     PsiElement grandParent) {
    consumer.consume(JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY, true,
                     "is",
                     "as",
                     "class",
                     "interface",
                     "internal",
                     "public",
                     "protected",
                     "native",
                     "override",
                     "dynamic",
                     "extends",
                     "implements",
                     "import",
                     "static",
                     "namespace",
                     "use",
                     "super",
                     "include",
                     //"get", // do not add since it will stop the auto completion even iff we have common getXXX()
                     //"set",
                     "package"
    );
    consumer.consume(JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY, false, "int", "uint");
  }
}
