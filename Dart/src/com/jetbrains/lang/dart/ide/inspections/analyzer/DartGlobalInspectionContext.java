package com.jetbrains.lang.dart.ide.inspections.analyzer;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.jetbrains.lang.dart.analyzer.AnalyzerMessage;
import com.jetbrains.lang.dart.analyzer.DartAnalyzerDriver;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DartGlobalInspectionContext implements GlobalInspectionContextExtension<DartGlobalInspectionContext> {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.inspections.analyzer.DartGlobalInspectionContext");

  public static final Key<DartGlobalInspectionContext> KEY = Key.create("DartGlobalInspectionContext");

  private final Map<VirtualFile, List<AnalyzerMessage>> libraryRoot2Errors = new THashMap<VirtualFile, List<AnalyzerMessage>>();

  public Map<VirtualFile, List<AnalyzerMessage>> getLibraryRoot2Errors() {
    return libraryRoot2Errors;
  }

  @Override
  public Key<DartGlobalInspectionContext> getID() {
    return KEY;
  }


  @Override
  public void performPreRunActivities(List<Tools> globalTools, List<Tools> localTools, GlobalInspectionContext context) {
    final Project project = context.getProject();
    AnalysisScope scope = context.getRefManager().getScope();
    SearchScope searchScope = scope == null ? GlobalSearchScope.EMPTY_SCOPE : scope.toSearchScope();
    final GlobalSearchScope globalSearchScope = (GlobalSearchScope)searchScope.union(GlobalSearchScope.EMPTY_SCOPE);
    final Set<String> allLibraryNames = ApplicationManager.getApplication().runReadAction(new Computable<Set<String>>() {
      @Override
      public Set<String> compute() {
        return DartLibraryIndex.getAllLibraryNames(project);
      }
    });
    List<VirtualFile> libraryRoots = new ArrayList<VirtualFile>();

    setIndicatorText("Finding Dart libraries");
    for (String libraryName : allLibraryNames) {
      libraryRoots.addAll(DartLibraryIndex.findSingleLibraryClass(libraryName, globalSearchScope));
    }

    setIndicatorText("Running Dart Analyzer");

    for (VirtualFile libraryRoot : libraryRoots) {
      analyzeLibrary(libraryRoot, project);
    }
  }

  private void analyzeLibrary(@NotNull VirtualFile libraryRoot, @NotNull Project project) {
    Module module = ModuleUtilCore.findModuleForFile(libraryRoot, project);
    if (module == null) {
      LOG.info("Cannot find module for: " + libraryRoot.getPath());
      return;
    }

    DartSettings settings = DartSettings.getSettingsForModule(module);
    final VirtualFile analyzer = settings == null ? null : settings.getAnalyzer();
    final DartAnalyzerDriver analyzerDriver =
      analyzer == null ? null : new DartAnalyzerDriver(module.getProject(), analyzer, settings.getSdkPath(), libraryRoot);

    setIndicatorText("Analyzing library root: " + libraryRoot.getName());

    if (analyzer == null) {
      LOG.info("Cannot run analyzer for: " + libraryRoot.getPath());
      return;
    }

    libraryRoot2Errors.put(libraryRoot, analyzerDriver.analyze());
  }

  private static void setIndicatorText(String text) {
    final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    if (indicator != null) {
      ProgressWrapper.unwrap(indicator).setText(text);
    }
  }

  @Override
  public void performPostRunActivities(List<InspectionProfileEntry> inspections, GlobalInspectionContext context) {
  }

  @Override
  public void cleanup() {
    libraryRoot2Errors.clear();
  }
}
