package com.intellij.lang.javascript.flex;

import com.intellij.application.options.PathMacrosImpl;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexOrderEnumerationHandler;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PathUtil;
import com.intellij.util.Processor;
import com.intellij.util.SystemProperties;
import com.intellij.util.xml.NanoXmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maxim.Mossienko
 */
public class FlexUtils {

  @NonNls private static final Pattern INFO_PLIST_EXECUTABLE_PATTERN =
    Pattern.compile("<key>CFBundleExecutable</key>(?:(?:\\s*)(?:<!--(?:.*)-->(?:\\s*))*)<string>(.*)</string>");

  private FlexUtils() {
  }

  public static FileChooserDescriptor createFileChooserDescriptor(final @Nullable String... allowedExtensions) {
    return allowedExtensions == null
           ? new FileChooserDescriptor(true, false, true, true, false, false)
           : new FileChooserDescriptor(true, false, true, true, false, false) {
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

  public static void createSampleApp(final Project project,
                                     final VirtualFile sourceRoot,
                                     final String sampleFileName,
                                     final TargetPlatform platform,
                                     final boolean isFlex4) throws IOException {
    final String sampleClassName = FileUtil.getNameWithoutExtension(sampleFileName);
    final String extension = FileUtil.getExtension(sampleFileName);
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
    final String sampleFileContent = FileUtil.loadTextAndClose(new InputStreamReader(stream)).replace("${class.name}", sampleClassName);
    final VirtualFile sampleApplicationFile = addFileWithContent(sampleFileName, sampleFileContent, sourceRoot);
    if (sampleApplicationFile != null) {
      final Runnable runnable = new Runnable() {
        public void run() {
          FileEditorManager.getInstance(project).openFile(sampleApplicationFile, true);
        }
      };

      if (project.isInitialized()) {
        runnable.run();
      }
      else {
        StartupManager.getInstance(project).registerPostStartupActivity(runnable);
      }
    }
  }

  public static VirtualFile addFileWithContent(final @NonNls String fileName, final @NonNls String fileContent, final VirtualFile dir)
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
  public static Sdk getSdkForActiveBC(final @NotNull Module module) {
    return ModuleType.get(module) instanceof FlexModuleType
           ? FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getSdk()
           : null;
  }

  /**
   * Looks through input stream containing XML document and finds all entries of XML elements listed in <code>xmlElements</code>.
   * Content of these elements is put to result map. XML namespaces are not taken into consideration.
   *
   * @param xmlInputStream input stream with xml content to parse
   * @param xmlElements    list of XML elements to look for.
   *                       Format is: <code>"&lt;root_element&gt;&lt;child_element&gt;&lt;subelement_to_look_for&gt;"</code>.
   *                       Listed XML elements SHOULD NOT contain subelements
   * @return map, keys are XML elements listed in <code>xmlElements</code>,
   *         values are all entries of respective element (may be empty list)
   */
  public static Map<String, List<String>> findXMLElements(final @NotNull InputStream xmlInputStream, final List<String> xmlElements) {
    final Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
    for (final String element : xmlElements) {
      resultMap.put(element, new ArrayList<String>());
    }

    NanoXmlUtil.parse(xmlInputStream, new NanoXmlUtil.IXMLBuilderAdapter() {

      private String currentElement = "";
      private final StringBuilder currentElementContent = new StringBuilder();

      public void startElement(final String name, final String nsPrefix, final String nsURI, final String systemID, final int lineNr)
        throws Exception {
        currentElement += "<" + name + ">";
      }

      public void endElement(final String name, final String nsPrefix, final String nsURI) throws Exception {
        if (xmlElements.contains(currentElement)) {
          resultMap.get(currentElement).add(currentElementContent.toString());
          currentElementContent.delete(0, currentElementContent.length());
        }
        assert currentElement.endsWith("<" + name + ">");
        currentElement = currentElement.substring(0, currentElement.length() - (name.length() + 2));
      }

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
   * Looks through input stream containing XML document and finds first entry of <code>xmlElement</code>.
   * XML namespaces are not taken into consideration.
   *
   * @param xmlInputStream input stream with xml content to parse
   * @param xmlElement     XML element to look for.
   *                       Format is: <code>"&lt;root_element&gt;&lt;child_element&gt;&lt;subelement_to_look_for&gt;"</code>.
   *                       XML element SHOULD NOT contain subelements
   * @return first found value of <code>xmlElement</code> tag, or <code>null</code> if non found or any exception occurs.
   */

  @Nullable
  public static String findXMLElement(final @NotNull InputStream xmlInputStream, final String xmlElement) {
    final Ref<String> result = new Ref<String>();

    NanoXmlUtil.parse(xmlInputStream, new NanoXmlUtil.IXMLBuilderAdapter() {

      private String currentElement = "";
      private final StringBuilder xmlElementContent = new StringBuilder();

      public void startElement(final String name, final String nsPrefix, final String nsURI, final String systemID, final int lineNr)
        throws Exception {
        currentElement += "<" + name + ">";
      }

      public void endElement(final String name, final String nsPrefix, final String nsURI) throws Exception {
        if (xmlElement.equals(currentElement)) {
          result.set(xmlElementContent.toString());
          stop();
        }
        assert currentElement.endsWith("<" + name + ">");
        currentElement = currentElement.substring(0, currentElement.length() - (name.length() + 2));
      }

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

  @Nullable
  public static String getMacExecutable(final @NotNull String appFolderPath) {
    try {
      final String text = FileUtil.loadFile(new File(appFolderPath + "/Contents/Info.plist"));
      Matcher m = INFO_PLIST_EXECUTABLE_PATTERN.matcher(text);
      if (!m.find()) return null;
      return appFolderPath + "/Contents/MacOS/" + m.group(1);
    }
    catch (IOException e) {
      return null;
    }
  }

  /**
   * If the first item of ComboBox model is <code>null</code> or not instance of <code>clazz</code> then it will be removed from the model.
   */
  public static void removeIncorrectItemFromComboBoxIfPresent(final JComboBox comboBox, final Class clazz) {
    final int oldSize = comboBox.getModel().getSize();
    final Object firstElement = comboBox.getModel().getElementAt(0);
    if (oldSize > 0 && (firstElement == null || !clazz.isAssignableFrom(firstElement.getClass()))) {
      final Object selectedItem = comboBox.getSelectedItem();
      final Object[] newObjects = new Object[oldSize - 1];
      for (int i = 0; i < newObjects.length; i++) {
        newObjects[i] = comboBox.getModel().getElementAt(i + 1);
      }
      comboBox.setModel(new DefaultComboBoxModel(newObjects));
      comboBox.setSelectedItem(selectedItem);
    }
  }

  public static String getFlexCompilerWorkDirPath(final Project project, final @Nullable Sdk flexSdk) {
    final VirtualFile baseDir = project.getBaseDir();
    return FlexSdkUtils.isFlex2Sdk(flexSdk) || FlexSdkUtils.isFlex3_0Sdk(flexSdk)
           ? getTempFlexConfigsDirPath() // avoid problems with spaces in temp dir path (fcsh from Flex SDK 2 is not patched)
           : (baseDir == null ? "" : baseDir.getPath());
  }

  public static VirtualFile getFlexCompilerWorkDir(final Project project, final @Nullable Sdk flexSdk) {
    return LocalFileSystem.getInstance().findFileByPath(getFlexCompilerWorkDirPath(project, flexSdk));
  }

  public static String getTempFlexConfigsDirPath() {
    return FileUtil.toSystemIndependentName(FileUtil.getTempDirectory()) + "/" +
           ApplicationNamesInfo.getInstance().getFullProductName().replace(' ', '_');
  }

  public static String getPathToMainClassFile(final String mainClassFqn, final Module module) {
    if (StringUtil.isEmpty(mainClassFqn)) return "";

    final String s = mainClassFqn.replace('.', '/');
    final String[] classFileRelPaths = {s + ".mxml", s + ".as"};

    for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
      for (final String classFileRelPath : classFileRelPaths) {
        final VirtualFile mainClassFile = VfsUtil.findRelativeFile(classFileRelPath, sourceRoot);
        if (mainClassFile != null) {
          return mainClassFile.getPath();
        }
      }
    }

    return "";
  }

  public static String getPathToFlexUnitTempDirectory(String projectName) {
    return getTempFlexConfigsDirPath() + "/flexunit-" +
           Integer.toHexString((SystemProperties.getUserName() + projectName).hashCode()).toUpperCase();
  }

  public static void removeFileLater(final @NotNull VirtualFile file) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              if (file.exists()) {
                file.delete(this);
              }
            }
            catch (IOException e) {/*ignore*/}
          }
        });
      }
    });
  }

  private static void processMxmlTags(final XmlTag rootTag,
                                      final JSResolveUtil.JSInjectedFilesVisitor injectedFilesVisitor,
                                      Processor<XmlTag> processor) {
    String namespace = JSResolveUtil.findMxmlNamespace(rootTag);

    XmlBackedJSClassImpl.InjectedScriptsVisitor scriptsVisitor =
      new XmlBackedJSClassImpl.InjectedScriptsVisitor(rootTag, false, false, false, injectedFilesVisitor, processor, true);
    scriptsVisitor.go();

    for (XmlTag s : rootTag.findSubTags("Metadata", namespace)) {
      processor.process(s);
    }
  }

  public static void processMxmlTags(final XmlTag rootTag, boolean isPhysical,
                                     final JSResolveUtil.JSInjectedFilesVisitor injectedFilesVisitor) {
    processMxmlTags(rootTag, injectedFilesVisitor,
                    new XmlBackedJSClassImpl.InjectedScriptsVisitor.InjectingProcessor(injectedFilesVisitor, rootTag, isPhysical));
  }

  public static void processMetaAttributesForClass(@NotNull PsiElement jsClass, @NotNull final JSResolveUtil.MetaDataProcessor processor) {
    JSResolveUtil.processMetaAttributesForClass(jsClass, processor);
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
                    JSResolveUtil.processMetaAttributesForClass(file, processor);
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

  public static String replacePathMacros(final @NotNull String text, final @NotNull Module module, final String sdkRootPath) {
    final StringBuilder builder = new StringBuilder(text);
    int startIndex;
    int endIndex = 0;

    while ((startIndex = builder.indexOf("${", endIndex)) >= 0) {
      endIndex = builder.indexOf("}", startIndex);
      if (endIndex > startIndex) {
        final String macroName = builder.substring(startIndex + 2, endIndex);
        final String macroValue;
        if (PathMacrosImpl.MODULE_DIR_MACRO_NAME.equals(macroName)) {
          final VirtualFile moduleFile = module.getModuleFile();
          macroValue = moduleFile == null ? null : moduleFile.getParent().getPath();
        }
        else if (PathMacrosImpl.PROJECT_DIR_MACRO_NAME.equals(macroName)) {
          final VirtualFile baseDir = module.getProject().getBaseDir();
          macroValue = baseDir == null ? null : baseDir.getPath();
        }
        else if (PathMacrosImpl.USER_HOME_MACRO_NAME.equals(macroName)) {
          macroValue = StringUtil.trimEnd((StringUtil.trimEnd(SystemProperties.getUserHome(), "/")), "\\");
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

  public static String getPathToBundledJar(String filename) {
    final URL url = FlexUtils.class.getResource("");
    String folder;
    if ("jar".equals(url.getProtocol())) {
      // running from build
      folder = "/plugins/flex/lib/";
    }
    else {
      // running from sources
      folder = "/flex/lib/";
    }
    return FileUtil.toSystemDependentName(PathManager.getHomePath() + folder + filename);
  }

  public static List<String> getOptionValues(final String commandLine, final String... optionAndAliases) {
    if (StringUtil.isEmpty(commandLine)) {
      return Collections.emptyList();
    }

    final List<String> result = new LinkedList<String>();

    for (CommandLineTokenizer tokenizer = new CommandLineTokenizer(commandLine); tokenizer.hasMoreTokens(); ) {
      final String token = tokenizer.nextToken();
      for (String option : optionAndAliases) {
        if (token.startsWith("-" + option + "=") || token.startsWith("-" + option + "+=")) {
          result.addAll(StringUtil.split(token.substring(token.indexOf("=") + 1), ","));
        }
        else if (token.equals("-" + option) && tokenizer.countTokens() > 0) {
          if (tokenizer.countTokens() > 0) {
            String nextToken;
            while (tokenizer.hasMoreTokens() && canBeCompilerOptionValue(nextToken = tokenizer.peekNextToken())) {
              tokenizer.nextToken(); // advance tokenizer position
              result.add(nextToken);
            }
          }
        }
      }
    }

    return result;
  }

  public static String removeOptions(final String commandLine, final String... optionsToRemove) {
    if (commandLine.isEmpty()) return commandLine;

    final StringBuilder buf = new StringBuilder();
    for (StringTokenizer tokenizer = new StringTokenizer(commandLine, " ", true); tokenizer.hasMoreTokens(); ) {
      final String token = tokenizer.nextToken();

      boolean remove = false;
      for (String option : optionsToRemove) {
        if (token.startsWith("-" + option)) {
          remove = true;
          break;
        }
      }

      if (remove) {
        String nextToken = null;

        WHILE: while (tokenizer.hasMoreElements()) {
          nextToken = tokenizer.nextToken();
          if (StringUtil.isEmptyOrSpaces(nextToken) || canBeCompilerOptionValue(nextToken)) {
            continue;
          }

          for (String option : optionsToRemove) {
            if (nextToken.startsWith("-" + option)) {
              continue WHILE;
            }
          }

          break;
        }

        if (nextToken != null && !canBeCompilerOptionValue(nextToken)) {
          buf.append(nextToken);
        }
      }
      else {
        buf.append(token);
      }
    }

    return buf.toString();
  }

  public static boolean canBeCompilerOptionValue(final String text) {
    if (text.startsWith("-")) {  // option or negative number
      return text.length() > 1 && Character.isDigit(text.charAt(1));
    }
    return !text.startsWith("+");
  }

  public static <T> boolean equalLists(final List<T> list1, final List<T> list2) {
    if (list1.size() != list2.size()) return false;

    final Iterator<T> iterator = list1.iterator();
    for (final T element : list2) {
      if (!iterator.next().equals(element)) return false;
    }

    return true;
  }

  public static String getContentOrModuleFolderPath(final Module module) {
    final String[] contentRootUrls = ModuleRootManager.getInstance(module).getContentRootUrls();
    return contentRootUrls.length > 0 ? VfsUtil.urlToPath(contentRootUrls[0]) : PathUtil.getParentPath(module.getModuleFilePath());
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
    else if (!folder.isDirectory()) {
      Messages.showErrorDialog(project, FlexBundle.message("selected.path.not.folder", FileUtil.toSystemDependentName(folderPath)),
                               errorMessageTitle);
      return null;
    }

    return folder;
  }

  public static boolean processCompilerOption(final Module module, final FlexBuildConfiguration bc, final String option,
                                              final Processor<Pair<String, String>> processor) {
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
    if (tabIndex > pos) {
      if (!processor.process(Pair.create(rawValue.substring(pos, tabIndex), rawValue.substring(tabIndex + 1)))) return false;
    }

    return true;
  }

  public static LinkageType convertLinkageType(final DependencyScope scope, final boolean isExported) {
    if (scope == DependencyScope.PROVIDED) {
      return LinkageType.External;
    }
    else if (scope == DependencyScope.TEST) {
      return LinkageType.Test;
    }
    else if (isExported) {
      return LinkageType.Include;
    }
    return DependencyType.DEFAULT_LINKAGE;
  }

  public static ModuleWithDependenciesScope getModuleWithDependenciesAndLibrariesScope(@NotNull Module module,
                                                                                       @NotNull FlexBuildConfiguration bc,
                                                                                       boolean includeTests) {
    // we cannot assert this since build configuration may be not yet persisted
    //if (!ArrayUtil.contains(bc, FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations())) {
    //  throw new IllegalArgumentException("Build configuration '" + bc.getName() + "' does not belong to module '" + module.getName() + "'");
    //}
    //
    module.putUserData(FlexOrderEnumerationHandler.FORCE_BC, bc);
    try {
      return new ModuleWithDependenciesScope(module, ModuleWithDependenciesScope.COMPILE |
                                                     ModuleWithDependenciesScope.MODULES |
                                                     ModuleWithDependenciesScope.LIBRARIES |
                                                     (includeTests ? ModuleWithDependenciesScope.TESTS : 0));
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
}
