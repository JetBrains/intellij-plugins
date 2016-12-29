package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.psi.HbCloseBlockMustache;
import com.dmarcotte.handlebars.psi.HbMustacheName;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class HbBlockMismatchAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof HbOpenBlockMustache) {
      HbOpenBlockMustache openBlockMustache = (HbOpenBlockMustache)element;
      HbMustacheName openBlockMustacheName = openBlockMustache.getBlockMustacheName();

      HbCloseBlockMustache closeBlockMustache = openBlockMustache.getPairedElement();
      if (closeBlockMustache != null) {
        HbMustacheName closeBlockMustacheName = closeBlockMustache.getBlockMustacheName();

        if (openBlockMustacheName == null || closeBlockMustacheName == null) {
          return;
        }

        String openBlockName = openBlockMustacheName.getName();
        String closeBlockName = closeBlockMustacheName.getName();
        if (!openBlockName.equals(closeBlockName)) {
          Annotation openBlockAnnotation
            = holder.createErrorAnnotation(openBlockMustacheName,
                                           HbBundle.message("hb.block.mismatch.inspection.open.block", openBlockName, closeBlockName));
          openBlockAnnotation.registerFix(new HbBlockMismatchFix(closeBlockName, openBlockName, true));
          openBlockAnnotation.registerFix(new HbBlockMismatchFix(openBlockName, closeBlockName, false));

          Annotation closeBlockAnnotation
            = holder.createErrorAnnotation(closeBlockMustacheName,
                                           HbBundle.message("hb.block.mismatch.inspection.close.block", openBlockName, closeBlockName));
          closeBlockAnnotation.registerFix(new HbBlockMismatchFix(openBlockName, closeBlockName, false));
          closeBlockAnnotation.registerFix(new HbBlockMismatchFix(closeBlockName, openBlockName, true));
        }
      }
      else {
        if (openBlockMustacheName == null) {
          return;
        }
        holder.createErrorAnnotation(openBlockMustacheName,
                                     HbBundle.message("hb.block.mismatch.inspection.missing.end.block", openBlockMustache.getName()));
      }
    }

    if (element instanceof HbCloseBlockMustache) {
      HbCloseBlockMustache closeBlockMustache = (HbCloseBlockMustache)element;
      PsiElement openBlockElement = closeBlockMustache.getPairedElement();
      if (openBlockElement == null) {
        HbMustacheName closeBlockMustacheName = closeBlockMustache.getBlockMustacheName();
        if (closeBlockMustacheName == null) {
          return;
        }
        holder.createErrorAnnotation(closeBlockMustacheName,
                                     HbBundle.message("hb.block.mismatch.inspection.missing.start.block", closeBlockMustache.getName()));
      }
    }
  }
}
