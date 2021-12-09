// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.compiler.server.BuildManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.ComponentSet;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.index.JSPackageIndexInfo;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.UIBundle;
import com.intellij.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

public final class FlexUnitPrecompileTask implements CompileTask {
  public static final Key<Collection<String>> FILES_TO_DELETE = Key.create("FlexUnitPrecompileTask.filesToRemove");

  private final Project myProject;
  private static final String TEST_RUNNER_VAR = "__testRunner";

  private static final int FLEX_UNIT_PORT_START = 7832;
  private static final int PORTS_ATTEMPT_NUMBER = 20;
  private static final int SWC_POLICY_PORT_START = FLEX_UNIT_PORT_START + PORTS_ATTEMPT_NUMBER;

  public FlexUnitPrecompileTask(Project project) {
    myProject = project;
  }

  // in case of external build this path is returned by FlexCommonUtils.getPathToFlexUnitTempDirectory()
  public static String getPathToFlexUnitTempDirectory(final Project project) {
    final BuildManager buildManager = BuildManager.getInstance();
    final File projectSystemDir = buildManager.getProjectSystemDirectory(project);
    if (projectSystemDir == null) {
      Logger.getInstance(FlexUnitPrecompileTask.class.getName()).error(project);
      return "";
    }

    return FileUtil.toSystemIndependentName(projectSystemDir.getPath()) + "/tmp";
  }

  @Override
  public boolean execute(@NotNull CompileContext context) {
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(context.getCompileScope());
    if (!(runConfiguration instanceof FlexUnitRunConfiguration)) {
      return true;
    }

    final Ref<Boolean> isDumb = new Ref<>(false);
    final RuntimeConfigurationException validationError =
      ApplicationManager.getApplication().runReadAction((NullableComputable<RuntimeConfigurationException>)() -> {
        if (DumbService.getInstance(myProject).isDumb()) {
          isDumb.set(true);
          return null;
        }
        try {
          runConfiguration.checkConfiguration();
          return null;
        }
        catch (RuntimeConfigurationException e) {
          return e;
        }
      });

    if (isDumb.get()) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
      return false;
    }

