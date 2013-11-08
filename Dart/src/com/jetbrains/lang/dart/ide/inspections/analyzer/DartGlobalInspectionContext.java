package com.jetbrains.lang.dart.ide.inspections.analyzer;

import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.error.AnalysisError;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartFileBasedSource;
import com.jetbrains.lang.dart.analyzer.DartInProcessAnnotator;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DartGlobalInspectionContext implements GlobalInspectionContextExtension<DartGlobalInspectionContext> {
  static final Key<DartGlobalInspectionContext> KEY = Key.create("DartGlobalInspectionContext");

  private final Map<VirtualFile, AnalysisError[]> libraryRoot2Errors = new THashMap<VirtualFile, AnalysisError[]>();

  public Map<VirtualFile, AnalysisError[]> getLibraryRoot2Errors() {
    return libraryRoot2Errors;
  }

  @NotNull
  @Override
  public Key<DartGlobalInspectionContext> getID() {
    return KEY;
  }

  @Override
  public void performPreRunActivities(@NotNull List<Tools> globalTools,
                                      @NotNull List<Tools> localTools,
                                      @NotNull GlobalInspectionContext context) {
    setIndicatorText("Looking for Dart files...");

    final GlobalSearchScope scope = GlobalSearchScope.EMPTY_SCOPE.union(context.getRefManager().getScope().toSearchScope());
    final Collection<VirtualFile> dartFiles = FileTypeIndex.getFiles(DartFileType.INSTANCE, scope);

    for (VirtualFile dartFile : dartFiles) {
      analyzeFile(dartFile, context.getProject());
    }
  }

  private void analyzeFile(@NotNull final VirtualFile virtualFile, @NotNull final Project project) {
    final DartInProcessAnnotator annotator = new DartInProcessAnnotator();

    final Pair<DartFileBasedSource, AnalysisContext> sourceAndContext =
      ApplicationManager.getApplication().runReadAction(new NullableComputable<Pair<DartFileBasedSource, AnalysisContext>>() {
        @Nullable
        public Pair<DartFileBasedSource, AnalysisContext> compute() {
          final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
          if (psiFile == null) return null;
          return annotator.collectInformation(psiFile);
        }
      });

    if (sourceAndContext == null) {
      return;
    }

    setIndicatorText("Analyzing " + virtualFile.getName() + "...");

    final AnalysisContext analysisContext = annotator.doAnnotate(sourceAndContext);
    if (analysisContext == null) return;


    libraryRoot2Errors.put(virtualFile, analysisContext.getErrors(DartFileBasedSource.getSource(project, virtualFile)).getErrors());
  }

  private static void setIndicatorText(String text) {
    final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    if (indicator != null) {
      ProgressWrapper.unwrap(indicator).setText(text);
    }
  }

  @Override
  public void performPostRunActivities(@NotNull List<InspectionToolWrapper> inspections, @NotNull GlobalInspectionContext context) {
  }

  @Override
  public void cleanup() {
    libraryRoot2Errors.clear();
  }
}
