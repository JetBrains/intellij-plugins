package org.angularjs.codeInsight;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSProcessor {
  public static void process(final PsiElement element, final Consumer<JSVariable> consumer) {
    final XmlFile file = (XmlFile)InjectedLanguageUtil.getTopLevelFile(element);
    final JSResolveUtil.JSInjectedFilesVisitor visitor = new JSResolveUtil.JSInjectedFilesVisitor() {
      @Override
      protected void process(JSFile file) {
        file.accept(new JSRecursiveElementVisitor() {
          @Override
          public void visitJSVariable(JSVariable node) {
            if (scopeMatches(element, node)) {
              consumer.consume(node);
            }
            super.visitJSVariable(node);
          }
        });
      }
    };
    final XmlDocument document = file.getDocument();
    if (document == null) return;
    for (XmlTag tag : PsiTreeUtil.getChildrenOfTypeAsList(document, XmlTag.class)) {
      new XmlBackedJSClassImpl.InjectedScriptsVisitor(tag, null, true, true, visitor, true).go();
    }
  }

  private static boolean scopeMatches(PsiElement element1, PsiElement element2) {
    return true;
  }
}
