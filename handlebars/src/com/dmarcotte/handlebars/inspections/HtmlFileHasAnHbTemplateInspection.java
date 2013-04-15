package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.file.HbFileType;
import com.dmarcotte.handlebars.util.HbUtils;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HtmlFileHasAnHbTemplateInspection extends LocalInspectionTool {
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
    if (file instanceof XmlFile && !HbFileType.hasHbAttribute(file.getVirtualFile()) && HbUtils.hasHbScriptTag((XmlFile)file)) {
      return new ProblemDescriptor[]{
        manager.createProblemDescriptor(
          file,
          HbBundle.message("inspection.html.file.has.a.template"),
          new LocalQuickFix[]{new MarkAsHbFix()},
          HighlightInfo.convertSeverityToProblemHighlight(getDefaultLevel().getSeverity()),
          isOnTheFly,
          false
        )
      };
    }
    return super.checkFile(file, manager, isOnTheFly);
  }

  private static class MarkAsHbFix extends IntentionAndQuickFixAction {
    @NotNull
    @Override
    public String getName() {
      return HbBundle.message("mark.as.hb.file");
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
        HbFileType.markAsHbFile(virtualFile, true);
        FileBasedIndex.getInstance().requestReindex(virtualFile);
        ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true);
      }
    }
  }
}
