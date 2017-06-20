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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import com.intellij.util.text.VersionComparatorUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.cidr.CocoaDocumentationManager;
import com.jetbrains.cidr.CocoaDocumentationManagerImpl;
import com.jetbrains.cidr.doc.XcodeDocumentationCandidateInfo;
import com.jetbrains.cidr.execution.ProcessHandlerWithPID;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.xcode.Xcode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Framework;
import org.jetbrains.plugins.ruby.motion.bridgesupport.FrameworkDependencyResolver;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.motion.run.MotionDeviceProcessHandler;
import org.jetbrains.plugins.ruby.motion.run.ProcessHandlerWithDetachSemaphore;
import org.jetbrains.plugins.ruby.motion.run.RubyMotionDeviceDebugProcess;
import org.jetbrains.plugins.ruby.motion.run.RubyMotionSimulatorDebugProcess;
import org.jetbrains.plugins.ruby.motion.symbols.FunctionSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionClassSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;
import org.jetbrains.plugins.ruby.rails.actions.generators.GeneratorsUtil;
import org.jetbrains.plugins.ruby.ruby.RModuleUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.REmptyType;
import org.jetbrains.plugins.ruby.ruby.interpret.PsiCallable;
import org.jetbrains.plugins.ruby.ruby.interpret.RCallArguments;
import org.jetbrains.plugins.ruby.ruby.interpret.RubyPsiInterpreter;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RArray;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RAssignmentExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RBinaryExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RequireInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RAssignmentExpressionNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RSelfAssignmentExpressionNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RShiftExpressionNavigator;
import org.jetbrains.plugins.ruby.ruby.run.ConsoleRunner;
import org.jetbrains.plugins.ruby.ruby.run.MergingCommandLineArgumentsProvider;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyAbstractCommandLineState;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.tasks.rake.RakeUtilBase;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.jetbrains.plugins.ruby.utils.MarkupConstants.SPACE;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionUtilImpl extends RubyMotionUtil {
  protected static final Key<ProjectType> PROJECT_TYPE = Key.create("ruby.motion.project.type");

  @Nullable
  public String getMotionDoc(PsiElement targetElement, @Nullable Symbol targetSymbol) {
    String descriptionText;
    final MotionSymbol motionSymbol = (MotionSymbol)targetSymbol;
    CocoaDocumentationManagerImpl.DocTokenType type = motionSymbol.getInfoType();
    CocoaDocumentationManagerImpl manager = (CocoaDocumentationManagerImpl)CocoaDocumentationManager.getInstance(targetSymbol.getProject());
    final Symbol parent = targetSymbol.getParentSymbol();
    final String parentName = parent != null ? parent.getName() : null;
    final CocoaDocumentationManagerImpl.DocumentationBean info =
      manager.getTokenInfo(targetElement, motionSymbol.getInfoName(),
                           Collections.singletonList(XcodeDocumentationCandidateInfo.create(parentName, type)));
    descriptionText = info != null ? patchObjCDoc(info.html, motionSymbol) : null;
    return descriptionText;
  }

  private static String patchObjCDoc(String html, MotionSymbol symbol) {
    if (symbol instanceof FunctionSymbol) {
      final FunctionSymbol fSymbol = (FunctionSymbol)symbol;
      final Function function = fSymbol.getFunction();
      final List<Pair<String, String>> arguments = function.getArguments();
      if (arguments.size() > 0) {
        for (Pair<String, String> argument : arguments) {
          html = html.replace("<code>" + argument.first + ":</code>", SPACE + SPACE + "<code>" + argument.first + "</code>: (" +
                                                                      getPresentableObjCType(fSymbol.getModule(), argument.second) + ") ");
        }
      }
    }
    // remove links
    html = html.replaceAll("<a href=[^>]*>", "");
    html = html.replaceAll("</a>", "");
    // remove declaration
    html = html.replaceAll("<p><b>Declaration:</b> <PRE>[^>]*</PRE></p>", "");
    html = html.replaceAll("<p><b>Declared In:</b> [^>]*</p>", "");
    return html;
  }

  private static String getPresentableObjCType(Module module, String type) {
    final RType primitiveType = MotionSymbolUtil.getTypeByName(module, type);
    if (primitiveType != REmptyType.INSTANCE) {
      return primitiveType.getPresentableName();
    }
    return "SEL".equals(type) ? "selector" : type.contains("(^)") ? "lambda" : type;
  }

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

  public String getSdkVersion(final Module module) {
    final String sdkVersion = module.getUserData(SDK_VERSION);
    if (sdkVersion != null) {
      return sdkVersion;
    }
    final Trinity<String, String[], ProjectType> sdkAndFrameworks = calculateAndCacheSdkAndFrameworks(module);
    return sdkAndFrameworks.first;
  }

  public boolean isOSX(@NotNull final Module module) {
    final ProjectType osx = module.getUserData(PROJECT_TYPE);
    if (osx != null) {
      return osx == ProjectType.OSX;
    }
    final Trinity<String, String[], ProjectType> sdkAndFrameworks = calculateAndCacheSdkAndFrameworks(module);
    return sdkAndFrameworks.third == ProjectType.OSX;
  }

  public boolean isAndroid(@NotNull final Module module) {
    final ProjectType android = module.getUserData(PROJECT_TYPE);
    if (android != null) {
      return android == ProjectType.ANDROID;
    }
    final Trinity<String, String[], ProjectType> sdkAndFrameworks = calculateAndCacheSdkAndFrameworks(module);
    return sdkAndFrameworks.third == ProjectType.ANDROID;
  }

  public Collection<Framework> getFrameworks(final Module module) {
    Collection<Framework> frameworks = module.getUserData(RubyMotionUtilExt.FRAMEWORKS_LIST);
    if (frameworks == null) {
      frameworks = FrameworkDependencyResolver.getInstance().getFrameworks(module);
      module.putUserData(RubyMotionUtilExt.FRAMEWORKS_LIST, frameworks);
    }
    return frameworks;
  }

  public String[] getRequiredFrameworks(final Module module) {
    final String[] frameworks = module.getUserData(REQUIRED_FRAMEWORKS);
    if (frameworks != null) {
      return frameworks;
    }
    final Trinity<String, String[], ProjectType> sdkAndFrameworks = calculateAndCacheSdkAndFrameworks(module);
    return sdkAndFrameworks.second;
  }

  private Trinity<String, String[], ProjectType> calculateAndCacheSdkAndFrameworks(Module module) {
    final Trinity<String, String[], ProjectType> sdkAndFrameworks = calculateSdkAndFrameworks(module);
    module.putUserData(SDK_VERSION, sdkAndFrameworks.first);
    module.putUserData(REQUIRED_FRAMEWORKS, sdkAndFrameworks.second);
    module.putUserData(PROJECT_TYPE, sdkAndFrameworks.third);
    return sdkAndFrameworks;
  }

  private Trinity<String, String[], ProjectType> calculateSdkAndFrameworks(@NotNull final Module module) {
    for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
      for (final VirtualFile file : root.getChildren()) {
        if (RakeUtilBase.isRakeFileByNamingConventions(file)) {
          final PsiFile psiFile =
            ReadAction.compute(() -> PsiManager.getInstance(module.getProject()).findFile(file));
          if (psiFile instanceof RFile) {
            return doCalculateSdkAndFrameworks((RFile)psiFile);
          }
        }
      }
    }
    return Trinity.create(getDefaultSdkVersion(ProjectType.IOS), DEFAULT_IOS_FRAMEWORKS, ProjectType.IOS);
  }

  protected String getDefaultSdkVersion(ProjectType projectType) {
    if (ApplicationManager.getApplication().isUnitTestMode() || !SystemInfo.isMac) {
      return "6.0";
    }

    if (projectType == ProjectType.ANDROID) {
      return "9";
    }

    final boolean osx = projectType == ProjectType.OSX;
    if ((osx && DEFAULT_OSX_SDK_VERSION == null) || DEFAULT_IOS_SDK_VERSION == null) {
      final File sdks = Xcode.getSubFile("Platforms/" + (osx ? "" : "iPhoneOS") + ".platform/Developer/SDKs/");
      final String[] list = sdks.list();
      String version = osx ? "10.7" : "4.3";
      final String prefix = osx ? OSX_SDK_PREFIX : IOS_SDK_PREFIX;
      if (list != null) {
        for (String sdk : list) {
          if (sdk.startsWith(prefix) && sdk.endsWith(SDK_SUFFIX)) {
            version = VersionComparatorUtil
              .max(version, sdk.substring(prefix.length()).substring(0, sdk.length() - prefix.length() - SDK_SUFFIX.length()));
          }
        }
      }
      if (osx) {
        DEFAULT_OSX_SDK_VERSION = version;
      } else {
        DEFAULT_IOS_SDK_VERSION = version;
      }
    }
    return osx ? DEFAULT_OSX_SDK_VERSION : DEFAULT_IOS_SDK_VERSION;
  }

  @TestOnly
  protected Pair<String, String[]> calculateSdkAndFrameworks(PsiFile file) {
    final Trinity<String, String[], ProjectType> result = doCalculateSdkAndFrameworks((RFile)file);
    return Pair.create(result.first, result.second);
  }

  private Trinity<String, String[], ProjectType> doCalculateSdkAndFrameworks(RFile file) {
    final ProjectType projectType = calculateProjectType(file);
    final Ref<String> sdkVersion = new Ref<>(getDefaultSdkVersion(projectType));
    final Set<String> frameworks = new HashSet<>();
    Collections.addAll(frameworks, projectType == ProjectType.OSX ? DEFAULT_OSX_FRAMEWORKS :
                                   projectType == ProjectType.ANDROID ? DEFAULT_ANDROID_FRAMEWORKS :
                                   DEFAULT_IOS_FRAMEWORKS);
    final RubyPsiInterpreter interpreter = new RubyPsiInterpreter(true);
    final PsiCallable callable = new PsiCallable() {
      @Override
      public void processCall(RCallArguments arguments) {
        final String command = arguments.getCommand();
        RAssignmentExpression assign = RAssignmentExpressionNavigator.getAssignmentByLeftPart(arguments.getCallElement());
        assign = assign == null ? RSelfAssignmentExpressionNavigator.getSelfAssignmentByLeftPart(arguments.getCallElement()) : assign;
        RBinaryExpression shift = assign == null ? RShiftExpressionNavigator.getShiftExpressionByLeftPart(arguments.getCallElement()) : null;
        final RPsiElement value = assign != null ? assign.getValue() : shift != null ? shift.getRightOperand() : null;
        if (value == null) {
          return;
        }
        final IElementType type = assign != null ? assign.getOperationType() : shift.getOperationType();

        if ("sdk_version".equals(command)) {
          sdkVersion.set(TextUtil.removeQuoting(value.getText()));
        } else if ("frameworks".equals(command)) {
          if (value instanceof RArray) {
            final String[] array = TextUtil.arrayToString((RArray)value).split(", ");
            if (type == RubyTokenTypes.tASSGN) {
              frameworks.clear();
              Collections.addAll(frameworks, array);
            } else if (type == RubyTokenTypes.tMINUS_OP_ASGN) {
              for (String s : array) {
                frameworks.remove(s);
              }
            } else {
              Collections.addAll(frameworks, array);
            }
          } else {
            frameworks.add(TextUtil.removeQuoting(value.getText()));
          }
        }
      }
    };

    interpreter.registerCallable(new PsiCallable() {
      @Override
      public void processCall(RCallArguments arguments) {
        arguments.interpretBlock(callable);
      }
    }, "Motion::Project::App.setup");
    interpreter.interpret(file, callable);
    return Trinity.create(sdkVersion.get(), ArrayUtil.toStringArray(frameworks), projectType);
  }

  private static ProjectType calculateProjectType(RFile file) {
    final List<RequireInfo> requires = file.getRequires();
    for (RequireInfo require : requires) {
      final String path = require.getPath();
      if (path.endsWith("template/osx") || path.endsWith("template/osx.rb")) {
        return ProjectType.OSX;
      }
      if (path.endsWith("template/android") || path.endsWith("template/android.rb")) {
        return ProjectType.ANDROID;
      }
    }
    return ProjectType.IOS;
  }

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
      public void processTerminated(ProcessEvent event) {
        FileUtil.moveDirWithContent(generatedApp, VfsUtilCore.virtualToIoFile(dir));
        tempDirectory.delete();

        if (module.isDisposed()) {
          return;
        }

        RModuleUtil.getInstance().refreshRubyModuleTypeContent(module);
        GeneratorsUtil.openFileInEditor(project, "app/app_delegate.rb", applicationHomePath);
        GeneratorsUtil.openFileInEditor(project, RakeUtilBase.RAKE_FILE, applicationHomePath);
        GeneratorsUtil.openFileInEditor(project, BundlerUtil.GEMFILE, applicationHomePath);
      }
    };
    final MergingCommandLineArgumentsProvider resultProvider =
      new MergingCommandLineArgumentsProvider(new String[] {getRubyMotionPath() + "/bin/motion", "create",
        "--template=" + projectType.name().toLowerCase(Locale.US), module.getName()},
                                              null, null, null, sdk);
    ConsoleRunner.run(module, null, processListener, filters, null, ConsoleRunner.ProcessLaunchMode.BACKGROUND_TASK_WITH_PROGRESS,
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

  @Override
  public XDebugSession createMotionDebugSession(final RunProfileState state,
                                                final ExecutionEnvironment env,
                                                final ProcessHandler serverProcessHandler) throws ExecutionException {
    final XDebugSession session = XDebuggerManager.getInstance(env.getProject()).
      startSession(env, new XDebugProcessStarter() {
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) {
          final CidrDebugProcess process;
          try {
            final RubyAbstractCommandLineState rubyState = (RubyAbstractCommandLineState)state;
            final TextConsoleBuilder consoleBuilder = rubyState.getConsoleBuilder();
            process = serverProcessHandler instanceof MotionDeviceProcessHandler ?
                      new RubyMotionDeviceDebugProcess(session, state, env.getExecutor(), consoleBuilder, serverProcessHandler) :
                      new RubyMotionSimulatorDebugProcess(session, state, env.getExecutor(), consoleBuilder, serverProcessHandler) {
                        @Override
                        protected ProcessHandlerWithPID createSimulatorProcessHandler(@NotNull RunParameters parameters,
                                                                                      boolean allowConcurrentSessions) throws ExecutionException {
                          final Module module = rubyState.getConfig().getModule();
                          assert module != null;
                          if (!getInstance().isOSX(module)) {
                            ((ProcessHandlerWithDetachSemaphore)serverProcessHandler).setDetachSemaphore(myProceedWithKillingSemaphore);
                          }
                          return (ProcessHandlerWithPID)serverProcessHandler;
                        }
                      };
            process.start();
          }
          catch (ExecutionException e) {
            throw new RuntimeException(e);
          }
          return process;
        }
      });
    return session;
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
