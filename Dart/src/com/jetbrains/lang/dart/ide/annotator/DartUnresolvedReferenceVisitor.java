package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartReferenceExpression;
import com.jetbrains.lang.dart.psi.DartVisitor;
import org.jetbrains.annotations.NotNull;

public class DartUnresolvedReferenceVisitor extends DartVisitor implements Annotator {
  private AnnotationHolder myHolder = null;

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    assert myHolder == null;
    myHolder = holder;
    try {
      element.accept(this);
    }
    finally {
      myHolder = null;
    }
  }

  @Override
  public void visitReferenceExpression(@NotNull DartReferenceExpression reference) {
    final String referenceText = reference.getText();
    final boolean isSimpleReference = referenceText != null && !"void".equals(referenceText) && !referenceText.contains(".");
    //final boolean isPrefix = referenceText != null &&
    //                         !DartResolveUtil.getImportedFilesByImportPrefix(reference.getContainingFile(), referenceText).isEmpty();
    if (isSimpleReference && /*!isPrefix && */reference.resolve() == null) {
      myHolder.createErrorAnnotation(reference, DartBundle.message("cannot.resolve.reference"));
    }
    super.visitReferenceExpression(reference);
  }
}
