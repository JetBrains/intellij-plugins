package com.intellij.javascript.flex.completion;

import com.intellij.lang.javascript.completion.JSCompletionContributor;
import com.intellij.lang.javascript.completion.JSCompletionKeyword;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.completion.JSKeywordsCompletionProvider;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.KeywordCompletionConsumer;
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

import static com.intellij.lang.javascript.completion.JSCompletionKeyword.AS;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.CLASS;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.CONST;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.DYNAMIC;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.EXTENDS;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.FINAL;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.FUNCTION;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.IMPLEMENTS;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.IMPORT;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.INCLUDE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.INT;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.INTERFACE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.INTERNAL;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.IS;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.NAMESPACE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.NATIVE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.OVERRIDE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.PACKAGE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.PRIVATE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.PROTECTED;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.PUBLIC;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.STATIC;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.SUPER;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.UINT;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.USE;
import static com.intellij.lang.javascript.completion.JSCompletionKeyword.VAR;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptCompletionKeywordsContributor extends JSCompletionKeywordsContributor {
  private static final @NonNls JSCompletionKeyword[] accessModifiers = {PUBLIC, PRIVATE, PROTECTED, INTERNAL};

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
          (grandParent instanceof JSAttributeList || grandParent != null && JSResolveUtil.isPlaceWhereNsCanBe(grandParent) ||
           grandGrandParent instanceof JSFile) &&
          (!(grandParent instanceof JSFunction) || ((JSFunction)grandParent).getReturnTypeElement() != parent)
        ) {
        consumer.consume(JSLookupPriority.SMART_KEYWORDS_PRIORITY, true, accessModifiers);
        consumer.consume(JSLookupPriority.SMART_KEYWORDS_PRIORITY, true, CLASS, FUNCTION, INTERFACE,
                         NAMESPACE, PACKAGE,
                         EXTENDS, IMPLEMENTS, IMPORT, OVERRIDE, STATIC, DYNAMIC, VAR, CONST, USE,
                         FINAL);
      }
      return false;
    }
    if (parent != null && grandParent != null &&
        JSResolveUtil.isInPlaceWhereTypeCanBeDuringCompletion(parent) && JSResolveUtil.isPlaceWhereNsCanBe(grandParent)
      ) {
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, false, JSKeywordsCompletionProvider.TYPE_LITERAL_VALUES);
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, false, FUNCTION);
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, true, accessModifiers);
      consumer.consume(JSLookupPriority.KEYWORDS_PRIORITY, true, EXTENDS, IMPLEMENTS, INCLUDE,
                       IMPORT, STATIC,
                       OVERRIDE, NAMESPACE, CLASS, INTERFACE, VAR, CONST, USE);
      return false;
    }
    return true;
  }

  @Override
  public void appendSpecificKeywords(@NotNull KeywordCompletionConsumer consumer,
                                     @NotNull PsiElement context,
                                     PsiElement grandParent) {
    consumer.consume(JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY, true,
                     IS,
                     AS,
                     CLASS,
                     INTERFACE,
                     INTERNAL,
                     PUBLIC,
                     PROTECTED,
                     NATIVE,
                     OVERRIDE,
                     DYNAMIC,
                     EXTENDS,
                     IMPLEMENTS,
                     IMPORT,
                     STATIC,
                     NAMESPACE,
                     USE,
                     SUPER,
                     INCLUDE,
                     //"get", // do not add since it will stop the auto completion even iff we have common getXXX()
                     //"set",
                     PACKAGE
    );
    consumer.consume(JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY, false, INT, UINT);
  }
}
