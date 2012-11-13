package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.CompilerConfigGeneratorRt;
import com.intellij.flex.build.FlexBuildTarget;
import com.intellij.flex.build.FlexBuildTargetType;
import com.intellij.flex.model.JpsFlexProjectLevelCompilerOptionsExtension;
import com.intellij.flex.model.bc.*;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtilRt;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlexBuilder extends TargetBuilder<BuildRootDescriptor, FlexBuildTarget> {

  private static Logger LOG = Logger.getInstance(FlexBuilder.class.getName());

  private enum Status {Ok, Failed, Cancelled}

  protected FlexBuilder() {
    super(Collections.singletonList(FlexBuildTargetType.INSTANCE));
  }

  @NotNull
  public String getPresentableName() {
    return "Flash Compiler";
  }

  public void build(@NotNull final FlexBuildTarget buildTarget,
                    @NotNull final DirtyFilesHolder<BuildRootDescriptor, FlexBuildTarget> holder,
                    @NotNull final BuildOutputConsumer outputConsumer,
                    @NotNull final CompileContext context) throws ProjectBuildException, IOException {
    final Collection<String> dirtyFilePaths = new ArrayList<String>();

    holder.processDirtyFiles(new FileProcessor<BuildRootDescriptor, FlexBuildTarget>() {
      public boolean apply(final FlexBuildTarget target, final File file, final BuildRootDescriptor root) throws IOException {
        assert target == buildTarget;
        dirtyFilePaths.add(file.getPath());
        return true;
      }
    });

    if (LOG.isDebugEnabled()) {
      final StringBuilder b = new StringBuilder();
      b.append(buildTarget.getId()).append(", ").append("dirty files: ").append(dirtyFilePaths.size());

      if (dirtyFilePaths.size() < 10) {
        for (String path : dirtyFilePaths) {
          b.append('\n').append(path);
        }
      }

      LOG.debug(b.toString());
    }

    final JpsFlexBuildConfiguration mainBC = buildTarget.getBC();

    final List<JpsFlexBuildConfiguration> bcsToCompile = getAllBCsToCompile(mainBC);

    if (dirtyFilePaths.isEmpty() && !FlexCommonUtils.isFlexUnitBC(mainBC)) {
      boolean outputFilesExist = true;

      for (JpsFlexBuildConfiguration bc : bcsToCompile) {
        if (!new File(bc.getActualOutputFilePath()).isFile()) {
          outputFilesExist = false;
          LOG.debug("recompile because output file doesn't exist: " + bc.getActualOutputFilePath());
          break;
        }
      }

      if (outputFilesExist) {
        return;
      }
    }

    for (JpsFlexBuildConfiguration bc : bcsToCompile) {
      final Status status = compileBuildConfiguration(context, bc);

      switch (status) {
        case Ok:
          outputConsumer.registerOutputFile(mainBC.getActualOutputFilePath(), dirtyFilePaths);
          //performPostCompileActions(context, bc, dirtyFilePaths, outputConsumer);
          context.processMessage(
            new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.INFO, FlexCommonBundle.message("compilation.successful")));
          break;

        case Failed:
          final String message = bc.getOutputType() == OutputType.Application
                                 ? FlexCommonBundle.message("compilation.failed")
                                 : FlexCommonBundle.message("compilation.failed.dependent.will.be.skipped");
          context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.INFO, message));

          throw new ProjectBuildException();

        case Cancelled:
          context.processMessage(
            new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.INFO, FlexCommonBundle.message("compilation.cancelled")));
          return;
      }
    }
  }

  private static String getCompilerName(final JpsFlexBuildConfiguration bc) {
    String postfix = bc.isTempBCForCompilation() ? " - " + FlexCommonUtils.getBCSpecifier(bc) : "";
    if (!bc.getName().equals(bc.getModule().getName())) postfix += " (module " + bc.getModule().getName() + ")";
    return "[" + bc.getName() + postfix + "]";
  }

  private static List<JpsFlexBuildConfiguration> getAllBCsToCompile(final JpsFlexBuildConfiguration bc) {
    final List<JpsFlexBuildConfiguration> result =
      new ArrayList<JpsFlexBuildConfiguration>(1 + bc.getRLMs().size() + bc.getCssFilesToCompile().size());

    result.add(bc);

    if (FlexCommonUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
      for (JpsFlexBuildConfiguration.RLMInfo rlm : bc.getRLMs()) {
        result.add(createRlmBC(bc, rlm));
      }

      for (String cssPath : bc.getCssFilesToCompile()) {
        if (new File(cssPath).isFile()) {
          result.add(createCssBC(bc, cssPath));
        }
      }
    }

    return result;
  }

  private static JpsFlexBuildConfiguration createRlmBC(final JpsFlexBuildConfiguration mainBC,
                                                       final JpsFlexBuildConfiguration.RLMInfo rlm) {
    final JpsFlexBuildConfiguration rlmBC = mainBC.getModule().getProperties().createTemporaryCopyForCompilation(mainBC);

    rlmBC.setOutputType(OutputType.RuntimeLoadedModule);
    rlmBC.setOptimizeFor(rlm.OPTIMIZE ? mainBC.getName() : ""); // any not empty string means that need to optimize

    final String subdir = PathUtilRt.getParentPath(rlm.OUTPUT_FILE);
    final String outputFileName = PathUtilRt.getFileName(rlm.OUTPUT_FILE);

    rlmBC.setMainClass(rlm.MAIN_CLASS);
    rlmBC.setOutputFileName(outputFileName);

    if (!subdir.isEmpty()) {
      final String outputFolder = PathUtilRt.getParentPath(mainBC.getActualOutputFilePath());
      rlmBC.setOutputFolder(outputFolder + "/" + subdir);
    }

    rlmBC.setUseHtmlWrapper(false);

    rlmBC.setRLMs(Collections.<JpsFlexBuildConfiguration.RLMInfo>emptyList());
    rlmBC.setCssFilesToCompile(Collections.<String>emptyList());

    final JpsFlexCompilerOptions compilerOptions = rlmBC.getCompilerOptions();
    compilerOptions.setResourceFilesMode(JpsFlexCompilerOptions.ResourceFilesMode.None);
    compilerOptions.setAdditionalOptions(FlexCommonUtils.removeOptions(compilerOptions.getAdditionalOptions(), "link-report"));

    return rlmBC;
  }

  private static JpsFlexBuildConfiguration createCssBC(final JpsFlexBuildConfiguration mainBC, final String cssPath) {
    final JpsFlexBuildConfiguration cssBC = mainBC.getModule().getProperties().createTemporaryCopyForCompilation(mainBC);
    cssBC.setOutputType(OutputType.Application);

    cssBC.setMainClass(cssPath);
    cssBC.setOutputFileName(FileUtil.getNameWithoutExtension(PathUtilRt.getFileName(cssPath)) + ".swf");

    final String cssDirPath = PathUtilRt.getParentPath(cssPath);
    String relativeToRoot = FlexCommonUtils.getPathRelativeToSourceRoot(mainBC.getModule(), cssDirPath);
    if (relativeToRoot == null) {
      relativeToRoot = FlexCommonUtils.getPathRelativeToContentRoot(mainBC.getModule(), cssDirPath);
    }

    if (!StringUtil.isEmpty(relativeToRoot)) {
      final String outputFolder = PathUtilRt.getParentPath(mainBC.getActualOutputFilePath());
      cssBC.setOutputFolder(outputFolder + "/" + relativeToRoot);
    }

    cssBC.setUseHtmlWrapper(false);
    cssBC.setRLMs(Collections.<JpsFlexBuildConfiguration.RLMInfo>emptyList());
    cssBC.setCssFilesToCompile(Collections.<String>emptyList());

    cssBC.getCompilerOptions().setResourceFilesMode(JpsFlexCompilerOptions.ResourceFilesMode.None);
    return cssBC;
  }

  private static Status compileBuildConfiguration(final CompileContext context, final JpsFlexBuildConfiguration bc) {
    setProgressMessage(context, bc);

    final String compilerName = getCompilerName(bc);

    try {
      final List<File> configFiles = createConfigFiles(bc);
      final String outputFilePath = bc.getActualOutputFilePath();

      if (!ensureCanCreateFile(new File(outputFilePath))) {
        context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR,
                                                   FlexCommonBundle.message("failed.to.create.file", bc.getActualOutputFilePath())));
        return Status.Failed;
      }

      return doCompile(context, bc, configFiles, compilerName);
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, e.getMessage()));
      return Status.Failed;
    }
  }

  private static boolean ensureCanCreateFile(@NotNull File file) {
    final int maxAttempts = 3; // FileUtil.ensureCanCreateFile() may return false because of race conditions

    for (int i = 0; i < maxAttempts; i++) {
      if (FileUtil.ensureCanCreateFile(file)) return true;

      try {
        //noinspection BusyWait
        Thread.sleep(10);
      }
      catch (InterruptedException ignore) {/**/}
    }

    return false;
  }

  private static void setProgressMessage(final CompileContext context, final JpsFlexBuildConfiguration bc) {
    String postfix = bc.isTempBCForCompilation() ? " - " + FlexCommonUtils.getBCSpecifier(bc) : "";
    if (!bc.getName().equals(bc.getModule().getName())) postfix += " (module " + bc.getModule().getName() + ")";
    context.processMessage(new ProgressMessage(FlexCommonBundle.message("compiling", bc.getName() + postfix)));
  }

  private static List<File> createConfigFiles(final JpsFlexBuildConfiguration bc) throws IOException {
    final ArrayList<File> configFiles = new ArrayList<File>(2);
    configFiles.add(CompilerConfigGeneratorRt.getOrCreateConfigFile(bc));

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!bc.isTempBCForCompilation() && !additionalConfigFilePath.isEmpty()) {
      final File additionalConfigFile = new File(additionalConfigFilePath);
      if (!additionalConfigFile.isFile()) {
        throw new IOException(
          FlexCommonBundle.message("additional.config.file.not.found.for.bc.0.of.module.1", additionalConfigFilePath, bc.getName(),
                                   bc.getModule().getName()));
      }
      configFiles.add(additionalConfigFile);
    }

    return configFiles;
  }

  private static Status doCompile(final CompileContext context,
                                  final JpsFlexBuildConfiguration bc,
                                  final List<File> configFiles,
                                  final String compilerName) {
    final boolean app = bc.getOutputType() != OutputType.Library;
    final JpsSdk<?> sdk = bc.getSdk();
    assert sdk != null;

    final List<String> compilerCommand = getMxmlcCompcCommand(bc.getModule().getProject(), sdk, app);
    final List<String> command = buildCommand(compilerCommand, configFiles, bc);

    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    processBuilder.directory(new File(FlexCommonUtils.getFlexCompilerWorkDirPath(bc.getModule().getProject())));

    try {
      final Process process = processBuilder.start();

      final FlexCompilerProcessHandler processHandler =
        new FlexCompilerProcessHandler(context, process, compilerName, StringUtil.join(command, " "));
      processHandler.startNotify();
      processHandler.waitFor();

      return processHandler.isCancelled() ? Status.Cancelled
                                          : processHandler.isCompilationFailed()
                                            ? Status.Failed
                                            : Status.Ok;
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, e.getMessage()));
      return Status.Failed;
    }
  }

  private static List<String> getMxmlcCompcCommand(final JpsProject project, final JpsSdk<?> flexSdk, final boolean isApp) {
    final String mainClass = isApp ? StringUtil.compareVersionNumbers(flexSdk.getVersionString(), "4") >= 0 ? "flex2.tools.Mxmlc"
                                                                                                            : "flex2.tools.Compiler"
                                   : "flex2.tools.Compc";

    String additionalClasspath = FileUtil.toSystemDependentName(FlexCommonUtils.getPathToBundledJar("idea-flex-compiler-fix.jar"));

    if (flexSdk.getSdkType() == JpsFlexSdkType.INSTANCE) {
      additionalClasspath += File.pathSeparator + FileUtil.toSystemDependentName(flexSdk.getHomePath() + "/lib/compc.jar");
    }

    return FlexCommonUtils.getCommandLineForSdkTool(project, flexSdk, additionalClasspath, mainClass);
  }

  private static List<String> buildCommand(final List<String> compilerCommand,
                                           final List<File> configFiles,
                                           final JpsFlexBuildConfiguration bc) {
    final List<String> command = new ArrayList<String>(compilerCommand);
    for (File configFile : configFiles) {
      command.add("-load-config=" + configFile.getPath());
    }

    final JpsSdk<?> sdk = bc.getSdk();
    assert sdk != null;

    addAdditionalOptions(command, bc.getModule(), sdk.getHomePath(),
                         JpsFlexProjectLevelCompilerOptionsExtension
                           .getProjectLevelCompilerOptions(bc.getModule().getProject()).getAdditionalOptions());
    addAdditionalOptions(command, bc.getModule(), sdk.getHomePath(),
                         bc.getModule().getProperties().getModuleLevelCompilerOptions().getAdditionalOptions());
    addAdditionalOptions(command, bc.getModule(), sdk.getHomePath(), bc.getCompilerOptions().getAdditionalOptions());

    return command;
  }

  private static void addAdditionalOptions(List<String> command, JpsModule module, String sdkHome, String additionalOptions) {
    if (!StringUtil.isEmpty(additionalOptions)) {
      // TODO handle -option="path with spaces"
      for (final String s : StringUtil.split(additionalOptions, " ")) {
        command.add(FlexCommonUtils.replacePathMacros(s, module, sdkHome));
      }
    }
  }

  public static void performPostCompileActions(final CompileContext context,
                                               final @NotNull JpsFlexBuildConfiguration bc,
                                               final Collection<String> dirtyFilePaths,
                                               final BuildOutputConsumer outputConsumer) throws ProjectBuildException {
    final JpsSdk<?> sdk = bc.getSdk();
    assert sdk != null;

    LinkageType linkage = bc.getDependencies().getFrameworkLinkage();
    if (linkage == LinkageType.Default) {
      linkage = FlexCommonUtils.getDefaultFrameworkLinkage(sdk.getVersionString(), bc.getNature());
    }

    if (linkage == LinkageType.RSL) {
      //handleFrameworkRsls(bc, compileInfoMessages);
    }

    if (bc.getOutputType() != OutputType.Application || FlexCommonUtils.isRLMTemporaryBC(bc) || FlexCommonUtils.isRuntimeStyleSheetBC(bc)) {
      return;
    }

    switch (bc.getTargetPlatform()) {
      case Web:
        if (bc.isUseHtmlWrapper()) {
          handleHtmlWrapper(context, bc, outputConsumer);
        }
        break;
      /*
      case Desktop:
        handleAirDescriptor(module, bc, bc.getAirDesktopPackagingOptions());
        break;
      case Mobile:
        if (bc.getAndroidPackagingOptions().isEnabled()) {
          handleAirDescriptor(module, bc, bc.getAndroidPackagingOptions());
        }
        if (bc.getIosPackagingOptions().isEnabled()) {
          handleAirDescriptor(module, bc, bc.getIosPackagingOptions());
        }
        break;
      */
    }
  }

  private static void handleHtmlWrapper(final CompileContext context,
                                        final JpsFlexBuildConfiguration bc,
                                        final BuildOutputConsumer outputConsumer) {
    final File templateDir = new File(bc.getWrapperTemplatePath());
    if (!templateDir.isDirectory()) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR,
                                                 FlexCommonBundle.message("html.wrapper.dir.not.found", bc.getWrapperTemplatePath())));
      return;
    }
    final File templateFile = new File(templateDir, FlexCommonUtils.HTML_WRAPPER_TEMPLATE_FILE_NAME);
    if (!templateFile.isFile()) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR,
                                                 FlexCommonBundle.message("no.index.template.html.file", bc.getWrapperTemplatePath())));
      return;
    }

    final InfoFromConfigFile info = InfoFromConfigFile.getInfoFromConfigFile(bc.getCompilerOptions().getAdditionalConfigFilePath());
    final String outputFolderPath = StringUtil.notNullize(info.getOutputFolderPath(), bc.getOutputFolder());
    final String outputFileName = bc.isTempBCForCompilation()
                                  ? bc.getOutputFileName()
                                  : StringUtil.notNullize(info.getOutputFileName(), bc.getOutputFileName());
    final String targetPlayer = StringUtil.notNullize(info.getTargetPlayer(), bc.getDependencies().getTargetPlayer());

    final File outputDir = new File(outputFolderPath);
    if (!outputDir.isDirectory()) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR,
                                                 FlexCommonBundle.message("output.folder.does.not.exist", outputFolderPath)));
      return;
    }

    for (File file : templateDir.listFiles()) {
      if (FlexCommonUtils.HTML_WRAPPER_TEMPLATE_FILE_NAME.equals(file.getName())) {
        final String wrapperText;
        try {
          wrapperText = FileUtil.loadFile(file);
        }
        catch (IOException e) {
          context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle
            .message("failed.to.load.template.file", file.getPath(), e.getMessage())));
          return;
        }

        if (!wrapperText.contains(FlexCommonUtils.SWF_MACRO)) {
          context.processMessage(
            new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle.message("no.swf.macro", file.getPath())));
          return;
        }

        final String mainClass = StringUtil.notNullize(info.getMainClass(bc.getModule()), bc.getMainClass());
        final String fixedText = replaceMacros(wrapperText, FileUtil.getNameWithoutExtension(outputFileName), targetPlayer,
                                               FlexCommonUtils.getPathToMainClassFile(mainClass, bc.getModule()));
        final String wrapperFileName = FlexCommonUtils.getWrapperFileName(bc);
        try {
          FileUtil.writeToFile(new File(outputDir, wrapperFileName), fixedText);
        }
        catch (IOException e) {
          context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle
            .message("failed.to.create.file.in", wrapperFileName, outputDir.getPath(), e.getMessage())));
        }
      }
      else {
        try {
          final File outputFile = new File(outputDir, file.getName());
          if (file.isDirectory()) {
            FileUtil.createDirectory(outputFile);
            FileUtil.copyDir(file, outputFile);
          }
          else {
            FileUtil.copy(file, outputFile);
          }
          outputConsumer.registerOutputFile(outputFile.getPath(), Collections.singletonList(file.getPath()));
        }
        catch (IOException e) {
          context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle
            .message("failed.to.copy.file", file.getName(), templateDir.getPath(), outputDir.getPath(), e.getMessage())));
        }
      }
    }
  }

  private static String replaceMacros(final String wrapperText, final String outputFileName, final String targetPlayer,
                                      final String mainClassPath) {
    final Map<String, String> replacementMap = new THashMap<String, String>();

    replacementMap.put(FlexCommonUtils.SWF_MACRO, outputFileName);
    replacementMap.put(FlexCommonUtils.TITLE_MACRO, outputFileName);
    replacementMap.put(FlexCommonUtils.APPLICATION_MACRO, outputFileName);
    replacementMap.put(FlexCommonUtils.BG_COLOR_MACRO, "#ffffff");
    replacementMap.put(FlexCommonUtils.WIDTH_MACRO, "100%");
    replacementMap.put(FlexCommonUtils.HEIGHT_MACRO, "100%");

    final List<String> versionParts = StringUtil.split(targetPlayer, ".");
    replacementMap.put(FlexCommonUtils.VERSION_MAJOR_MACRO, versionParts.size() >= 1 ? versionParts.get(0) : "0");
    replacementMap.put(FlexCommonUtils.VERSION_MINOR_MACRO, versionParts.size() >= 2 ? versionParts.get(1) : "0");
    replacementMap.put(FlexCommonUtils.VERSION_REVISION_MACRO, versionParts.size() >= 3 ? versionParts.get(2) : "0");

    String swfMetadata = null;

    final File mainClassFile = new File(mainClassPath);
    if (mainClassFile.isFile()) {
      try {
        if ("mxml".equals(FileUtil.getExtension(mainClassPath))) {
          final Document document = JDOMUtil.loadDocument(mainClassFile);
          final Element rootElement = document.getRootElement();
          Element metadataElement = rootElement.getChild("Metadata", Namespace.getNamespace("http://www.adobe.com/2006/mxml"));
          if (metadataElement == null) {
            metadataElement = rootElement.getChild("Metadata", Namespace.getNamespace("http://ns.adobe.com/mxml/2009"));
          }
          if (metadataElement != null) {
            swfMetadata = getSwfMetadata(metadataElement.getTextNormalize());
          }
        }
        else if ("as".equals(FileUtil.getExtension(mainClassPath))) {
          swfMetadata = getSwfMetadata(FileUtil.loadFile(mainClassFile));
        }
      }
      catch (JDOMException ignore) {/*unlucky*/}
      catch (IOException ignore) {/*unlucky*/}
    }

    final Map<String, String> attributesMap = getAttributesMap(swfMetadata);

    ContainerUtil.putIfNotNull(FlexCommonUtils.TITLE_MACRO, attributesMap.get(FlexCommonUtils.TITLE_ATTR), replacementMap);
    ContainerUtil.putIfNotNull(FlexCommonUtils.BG_COLOR_MACRO, attributesMap.get(FlexCommonUtils.BG_COLOR_ATTR), replacementMap);
    ContainerUtil.putIfNotNull(FlexCommonUtils.WIDTH_MACRO, attributesMap.get(FlexCommonUtils.WIDTH_ATTR), replacementMap);
    ContainerUtil.putIfNotNull(FlexCommonUtils.HEIGHT_MACRO, attributesMap.get(FlexCommonUtils.HEIGHT_ATTR), replacementMap);

    return FlexCommonUtils.replace(wrapperText, replacementMap);
  }

  @Nullable
  private static String getSwfMetadata(final String text) {
    // todo use lexer
    int swfIndex = -1;
    while ((swfIndex = text.indexOf("[SWF", swfIndex + 1)) > -1) {
      final String textBefore = text.substring(0, swfIndex);
      final int lfIndex = Math.max(textBefore.lastIndexOf('\n'), textBefore.lastIndexOf('\r'));
      final int lineCommentIndex = textBefore.lastIndexOf("//");
      if (lineCommentIndex <= lfIndex) {
        final int endIndex = text.indexOf(']', swfIndex);
        return endIndex > swfIndex ? text.substring(swfIndex, endIndex + 1) : null;
      }
    }
    return null;
  }

  private static Map<String, String> getAttributesMap(final String metadata) {
    if (metadata == null) return Collections.emptyMap();

    final THashMap<String, String> result = new THashMap<String, String>();

    final int beginIndex = metadata.indexOf('(');
    final int endIndex = metadata.lastIndexOf(')');

    if (endIndex > beginIndex) {
      for (String attribute : StringUtil.split(metadata.substring(beginIndex + 1, endIndex), ",")) {
        final int eqIndex = attribute.indexOf('=');
        if (eqIndex > 0) {
          final String name = attribute.substring(0, eqIndex).trim();
          final String value = StringUtil.stripQuotesAroundValue(attribute.substring(eqIndex+1).trim());
          result.put(name, value);
        }
      }
    }

    return result;
  }
}
