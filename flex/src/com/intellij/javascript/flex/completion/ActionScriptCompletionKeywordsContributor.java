package com.intellij.javascript.flex.completion;

import com.intellij.lang.javascript.completion.JSCompletionContributor;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.completion.JavaScriptCompletionData;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;

import java.util.List;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptCompletionKeywordsContributor extends JSCompletionKeywordsContributor {
  private static final @NonNls String[] accessModifiers = {"public", "private", "protected", "internal"};

  @Override
  public boolean process(List<Trinity<String, Integer, Boolean>> keywords, PsiElement context) {
    if (JSCompletionContributor.getInstance().isDoingSmartCodeCompleteAction()) return false;
    final PsiElement parent = context.getParent();
    final PsiElement grandParent = parent.getParent();
    final PsiElement grandGrandParent = grandParent.getParent();
    if (parent instanceof JSReferenceExpression && (
      (JSResolveUtil.isExprInTypeContext((JSReferenceExpression)parent) ||
       (grandParent instanceof JSExpressionStatement && (JSResolveUtil.isPlaceWhereNsCanBe(grandParent) ||
                                                         grandGrandParent instanceof JSFile &&
                                                         grandGrandParent.getContext() == null)) ||
       grandParent instanceof JSAttributeList
      ) ||
      parent instanceof JSAttributeNameValuePair)
      ) {
      if (!(grandParent instanceof JSImportStatement) &&
          (grandParent instanceof JSAttributeList || JSResolveUtil.isPlaceWhereNsCanBe(grandParent) ||
           grandGrandParent instanceof JSFile) &&
          (!(grandParent instanceof JSFunction) || ((JSFunction)grandParent).getReturnTypeElement() != parent)
        ) {
        JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.SMART_KEYWORDS_PRIORITY, true, accessModifiers);
        JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.SMART_KEYWORDS_PRIORITY, true, "class", "function", "interface",
                                                "namespace", "package",
                                                "extends", "implements", "import", "override", "static", "dynamic", "var", "const", "use",
                                                "final");
        return false;
      }
      else return false;
    }
    if (JSResolveUtil.isInPlaceWhereTypeCanBeDuringCompletion(parent) && JSResolveUtil.isPlaceWhereNsCanBe(grandParent)
      ) {
      JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.KEYWORDS_PRIORITY, false, JavaScriptCompletionData.TYPE_LITERAL_VALUES);
      JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.KEYWORDS_PRIORITY, false, "function");
      JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.KEYWORDS_PRIORITY, true, accessModifiers);
      JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.KEYWORDS_PRIORITY, true, "extends", "implements", "include",
                                              "import", "static",
                                              "override", "namespace", "class", "interface", "var", "use");
      return false;
    }
    return true;
  }

  @Override
  public void appendSpecificKeywords(List<Trinity<String, Integer, Boolean>> keywords) {
    JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.NON_CONTEXT_KEYWORDS_PRIORITY, true,
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
    JavaScriptCompletionData.appendKeywords(keywords, JSLookupUtilImpl.NON_CONTEXT_KEYWORDS_PRIORITY, false, "int", "uint");
  }
}
