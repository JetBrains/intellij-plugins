// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.mxml.MxmlJSClassProvider;
import com.intellij.lang.javascript.flex.projectStructure.FlexOrderEnumerationHandler;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.Processor;
import com.intellij.util.SystemProperties;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.xml.NanoXmlBuilder;
import com.intellij.util.xml.NanoXmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.serialization.PathMacroUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Maxim.Mossienko
 */
public final class FlexUtils {

  private FlexUtils() {
  }

  public static FileChooserDescriptor createFileChooserDescriptor(final String @Nullable ... allowedExtensions) {
    return allowedExtensions == null
           ? new FileChooserDescriptor(true, false, true, true, false, false)
           : new FileChooserDescriptor(true, false, true, true, false, false) {
             @Override
             public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
               return super.isFileVisible(file, showHiddenFiles) &&
                      (file.isDirectory() || isAllowedExtension(file.getExtension()));
             }

             private boolean isAllowedExtension(final String extension) {
               for (String allowedExtension : allowedExtensions) {
                 if (allowedExtension.equalsIgnoreCase(extension)) return true;
               }
               return false;
             }
           };
  }

  static void createSampleApp(final Project project,
                              final VirtualFile sourceRoot,
                              final String sampleFileName,
                              final TargetPlatform platform,
                              final boolean isFlex4) throws IOException {
    final String sampleClassName = FileUtilRt.getNameWithoutExtension(sampleFileName);
    final String extension = FileUtilRt.getExtension(sampleFileName);
    final String sampleTechnology = platform == TargetPlatform.Mobile ? "AIRMobile" : platform == TargetPlatform.Desktop ? "AIR" : "Flex";

    String suffix = "";
    if ("mxml".equalsIgnoreCase(extension)) {
      if (platform == TargetPlatform.Mobile) {
        suffix = "_ViewNavigator";
      }
      else if (isFlex4) {
        suffix = "_Spark";
      }
    }

    final String helloWorldTemplate = "HelloWorld_" + sampleTechnology + suffix + "." + extension + ".ft";
    final InputStream stream = FlexUtils.class.getResourceAsStream(helloWorldTemplate);
    assert stream != null : helloWorldTemplate;
    final String sampleFileContent = FileUtil.loadTextAndClose(new InputStreamReader(stream,
                                                                                     StandardCharsets.UTF_8)).replace("${class.name}", sampleClassName);
    VirtualFile sampleApplicationFile = addFileWithContent(sampleFileName, sampleFileContent, sourceRoot);
    Runnable runnable = () -> FileEditorManager.getInstance(project).openFile(sampleApplicationFile, true);
    if (project.isInitialized()) {
      runnable.run();
    }
    else {
      StartupManager.getInstance(project).runAfterOpened(() -> {
        ApplicationManager.getApplication().invokeLater(runnable);
      });
    }
  }

  public static VirtualFile addFileWithContent(@NonNls final String fileName, byte[] fileContent, final VirtualFile dir)
    throws IOException {
    VirtualFile file = dir.findChild(fileName);
    if (file == null) {
      file = dir.createChildData(FlexUtils.class, fileName);
    }
    else if (SystemInfo.isWindows) {
      file.rename(FlexUtils.class, fileName); // ensure the right case
    }
    file.setBinaryContent(fileContent);
    return file;
  }
  public static @NotNull VirtualFile addFileWithContent(@NonNls final String fileName, @NonNls final String fileContent, final VirtualFile dir)
    throws IOException {
    VirtualFile data = dir.findChild(fileName);
    if (data == null) {
      data = dir.createChildData(FlexUtils.class, fileName);
    }
    else if (SystemInfo.isWindows) {
      data.rename(FlexUtils.class, fileName); // ensure the right case
    }
    VfsUtil.saveText(data, fileContent);
    return data;
  }

  @Nullable
  public static Sdk getSdkForActiveBC(@NotNull final Module module) {
    return ModuleType.get(module) instanceof FlexModuleType
           ? FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getSdk()
           : null;
  }

  /**
   * Looks through input stream containing XML document and finds all entries of XML elements listed in {@code xmlElements}.
   * Content of these elements is put to result map. XML namespaces are not taken into consideration.
   *
   * @param xmlInputStream input stream with xml content to parse
   * @param xmlElements    list of XML elements to look for.
   *                       Format is: {@code "<root_element><child_element><subelement_to_look_for>"}.
   *                       Listed XML elements SHOULD NOT contain subelements
   * @return map, keys are XML elements listed in {@code xmlElements},
   *         values are all entries of respective element (may be empty list)
   */
  public static Map<String, List<String>> findXMLElements(@NotNull final InputStream xmlInputStream, final List<String> xmlElements) {
    final Map<String, List<String>> resultMap = new HashMap<>();
    for (final String element : xmlElements) {
      resultMap.put(element, new ArrayList<>());
    }

    NanoXmlUtil.parse(xmlInputStream, new NanoXmlBuilder() {
      private String currentElement = "";
      private final StringBuilder currentElementContent = new StringBuilder();

      @Override
      public void startElement(final String name, final String nsPrefix, final String nsURI, final String systemID, final int lineNr) {
        currentElement += "<" + name + ">";
      }

      @Override
      public void endElement(final String name, final String nsPrefix, final String nsURI) {
        if (xmlElements.contains(currentElement)) {
          resultMap.get(currentElement).add(currentElementContent.toString());
          currentElementContent.delete(0, currentElementContent.length());
        }
        assert currentElement.endsWith("<" + name + ">");
        currentElement = currentElement.substring(0, currentElement.length() - (name.length() + 2));
      }

      @Override
      public void addPCData(final Reader reader, final String systemID, final int lineNr) throws Exception {
        if (xmlElements.contains(currentElement)) {
          char[] chars = new char[128];
          int read;
          while ((read = reader.read(chars)) > 0) {
            currentElementContent.append(chars, 0, read);
          }
        }
      }
    });

    return resultMap;
  }

  /**
   * Looks through input stream containing XML document and finds first entry of {@code xmlElement}.
   * XML namespaces are not taken into consideration.
   *
   * @param xmlInputStream input stream with xml content to parse
   * @param xmlElement     XML element to look for.
   *                       Format is: {@code "<root_element><child_element><subelement_to_look_for>"}.
   *                       XML element SHOULD NOT contain subelements
   * @return first found value of {@code xmlElement} tag, or {@code null} if non found or any exception occurs.
   */

  @Nullable
  public static String findXMLElement(@NotNull final InputStream xmlInputStream, final String xmlElement) {
    final Ref<String> result = new Ref<>();

    NanoXmlUtil.parse(xmlInputStream, new NanoXmlBuilder() {
      private String currentElement = "";
      private final StringBuilder xmlElementContent = new StringBuilder();

      @Override
      public void startElement(final String name, final String nsPrefix, final String nsURI, final String systemID, final int lineNr) {
        currentElement += "<" + name + ">";
      }

      @Override
      public void endElement(final String name, final String nsPrefix, final String nsURI) throws Exception {
        if (xmlElement.equals(currentElement)) {
          result.set(xmlElementContent.toString());
          NanoXmlBuilder.stop();
        }
        assert currentElement.endsWith("<" + name + ">");
        currentElement = currentElement.substring(0, currentElement.length() - (name.length() + 2));
      }

      @Override
      public void addPCData(final Reader reader, final String systemID, final int lineNr) throws Exception {
        if (xmlElement.equals(currentElement)) {
          char[] chars = new char[128];
          int read;
          while ((read = reader.read(chars)) > 0) {
            xmlElementContent.append(chars, 0, read);
          }
        }
      }
    });

    return result.get();
  }

  public static String getFlexCompilerWorkDirPath(final Project project, @Nullable final Sdk flexSdk) {
    final VirtualFile baseDir = project.getBaseDir();
    return FlexSdkUtils.isFlex2Sdk(flexSdk) || FlexSdkUtils.isFlex3_0Sdk(flexSdk)
           ? FlexCommonUtils.getTempFlexConfigsDirPath() //avoid problems with spaces in temp dir path (fcsh from Flex SDK 2 is not patched)
           : baseDir == null ? "" : baseDir.getPath();
  }

  public static String getPathToMainClassFile(final String mainClassFqn, final Module module) {
    if (StringUtil.isEmpty(mainClassFqn)) return "";

    final String s = mainClassFqn.replace('.', '/');
    final String[] classFileRelPaths = {s + ".mxml", s + ".as"};

    for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
      for (final String classFileRelPath : classFileRelPaths) {
        final VirtualFile mainClassFile = VfsUtilCore.findRelativeFile(classFileRelPath, sourceRoot);
        if (mainClassFile != null) {
          return mainClassFile.getPath();
        }
      }
    }

    return "";
  }

  public static void removeFileLater(@NotNull final VirtualFile file) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            try {
              if (file.exists()) {
                file.delete(this);
              }
            }
            catch (IOException ignored) {/*ignore*/}
          }
        });
      }
    });
  }

  private static String findMxmlNamespace(XmlTag rootTag) {
    String namespace = "";

    for(String candidateNs: MxmlJSClass.MXML_URIS) {
      if (rootTag.getPrefixByNamespace(candidateNs) != null) {
        namespace = candidateNs;
      }

      if (!namespace.isEmpty()) break;
    }
    return namespace;
  }

  public static void processMxmlTags(final XmlTag rootTag, boolean isPhysical,
                                     final JSResolveUtil.JSInjectedFilesVisitor injectedFilesVisitor) {
    Processor<XmlTag> processor =
      new XmlBackedJSClassImpl.InjectedScriptsVisitor.InjectingProcessor(injectedFilesVisitor, rootTag, isPhysical);
    String namespace = findMxmlNamespace(rootTag);

    XmlBackedJSClassImpl.InjectedScriptsVisitor scriptsVisitor =
      new XmlBackedJSClassImpl.InjectedScriptsVisitor(rootTag, MxmlJSClassProvider.getInstance(), false, false, injectedFilesVisitor,
                                                      processor, isPhysical);
    scriptsVisitor.go();

    for (XmlTag s : rootTag.findSubTags(FlexPredefinedTagNames.METADATA, namespace)) {
      processor.process(s);
    }
  }

  public static void processMetaAttributesForClass(@NotNull PsiElement jsClass, @NotNull final ActionScriptResolveUtil.MetaDataProcessor processor) {
    ActionScriptResolveUtil.processMetaAttributesForClass(jsClass, processor);
    if (jsClass instanceof XmlBackedJSClassImpl) {
      PsiElement parent = jsClass.getParent();
      if (parent != null) {
        PsiFile file = parent.getContainingFile();
        if (file instanceof XmlFile) {
          XmlDocument document = ((XmlFile)file).getDocument();
          if (document != null) {
            XmlTag rootTag = document.getRootTag();
            if (rootTag != null) {
              JSResolveUtil.JSInjectedFilesVisitor visitor = new JSResolveUtil.JSInjectedFilesVisitor() {
                @Override
                protected void process(JSFile file) {
                  if (file != null) {
                    ActionScriptResolveUtil.processMetaAttributesForClass(file, processor);
                  }
                }
              };
              processMxmlTags(rootTag, true, visitor);
            }
          }
        }
      }
    }
  }

  public static String replacePathMacros(@NotNull final String text, @NotNull final Module module, final String sdkRootPath) {
    final StringBuilder builder = new StringBuilder(text);
    int startIndex;
    int endIndex = 0;

    while ((startIndex = builder.indexOf("${", endIndex)) >= 0) {
      endIndex = builder.indexOf("}", startIndex);
      if (endIndex > startIndex) {
        final String macroName = builder.substring(startIndex + 2, endIndex);
        final String macroValue;
        if (PathMacroUtil.MODULE_DIR_MACRO_NAME.equals(macroName)) {
          macroValue = ModuleUtilCore.getModuleDirPath(module);
        }
        else if (PathMacroUtil.PROJECT_DIR_MACRO_NAME.equals(macroName)) {
          macroValue = module.getProject().getBasePath();
        }
        else if (PathMacroUtil.USER_HOME_NAME.equals(macroName)) {
          macroValue = StringUtil.trimEnd(StringUtil.trimEnd(SystemProperties.getUserHome(), "/"), "\\");
        }
        else if (CompilerOptionInfo.FLEX_SDK_MACRO_NAME.equals(macroName)) {
          macroValue = sdkRootPath;
        }
        else {
          macroValue = PathMacros.getInstance().getValue(macroName);
        }

        if (macroValue != null && !StringUtil.isEmptyOrSpaces(macroValue)) {
          builder.replace(startIndex, endIndex + 1, macroValue);
          endIndex = endIndex + macroValue.length() - (macroName.length() + 3);
        }
      }
      else {
        break;
      }
    }

    return builder.toString();
  }

  public static <T> boolean equalLists(final List<? extends T> list1, final List<? extends T> list2) {
    if (list1.size() != list2.size()) return false;

    final Iterator<? extends T> iterator = list1.iterator();
    for (final T element : list2) {
      if (!iterator.next().equals(element)) return false;
    }

    return true;
  }

  public static String getContentOrModuleFolderPath(final Module module) {
    final String[] contentRootUrls = ModuleRootManager.getInstance(module).getContentRootUrls();
    return contentRootUrls.length > 0 ? VfsUtilCore.urlToPath(contentRootUrls[0]) : PathUtil.getParentPath(module.getModuleFilePath());
  }

  @Nullable
  public static VirtualFile createDirIfMissing(final Project project,
                                               final boolean interactive,
                                               final String folderPath,
                                               final String errorMessageTitle) {
    VirtualFile folder = LocalFileSystem.getInstance().findFileByPath(folderPath);
    if (folder == null) {
      try {
        folder = VfsUtil.createDirectories(folderPath);
      }
      catch (IOException e) {
        if (interactive) {
          Messages.showErrorDialog(project,
                                   FlexBundle
                                     .message("failed.to.create.folder", FileUtil.toSystemDependentName(folderPath), e.getMessage()),
                                   errorMessageTitle);
        }
        return null;
      }
    }

    if (folder == null) {
      if (interactive) {
        Messages.showErrorDialog(project, FlexBundle.message("failed.to.create.folder", folderPath, "unknown error"), errorMessageTitle);
      }
      return null;
    }
    if (!folder.isDirectory()) {
      Messages.showErrorDialog(project, FlexBundle.message("selected.path.not.folder", FileUtil.toSystemDependentName(folderPath)),
                               errorMessageTitle);
      return null;
    }

    return folder;
  }

  public static boolean processCompilerOption(final Module module, final FlexBuildConfiguration bc, final String option,
                                              final Processor<? super Pair<String, String>> processor) {
    String rawValue = bc.getCompilerOptions().getOption(option);
    if (rawValue == null) rawValue = FlexBuildConfigurationManager.getInstance(module).getModuleLevelCompilerOptions().getOption(option);
    if (rawValue == null) {
      rawValue =
        FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions().getOption(option);
    }

    if (rawValue == null) return true;

    int pos = 0;
    while (true) {
      int index = rawValue.indexOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR, pos);
      if (index == -1) break;

      String token = rawValue.substring(pos, index);
      final int tabIndex = token.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);

      if (tabIndex > 0 && !processor.process(Pair.create(token.substring(0, tabIndex), token.substring(tabIndex + 1)))) return false;

      pos = index + 1;
    }

    final int tabIndex = rawValue.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, pos);
    return tabIndex <= pos || processor.process(Pair.create(rawValue.substring(pos, tabIndex), rawValue.substring(tabIndex + 1)));
  }

  public static LinkageType convertLinkageType(final DependencyScope scope, final boolean isExported) {
    if (scope == DependencyScope.PROVIDED) {
      return LinkageType.External;
    }
    if (scope == DependencyScope.TEST) {
      return LinkageType.Test;
    }
    if (isExported) {
      return LinkageType.Include;
    }
    return DependencyType.DEFAULT_LINKAGE;
  }

  public static GlobalSearchScope getModuleWithDependenciesAndLibrariesScope(@NotNull Module module,
                                                                             @NotNull FlexBuildConfiguration bc,
                                                                             boolean includeTests) {
    // we cannot assert this since build configuration may be not yet persisted
    //if (!ArrayUtil.contains(bc, FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations())) {
    //  throw new IllegalArgumentException("Build configuration '" + bc.getName() + "' does not belong to module '" + module.getName() + "'");
    //}
    //
    module.putUserData(FlexOrderEnumerationHandler.FORCE_BC, bc);
    try {
      return module.getModuleWithDependenciesAndLibrariesScope(includeTests);
    }
    finally {
      module.putUserData(FlexOrderEnumerationHandler.FORCE_BC, null);
    }
  }

  public static String getOwnIpAddress() {
    try {
      final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        final Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          final InetAddress inetAddress = inetAddresses.nextElement();
          if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
            return inetAddress.getHostAddress();
          }
        }
      }
    }
    catch (SocketException ignore) {/* ignore */}
    return "unknown";
  }

  public static boolean isMxmlNs(final String ns) {
    return ArrayUtil.contains(ns, MxmlJSClass.MXML_URIS);
  }

  public static boolean packageExists(String packageName, final GlobalSearchScope scope) {
    if (StringUtil.isEmpty(packageName)) return true;

    return !FileBasedIndex.getInstance().getValues(JSPackageIndex.INDEX_ID, packageName, scope).isEmpty();
  }
}
