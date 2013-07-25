package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionParameterImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagFunctionImpl;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 12.02.2009
 */
public class CfmllFindUsagesProvider implements FindUsagesProvider {
    public WordsScanner getWordsScanner() {
        return null;
    }

  public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
    return psiElement instanceof CfmlReferenceExpression || (psiElement instanceof CfmlTagFunctionImpl) ||
           (psiElement instanceof CfmlTag && ((CfmlTag)psiElement).getTagName().equalsIgnoreCase("cfargument")) ||
           psiElement instanceof CfmlFunctionImpl || psiElement instanceof CfmlFunctionParameterImpl;
  }

    public String getHelpId(@NotNull final PsiElement psiElement) {
        return null;
    }

    @NotNull
    public String getType(@NotNull final PsiElement element) {
      return element instanceof CfmlReferenceExpression
             ? "reference"
             : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl ? "Function Name" : "Argument Name";
    }

    @NotNull
    public String getDescriptiveName(@NotNull final PsiElement element) {
      return element instanceof CfmlReferenceExpression
             ? "reference"
             : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl ? "function" : "argument";
    }

    @NotNull
    public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
        return element.getText();
    }
}

