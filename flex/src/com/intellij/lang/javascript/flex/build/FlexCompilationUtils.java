package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateAirDescriptorTemplateDialog;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.util.PathUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.ZipUtil;
import com.intellij.util.text.StringTokenizer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static com.intellij.lang.javascript.flex.projectStructure.ui.CreateAirDescriptorTemplateDialog.AirDescriptorOptions;
import static com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog.HTML_WRAPPER_TEMPLATE_FILE_NAME;
import static com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog.VERSION_MAJOR_MACRO;
import static com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog.VERSION_MINOR_MACRO;
import static com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog.VERSION_REVISION_MACRO;

public class FlexCompilationUtils {

  public static final String SWF_MACRO = "${swf}";

  private static final String[] MACROS_TO_REPLACE =
    {SWF_MACRO, "${title}", "${application}", "${bgcolor}", "${width}", "${height}", VERSION_MAJOR_MACRO, VERSION_MINOR_MACRO,
      VERSION_REVISION_MACRO};

  private FlexCompilationUtils() {
  }

  static void deleteCacheForFile(final String filePath) throws IOException {
    final VirtualFile cacheFile = LocalFileSystem.getInstance().findFileByPath(filePath + ".cache");
    if (cacheFile != null) {
      final Ref<IOException> exceptionRef = new Ref<IOException>();

      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              if (cacheFile.exists()) {
                try {
                  cacheFile.delete(this);
                }
                catch (IOException e) {
                  exceptionRef.set(e);
                }
              }
            }
          });
        }
      }, ProgressManager.getInstance().getProgressIndicator().getModalityState());

      if (!exceptionRef.isNull()) {
        throw exceptionRef.get();
      }
    }
  }

  static List<String> buildCommand(final List<String> compilerCommand,
                                   final List<VirtualFile> configFiles,
                                   final Module module,
                                   final FlexIdeBuildConfiguration bc) {
    final List<String> command = new ArrayList<String>(compilerCommand);
    for (VirtualFile configFile : configFiles) {
      command.add("-load-config=" + configFile.getPath());
    }

    final Sdk sdk = bc.getSdk();
    assert sdk != null;

    addAdditionalOptions(command, module, sdk.getHomePath(),
                         FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions()
                           .getAdditionalOptions());
    addAdditionalOptions(command, module, sdk.getHomePath(),
                         FlexBuildConfigurationManager.getInstance(module).getModuleLevelCompilerOptions().getAdditionalOptions());
    addAdditionalOptions(command, module, sdk.getHomePath(), bc.getCompilerOptions().getAdditionalOptions());

    return command;
  }

  private static void addAdditionalOptions(final List<String> command,
                                           final Module module,
                                           final String sdkHome,
                                           final String additionalOptions) {
    if (!StringUtil.isEmpty(additionalOptions)) {
      // TODO handle -option="path with spaces"
      for (final String s : StringUtil.split(additionalOptions, " ")) {
        command.add(FlexUtils.replacePathMacros(s, module, sdkHome));
      }
    }
  }

  static List<String> getMxmlcCompcCommand(final Project project, final Sdk flexSdk, final boolean isApp) {
    final List<String> command = new ArrayList<String>();

    final String className =
      isApp ? (FlexSdkUtils.isFlex4Sdk(flexSdk) ? "flex2.tools.Mxmlc" : "flex2.tools.Compiler") : "flex2.tools.Compc";

    String additionalClasspath = FileUtil.toSystemDependentName(FlexUtils.getPathToBundledJar("idea-flex-compiler-fix.jar"));
    if (!(flexSdk.getSdkType() instanceof FlexmojosSdkType)) {
      additionalClasspath += File.pathSeparator + FileUtil.toSystemDependentName(flexSdk.getHomePath() + "/lib/compc.jar");
    }

    command.addAll(FlexSdkUtils.getCommandLineForSdkTool(project, flexSdk, additionalClasspath, className, null));

    return command;
  }

  /**
   * returns <code>false</code> if compilation error found in output
   */
  static boolean handleCompilerOutput(final FlexCompilationManager compilationManager,
                                      final FlexCompilationTask task,
                                      final String output) {
    boolean failureDetected = false;
    final StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");

    while (tokenizer.hasMoreElements()) {
      final String text = tokenizer.nextElement();
      if (!StringUtil.isEmptyOrSpaces(text)) {

        final Matcher matcher = FlexCompilerHandler.errorPattern.matcher(text);

        if (matcher.matches()) {
          final String file = matcher.group(1);
          final String additionalInfo = matcher.group(2);
          final String line = matcher.group(3);
          final String column = matcher.group(4);
          final String type = matcher.group(5);
          final String message = matcher.group(6);

          final CompilerMessageCategory messageCategory =
            "Warning".equals(type) ? CompilerMessageCategory.WARNING : CompilerMessageCategory.ERROR;
          final VirtualFile relativeFile = VfsUtil.findRelativeFile(file, null);

          final StringBuilder fullMessage = new StringBuilder();
          if (relativeFile == null) fullMessage.append(file).append(": ");
          if (additionalInfo != null) fullMessage.append(additionalInfo).append(' ');
          fullMessage.append(message);

          compilationManager.addMessage(task, messageCategory, fullMessage.toString(), relativeFile != null ? relativeFile.getUrl() : null,
                                        line != null ? Integer.parseInt(line) : 0, column != null ? Integer.parseInt(column) : 0);
          failureDetected |= messageCategory == CompilerMessageCategory.ERROR;
        }
        else if (text.startsWith("Error: ") || text.startsWith("Exception in thread \"main\" ")) {
          final String updatedText = text.startsWith("Error: ") ? text.substring("Error: ".length()) : text;
          compilationManager.addMessage(task, CompilerMessageCategory.ERROR, updatedText, null, -1, -1);
          failureDetected = true;
        }
        else {
          compilationManager.addMessage(task, CompilerMessageCategory.INFORMATION, text, null, -1, -1);
        }
      }
    }

    return !failureDetected;
  }

  public static void ensureOutputFileWritable(final Project project, final String filePath) {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (file != null && !file.isWritable()) {
      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        public void run() {
          ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(file);
        }
      }, ModalityState.defaultModalityState());
    }
  }

  public static void performPostCompileActions(final Module module,
                                               final @NotNull FlexIdeBuildConfiguration bc) throws FlexCompilerException {
    if (BCUtils.isRuntimeStyleSheetBC(bc)) return;

    switch (bc.getTargetPlatform()) {
      case Web:
        if (bc.isUseHtmlWrapper()) {
          handleHtmlWrapper(bc);
        }
        break;
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
    }
  }

  private static void handleHtmlWrapper(final FlexIdeBuildConfiguration bc) throws FlexCompilerException {
    final VirtualFile templateDir = LocalFileSystem.getInstance().findFileByPath(bc.getWrapperTemplatePath());
    if (templateDir == null || !templateDir.isDirectory()) {
      throw new FlexCompilerException(FlexBundle.message("html.wrapper.dir.not.found", bc.getWrapperTemplatePath()));
    }
    final VirtualFile templateFile = templateDir.findChild(HTML_WRAPPER_TEMPLATE_FILE_NAME);
    if (templateFile == null) {
      throw new FlexCompilerException(FlexBundle.message("no.index.template.html.file", bc.getWrapperTemplatePath()));
    }

    final InfoFromConfigFile info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(bc.getCompilerOptions().getAdditionalConfigFilePath());
    final String outputFolderPath = StringUtil.notNullize(info.getOutputFolderPath(), bc.getOutputFolder());
    final String outputFileName = bc.isTempBCForCompilation()
                                  ? bc.getOutputFileName()
                                  : StringUtil.notNullize(info.getOutputFileName(), bc.getOutputFileName());
    final String targetPlayer = StringUtil.notNullize(info.getTargetPlayer(), bc.getDependencies().getTargetPlayer());

    final VirtualFile outputDir = LocalFileSystem.getInstance().findFileByPath(outputFolderPath);
    if (outputDir == null || !outputDir.isDirectory()) {
      throw new FlexCompilerException(FlexBundle.message("output.folder.does.not.exist", outputFolderPath));
    }

    final Ref<FlexCompilerException> exceptionRef = new Ref<FlexCompilerException>();
    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      public void run() {
        exceptionRef.set(ApplicationManager.getApplication().runWriteAction(new NullableComputable<FlexCompilerException>() {
          public FlexCompilerException compute() {
            for (VirtualFile file : templateDir.getChildren()) {
              if (file == templateFile) {
                final String wrapperText;
                try {
                  wrapperText = VfsUtilCore.loadText(file);
                }
                catch (IOException e) {
                  return new FlexCompilerException(
                    FlexBundle.message("failed.to.load.template.file", file.getPresentableUrl(), e.getMessage()));
                }

                if (!wrapperText.contains(SWF_MACRO)) {
                  return new FlexCompilerException(FlexBundle.message("no.swf.macro", FileUtil.toSystemDependentName(file.getPath())));
                }

                final String fixedText = replaceMacros(wrapperText, FileUtil.getNameWithoutExtension(outputFileName), targetPlayer);
                final String wrapperFileName = BCUtils.getWrapperFileName(bc);
                try {
                  FlexUtils.addFileWithContent(wrapperFileName, fixedText, outputDir);
                }
                catch (IOException e) {
                  return new FlexCompilerException(
                    FlexBundle.message("failed.to.create.file", wrapperFileName, outputDir.getPresentableUrl(), e.getMessage()));
                }
              }
              else {
                try {
                  file.copy(this, outputDir, file.getName());
                }
                catch (IOException e) {
                  return new FlexCompilerException(FlexBundle.message("failed.to.copy.file", file.getName(), templateDir.getPath(),
                                                                      outputDir.getPath(), e.getMessage()));
                }
              }
            }
            return null;
          }
        }));
      }
    }, ModalityState.any());

    if (!exceptionRef.isNull()) {
      throw exceptionRef.get();
    }
  }

  private static String replaceMacros(final String wrapperText, final String outputFileName, final String targetPlayer) {
    final List<String> versionParts = StringUtil.split(targetPlayer, ".");
    final String major = versionParts.size() >= 1 ? versionParts.get(0) : "0";
    final String minor = versionParts.size() >= 2 ? versionParts.get(1) : "0";
    final String revision = versionParts.size() >= 3 ? versionParts.get(2) : "0";
    final String[] replacement = {outputFileName, outputFileName, outputFileName, "#ffffff", "100%", "100%", major, minor, revision};
    return StringUtil.replace(wrapperText, MACROS_TO_REPLACE, replacement);
  }

  private static void handleAirDescriptor(final Module module, final FlexIdeBuildConfiguration bc,
                                          final AirPackagingOptions packagingOptions) throws FlexCompilerException {
    if (packagingOptions.isUseGeneratedDescriptor()) {
      final boolean android = packagingOptions instanceof AndroidPackagingOptions;
      final boolean ios = packagingOptions instanceof IosPackagingOptions;
      generateAirDescriptor(module, bc, BCUtils.getGeneratedAirDescriptorName(bc, packagingOptions), android, ios);
    }
    else {
      copyAndFixCustomAirDescriptor(bc, packagingOptions.getCustomDescriptorPath());
    }
  }

  public static void generateAirDescriptor(final Module module, final FlexIdeBuildConfiguration bc, final String descriptorFileName,
                                           final boolean android, final boolean ios) throws FlexCompilerException {
    final Ref<FlexCompilerException> exceptionRef = new Ref<FlexCompilerException>();

    final Runnable runnable = new Runnable() {
      public void run() {
        final Sdk sdk = bc.getSdk();
        assert sdk != null;

        final String outputFilePath = bc.getActualOutputFilePath();
        final String outputFolderPath = PathUtil.getParentPath(outputFilePath);
        final VirtualFile outputFolder = LocalFileSystem.getInstance().findFileByPath(outputFolderPath);
        if (outputFolder == null) {
          exceptionRef.set(new FlexCompilerException(
            "Failed to generate AIR descriptor. Folder does not exist: " + FileUtil.toSystemDependentName(outputFolderPath)));
          return;
        }

        final String airVersion = FlexSdkUtils.getAirVersion(StringUtil.notNullize(sdk.getVersionString()));
        final String appId = fixApplicationId(bc.getMainClass());
        final String appName = StringUtil.getShortName(bc.getMainClass());
        final String swfName = PathUtil.getFileName(outputFilePath);
        final String[] extensions = getAirExtensionIDs(module, bc.getDependencies());

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              final AirDescriptorOptions descriptorOptions =
                new AirDescriptorOptions(airVersion, appId, appName, swfName, extensions, android, ios);
              final String descriptorText = CreateAirDescriptorTemplateDialog.getAirDescriptorText(descriptorOptions);

              FlexUtils.addFileWithContent(descriptorFileName, descriptorText, outputFolder);
            }
            catch (IOException e) {
              exceptionRef.set(new FlexCompilerException("Failed to generate AIR descriptor: " + e.getMessage()));
            }
          }
        });
      }
    };

    if (ApplicationManager.getApplication().isDispatchThread()) {
      runnable.run();
    }
    else {
      ApplicationManager.getApplication().invokeAndWait(runnable, ModalityState.any());
    }

    if (!exceptionRef.isNull()) {
      throw exceptionRef.get();
    }
  }

  public static Collection<VirtualFile> getANEFiles(final Module module, final Dependencies dependencies) {
    final Collection<VirtualFile> result = new ArrayList<VirtualFile>();

    for (DependencyEntry entry : dependencies.getEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry =
          FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, ModuleRootManager.getInstance(module));
        if (orderEntry != null) {
          for (VirtualFile libFile : orderEntry.getRootFiles(OrderRootType.CLASSES)) {
            addIfANE(result, libFile);
          }
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(module.getProject(), (SharedLibraryEntry)entry);
        if (library != null) {
          for (VirtualFile libFile : library.getFiles((OrderRootType.CLASSES))) {
            addIfANE(result, libFile);
          }
        }
      }
    }
    return result;
  }

  private static void addIfANE(final Collection<VirtualFile> result, final VirtualFile libFile) {
    final VirtualFile realFile = FlexCompilerHandler.getRealFile(libFile);
    if (realFile != null && !realFile.isDirectory() && "ane".equalsIgnoreCase(realFile.getExtension())) {
      result.add(realFile);
    }
  }

  public static String[] getAirExtensionIDs(final Module module, final Dependencies dependencies) {
    final Collection<VirtualFile> aneFiles = getANEFiles(module, dependencies);
    final Collection<String> extensionIDs = new ArrayList<String>();
    for (VirtualFile aneFile : aneFiles) {
      final String extensionId = getExtensionId(aneFile);
      ContainerUtil.addIfNotNull(extensionIDs, extensionId);
    }
    return extensionIDs.toArray(new String[extensionIDs.size()]);
  }

  @Nullable
  private static String getExtensionId(final VirtualFile aneFile) {
    final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(aneFile);
    if (jarRoot == null) return null;
    final VirtualFile xmlFile = VfsUtil.findRelativeFile("META-INF/ANE/extension.xml", jarRoot);
    if (xmlFile == null) return null;
    try {
      return FlexUtils.findXMLElement(xmlFile.getInputStream(), "<extension><id>");
    }
    catch (IOException e) {
      return null;
    }
  }

  public static void unzipANEFiles(final Collection<VirtualFile> aneFiles, final ProgressIndicator indicator) {
    final File baseDir = new File(getPathToUnzipANE());
    if (!baseDir.exists()) {
      if (!baseDir.mkdir()) {
        Logger.getLogger(FlexCompilationUtils.class.getName()).warning("Failed to create " + baseDir.getPath());
        return;
      }
    }

    for (VirtualFile file : aneFiles) {
      if (indicator != null && indicator.isCanceled()) return;

      final File subDir = new File(baseDir, file.getName());
      if (!subDir.exists()) {
        if (!subDir.mkdir()) {
          Logger.getLogger(FlexCompilationUtils.class.getName()).warning("Failed to create " + baseDir.getPath());
          continue;
        }
      }

      try {
        ZipUtil.extract(new File(file.getPath()), subDir, null, true);
      }
      catch (IOException e) {
        Logger.getLogger(FlexCompilationUtils.class.getName()).warning("Failed to unzip " + file.getPath() + " to " + baseDir.getPath());
      }
    }
  }

  public static void deleteUnzippedANEFiles() {
    FileUtil.delete(new File(getPathToUnzipANE()));
  }

  public static String getPathToUnzipANE() {
    return FileUtil.getTempDirectory() + File.separator + "IntelliJ_ANE_unzipped";
  }

  public static String fixApplicationId(final String appId) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < appId.length(); i++) {
      final char ch = appId.charAt(i);
      if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '-' || ch == '.') {
        builder.append(ch);
      }
    }
    return builder.toString();
  }

  private static void copyAndFixCustomAirDescriptor(final FlexIdeBuildConfiguration bc,
                                                    final String customDescriptorPath) throws FlexCompilerException {
    final VirtualFile descriptorTemplateFile = LocalFileSystem.getInstance().findFileByPath(customDescriptorPath);
    if (descriptorTemplateFile == null) {
      throw new FlexCompilerException("Custom AIR descriptor file not found: " + customDescriptorPath);
    }

    final String outputFilePath = bc.getActualOutputFilePath();
    final String outputFolderPath = PathUtil.getParentPath(outputFilePath);
    final VirtualFile outputFolder = LocalFileSystem.getInstance().findFileByPath(outputFolderPath);
    if (outputFolder == null) {
      throw new FlexCompilerException("Failed to copy AIR descriptor. Folder does not exist: " + outputFolderPath);
    }

    final Ref<FlexCompilerException> exceptionRef = new Ref<FlexCompilerException>();

    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              final String content = fixInitialContent(descriptorTemplateFile, PathUtil.getFileName(outputFilePath));
              FlexUtils.addFileWithContent(descriptorTemplateFile.getName(), content, outputFolder);
            }
            catch (FlexCompilerException e) {
              exceptionRef.set(e);
            }
            catch (IOException e) {
              exceptionRef.set(new FlexCompilerException("Failed to copy AIR descriptor to output folder", null, -1, -1));
            }
          }
        });
      }
    }, ModalityState.any());

    if (!exceptionRef.isNull()) {
      throw exceptionRef.get();
    }
  }

  private static String fixInitialContent(final VirtualFile descriptorFile, final String swfName) throws FlexCompilerException {
    try {
      final Document document;
      try {
        document = JDOMUtil.loadDocument(descriptorFile.getInputStream());
      }
      catch (IOException e) {
        throw new FlexCompilerException("Failed to read AIR descriptor content: " + e.getMessage(), descriptorFile.getUrl(), -1, -1);
      }

      final Element rootElement = document.getRootElement();
      if (rootElement == null || !"application".equals(rootElement.getName())) {
        throw new FlexCompilerException("AIR descriptor file has incorrect root tag", descriptorFile.getUrl(), -1, -1);
      }

      Element initialWindowElement = rootElement.getChild("initialWindow", rootElement.getNamespace());
      if (initialWindowElement == null) {
        initialWindowElement = new Element("initialWindow", rootElement.getNamespace());
        rootElement.addContent(initialWindowElement);
      }

      Element contentElement = initialWindowElement.getChild("content", rootElement.getNamespace());
      if (contentElement == null) {
        contentElement = new Element("content", rootElement.getNamespace());
        initialWindowElement.addContent(contentElement);
      }

      contentElement.setText(swfName);

      return JDOMUtil.writeDocument(document, SystemProperties.getLineSeparator());
    }
    catch (JDOMException e) {
      throw new FlexCompilerException("AIR descriptor file has incorrect format: " + e.getMessage(), descriptorFile.getUrl(), -1, -1);
    }
  }
}
