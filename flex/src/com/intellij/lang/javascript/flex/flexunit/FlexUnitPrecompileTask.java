package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.airdescriptor.AirDescriptorParameters;
import com.intellij.lang.javascript.flex.actions.airdescriptor.CreateAirDescriptorAction;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerHandler;
import com.intellij.lang.javascript.flex.run.FlexRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.index.JSPackageIndexInfo;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SystemProperties;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class FlexUnitPrecompileTask implements CompileTask {

  private static final String FLEX_UNIT_LAUNCHER_BASE = "____FlexUnitLauncherBase.mxml";
  private static final String FLEX_UNIT_LAUNCHER_MXML = "____FlexUnitLauncher.mxml";
  private static final String FLEX_UNIT_LOG_TARGET = "____FlexUnitTestListener.as";

  public static final Key<Collection<String>> FILES_TO_DELETE = Key.create("FlexUnitPrecompileTask.filesToRemove");

  private final Project myProject;
  private static final String TEST_SUITE_VAR = "__testSuite__";

  private static final int FLEXUNIT_PORT_START = 7832;
  private static final int PORTS_ATTEMPT_NUMBER = 20;
  private static final int SWC_POLICY_PORT_START = FLEXUNIT_PORT_START + PORTS_ATTEMPT_NUMBER;

  public FlexUnitPrecompileTask(Project project) {
    myProject = project;
  }

  public boolean execute(CompileContext context) {
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(context.getCompileScope());
    if (!(runConfiguration instanceof FlexUnitRunConfiguration)) {
      return true;
    }

    final Ref<Boolean> isDumb = new Ref<Boolean>(false);
    final RuntimeConfigurationException validationError =
      ApplicationManager.getApplication().runReadAction(new NullableComputable<RuntimeConfigurationException>() {
        public RuntimeConfigurationException compute() {
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

    int flexUnitPort = ServerConnectionBase.getFreePort(FLEXUNIT_PORT_START, PORTS_ATTEMPT_NUMBER);
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

    final Pair<Module, FlexUnitSupport> supportForModule =
      ApplicationManager.getApplication().runReadAction(new NullableComputable<Pair<Module, FlexUnitSupport>>() {
        public Pair<Module, FlexUnitSupport> compute() {
          if (DumbService.getInstance(myProject).isDumb()) return null;
          Module module = ModuleManager.getInstance(myProject).findModuleByName(params.getModuleName());
          FlexUnitSupport flexUnitSupport = FlexUnitSupport.getSupport(module);
          return Pair.create(module, flexUnitSupport);
        }
      });

    if (supportForModule == null) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
      return false;
    }

    final GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(supportForModule.first);

    StringBuilder imports = new StringBuilder();
    StringBuilder code = new StringBuilder();

    final boolean flexUnit4;
    switch (params.getScope()) {
      case Class: {
        final Ref<Boolean> isFlexUnit1Suite = new Ref<Boolean>();
        final Ref<Boolean> isSuite = new Ref<Boolean>();
        Set<String> customRunners = ApplicationManager.getApplication().runReadAction(new NullableComputable<Set<String>>() {
          public Set<String> compute() {
            if (DumbService.getInstance(myProject).isDumb()) return null;
            Set<String> result = new THashSet<String>();
            final JSClass clazz = (JSClass)JSResolveUtil.findClassByQName(params.getClassName(), moduleScope);
            collectCustomRunners(result, clazz, supportForModule.second, null);
            isFlexUnit1Suite.set(supportForModule.second.isFlexUnit1SuiteSubclass(clazz));
            isSuite.set(supportForModule.second.isSuite(clazz));
            return result;
          }
        });

        if (customRunners == null) {
          context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
          return false;
        }
        // FlexUnit4 can't run FlexUnit1 TestSuite subclasses, fallback to FlexUnit1 runner
        flexUnit4 = supportForModule.second.flexUnit4Present && !isFlexUnit1Suite.get();
        generateImportCode(imports, params.getClassName(), customRunners);
        generateTestClassCode(code, params.getClassName(), flexUnit4, customRunners, isSuite.get());
      }
      break;

      case Method: {
        Set<String> customRunners = ApplicationManager.getApplication().runReadAction(new NullableComputable<Set<String>>() {
          public Set<String> compute() {
            if (DumbService.getInstance(myProject).isDumb()) return null;
            Set<String> result = new THashSet<String>();
            final JSClass clazz = (JSClass)JSResolveUtil.findClassByQName(params.getClassName(), moduleScope);
            collectCustomRunners(result, clazz, supportForModule.second, null);
            return result;
          }
        });
        if (customRunners == null) {
          context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("dumb.mode.flex.unit.warning"), null, -1, -1);
          return false;
        }

        flexUnit4 = supportForModule.second.flexUnit4Present;
        generateImportCode(imports, params.getClassName(), customRunners);
        generateTestMethodCode(code, params.getClassName(), params.getMethodName(), flexUnit4, customRunners);
      }
      break;

      case Package: {
        final Collection<Pair<String, Set<String>>> classes =
          ApplicationManager.getApplication().runReadAction(new NullableComputable<Collection<Pair<String, Set<String>>>>() {
            public Collection<Pair<String, Set<String>>> compute() {
              if (DumbService.getInstance(myProject).isDumb()) return null;

              final Collection<Pair<String, Set<String>>> result = new ArrayList<Pair<String, Set<String>>>();
              JSPackageIndex
                .processElementsInScopeRecursive(params.getPackageName(), new JSPackageIndex.PackageQualifiedElementsProcessor() {
                  public boolean process(String qualifiedName, JSPackageIndexInfo.Kind kind, boolean isPublic) {
                    if (kind == JSPackageIndexInfo.Kind.CLASS) {
                      PsiElement clazz = JSResolveUtil.findClassByQName(qualifiedName, moduleScope);
                      if (clazz instanceof JSClass && supportForModule.second.isTestClass((JSClass)clazz, false)) {
                        Set<String> customRunners = new THashSet<String>();
                        collectCustomRunners(customRunners, (JSClass)clazz, supportForModule.second, null);
                        result.add(Pair.create(((JSClass)clazz).getQualifiedName(), customRunners));
                      }
                    }
                    return true;
                  }
                }, moduleScope, myProject);
              return result;
            }
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

        flexUnit4 = supportForModule.second.flexUnit4Present;
        for (Pair<String, Set<String>> classAndRunner : classes) {
          generateImportCode(imports, classAndRunner.first, classAndRunner.second);
          generateTestClassCode(code, classAndRunner.first, flexUnit4, classAndRunner.second, false);
        }
      }
      break;
      default:
        flexUnit4 = false;
        assert false : "Unknown scope: " + params.getScope();
    }

    String prefix = supportForModule.first.getName().replaceAll("[^\\p{Alnum}]", "_") +
                    "_" +
                    SystemProperties.getUserName().toLowerCase().replaceAll("[^\\p{Alnum}]", "_");

    final String launcherBaseFileName = prefix + FLEX_UNIT_LAUNCHER_BASE;
    final String launcherFileName = prefix + FLEX_UNIT_LAUNCHER_MXML;
    final String logTargetFileName = prefix + FLEX_UNIT_LOG_TARGET;

    String launcherText;
    String logTargetText;
    final String launcherBaseText;
    try {
      launcherText = FlexUnitRunConfiguration.getLauncherTemplate(flexUnit4);
      launcherBaseText = FlexUnitRunConfiguration.getLauncherBaseText();
      logTargetText = FlexUnitRunConfiguration.getLogTargetText();
    }
    catch (IOException e) {
      context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      return false;
    }

    final boolean airDependent = FlexSdkUtils.hasDependencyOnAir(supportForModule.first);
    if (airDependent) {
      generateImportCode(imports, "flash.desktop.NativeApplication");
    }

    launcherText = launcherText.replace("/*imports*/", imports);
    launcherText = launcherText.replace("/*code*/", code);
    launcherText = launcherText.replace("/*port*/", String.valueOf(flexUnitPort));
    launcherText = launcherText.replace("/*socketPolicyPort*/", String.valueOf(socketPolicyPort));
    launcherText = launcherText.replace("/*base*/", FileUtil.getNameWithoutExtension(launcherBaseFileName));
    launcherText = launcherText.replace("/*module*/", supportForModule.first.getName());
    launcherText =
      launcherText.replace("/*closeApp*/", airDependent ? "NativeApplication.nativeApplication.exit(0)" : "fscommand(\"quit\")");
    launcherText = launcherText.replace("/*logTargetClass*/", FileUtil.getNameWithoutExtension(logTargetFileName));
    launcherText = launcherText.replace("/*logEnabled*/", params.getOutputLogLevel() != null ? "true" : "false");
    launcherText = launcherText.replace("/*logLevel*/", params.getOutputLogLevel() != null
                                                        ? params.getOutputLogLevel().getFlexConstant()
                                                        : FlexUnitRunnerParameters.OutputLogLevel.values()[0].getFlexConstant());

    logTargetText = logTargetText.replace("/*className*/", FileUtil.getNameWithoutExtension(logTargetFileName));

    final VirtualFile tempDir = LocalFileSystem.getInstance().findFileByIoFile(new File(FlexUtils.getPathToFlexUnitTempDirectory()));

    final Ref<VirtualFile> launcherFile = new Ref<VirtualFile>();
    final Ref<IOException> createLauncherError = new Ref<IOException>();
    final String logTargetText1 = logTargetText;
    final String launcherText1 = launcherText;
    final Collection<String> filesToDelete = new ArrayList<String>();
    final Runnable createLauncherRunnable = new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              VirtualFile launcherBaseFile = FlexUtils.addFileWithContent(launcherBaseFileName, launcherBaseText, tempDir);
              VirtualFile logTargetFile = FlexUtils.addFileWithContent(logTargetFileName, logTargetText1, tempDir);
              launcherFile.set(FlexUtils.addFileWithContent(launcherFileName, launcherText1, tempDir));

              filesToDelete.add(launcherFile.get().getPath());
              filesToDelete.add(launcherBaseFile.getPath());
              filesToDelete.add(logTargetFile.getPath());
            }
            catch (IOException ex) {
              createLauncherError.set(ex);
            }
          }
        });
      }
    };
    if (ApplicationManager.getApplication().isDispatchThread()) {
      createLauncherRunnable.run();
    }
    else {
      ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();
      ApplicationManager.getApplication()
        .invokeAndWait(createLauncherRunnable, pi != null ? pi.getModalityState() : ModalityState.NON_MODAL);
    }

    if (!createLauncherError.isNull()) {
      context.addMessage(CompilerMessageCategory.ERROR, createLauncherError.get().getMessage(), null, -1, -1);
      return false;
    }

    context.putUserData(FILES_TO_DELETE, filesToDelete);

    final FlexBuildConfiguration originalConfig =
      FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(supportForModule.first).iterator().next();

    final FlexBuildConfiguration buildConfig = originalConfig.clone();

    buildConfig.setType(FlexBuildConfiguration.Type.FlexUnit);
    buildConfig.MAIN_CLASS = launcherFileName.substring(0, launcherFileName.length() - ".mxml".length());
    buildConfig.DO_BUILD = true;
    buildConfig.OUTPUT_TYPE = FlexBuildConfiguration.APPLICATION;
    // '_' symbol is a sort of a marker that it is autogenerated swf file name
    buildConfig.OUTPUT_FILE_NAME = "_flexunit.swf";

    context.putUserData(FlexCompilerHandler.OVERRIDE_BUILD_CONFIG, buildConfig);

    if (airDependent) {
      params.setRunAsAir(true);
      String airVersion = FlexSdkUtils.getAirVersion(FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(supportForModule.first));
      String applicationId = "flexunit.test.application";
      final String descriptorFileName = "_" + supportForModule.first.getName() + "-air-flexunit.xml";
      final String descriptorFolderPath = buildConfig.getCompileOutputPathForTests();
      final AirDescriptorParameters descriptorParams =
        new AirDescriptorParameters(descriptorFileName, descriptorFolderPath, airVersion, applicationId, applicationId, applicationId,
                                    "0.0", buildConfig.OUTPUT_FILE_NAME, FlexBundle.message("flexunit.test.runner.caption"), 500, 400);
      final Ref<IOException> createDescriptorError = new Ref<IOException>();
      Runnable createDescriptorRunnable = new Runnable() {
        public void run() {
          try {
            CreateAirDescriptorAction.createAirDescriptor(descriptorParams);
          }
          catch (IOException e) {
            createDescriptorError.set(e);
          }
        }
      };

      if (ApplicationManager.getApplication().isDispatchThread()) {
        createDescriptorRunnable.run();
      }
      else {
        ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();
        ApplicationManager.getApplication()
          .invokeAndWait(createDescriptorRunnable, pi != null ? pi.getModalityState() : ModalityState.NON_MODAL);
      }
      if (!createDescriptorError.isNull()) {
        context.addMessage(CompilerMessageCategory.ERROR, createDescriptorError.get().getMessage(), null, -1, -1);
        return false;
      }

      params.setAirDescriptorPath(descriptorFolderPath + "/" + descriptorFileName);
      params.setAirRootDirPath(descriptorFolderPath);
    }
    else {
      params.setRunAsAir(false);
      params.setRunMode(FlexRunnerParameters.RunMode.HtmlOrSwfFile);
      params.setHtmlOrSwfFilePath(buildConfig.getCompileOutputPathForTests() + "/" + buildConfig.OUTPUT_FILE_NAME);
    }
    return true;
  }

  private static void collectCustomRunners(Set<String> result,
                                           JSClass testClass,
                                           FlexUnitSupport flexUnitSupport,
                                           @Nullable Collection<JSClass> seen) {
    if (seen != null && seen.contains(testClass)) return;
    final String customRunner = FlexUnitSupport.getCustomRunner(testClass);
    if (!StringUtil.isEmptyOrSpaces(customRunner)) result.add(customRunner);
    if (flexUnitSupport.isSuite(testClass)) {
      if (seen == null) seen = new THashSet<JSClass>();
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
                                             boolean flexUnit4Present,
                                             Collection<String> customRunners) {
    if (flexUnit4Present) {
      code.append(TEST_SUITE_VAR).append(".push(Request.method(").append(className).append(", \"").append(methodName).append("\"));\n");
    }
    else {
      code.append("var __test__ : TestCase = new ").append(className).append("();\n");
      code.append("__test__.methodName = \"").append(methodName).append("\";\n");
      code.append(TEST_SUITE_VAR).append(".addTest(__test__);\n");
    }
    generateReferences(code, className, customRunners);
  }

  private static void generateTestClassCode(StringBuilder code,
                                            String className,
                                            boolean flexUnit4Present,
                                            Collection<String> customRunners,
                                            boolean isSuite) {
    if (flexUnit4Present) {
      code.append(TEST_SUITE_VAR).append(".push(Request.aClass(").append(className).append("));\n");
    }
    else {
      if (isSuite) {
        code.append(TEST_SUITE_VAR).append(".addTest(new ").append(className).append("());\n");
      }
      else {
        code.append(TEST_SUITE_VAR).append(".addTestSuite(").append(className).append(");\n");
      }
    }
    generateReferences(code, className, customRunners);
  }

  private static void generateReferences(StringBuilder code, String className, Collection<String> classes) {
    int i = 1;
    for (String qname : classes) {
      code.append("var __ref_").append(className.replace(".", "_")).append("_").append(i++).append("_ : ").append(qname).append(";\n");
    }
  }

}
