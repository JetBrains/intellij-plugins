package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.file.HbFileType;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.dmarcotte.handlebars.util.HbUtils;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkAsHtmlInspection extends LocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return HbBundle.message("inspections.group.name");
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (!isOnTheFly) {
      return super.checkFile(file, manager, isOnTheFly);
    }
    if (HbFileType.hasHbAttribute(file.getVirtualFile()) &&
        file instanceof HbPsiFile &&
        !ContainerUtil.exists(file.getViewProvider().getAllFiles(), new Condition<PsiFile>() {
          @Override
          public boolean value(PsiFile psiFile) {
            return psiFile instanceof XmlFile && HbUtils.hasHbScriptTag((XmlFile)psiFile);
          }
        })) {
      return new ProblemDescriptor[]{
        manager.createProblemDescriptor(
          file,
          HbBundle.message("inspection.html.file.has.no.template"),
          new LocalQuickFix[]{new UnmarkFix()},
          HighlightInfo.convertSeverityToProblemHighlight(getDefaultLevel().getSeverity()),
          isOnTheFly,
          false
        )
      };
    }
    return super.checkFile(file, manager, isOnTheFly);
  }

  private static class UnmarkFix extends IntentionAndQuickFixAction {
    @NotNull
    @Override
    public String getName() {
      return HbBundle.message("unmark.hb.file");
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return HbBundle.message("inspections.group.name");
    }

    @Override
    public void applyFix(Project project, PsiFile file, @Nullable Editor editor) {
      VirtualFile virtualFile = file.getVirtualFile();
      if (virtualFile != null) {
        HbFileType.markAsHbFile(virtualFile, false);
        FileBasedIndex.getInstance().requestReindex(virtualFile);
        ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true);
      }
    }
  }
}
