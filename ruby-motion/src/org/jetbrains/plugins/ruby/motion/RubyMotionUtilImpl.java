/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Framework;
import org.jetbrains.plugins.ruby.motion.bridgesupport.FrameworkDependencyResolver;
import org.jetbrains.plugins.ruby.motion.symbols.MotionClassSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;
import org.jetbrains.plugins.ruby.rails.actions.generators.GeneratorsUtil;
import org.jetbrains.plugins.ruby.ruby.RModuleUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.run.ConsoleRunner;
import org.jetbrains.plugins.ruby.ruby.run.MergingCommandLineArgumentsProvider;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.tasks.rake.RakeUtilBase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionUtilImpl extends RubyMotionUtil {
  protected static final Key<ProjectType> PROJECT_TYPE = Key.create("ruby.motion.project.type");

  @Contract("null -> false")
  public boolean isRubyMotionModule(@Nullable final Module module) {
    if (module == null) {
      return false;
    }
    for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
      for (final VirtualFile file : root.getChildren()) {
        if (RakeUtilBase.isRakeFileByNamingConventions(file)) {
          try {
            final String text = VfsUtilCore.loadText(file);
            if (text.contains("Motion::Project")) {
              return true;
            }
          }
          catch (IOException ignored) {
          }
        }
      }
    }
    return false;
  }

  public boolean hasMacRubySupport(@Nullable PsiElement element) {
    final PsiFile psiFile = element == null ? null : element.getContainingFile();
    if (psiFile == null) return false;

    return CachedValuesManager.getCachedValue(psiFile, () -> CachedValueProvider.Result
      .create(hasMacRubySupport(psiFile), PsiModificationTracker.MODIFICATION_COUNT));
  }

  private boolean hasMacRubySupport(PsiFile psiFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    final Sdk sdk = RModuleUtil.getInstance().findRubySdkForModule(module);
    // module has MacRuby SDK
    if (RubySdkUtil.isMacRuby(sdk)) return true;

    // file is inside RubyMotion
    final VirtualFile file = psiFile.getVirtualFile();
    final String path = file != null ? file.getPath() : null;
    if (path != null && StringUtil.startsWithIgnoreCase(path, getRubyMotionPath())) {
      return true;
    }

    // module has RubyMotion support
    return hasRubyMotionSupport(module) || getModuleWithMotionSupport(psiFile.getProject()) != null;
  }

  public boolean hasRubyMotionSupport(@Nullable Module module) {
    return getRubyMotionFacet(module) != null;
  }

  @Override
  public String getSdkVersion(Module module) {
    final Trinity<String, String[], ProjectType> sdkAndFrameworks = Trinity.create("6.0", DEFAULT_IOS_FRAMEWORKS, ProjectType.IOS);
    module.putUserData(SDK_VERSION, sdkAndFrameworks.first);
    module.putUserData(REQUIRED_FRAMEWORKS, sdkAndFrameworks.second);
    module.putUserData(PROJECT_TYPE, sdkAndFrameworks.third);
    return sdkAndFrameworks.first;
  }

  @Override
  public String[] getRequiredFrameworks(Module module) {
    return DEFAULT_IOS_FRAMEWORKS;
  }

  public Collection<Framework> getFrameworks(final Module module) {
    Collection<Framework> frameworks = module.getUserData(RubyMotionUtilExt.FRAMEWORKS_LIST);
    if (frameworks == null) {
      frameworks = FrameworkDependencyResolver.getInstance().getFrameworks(module);
      module.putUserData(RubyMotionUtilExt.FRAMEWORKS_LIST, frameworks);
    }
    return frameworks;
  }
  
  @TestOnly
  public void resetSdkAndFrameworks(Module module) {
    module.putUserData(SDK_VERSION, null);
    module.putUserData(REQUIRED_FRAMEWORKS, null);
    module.putUserData(RubyMotionUtilExt.FRAMEWORKS_LIST, null);
    MotionSymbolUtil.MotionTypeCache.getInstance(module).reset();
    MotionSymbolUtil.MotionSymbolsCache.getInstance(module).reset();
  }

  public boolean isIgnoredFrameworkName(String name) {
    return name.equals("RubyMotion") || name.equals("UIAutomation");
  }


  public String getMainRakeTask(@NotNull final Module module) {
    return isOSX(module) ? "run" :
           isAndroid(module) ? "emulator" :
           "simulator";
  }

  public String getRubyMotionPath() {
    return ApplicationManager.getApplication().isUnitTestMode() ?
           PathManager.getHomePath() + "/ruby/gemsData/RubyMotion" :
           RUBY_MOTION_PATH;
  }

  public boolean rubyMotionPresent() {
    return new File(getRubyMotionPath() + "/bin/motion").exists();
  }

  public void generateApp(final VirtualFile dir,
                                 final Module module,
                                 Sdk sdk,
                                 final ProjectType projectType)  {
    final Project project = module.getProject();
    final String applicationHomePath = dir.getPath();
    final File tempDirectory;
    try {
      tempDirectory = FileUtil.createTempDirectory("RubyMotion", ".RubyMine");
    } catch (IOException e) {
      throw new Error(e);
    }
    final File generatedApp = new File(tempDirectory, module.getName());
    final Filter[] filters = null;
    final ProcessAdapter processListener = new ProcessAdapter() {
      public void processTerminated(@NotNull ProcessEvent event) {
        FileUtil.moveDirWithContent(generatedApp, VfsUtilCore.virtualToIoFile(dir));
        tempDirectory.delete();

        if (module.isDisposed()) {
          return;
        }

        RModuleUtil.getInstance().refreshRubyModuleTypeContent(module);
        GeneratorsUtil.openFileInEditor(project, "app/app_delegate.rb", applicationHomePath);
        GeneratorsUtil.openFileInEditor(project, RakeUtilBase.RAKE_FILE, applicationHomePath);
        BundlerUtil.openGemFileInEditor(project, applicationHomePath);
      }
    };
    final MergingCommandLineArgumentsProvider resultProvider =
      new MergingCommandLineArgumentsProvider(new String[] {getRubyMotionPath() + "/bin/motion", "create",
        "--template=" + projectType.name().toLowerCase(Locale.US), module.getName()},
                                              null, null, null, sdk);
    ConsoleRunner.run(project, module, null, processListener, filters, null, ConsoleRunner.ProcessLaunchMode.BACKGROUND_TASK_WITH_PROGRESS,
                      "Generating RubyMotion Application '" + module.getName() + "'...", tempDirectory.getAbsolutePath(), resultProvider,
                      null, false);
  }

  @Deprecated
  @Nullable
  public Module getModuleWithMotionSupport(final @NotNull Project project) {
    Module[] allModules = ModuleManager.getInstance(project).getModules();
    for (Module module : allModules) {
      if (getRubyMotionFacet(module) != null) {
        return module;
      }
    }
    return null;
  }

  @Contract(value = "null -> null")
  @Nullable
  public Facet getRubyMotionFacet(@Nullable final Module module) {
    if (module == null || module.isDisposed()) return null;
    for (Facet facet : FacetManager.getInstance(module).getAllFacets()) {
      if (facet.getType().getStringId().equals("ruby_motion")) {
        return facet;
      }
    }
    return null;
  }
  
  public boolean isMotionSymbol(@Nullable Symbol targetSymbol) {
    return targetSymbol instanceof MotionSymbol;
  }

  public Symbol getMotionSuperclass(Symbol targetSymbol, PsiElement invocationPoint) {
    return targetSymbol instanceof MotionClassSymbol ?
           ((MotionClassSymbol)targetSymbol).getSuperClassSymbol(invocationPoint) :
           null;
  }

  public enum ProjectType {
    IOS("iOS"),
    OSX("OS X"),
    ANDROID("Android"),
    GEM("Gem");
    private final String myDisplayName;

    ProjectType(String displayName) {
      myDisplayName = displayName;
    }

    @Override
    public String toString() {
      return myDisplayName;
    }
  }
}
