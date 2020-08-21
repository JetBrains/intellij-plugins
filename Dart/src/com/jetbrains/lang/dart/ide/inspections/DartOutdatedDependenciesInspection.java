// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.flutter.FlutterUtil;
import com.jetbrains.lang.dart.ide.actions.DartPubActionBase;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DotPackagesFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartOutdatedDependenciesInspection extends LocalInspectionTool {
  private final Set<String> myIgnoredPubspecPaths = new THashSet<>(); // remember for the current session only, do not serialize

  @Override
  public ProblemDescriptor @Nullable [] checkFile(@NotNull final PsiFile psiFile,
                                                  @NotNull final InspectionManager manager,
                                                  final boolean isOnTheFly) {
    if (!isOnTheFly) return null;

    if (!(psiFile instanceof DartFile)) return null;

    if (Registry.is("dart.projects.without.pubspec", false)) return null;

    if (DartPubActionBase.isInProgress()) return null;

    final VirtualFile file = DartResolveUtil.getRealVirtualFile(psiFile);
    if (file == null || !file.isInLocalFileSystem()) return null;

    final Project project = psiFile.getProject();
    if (!ProjectRootManager.getInstance(project).getFileIndex().isInContent(file)) return null;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    final Module module = ModuleUtilCore.findModuleForFile(file, project);
    if (module == null || sdk == null || !DartSdkLibUtil.isDartSdkEnabled(module)) return null;
    if (FlutterUtil.isFlutterPluginInstalled() && FlutterUtil.isFlutterModule(module)) return null;

    final VirtualFile pubspecFile = PubspecYamlUtil.findPubspecYamlFile(project, file);
    if (pubspecFile == null || myIgnoredPubspecPaths.contains(pubspecFile.getPath())) return null;

    final String projectName = PubspecYamlUtil.getDartProjectName(pubspecFile);
    if (projectName == null || !StringUtil.isJavaIdentifier(projectName)) return null; // 'pub get' will fail anyway

    final VirtualFile dotPackagesFile = pubspecFile.getParent().findChild(DotPackagesFileUtil.DOT_PACKAGES);

    if (dotPackagesFile == null) {
      return createProblemDescriptors(manager, psiFile, pubspecFile, DartBundle.message("pub.get.never.done"));
    }

    if (FileDocumentManager.getInstance().isFileModified(pubspecFile) || pubspecFile.getTimeStamp() > dotPackagesFile.getTimeStamp()) {
      return createProblemDescriptors(manager, psiFile, pubspecFile, DartBundle.message("pubspec.edited"));
    }

    return null;
  }

  private ProblemDescriptor @NotNull [] createProblemDescriptors(@NotNull InspectionManager manager,
                                                                 @NotNull PsiFile psiFile,
                                                                 @NotNull VirtualFile pubspecFile,
                                                                 @NotNull @InspectionMessage String errorMessage) {
    final LocalQuickFix[] fixes = new LocalQuickFix[]{
      new RunPubFix(DartBundle.message("pub.get"), "Dart.pub.get"),
      new RunPubFix(DartBundle.message("pub.upgrade"), "Dart.pub.upgrade"),
      new OpenPubspecFix(),
      new IgnoreWarningFix(myIgnoredPubspecPaths, pubspecFile.getPath())};

    return new ProblemDescriptor[]{
      manager.createProblemDescriptor(psiFile, errorMessage, true, fixes, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)};
  }

  private static final class RunPubFix extends IntentionAndQuickFixAction {
    private final @IntentionName String myFixName;
    private final String myActionId;

    private RunPubFix(@NotNull @IntentionName String fixName, @NotNull @NonNls String actionId) {
      myFixName = fixName;
      myActionId = actionId;
    }

    @Override
    @NotNull
    public String getName() {
      return myFixName;
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return getName();
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final PsiFile psiFile, @Nullable final Editor editor) {
      final VirtualFile file = DartResolveUtil.getRealVirtualFile(psiFile);
      if (file == null || !file.isInLocalFileSystem()) return;

      final VirtualFile pubspecFile = PubspecYamlUtil.findPubspecYamlFile(project, file);
      if (pubspecFile == null) return;

      final Module module = ModuleUtilCore.findModuleForFile(file, project);
      if (module == null) return;

      final AnAction pubGetAction = ActionManager.getInstance().getAction(myActionId);
      if (pubGetAction instanceof DartPubActionBase) {
        ((DartPubActionBase)pubGetAction).performPubAction(module, pubspecFile, false);
      }
    }
  }

  private static class OpenPubspecFix extends IntentionAndQuickFixAction {
    @Override
    @NotNull
    public String getName() {
      return DartBundle.message("open.pubspec");
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return getName();
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final PsiFile psiFile, @Nullable final Editor editor) {
      final VirtualFile file = DartResolveUtil.getRealVirtualFile(psiFile);
      if (file == null || !file.isInLocalFileSystem()) return;

      final VirtualFile pubspecFile = PubspecYamlUtil.findPubspecYamlFile(project, file);
      if (pubspecFile == null) return;

      PsiNavigationSupport.getInstance().createNavigatable(project, pubspecFile, -1).navigate(true);
    }
  }

  private static class IgnoreWarningFix extends IntentionAndQuickFixAction {
    @NotNull private final Set<? super String> myIgnoredPubspecPaths;
    @NotNull private final String myPubspecPath;

    IgnoreWarningFix(@NotNull final Set<? super String> ignoredPubspecPaths, @NotNull final String pubspecPath) {
      myIgnoredPubspecPaths = ignoredPubspecPaths;
      myPubspecPath = pubspecPath;
    }

    @Override
    @NotNull
    public String getName() {
      return DartBundle.message("ignore.warning");
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return getName();
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final PsiFile psiFile, @Nullable final Editor editor) {
      myIgnoredPubspecPaths.add(myPubspecPath);
      DaemonCodeAnalyzer.getInstance(project).restart();
    }
  }
}
