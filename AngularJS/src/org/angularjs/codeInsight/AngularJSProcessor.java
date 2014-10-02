package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.resolve.ImplicitJSVariableImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.html.HtmlEmbeddedContentImpl;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.Consumer;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSRecursiveVisitor;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSProcessor {
  private static final Map<String, String> NG_REPEAT_IMPLICITS = new HashMap<String, String>();
  static {
    NG_REPEAT_IMPLICITS.put("$index", "Number");
    NG_REPEAT_IMPLICITS.put("$first", "Boolean");
    NG_REPEAT_IMPLICITS.put("$middle", "Boolean");
    NG_REPEAT_IMPLICITS.put("$last", "Boolean");
    NG_REPEAT_IMPLICITS.put("$even", "Boolean");
    NG_REPEAT_IMPLICITS.put("$odd", "Boolean");
  }

  public static void process(final PsiElement element, final Consumer<JSNamedElement> consumer) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(element);
    final PsiFile hostFile = FileContextUtil.getContextFile(original != element ? original : element.getContainingFile().getOriginalFile());
    if (hostFile == null) return;

    final XmlFile file = (XmlFile)hostFile;
    final JSResolveUtil.JSInjectedFilesVisitor visitor = new JSResolveUtil.JSInjectedFilesVisitor() {
      @Override
      protected void process(JSFile file) {
        file.accept(new AngularJSRecursiveVisitor() {
          @Override
          public void visitJSDefinitionExpression(JSDefinitionExpression node) {
            if (scopeMatches(original, node)) {
              consumer.consume(node);
            }
            super.visitJSDefinitionExpression(node);
          }

          @Override
          public void visitAngularJSAsExpression(AngularJSAsExpression asExpression) {
            final JSDefinitionExpression def = asExpression.getDefinition();
            if (def != null && scopeMatches(original, asExpression)) {
              consumer.consume(def);
            }
          }

          @Override
          public void visitAngularJSRepeatExpression(AngularJSRepeatExpression repeatExpression) {
            if (scopeMatches(original, repeatExpression)) {
              for (Map.Entry<String, String> entry : NG_REPEAT_IMPLICITS.entrySet()) {
                consumer.consume(new ImplicitJSVariableImpl(entry.getKey(), entry.getValue(), repeatExpression));
              }
            }
            super.visitAngularJSRepeatExpression(repeatExpression);
          }
        });
      }
    };
    processDocument(visitor, file.getDocument());
  }

  private static void processDocument(final JSResolveUtil.JSInjectedFilesVisitor visitor, XmlDocument document) {
    if (document == null) return;
    for (XmlTag tag : PsiTreeUtil.getChildrenOfTypeAsList(document, XmlTag.class)) {
      new XmlBackedJSClassImpl.InjectedScriptsVisitor(tag, null, true, true, visitor, true){
        @Override
        public boolean execute(@NotNull PsiElement element) {
          if (element instanceof HtmlEmbeddedContentImpl) {
            processDocument(visitor, PsiTreeUtil.findChildOfType(element, XmlDocument.class));
          }
          return super.execute(element);
        }
      }.go();
    }
  }

  private static boolean scopeMatches(PsiElement element, PsiElement declaration) {
    final InjectedLanguageManager injector = InjectedLanguageManager.getInstance(element.getProject());
    final PsiLanguageInjectionHost elementContainer = injector.getInjectionHost(element);
    final XmlTagChild elementTag = PsiTreeUtil.getNonStrictParentOfType(elementContainer, XmlTag.class, XmlText.class);
    final PsiLanguageInjectionHost declarationContainer = injector.getInjectionHost(declaration);
    final XmlTagChild declarationTag = PsiTreeUtil.getNonStrictParentOfType(declarationContainer, XmlTag.class, XmlText.class);

    if (declarationContainer != null && elementContainer != null && elementTag != null && declarationTag != null) {
      return PsiTreeUtil.isAncestor(declarationTag, elementTag, true) ||
             (PsiTreeUtil.isAncestor(declarationTag, elementTag, false) &&
              declarationContainer.getTextOffset() < elementContainer.getTextOffset());
    }
    return true;
  }
}
