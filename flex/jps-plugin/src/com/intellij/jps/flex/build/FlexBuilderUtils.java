package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.AirDescriptorOptions;
import com.intellij.flex.model.bc.*;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtilRt;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.library.JpsLibrary;
import org.jetbrains.jps.model.library.JpsOrderRootType;
import org.jetbrains.jps.model.library.sdk.JpsSdk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FlexBuilderUtils {

  public static String getCompilerName(final JpsFlexBuildConfiguration bc) {
    String postfix = bc.isTempBCForCompilation() ? " - " + FlexCommonUtils.getBCSpecifier(bc) : "";
    if (!bc.getName().equals(bc.getModule().getName())) postfix += " (module " + bc.getModule().getName() + ")";
    return "[" + bc.getName() + postfix + "]";
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
      case Desktop:
        handleAirDescriptor(context, outputConsumer, dirtyFilePaths, bc, bc.getAirDesktopPackagingOptions());
        break;
      case Mobile:
        if (bc.getAndroidPackagingOptions().isEnabled()) {
          handleAirDescriptor(context, outputConsumer, dirtyFilePaths, bc, bc.getAndroidPackagingOptions());
        }
        if (bc.getIosPackagingOptions().isEnabled()) {
          handleAirDescriptor(context, outputConsumer, dirtyFilePaths, bc, bc.getIosPackagingOptions());
        }
        break;
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
          final File outputFile = new File(outputDir, wrapperFileName);
          FileUtil.writeToFile(outputFile, fixedText);
          outputConsumer.registerOutputFile(outputFile.getPath(), Collections.singletonList(file.getPath()));
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
          final String value = StringUtil.stripQuotesAroundValue(attribute.substring(eqIndex + 1).trim());
          result.put(name, value);
        }
      }
    }

    return result;
  }

  private static void handleAirDescriptor(final CompileContext context,
                                          final BuildOutputConsumer outputConsumer,
                                          final Collection<String> dirtyFilePaths,
                                          final JpsFlexBuildConfiguration bc,
                                          final JpsAirPackagingOptions packagingOptions) {
    if (packagingOptions.isUseGeneratedDescriptor()) {
      final boolean android = packagingOptions instanceof JpsAndroidPackagingOptions;
      final boolean ios = packagingOptions instanceof JpsIosPackagingOptions;
      final String descriptorFileName = FlexCommonUtils.getGeneratedAirDescriptorName(bc, packagingOptions);
      generateAirDescriptor(context, outputConsumer, dirtyFilePaths, bc, descriptorFileName, android, ios);
    }
    else {
      copyAndFixCustomAirDescriptor(context, outputConsumer, bc, packagingOptions.getCustomDescriptorPath());
    }
  }

  private static void generateAirDescriptor(final CompileContext context,
                                            final BuildOutputConsumer outputConsumer,
                                            final Collection<String> dirtyFilePaths,
                                            final JpsFlexBuildConfiguration bc,
                                            final String descriptorFileName,
                                            final boolean android,
                                            final boolean ios) {

    final JpsSdk<?> sdk = bc.getSdk();
    assert sdk != null;

    final String outputFilePath = bc.getActualOutputFilePath();
    final String outputFolderPath = PathUtilRt.getParentPath(outputFilePath);
    final File outputFolder = new File(outputFolderPath);
    if (!outputFolder.isDirectory()) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle
        .message("output.folder.does.not.exist", outputFolder.getPath())));
      return;
    }

    final String airVersion = FlexCommonUtils.getAirVersion(StringUtil.notNullize(sdk.getVersionString()));
    final String appId = FlexCommonUtils.fixApplicationId(bc.getMainClass());
    final String appName = StringUtil.getShortName(bc.getMainClass());
    final String swfName = PathUtilRt.getFileName(outputFilePath);
    final String[] extensions = getAirExtensionIDs(bc);

    try {
      final AirDescriptorOptions descriptorOptions =
        new AirDescriptorOptions(airVersion, appId, appName, swfName, extensions, android, ios);
      final String descriptorText = descriptorOptions.getAirDescriptorText();

      final File outputFile = new File(outputFolder, descriptorFileName);
      FileUtil.writeToFile(outputFile, descriptorText);
      outputConsumer.registerOutputFile(outputFile.getPath(), dirtyFilePaths);
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR,
                                                 FlexCommonBundle.message("failed.to.generate.air.descriptor", e.getMessage())));
    }
  }

  private static Collection<File> getANEFiles(final JpsFlexBuildConfiguration bc) {
    final Collection<File> result = new ArrayList<File>();

    for (JpsFlexDependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof JpsLibraryDependencyEntry) {
        final JpsLibrary library = ((JpsLibraryDependencyEntry)entry).getLibrary();
        if (library != null) {
          for (File libFile : library.getFiles(JpsOrderRootType.COMPILED)) {
            if (libFile.isFile() && "ane".equals(FileUtil.getExtension(libFile.getName()))) {
              result.add(libFile);
            }
          }
        }
      }
    }
    return result;
  }

  private static String[] getAirExtensionIDs(final JpsFlexBuildConfiguration bc) {
    final Collection<String> result = new ArrayList<String>();

    for (File aneFile : getANEFiles(bc)) {
      final String extensionId = getExtensionId(aneFile);
      ContainerUtil.addIfNotNull(result, extensionId);
    }

    return result.toArray(new String[result.size()]);
  }

  @Nullable
  private static String getExtensionId(final File aneFile) {
    try {
      final ZipFile zipFile = new ZipFile((aneFile));
      try {
        final ZipEntry entry = zipFile.getEntry("META-INF/ANE/extension.xml");
        if (entry != null) {
          final InputStream is = zipFile.getInputStream(entry);
          return FlexCommonUtils.findXMLElement(is, "<extension><id>");
        }
      }
      finally {
        zipFile.close();
      }
    }
    catch (IOException e) {/**/}
    return null;
  }

  private static void copyAndFixCustomAirDescriptor(final CompileContext context,
                                                    final BuildOutputConsumer outputConsumer,
                                                    final JpsFlexBuildConfiguration bc,
                                                    final String customDescriptorPath) {
    final File descriptorTemplateFile = new File(customDescriptorPath);
    if (!descriptorTemplateFile.isFile()) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR,
                                                 FlexCommonBundle.message("air.descriptor.not.found", customDescriptorPath)));
      return;
    }

    final String outputFilePath = bc.getActualOutputFilePath();
    final String outputFolderPath = PathUtilRt.getParentPath(outputFilePath);
    final File outputFolder = new File(outputFolderPath);
    if (!outputFolder.isDirectory()) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle
        .message("output.folder.does.not.exist", outputFolder.getPath())));
      return;
    }


    final String content;

    try {
      content = fixInitialContent(descriptorTemplateFile, PathUtilRt.getFileName(outputFilePath));
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle.message(
        "failed.to.open.air.descriptor", descriptorTemplateFile.getPath(), e.getMessage())));
      return;
    }
    catch (JDOMException e) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR, FlexCommonBundle.message(
        "incorrect.air.descriptor.content", descriptorTemplateFile.getPath(), e.getMessage())));
      return;
    }


    try {
      final File outputFile = new File(outputFolder, descriptorTemplateFile.getName());
      FileUtil.writeToFile(outputFile, content);
      outputConsumer.registerOutputFile(outputFile.getPath(), Collections.singletonList(descriptorTemplateFile.getPath()));
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(getCompilerName(bc), BuildMessage.Kind.ERROR,
                                                 FlexCommonBundle.message("failed.to.copy.air.descriptor", e.getMessage())));
    }
  }

  private static String fixInitialContent(final File descriptorFile, final String swfName) throws IOException, JDOMException {
    final Document document = JDOMUtil.loadDocument(descriptorFile);
    final Element rootElement = document.getRootElement();
    if (rootElement == null || !"application".equals(rootElement.getName())) {
      throw new JDOMException("incorrect root tag");
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
}
