package com.jetbrains.plugins.jade.validation;

import com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.jetbrains.plugins.jade.psi.impl.JadeFilePathImpl;
import org.jetbrains.annotations.NotNull;

public final class JadeAnnotatingVisitor extends XmlElementVisitor implements Annotator {
  @Override
  public void annotate(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    if (element instanceof JadeFilePathImpl) {
      PsiReference[] references = element.getReferences();
      for (PsiReference reference : references) {
        ProgressManager.checkCanceled();
        if (reference == null) {
          continue;
        }
        if (!XmlHighlightVisitor.hasBadResolve(reference, false)) {
          continue;
        }
        String description = XmlHighlightVisitor.getErrorDescription(reference);

        TextRange rangeInFile = reference.getRangeInElement().shiftRight(reference.getElement().getTextRange().getStartOffset());
        AnnotationBuilder builder = holder.newAnnotation(HighlightSeverity.WARNING, description)
          .range(rangeInFile);
        LocalQuickFix[] fixes = ((FileReference)reference).getQuickFixes();
        if (fixes != null) {
          for (LocalQuickFix fix : fixes) {
            if (fix instanceof IntentionAction) {
              builder = builder.withFix((IntentionAction)fix);
            }
          }
        }
        builder.create();
      }
    }
  }
}
