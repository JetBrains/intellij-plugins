package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.psi.HbCloseBlockMustache;
import com.dmarcotte.handlebars.psi.HbMustacheName;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
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

        if (openBlockMustacheName != null && closeBlockMustacheName != null) {
          String openBlockName = openBlockMustacheName.getName();
          String closeBlockName = closeBlockMustacheName.getName();
          if (!openBlockName.equals(closeBlockName)) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                                 HbBundle.message("hb.block.mismatch.inspection.open.block", openBlockName, closeBlockName))
              .range(openBlockMustacheName)
              .withFix(new HbBlockMismatchFix(closeBlockName, openBlockName, true))
              .withFix(new HbBlockMismatchFix(openBlockName, closeBlockName, false)).create();
          }
        }
      }
      else {
        if (openBlockMustacheName != null) {
          holder.newAnnotation(HighlightSeverity.ERROR,
                               HbBundle.message("hb.block.mismatch.inspection.missing.end.block", openBlockMustache.getName()))
            .range(openBlockMustacheName).create();
        }
      }
    }

    if (element instanceof HbCloseBlockMustache) {
      HbCloseBlockMustache closeBlockMustache = (HbCloseBlockMustache)element;
      HbOpenBlockMustache openBlockMustache = closeBlockMustache.getPairedElement();
      if (openBlockMustache == null) {
        HbMustacheName closeBlockMustacheName = closeBlockMustache.getBlockMustacheName();
        if (closeBlockMustacheName != null) {
          holder.newAnnotation(HighlightSeverity.ERROR,
                               HbBundle.message("hb.block.mismatch.inspection.missing.start.block", closeBlockMustache.getName()))
            .range(closeBlockMustacheName).create();
        }
      }
      else {
        HbMustacheName openBlockMustacheName = openBlockMustache.getBlockMustacheName();

        HbMustacheName closeBlockMustacheName = closeBlockMustache.getBlockMustacheName();

        if (closeBlockMustacheName != null && openBlockMustacheName != null) {
          String openBlockName = openBlockMustacheName.getName();
          String closeBlockName = closeBlockMustacheName.getName();
          if (!openBlockName.equals(closeBlockName)) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                                 HbBundle.message("hb.block.mismatch.inspection.close.block", openBlockName, closeBlockName))
              .range(closeBlockMustacheName)
              .withFix(new HbBlockMismatchFix(openBlockName, closeBlockName, false))
              .withFix(new HbBlockMismatchFix(closeBlockName, openBlockName, true)).create();
          }
        }
      }
    }
  }
}
