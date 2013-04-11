package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.psi.HbCloseBlockMustache;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.dmarcotte.handlebars.psi.HbPath;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class HbBlockMismatchInspection implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof HbOpenBlockMustache) {
      HbOpenBlockMustache openBlockMustache = (HbOpenBlockMustache)element;
      HbPath openBlockMainPath = openBlockMustache.getBlockMainPath();

      HbCloseBlockMustache closeBlockMustache = openBlockMustache.getPairedElement();
      if (closeBlockMustache != null) {
        HbPath closeBlockMainPath = closeBlockMustache.getBlockMainPath();

        if (openBlockMainPath == null || closeBlockMainPath == null) {
          return;
        }

        String openBlockName = openBlockMainPath.getName();
        String closeBlockName = closeBlockMainPath.getName();
        if (!openBlockName.equals(closeBlockName)) {
          Annotation openBlockAnnotation
            = holder.createErrorAnnotation(openBlockMainPath,
                                           HbBundle.message("hb.block.mismatch.inspection.open.block", openBlockName, closeBlockName));
          openBlockAnnotation.registerFix(new HbBlockMismatchFix(closeBlockName, openBlockName, true));
          openBlockAnnotation.registerFix(new HbBlockMismatchFix(openBlockName, closeBlockName, false));

          Annotation closeBlockAnnotation
            = holder.createErrorAnnotation(closeBlockMainPath,
                                           HbBundle.message("hb.block.mismatch.inspection.close.block", openBlockName, closeBlockName));
          closeBlockAnnotation.registerFix(new HbBlockMismatchFix(openBlockName, closeBlockName, false));
          closeBlockAnnotation.registerFix(new HbBlockMismatchFix(closeBlockName, openBlockName, true));
        }
      }
      else {
        holder.createErrorAnnotation(openBlockMainPath,
                                     HbBundle.message("hb.block.mismatch.inspection.missing.end.block", openBlockMustache.getName()));
      }
    }

    if (element instanceof HbCloseBlockMustache) {
      HbCloseBlockMustache closeBlockMustache = (HbCloseBlockMustache)element;
      PsiElement openBlockElement = closeBlockMustache.getPairedElement();
      if (openBlockElement == null) {
        HbPath closeBlockMainPath = closeBlockMustache.getBlockMainPath();
        if (closeBlockMainPath == null) {
          return;
        }
        holder.createErrorAnnotation(closeBlockMainPath,
                                     HbBundle.message("hb.block.mismatch.inspection.missing.start.block", closeBlockMustache.getName()));
      }
    }
  }
}
