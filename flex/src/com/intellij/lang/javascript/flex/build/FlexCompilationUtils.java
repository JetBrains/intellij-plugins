package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateAirDescriptorTemplateDialog;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
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
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PathUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.ZipUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
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
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static com.intellij.lang.javascript.flex.projectStructure.ui.CreateAirDescriptorTemplateDialog.AirDescriptorOptions;

public class FlexCompilationUtils {

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
                                   final FlexBuildConfiguration bc) {
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
    final String mainClass = isApp
                             ? (FlexSdkUtils.isFlex4Sdk(flexSdk) ? "flex2.tools.Mxmlc" : "flex2.tools.Compiler")
                             : "flex2.tools.Compc";

    String additionalClasspath = FileUtil.toSystemDependentName(FlexCommonUtils.getPathToBundledJar("idea-flex-compiler-fix.jar"));

    if (!(flexSdk.getSdkType() instanceof FlexmojosSdkType)) {
      additionalClasspath += File.pathSeparator + FileUtil.toSystemDependentName(flexSdk.getHomePath() + "/lib/compc.jar");
    }

    return FlexSdkUtils.getCommandLineForSdkTool(project, flexSdk, additionalClasspath, mainClass, null);
  }

  static List<String> getASC20Command(final Project project, final Sdk flexSdk, final boolean isApp) {
    final String mainClass = isApp ? "com.adobe.flash.compiler.clients.MXMLC" : "com.adobe.flash.compiler.clients.COMPC";

    final String additionalClasspath = flexSdk.getSdkType() instanceof FlexmojosSdkType
                                       ? null
                                       : FileUtil.toSystemDependentName(flexSdk.getHomePath() + "/lib/compiler.jar");

    return FlexSdkUtils.getCommandLineForSdkTool(project, flexSdk, additionalClasspath, mainClass, null);
  }

  /**
   * returns <code>false</code> if compilation error found in output
   */
  static boolean handleCompilerOutput(final FlexCompilationManager compilationManager,
                                      final FlexCompilationTask task,
                                      final String output) {
    boolean failureDetected = false;

    final List<String> lines = StringUtil.split(StringUtil.replace(output, "\r", "\n"), "\n");

    for (int i = 0; i < lines.size(); i++) {
      final String text = lines.get(i);

      if (StringUtil.isEmptyOrSpaces(text) || "^".equals(text.trim())) {
        continue;
      }

      final String nextLine = i + 1 < lines.size() ? lines.get(i + 1) : null;
      if (nextLine != null && nextLine.trim().equals("^")) {
        // do not print line of code with error/warning
        continue;
      }

      final Matcher matcher = FlexCommonUtils.ERROR_PATTERN.matcher(text);

      if (matcher.matches()) {
        final String filePath = matcher.group(1);
        final String additionalInfo = matcher.group(2);
        final String line = matcher.group(3);
        final String column = matcher.group(4);
        final String type = matcher.group(5);
        final String message = matcher.group(6);

        final CompilerMessageCategory messageCategory = "Warning".equals(type) ? CompilerMessageCategory.WARNING
                                                                               : CompilerMessageCategory.ERROR;
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

        final StringBuilder fullMessage = new StringBuilder();
        if (file == null) fullMessage.append(filePath).append(": ");
        if (additionalInfo != null) fullMessage.append(additionalInfo).append(' ');
        fullMessage.append(message);

        compilationManager.addMessage(task, messageCategory, fullMessage.toString(), file != null ? file.getUrl() : null,
                                      line != null ? Integer.parseInt(line) : 0, column != null ? Integer.parseInt(column) : 0);
        failureDetected |= messageCategory == CompilerMessageCategory.ERROR;
      }
      else if (text.startsWith("Error: ") || text.startsWith("Exception in thread \"")) {
        final String updatedText = text.startsWith("Error: ") ? text.substring("Error: ".length()) : text;
        compilationManager.addMessage(task, CompilerMessageCategory.ERROR, updatedText, null, -1, -1);
        failureDetected = true;
      }
      else {
        compilationManager.addMessage(task, CompilerMessageCategory.INFORMATION, text, null, -1, -1);
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
                                               final @NotNull FlexBuildConfiguration bc,
                                               final List<String> compileInfoMessages) throws FlexCompilerException {
    // could be created by external build
    FlexCompilationManager.refreshAndFindFileInWriteAction(bc.getActualOutputFilePath());

    final Sdk sdk = bc.getSdk();
    assert sdk != null;

    LinkageType linkage = bc.getDependencies().getFrameworkLinkage();
    if (linkage == LinkageType.Default) {
      linkage = FlexCommonUtils.getDefaultFrameworkLinkage(sdk.getVersionString(), bc.getNature());
    }

    if (linkage == LinkageType.RSL) {
      handleFrameworkRsls(bc, compileInfoMessages);
    }

    if (bc.getOutputType() != OutputType.Application || BCUtils.isRLMTemporaryBC(bc) || BCUtils.isRuntimeStyleSheetBC(bc)) return;

    switch (bc.getTargetPlatform()) {
      case Web:
        if (bc.isUseHtmlWrapper()) {
          handleHtmlWrapper(module, bc);
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

  private static void handleFrameworkRsls(final FlexBuildConfiguration bc,
                                          final List<String> compileInfoMessages) throws FlexCompilerException {
    final Sdk sdk = bc.getSdk();
    assert sdk != null;

    if (StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.8") >= 0) {
      final List<String> rsls = getRequiredRsls(compileInfoMessages);
      final Collection<File> filesToRefresh = new THashSet<File>();

      final String rslBaseDir = sdk.getHomePath() + "/frameworks/rsls/";
      final String outputPath = PathUtil.getParentPath(bc.getActualOutputFilePath());

      for (String rsl : rsls) {
        final File file = new File(rslBaseDir + rsl);
        if (file.isFile()) {
          try {
            final File toFile = new File(outputPath + '/' + rsl);
            FileUtil.copy(file, toFile);
            filesToRefresh.add(toFile);
          }
          catch (IOException e) {
            throw new FlexCompilerException(FlexCommonBundle.message("failed.to.copy.file", rsl, rslBaseDir, outputPath, e.getMessage()));
          }
        }
      }

      CompilerUtil.refreshIOFiles(filesToRefresh);
    }
  }

  private static List<String> getRequiredRsls(final List<String> compileInfoMessages) {
    final List<String> rsls = new ArrayList<String>();

    boolean rslListStarted = false;

    for (String message : compileInfoMessages) {
      if (rslListStarted) {
        // see tools_en.properties from Flex SDK sources
        if (message.startsWith("\u0020\u0020\u0020\u0020") && message.length() > 4 && !Character.isWhitespace(message.charAt(5))) {
          final String text = message.substring(4);
          final int nextSpaceIndex = text.indexOf(' ');
          rsls.add(nextSpaceIndex == -1 ? text : text.substring(0, nextSpaceIndex));
        }
        else {
          break;
        }
      }
      else if ("Required RSLs:".equals(message)) {
        rslListStarted = true;
      }
    }
    return rsls;
  }

  private static void handleHtmlWrapper(final Module module, final FlexBuildConfiguration bc) throws FlexCompilerException {
    final VirtualFile templateDir = LocalFileSystem.getInstance().findFileByPath(bc.getWrapperTemplatePath());
    if (templateDir == null || !templateDir.isDirectory()) {
      throw new FlexCompilerException(FlexCommonBundle.message("html.wrapper.dir.not.found", bc.getWrapperTemplatePath()));
    }
    final VirtualFile templateFile = templateDir.findChild(FlexCommonUtils.HTML_WRAPPER_TEMPLATE_FILE_NAME);
    if (templateFile == null) {
      throw new FlexCompilerException(FlexCommonBundle.message("no.index.template.html.file", bc.getWrapperTemplatePath()));
    }

    final InfoFromConfigFile info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(bc.getCompilerOptions().getAdditionalConfigFilePath());
    final String outputFolderPath = StringUtil.notNullize(info.getOutputFolderPath(), bc.getOutputFolder());
    final String outputFileName = bc.isTempBCForCompilation()
                                  ? bc.getOutputFileName()
                                  : StringUtil.notNullize(info.getOutputFileName(), bc.getOutputFileName());
    final String targetPlayer = StringUtil.notNullize(info.getTargetPlayer(), bc.getDependencies().getTargetPlayer());

    final VirtualFile outputDir = LocalFileSystem.getInstance().findFileByPath(outputFolderPath);
    if (outputDir == null || !outputDir.isDirectory()) {
      throw new FlexCompilerException(FlexCommonBundle.message("output.folder.does.not.exist", outputFolderPath));
    }

    final Ref<FlexCompilerException> exceptionRef = new Ref<FlexCompilerException>();
    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      public void run() {
        exceptionRef.set(ApplicationManager.getApplication().runWriteAction(new NullableComputable<FlexCompilerException>() {
          public FlexCompilerException compute() {
            for (VirtualFile file : templateDir.getChildren()) {
              if (Comparing.equal(file, templateFile)) {
                final String wrapperText;
                try {
                  wrapperText = VfsUtilCore.loadText(file);
                }
                catch (IOException e) {
                  return new FlexCompilerException(
                    FlexCommonBundle.message("failed.to.load.template.file", file.getPresentableUrl(), e.getMessage()));
                }

                if (!wrapperText.contains(FlexCommonUtils.SWF_MACRO)) {
                  return new FlexCompilerException(
                    FlexCommonBundle.message("no.swf.macro", FileUtil.toSystemDependentName(file.getPath())));
                }

                final String mainClass = StringUtil.notNullize(info.getMainClass(module), bc.getMainClass());
                final PsiElement jsClass = JSResolveUtil.findClassByQName(mainClass, module.getModuleScope());

                final String fixedText = replaceMacros(wrapperText, FileUtil.getNameWithoutExtension(outputFileName), targetPlayer,
                                                       jsClass instanceof JSClass ? (JSClass)jsClass : null);
                final String wrapperFileName = BCUtils.getWrapperFileName(bc);
                try {
                  FlexUtils.addFileWithContent(wrapperFileName, fixedText, outputDir);
                }
                catch (IOException e) {
                  return new FlexCompilerException(
                    FlexCommonBundle.message("failed.to.create.file.in", wrapperFileName, outputDir.getPresentableUrl(), e.getMessage()));
                }
              }
              else {
                try {
                  file.copy(this, outputDir, file.getName());
                }
                catch (IOException e) {
                  return new FlexCompilerException(FlexCommonBundle.message("failed.to.copy.file", file.getName(), templateDir.getPath(),
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

  private static String replaceMacros(final String wrapperText, final String outputFileName, final String targetPlayer,
                                      final @Nullable JSClass mainClass) {
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

    final Ref<JSAttribute> swfMetadataRef = new Ref<JSAttribute>();

    final PsiFile psiFile = mainClass == null ? null : mainClass.getContainingFile();

    if (psiFile instanceof XmlFile) {
      final XmlTag rootTag = ((XmlFile)psiFile).getRootTag();
      if (rootTag != null) {
        final String ns = rootTag.getPrefixByNamespace(JavaScriptSupportLoader.MXML_URI3) == null
                          ? JavaScriptSupportLoader.MXML_URI
                          : JavaScriptSupportLoader.MXML_URI3;
        for (XmlTag tag : rootTag.findSubTags(FlexPredefinedTagNames.METADATA, ns)) {
          JSResolveUtil.processInjectedFileForTag(tag, new JSResolveUtil.JSInjectedFilesVisitor() {
            protected void process(final JSFile file) {
              for (PsiElement elt : file.getChildren()) {
                if (elt instanceof JSAttributeList) {
                  final JSAttribute swfMetadata = ((JSAttributeList)elt).findAttributeByName("SWF");
                  if (swfMetadataRef.isNull() && swfMetadata != null) {
                    swfMetadataRef.set(swfMetadata);
                    return;
                  }
                }
              }
            }
          });
        }
      }
    }
    else {
      final JSAttributeList attributeList = mainClass == null ? null : mainClass.getAttributeList();
      swfMetadataRef.set(attributeList == null ? null : attributeList.findAttributeByName("SWF"));
    }


    if (!swfMetadataRef.isNull()) {
      final JSAttribute swfMetadata = swfMetadataRef.get();

      final JSAttributeNameValuePair titleAttr = swfMetadata.getValueByName(FlexCommonUtils.TITLE_ATTR);
      ContainerUtil.putIfNotNull(FlexCommonUtils.TITLE_MACRO, titleAttr == null ? null : titleAttr.getSimpleValue(), replacementMap);

      final JSAttributeNameValuePair bgColorAttr = swfMetadata.getValueByName(FlexCommonUtils.BG_COLOR_ATTR);
      ContainerUtil.putIfNotNull(FlexCommonUtils.BG_COLOR_MACRO, bgColorAttr == null ? null : bgColorAttr.getSimpleValue(), replacementMap);

      final JSAttributeNameValuePair widthAttr = swfMetadata.getValueByName(FlexCommonUtils.WIDTH_ATTR);
      ContainerUtil.putIfNotNull(FlexCommonUtils.WIDTH_MACRO, widthAttr == null ? null : widthAttr.getSimpleValue(), replacementMap);

      final JSAttributeNameValuePair heightAttr = swfMetadata.getValueByName(FlexCommonUtils.HEIGHT_ATTR);
      ContainerUtil.putIfNotNull(FlexCommonUtils.HEIGHT_MACRO, heightAttr == null ? null : heightAttr.getSimpleValue(), replacementMap);
    }

    return FlexCommonUtils.replace(wrapperText, replacementMap);
  }

  private static void handleAirDescriptor(final Module module, final FlexBuildConfiguration bc,
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

  public static void generateAirDescriptor(final Module module, final FlexBuildConfiguration bc, final String descriptorFileName,
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
        final String[] extensions = getAirExtensionIDs(ModuleRootManager.getInstance(module), bc.getDependencies());

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

  public static Collection<VirtualFile> getANEFiles(final ModuleRootModel moduleRootModel, final Dependencies dependencies) {
    final Collection<VirtualFile> result = new ArrayList<VirtualFile>();

    for (DependencyEntry entry : dependencies.getEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry =
          FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, moduleRootModel);
        if (orderEntry != null) {
          for (VirtualFile libFile : orderEntry.getRootFiles(OrderRootType.CLASSES)) {
            addIfANE(result, libFile);
          }
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(moduleRootModel.getModule().getProject(), (SharedLibraryEntry)entry);
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

  public static String[] getAirExtensionIDs(final ModuleRootModel moduleRootModel, final Dependencies dependencies) {
    final Collection<VirtualFile> aneFiles = getANEFiles(moduleRootModel, dependencies);
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

  private static void copyAndFixCustomAirDescriptor(final FlexBuildConfiguration bc,
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
