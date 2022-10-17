package com.intellij.plugins.drools.lang.psi.searchers;

import com.intellij.plugins.drools.lang.psi.DroolsFunctionStatement;
import com.intellij.plugins.drools.lang.psi.DroolsNameId;
import com.intellij.pom.PomDeclarationSearcher;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

public class DroolsFunctionDeclarationSearcher extends PomDeclarationSearcher {
  @Override
  public void findDeclarationsAt(@NotNull PsiElement element, int offsetInElement, @NotNull Consumer<? super PomTarget> consumer) {
    if (element instanceof DroolsNameId) {
      final DroolsFunctionStatement identifierOwner = PsiTreeUtil.getParentOfType(element, DroolsFunctionStatement.class);
      if (identifierOwner != null) {
        consumer.consume(identifierOwner);
      }
    }
  }
}

