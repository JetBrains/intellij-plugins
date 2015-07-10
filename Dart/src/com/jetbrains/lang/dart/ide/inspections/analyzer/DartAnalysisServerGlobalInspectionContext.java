package com.jetbrains.lang.dart.ide.inspections.analyzer;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerAnnotator;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import gnu.trove.THashMap;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DartAnalysisServerGlobalInspectionContext
  implements GlobalInspectionContextExtension<DartAnalysisServerGlobalInspectionContext> {

  static final Key<DartAnalysisServerGlobalInspectionContext> KEY = Key.create("DartAnalysisServerGlobalInspectionContext");

  @NotNull private final Map<VirtualFile, AnalysisError[]> myVirtualFile2ErrorsMap = new THashMap<VirtualFile, AnalysisError[]>();

  @NotNull
  public Map<VirtualFile, AnalysisError[]> getVirtualFile2ErrorsMap() {
    return myVirtualFile2ErrorsMap;
  }

  @NotNull
  @Override
  public Key<DartAnalysisServerGlobalInspectionContext> getID() {
    return KEY;
  }

  @Override
  public void performPreRunActivities(@NotNull final List<Tools> globalTools,
                                      @NotNull final List<Tools> localTools,
                                      @NotNull final GlobalInspectionContext context) {
    final AnalysisScope analysisScope = context.getRefManager().getScope();
    if (analysisScope == null) return;

    final GlobalSearchScope scope = GlobalSearchScope.EMPTY_SCOPE.union(analysisScope.toSearchScope());

    updateIndicator("Looking for Dart files...", -1);

    final Collection<VirtualFile> dartFiles = ApplicationManager.getApplication().runReadAction(new Computable<Collection<VirtualFile>>() {
      @Override
      public Collection<VirtualFile> compute() {
        return FileTypeIndex.getFiles(DartFileType.INSTANCE, scope);
      }
    });

    double index = 0.0;
    final int size = dartFiles.size();

    for (final VirtualFile dartFile : dartFiles) {
      index++;
      analyzeFile(dartFile, context.getProject(), index / size);
    }
  }

  private void analyzeFile(@NotNull final VirtualFile virtualFile, @NotNull final Project project, final double progressFraction) {
    updateIndicator("Analyzing " + virtualFile.getName() + "...", progressFraction);

    final DartAnalysisServerAnnotator annotator = new DartAnalysisServerAnnotator();

    final DartAnalysisServerAnnotator.AnnotatorInfo annotatorInfo =
      ApplicationManager.getApplication().runReadAction(new NullableComputable<DartAnalysisServerAnnotator.AnnotatorInfo>() {
        @Nullable
        public DartAnalysisServerAnnotator.AnnotatorInfo compute() {
          final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
          if (psiFile == null) return null;
          return annotator.collectInformation(psiFile, null, false);
        }
      });
    if (annotatorInfo == null) return;

    annotatorInfo.setLongerAnalysisTimeout(true);

    final AnalysisError[] errors = DartAnalysisServerService.getInstance().analysis_getErrors(annotatorInfo);
    if (errors == null || errors.length == 0) return;

    myVirtualFile2ErrorsMap.put(virtualFile, errors);
  }

  private static void updateIndicator(@NotNull final String text, final double progressFraction) {
    final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    if (indicator != null) {
      final ProgressIndicator progressIndicator = ProgressWrapper.unwrap(indicator);
      progressIndicator.setText(text);
      if (progressFraction == -1) {
        progressIndicator.setIndeterminate(true);
      }
      else {
        progressIndicator.setIndeterminate(false);
        progressIndicator.setFraction(progressFraction);
      }
    }
  }

  @Override
  public void performPostRunActivities(@NotNull final List<InspectionToolWrapper> inspections,
                                       @NotNull final GlobalInspectionContext context) {
  }

  @Override
  public void cleanup() {
    myVirtualFile2ErrorsMap.clear();
  }
}