    if (validationError != null) {
      context
        .addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("configuration.not.valid", validationError.getMessage()), null, -1,
                    -1);
      return false;
    }

    int flexUnitPort = ServerConnectionBase.getFreePort(FLEX_UNIT_PORT_START, PORTS_ATTEMPT_NUMBER);
    if (flexUnitPort == -1) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("no.free.port"), null, -1, -1);
      return false;
    }

    final int socketPolicyPort;
    if (SystemInfo.isWindows && ServerConnectionBase.tryPort(SwfPolicyFileConnection.DEFAULT_PORT)) {
      socketPolicyPort = SwfPolicyFileConnection.DEFAULT_PORT;
    }
    else {
      socketPolicyPort = ServerConnectionBase.getFreePort(SWC_POLICY_PORT_START, PORTS_ATTEMPT_NUMBER);
    }

    if (socketPolicyPort == -1) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("no.free.port"), null, -1, -1);
      return false;
    }

    final FlexUnitRunnerParameters params = ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters();
    params.setPort(flexUnitPort);
    params.setSocketPolicyPort(socketPolicyPort);

    final Ref<Module> moduleRef = new Ref<>();
    final Ref<FlexBuildConfiguration> bcRef = new Ref<>();
    final Ref<FlexUnitSupport> supportRef = new Ref<>();

    ApplicationManager.getApplication().runReadAction(() -> {
      if (DumbService.getInstance(myProject).isDumb()) return;

      try {
        final Pair<Module, FlexBuildConfiguration> moduleAndBC = params.checkAndGetModuleAndBC(myProject);
        moduleRef.set(moduleAndBC.first);
        bcRef.set(moduleAndBC.second);
        supportRef.set(FlexUnitSupport.getSupport(moduleAndBC.second, moduleAndBC.first));
      }
      catch (RuntimeConfigurationError e) {
        // already checked above, can't happen
        throw new RuntimeException(e);
      }
    });

    final Module module = moduleRef.get();
    final FlexBuildConfiguration bc = bcRef.get();
    final FlexUnitSupport support = supportRef.get();

    if (bc == null || support == null) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
      return false;
    }

    final GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);

    StringBuilder imports = new StringBuilder();
    StringBuilder code = new StringBuilder();

    final boolean flexUnit4;
    switch (params.getScope()) {
      case Class: {
        final Ref<Boolean> isFlexUnit1Suite = new Ref<>();
        final Ref<Boolean> isSuite = new Ref<>();
        Set<String> customRunners = ApplicationManager.getApplication().runReadAction((NullableComputable<Set<String>>)() -> {
          if (DumbService.getInstance(myProject).isDumb()) return null;
          Set<String> result = new HashSet<>();
          final JSClass clazz = (JSClass)ActionScriptClassResolver.findClassByQNameStatic(params.getClassName(), moduleScope);
          collectCustomRunners(result, clazz, support, null);
          isFlexUnit1Suite.set(support.isFlexUnit1SuiteSubclass(clazz));
          isSuite.set(support.isSuite(clazz));
          return result;
        });

        if (customRunners == null) {
          context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
          return false;
        }
        // FlexUnit4 can't run FlexUnit1 TestSuite subclasses, fallback to FlexUnit1 runner
        flexUnit4 = support.flexUnit4Present && !isFlexUnit1Suite.get();
        generateImportCode(imports, params.getClassName(), customRunners);
        generateTestClassCode(code, params.getClassName(), customRunners, isSuite.get());
      }
      break;

      case Method: {
        Set<String> customRunners = ApplicationManager.getApplication().runReadAction((NullableComputable<Set<String>>)() -> {
          if (DumbService.getInstance(myProject).isDumb()) return null;
          Set<String> result = new HashSet<>();
          final JSClass clazz = (JSClass)ActionScriptClassResolver.findClassByQNameStatic(params.getClassName(), moduleScope);
          collectCustomRunners(result, clazz, support, null);
          return result;
        });
        if (customRunners == null) {
          context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
          return false;
        }

        flexUnit4 = support.flexUnit4Present;
        generateImportCode(imports, params.getClassName(), customRunners);
        generateTestMethodCode(code, params.getClassName(), params.getMethodName(), customRunners);
      }
      break;

      case Package: {
        final Collection<Pair<String, Set<String>>> classes =
          ApplicationManager.getApplication().runReadAction((NullableComputable<Collection<Pair<String, Set<String>>>>)() -> {
            if (DumbService.getInstance(myProject).isDumb()) return null;

            final Collection<Pair<String, Set<String>>> result = new ArrayList<>();
            JSPackageIndex
              .processElementsInScopeRecursive(params.getPackageName(), new JSPackageIndex.PackageQualifiedElementsProcessor() {
                @Override
                public boolean process(String qualifiedName, JSPackageIndexInfo.Kind kind, boolean isPublic) {
                  if (kind == JSPackageIndexInfo.Kind.CLASS) {
                    PsiElement clazz = ActionScriptClassResolver.findClassByQNameStatic(qualifiedName, moduleScope);
                    if (clazz instanceof JSClass && support.isTestClass((JSClass)clazz, false)) {
                      Set<String> customRunners = new HashSet<>();
                      collectCustomRunners(customRunners, (JSClass)clazz, support, null);
                      result.add(Pair.create(((JSClass)clazz).getQualifiedName(), customRunners));
                    }
                  }
                  return true;
                }
              }, moduleScope, myProject);
            return result;
          });

        if (classes == null) {
          context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
          return false;
        }

        if (classes.isEmpty()) {
          String message = MessageFormat.format("No tests found in package ''{0}''", params.getPackageName());
          context.addMessage(CompilerMessageCategory.WARNING, message, null, -1, -1);
          return false;
        }

        flexUnit4 = support.flexUnit4Present;
        for (Pair<String, Set<String>> classAndRunner : classes) {
          generateImportCode(imports, classAndRunner.first, classAndRunner.second);
          generateTestClassCode(code, classAndRunner.first, classAndRunner.second, false);
        }
      }
      break;
      default:
        flexUnit4 = false;
        assert false : "Unknown scope: " + params.getScope();
    }

    if (!flexUnit4 && bc.isPureAs()) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("cant.execute.flexunit1.for.pure.as.bc"), null, -1, -1);
    }

    String launcherText;
    try {
      launcherText = getLauncherTemplate(bc);
    }
    catch (IOException e) {
      context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      return false;
    }

    final boolean desktop = bc.getTargetPlatform() == TargetPlatform.Desktop;
    if (desktop) {
      generateImportCode(imports, "flash.desktop.NativeApplication");
    }

    launcherText = replace(launcherText, "/*imports*/", imports.toString());
    launcherText =
      replace(launcherText, "/*test_runner*/", flexUnit4 ? FlexCommonUtils.FLEXUNIT_4_TEST_RUNNER : FlexCommonUtils.FLEXUNIT_1_TEST_RUNNER);
    launcherText = replace(launcherText, "/*code*/", code.toString());
    launcherText = replace(launcherText, "/*port*/", String.valueOf(flexUnitPort));
    launcherText = replace(launcherText, "/*socketPolicyPort*/", String.valueOf(socketPolicyPort));
    launcherText = replace(launcherText, "/*module*/", module.getName());
    if (!bc.isPureAs()) {
      final FlexUnitRunnerParameters.OutputLogLevel logLevel = params.getOutputLogLevel();
      launcherText = replace(launcherText, "/*isLogEnabled*/", logLevel != null ? "1" : "0");
      launcherText = replace(launcherText, "/*logLevel*/", logLevel != null
                                                           ? logLevel.getFlexConstant()
                                                           : FlexUnitRunnerParameters.OutputLogLevel.All.getFlexConstant());
    }


    final File tmpDir = new File(getPathToFlexUnitTempDirectory(myProject));
    boolean ok = true;
    if (tmpDir.isFile()) ok &= FileUtil.delete(tmpDir);
    if (!tmpDir.isDirectory()) ok &= tmpDir.mkdirs();
    if (!ok) {
      final String message =
        UIBundle.message("create.new.folder.could.not.create.folder.error.message", FileUtil.toSystemDependentName(tmpDir.getPath()));
      context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1);
      return false;
    }

    final String fileName = FlexCommonUtils.FLEX_UNIT_LAUNCHER + FlexCommonUtils.getFlexUnitLauncherExtension(bc.getNature());
    final File launcherFile = new File(tmpDir, fileName);
    FileUtil.delete(launcherFile);

    try {
      FileUtil.writeToFile(launcherFile, launcherText);
    }
    catch (IOException e) {
      context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      return false;
    }

    context.putUserData(FILES_TO_DELETE, Collections.singletonList(launcherFile.getPath()));
    return true;
  }

  private static String replace(final String text, final String pattern, final String replacement) {
    if (!text.contains(pattern)) {
      throw new RuntimeException("Pattern '" + pattern + "' not found in launcher text");
    }
    return text.replace(pattern, replacement);
  }

  private static void collectCustomRunners(Set<String> result,
                                           JSClass testClass,
                                           FlexUnitSupport flexUnitSupport,
                                           @Nullable Collection<JSClass> seen) {
    if (seen != null && seen.contains(testClass)) return;
    final String customRunner = FlexUnitSupport.getCustomRunner(testClass);
    if (!StringUtil.isEmptyOrSpaces(customRunner)) result.add(customRunner);
    if (flexUnitSupport.isSuite(testClass)) {
      if (seen == null) seen = new HashSet<>();
      seen.add(testClass);
      for (JSClass referencedClass : flexUnitSupport.getSuiteTestClasses(testClass)) {
        collectCustomRunners(result, referencedClass, flexUnitSupport, seen);
      }
    }
  }

  private static void generateImportCode(StringBuilder imports, String className, Collection<String> customRunners) {
    if (!StringUtil.isEmpty(StringUtil.getPackageName(className))) {
      generateImportCode(imports, className);
    }
    for (String customRunner : customRunners) {
      generateImportCode(imports, customRunner);
    }
  }

  private static void generateImportCode(StringBuilder imports, String qname) {
    imports.append("import ").append(qname).append(";\n");
  }

  private static void generateTestMethodCode(StringBuilder code,
                                             String className,
                                             String methodName,
                                             Collection<String> customRunners) {
    code.append(TEST_RUNNER_VAR).append(".addTestMethod(").append(className).append(", \"").append(methodName).append("\");\n");
    generateReferences(code, className, customRunners);
  }

  private static void generateTestClassCode(StringBuilder code,
                                            String className,
                                            Collection<String> customRunners,
                                            boolean isSuite) {
    code.append(TEST_RUNNER_VAR).append(".").append(isSuite ? "addTestSuiteClass(" : "addTestClass(").append(className).append(");\n");
    generateReferences(code, className, customRunners);
  }

  private static void generateReferences(StringBuilder code, String className, Collection<String> classes) {
    int i = 1;
    for (String qname : classes) {
      code.append("var __ref_").append(className.replace(".", "_")).append("_").append(i++).append("_ : ").append(qname).append(";\n");
    }
  }

  private static String getLauncherTemplate(FlexBuildConfiguration bc) throws IOException {
    String templateName;
    if (bc.isPureAs()) {
      templateName = "LauncherTemplateAs.as";
    }
    else if (bc.getNature().isMobilePlatform() || bc.getDependencies().getComponentSet() == ComponentSet.SparkOnly) {
      templateName = "LauncherTemplateSpark.mxml";
    }
    else {
      templateName = "LauncherTemplateMx.mxml";
    }
    return ResourceUtil.loadText(Objects.requireNonNull(FlexUnitPrecompileTask.class.getClassLoader()
                                                          .getResourceAsStream("unittestingsupport/" + templateName)));
  }
}
