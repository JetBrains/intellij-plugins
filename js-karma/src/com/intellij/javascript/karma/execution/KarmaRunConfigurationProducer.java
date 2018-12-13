// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.testFramework.JsTestElementPath;
import com.intellij.javascript.testFramework.PreferableRunConfiguration;
import com.intellij.javascript.testFramework.interfaces.mochaTdd.MochaTddFileStructure;
import com.intellij.javascript.testFramework.interfaces.mochaTdd.MochaTddFileStructureBuilder;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder;
import com.intellij.javascript.testing.JsTestRunConfigurationProducer;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public final class KarmaRunConfigurationProducer extends JsTestRunConfigurationProducer<KarmaRunConfiguration> {
  public KarmaRunConfigurationProducer() {
    super(Collections.singletonList(KarmaUtil.KARMA_PACKAGE_NAME));
  }

  @NotNull
  @Override
  public ConfigurationFactory getConfigurationFactory() {
    return KarmaConfigurationType.getInstance();
  }

  private boolean isTestRunnerPackageAvailableFor(@NotNull PsiElement element, @NotNull ConfigurationContext context) {
    if (context.getOriginalConfiguration(KarmaConfigurationType.getInstance()) instanceof KarmaRunConfiguration) {
      return true;
    }
    return super.isTestRunnerPackageAvailableFor(element);
  }

  @Override
  protected boolean setupConfigurationFromCompatibleContext(@NotNull KarmaRunConfiguration configuration,
                                                            @NotNull ConfigurationContext context,
                                                            @NotNull Ref<PsiElement> sourceElement) {
    PsiElement element = context.getPsiLocation();
    if (element == null || !isTestRunnerPackageAvailableFor(element, context)) {
      return false;
    }
    Pair<KarmaRunSettings, PsiElement> pair = setup(element, configuration.getRunSettings());
    if (pair != null) {
      configuration.setRunSettings(pair.getFirst());
      sourceElement.set(pair.getSecond());
      configuration.setGeneratedName();
      configuration.onNewConfigurationCreated();
      return true;
    }
    return false;
  }

  @Nullable
  private static Pair<KarmaRunSettings, PsiElement> setup(@Nullable PsiElement element,
                                                          @NotNull KarmaRunSettings templateSettings) {
    JSFile file = ObjectUtils.tryCast(element != null ? element.getContainingFile() : null, JSFile.class);
    VirtualFile virtualFile = PsiUtilCore.getVirtualFile(file);
    if (virtualFile == null) {
      return null;
    }
    if (!(element instanceof PsiFileSystemItem)) {
      Pair<KarmaRunSettings, PsiElement> suiteOrTestConfiguration = setupAsSuiteOrTest(file, virtualFile, element, templateSettings);
      if (suiteOrTestConfiguration != null) {
        return suiteOrTestConfiguration;
      }
    }
    if (KarmaUtil.isKarmaConfigFile(virtualFile.getNameSequence(), false)) {
      return Pair.create(templateSettings.toBuilder()
                           .setScopeKind(KarmaScopeKind.ALL)
                           .setConfigPath(virtualFile.getPath()).build(), file);
    }
    if (file.getTestFileType() != null) {
      KarmaRunSettings settings = guessConfigFileIfNeeded(templateSettings, virtualFile, element.getProject());
      return Pair.create(settings.toBuilder()
                           .setScopeKind(KarmaScopeKind.TEST_FILE)
                           .setTestFilePath(virtualFile.getPath()).build(), file);
    }
    return null;
  }

  @Nullable
  private static Pair<KarmaRunSettings, PsiElement> setupAsSuiteOrTest(@NotNull JSFile file,
                                                                       @NotNull VirtualFile virtualFile,
                                                                       @NotNull PsiElement element,
                                                                       @NotNull KarmaRunSettings templateSettings) {
    TextRange textRange = element.getTextRange();
    if (textRange == null || !file.isTestFile()) {
      return null;
    }
    JasmineFileStructure jasmineStructure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(file);
    JsTestElementPath path = jasmineStructure.findTestElementPath(textRange);
    if (path == null) {
      MochaTddFileStructure mochaTddFileStructure = MochaTddFileStructureBuilder.getInstance().fetchCachedTestFileStructure(file);
      path = mochaTddFileStructure.findTestElementPath(textRange);
    }
    if (path != null) {
      templateSettings = guessConfigFileIfNeeded(templateSettings, virtualFile, element.getProject());
      KarmaRunSettings.Builder builder = templateSettings.toBuilder();
      String testName = path.getTestName();
      if (testName == null) {
        builder.setScopeKind(KarmaScopeKind.SUITE);
        builder.setTestNames(path.getSuiteNames());
      }
      else {
        builder.setScopeKind(KarmaScopeKind.TEST);
        builder.setTestNames(path.getAllNames());
      }
      return Pair.create(builder.build(), path.getTestElement());
    }
    return null;
  }

  @NotNull
  private static KarmaRunSettings guessConfigFileIfNeeded(@NotNull KarmaRunSettings settings,
                                                          @NotNull VirtualFile contextFile,
                                                          @NotNull Project project) {
    if (!settings.getConfigPathSystemDependent().isEmpty()) {
      return settings;
    }
    ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
    VirtualFile dir = contextFile.isDirectory() ? contextFile : contextFile.getParent();
    if (dir == null) {
      return settings;
    }
    VirtualFile contentRoot = fileIndex.getContentRootForFile(dir, false);
    while (dir != null && contentRoot != null) {
      VirtualFile[] children = dir.getChildren();
      for (VirtualFile child : children) {
        if (KarmaUtil.isKarmaConfigFile(child.getNameSequence(), true)) {
          return settings.toBuilder().setConfigPath(child.getPath()).build();
        }
      }
      if (dir.equals(contentRoot)) {
        dir = dir.getParent();
        contentRoot = fileIndex.getContentRootForFile(dir, false);
      }
      else {
        dir = dir.getParent();
      }
    }
    return settings;
  }

  @Override
  protected boolean isConfigurationFromCompatibleContext(@NotNull KarmaRunConfiguration configuration,
                                                         @NotNull ConfigurationContext context) {
    KarmaRunSettings confSettings = configuration.getRunSettings();
    // use configuration.getRunSettings() as template settings to use equals method
    Pair<KarmaRunSettings, PsiElement> data = setup(context.getPsiLocation(), confSettings);
    return data != null && confSettings.equals(data.first);
  }

  @Override
  public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
    PsiFile psiFile = self.getSourceElement().getContainingFile();
    if (psiFile != null && other != null) {
      PreferableRunConfiguration otherRc = ObjectUtils.tryCast(other.getConfiguration(), PreferableRunConfiguration.class);
      if (otherRc != null && otherRc.isPreferredOver(self.getConfiguration(), psiFile)) {
        return false;
      }
    }
    return true;
  }
}
