package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.text.CharSequenceReader;
import com.intellij.util.xml.NanoXmlUtil;
import org.jdom.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

import static com.intellij.lang.javascript.flex.sdk.FlexSdkUtils.*;

public class FlexCompilerConfigFileUtil {

  private static final Key<Pair<Long, Collection<NamespacesInfo>>> MOD_STAMP_TO_NAMESPACES_INFOS =
    Key.create("MOD_STAMP_TO_NAMESPACES_INFOS");

  public static final String FLEX_CONFIG = "flex-config";
  public static final String COMPILER = "compiler";
  public static final String SOURCE_PATH = "source-path";

  public static final String EXTERNAL_LIBRARY_PATH = "external-library-path";
  public static final String INCLUDE_LIBRARIES = "include-libraries";
  public static final String LIBRARY = "library";
  public static final String LIBRARY_PATH = "library-path";
  public static final String PATH_ELEMENT = "path-element";
  public static final String APPEND = "append";

  public static final String INCLUDE_NAMESPACES = "include-namespaces";
  public static final String NAMESPACES = "namespaces";
  public static final String NAMESPACE = "namespace";
  public static final String MANIFEST = "manifest";
  public static final String URI = "uri";

  public static final String DEFINE = "define";
  public static final String NAME = "name";
  public static final String VALUE = "value";

  public static final String FILE_SPECS = "file-specs";
  public static final String OUTPUT = "output";

  private static final String[] ELEMENTS_TO_REMOVE =
    {
      // these 8 options are for SWC compilation only, not applicable for SWF compilation
      "compute-digest", "directory", "include-classes", "include-file",
      "include-lookup-only", "include-namespaces", "include-sources", "include-stylesheet",

      // these 3 settings are non-appendable elements that are added to generated config file by FlexCompilerHandler#generateConfigFileText()
      "debug", "file-specs", "output",

      // load-externs option should not be used because it can lead to runtime errors like IDEA-70155
      "load-externs"
    };

  public static class InfoFromConfigFile {
    public static InfoFromConfigFile DEFAULT = new InfoFromConfigFile(null, null, null, null);

    public final @Nullable String mainClass;
    public final @Nullable String outputFileName;
    public final @Nullable String outputFolderPath;
    public final @Nullable String targetPlayer;

    private InfoFromConfigFile(final @Nullable String mainClass,
                               final @Nullable String outputFileName,
                               final @Nullable String outputFolderPath,
                               final @Nullable String targetPlayer) {
      this.mainClass = mainClass;
      this.outputFileName = outputFileName;
      this.outputFolderPath = outputFolderPath;
      this.targetPlayer = targetPlayer;
    }
  }

  public static class NamespacesInfo {
    public final String namespace;
    public final String manifest;
    public final boolean includedInSwc;

    private NamespacesInfo(final String namespace, final String manifest, final boolean includedInSwc) {
      this.namespace = namespace;
      this.manifest = manifest;
      this.includedInSwc = includedInSwc;
    }
  }

  private FlexCompilerConfigFileUtil() {
  }

  static String mergeWithCustomConfigFile(final String configText, final String customConfigFilePath, final String cssFilePath) {
    final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(customConfigFilePath);
    if (configFile == null) {
      return configText;
    }

    final Document document;
    try {
      document = JDOMUtil.loadDocument(configFile.getInputStream());
    }
    catch (JDOMException e) {
      return configText;
    }
    catch (IOException e) {
      return configText;
    }

    final Element rootElement = document.getRootElement();
    if (!FLEX_CONFIG.equals(rootElement.getName())) {
      return configText;
    }

    addSourcePathIfCssCompilation(rootElement, cssFilePath);
    removeSwcSpecificElementsRecursively(rootElement);
    makeLibrariesMergedIntoCode(rootElement, cssFilePath == null, cssFilePath != null);

    try {
      appendDocument(rootElement, JDOMUtil.loadDocument(configText));
    }
    catch (IOException e) {
      assert false : e.getMessage() + "\n" + configText;
    }
    catch (JDOMException e) {
      assert false : e.getMessage() + "\n" + configText;
    }

    return JDOMUtil.writeDocument(document, "\n");
  }

  private static void addSourcePathIfCssCompilation(final Element rootElement, final String cssFilePath) {
    // folder that contains css file must be source folder and must be first in source-path list, otherwise stupid compiler says that css file must have package corresponding its path
    if (cssFilePath == null) return;

    final int lastSlashIndex = cssFilePath.lastIndexOf('/');
    final String cssDirPath = lastSlashIndex > 0 ? cssFilePath.substring(0, lastSlashIndex) : null;

    if (cssDirPath != null) {
      final Element compilerElement = new Element(COMPILER, rootElement.getNamespace());
      rootElement.addContent(0, compilerElement);
      final Element sourcePathElement = new Element(SOURCE_PATH, compilerElement.getNamespace());
      compilerElement.addContent(sourcePathElement);
      final Element pathElement = new Element(PATH_ELEMENT, sourcePathElement.getNamespace());
      sourcePathElement.addContent(pathElement);
      pathElement.addContent(cssDirPath);
    }
  }

  private static void removeSwcSpecificElementsRecursively(final Element element) {
    for (final String elementName : ELEMENTS_TO_REMOVE) {
      element.removeChildren(elementName, element.getNamespace());
    }

    // noinspection unchecked
    for (final Element child : (Iterable<Element>)element.getChildren()) {
      removeSwcSpecificElementsRecursively(child);
    }
  }

  private static void makeLibrariesMergedIntoCode(final Element rootElement, final boolean externalLibs, final boolean includedLibs) {
    final Namespace namespace = rootElement.getNamespace();

    final Collection<String> paths = removeLibs(rootElement, externalLibs, includedLibs);

    if (!paths.isEmpty()) {
      final Element compilerElement = rootElement.getChild(COMPILER, namespace);

      Element libraryPathElement = compilerElement.getChild(LIBRARY_PATH, namespace);
      if (libraryPathElement == null) {
        libraryPathElement = new Element(LIBRARY_PATH, namespace);
        libraryPathElement.setAttribute(new Attribute(APPEND, "true"));
        compilerElement.addContent(libraryPathElement);
      }

      for (final String path : paths) {
        final Element pathElement = new Element(PATH_ELEMENT, namespace);
        pathElement.addContent(path);
        libraryPathElement.addContent(pathElement);
      }
    }
  }

  private static Collection<String> removeLibs(final Element rootElement, final boolean removeExternal, final boolean removeIncluded) {
    final Namespace namespace = rootElement.getNamespace();

    final Collection<String> result = new ArrayList<String>();

    //noinspection unchecked
    for (Element compilerElement : ((Iterable<Element>)rootElement.getChildren(COMPILER, namespace))) {
      if (removeExternal) {
        //noinspection unchecked
        for (Element externalLibraryPathElement : ((Iterable<Element>)compilerElement.getChildren(EXTERNAL_LIBRARY_PATH, namespace))) {
          final Collection<Element> pathElementsToRemove = new ArrayList<Element>();
          //noinspection unchecked
          for (Element pathElement : ((Iterable<Element>)externalLibraryPathElement.getChildren(PATH_ELEMENT, namespace))) {
            final String path = pathElement.getText();
            final String fileName = path.substring(FileUtil.toSystemIndependentName(path).lastIndexOf("/") + 1);
            if (fileName.startsWith("playerglobal") || fileName.startsWith("airglobal")) {
              continue;
            }

            result.add(path);
            pathElementsToRemove.add(pathElement);
          }

          for (Element pathElement : pathElementsToRemove) {
            externalLibraryPathElement.removeContent(pathElement);
          }
        }
      }

      if (removeIncluded) {
        //noinspection unchecked
        for (Element includeLibrariesElement : ((Iterable<Element>)compilerElement.getChildren(INCLUDE_LIBRARIES, namespace))) {
          final Collection<Element> libraryElementsToRemove = new ArrayList<Element>();
          //noinspection unchecked
          for (Element libraryElement : ((Iterable<Element>)includeLibrariesElement.getChildren(LIBRARY, namespace))) {
            result.add(libraryElement.getText());
            libraryElementsToRemove.add(libraryElement);
          }

          for (Element pathElement : libraryElementsToRemove) {
            includeLibrariesElement.removeContent(pathElement);
          }
        }
      }
    }
    return result;
  }

  private static void appendDocument(final Element rootElement, final Document document) {
    final Element otherRootElement = document.getRootElement();
    assert rootElement != null && FLEX_CONFIG.equals(rootElement.getName()) : JDOMUtil.writeDocument(document, "\n");

    //noinspection unchecked
    for (final Element element : ((Iterable<Element>)otherRootElement.getChildren())) {
      rootElement.addContent((Element)element.clone());
    }
  }

  public static Collection<NamespacesInfo> getNamespacesInfos(final VirtualFile configFile) {
    if (configFile == null || !configFile.isValid() || configFile.isDirectory()) {
      return Collections.emptyList();
    }

    Pair<Long, Collection<NamespacesInfo>> data = configFile.getUserData(MOD_STAMP_TO_NAMESPACES_INFOS);

    final FileDocumentManager documentManager = FileDocumentManager.getInstance();
    final com.intellij.openapi.editor.Document cachedDocument = documentManager.getCachedDocument(configFile);
    final Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : configFile.getModificationCount();
    final Long cachedTimestamp = data == null ? null : data.first;

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      data = null;
      configFile.putUserData(MOD_STAMP_TO_NAMESPACES_INFOS, data);

      try {
        final NamespacesXmlBuilder builder = new NamespacesXmlBuilder();
        if (cachedDocument != null) {
          //noinspection IOResourceOpenedButNotSafelyClosed
          NanoXmlUtil.parse(new CharSequenceReader(cachedDocument.getCharsSequence()), builder);
        }
        else {
          NanoXmlUtil.parse(configFile.getInputStream(), builder);
        }

        final Collection<NamespacesInfo> namespacesInfos = new ArrayList<NamespacesInfo>();
        final Collection<String> includedInSwcNamespaces = builder.getIncludedNamespaces();
        for (Pair<String, String> namespaceAndManifest : builder.getNamespacesAndManifests()) {
          namespacesInfos.add(new NamespacesInfo(namespaceAndManifest.first, namespaceAndManifest.second,
                                                 includedInSwcNamespaces.contains(namespaceAndManifest.first)));
        }

        data = Pair.create(currentTimestamp, namespacesInfos);
        configFile.putUserData(MOD_STAMP_TO_NAMESPACES_INFOS, data);
      }
      catch (IOException ignored) {
      }
    }

    return data == null ? Collections.<NamespacesInfo>emptyList() : data.second;
  }

  public static InfoFromConfigFile getInfoFromConfigFile(final Module module, final String configFilePath) {
    String mainClass = null;
    String outputPath = null;
    String targetPlayer = null;

    final VirtualFile configFile = configFilePath.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(configFilePath);
    if (configFile != null) {
      final FileDocumentManager manager = FileDocumentManager.getInstance();
      if (manager.isFileModified(configFile)) {
        final com.intellij.openapi.editor.Document document = manager.getCachedDocument(configFile);
        if (document != null) {
          manager.saveDocument(document);
        }
      }

      final List<String> xmlElements = Arrays.asList(FILE_SPEC_ELEMENT, OUTPUT_ELEMENT, TARGET_PLAYER_ELEMENT);
      try {
        final Map<String, List<String>> map = FlexUtils.findXMLElements(configFile.getInputStream(), xmlElements);

        final List<String> fileSpecList = map.get(FILE_SPEC_ELEMENT);
        if (!fileSpecList.isEmpty()) {
          mainClass = getClassForOutputTagValue(module, fileSpecList.get(0), configFile.getParent());
        }

        final List<String> outputList = map.get(OUTPUT_ELEMENT);
        if (!outputList.isEmpty()) {
          outputPath = outputList.get(0);
          if (!FileUtil.isAbsolute(outputPath)) {
            outputPath = configFile.getParent().getPath() + "/" + outputPath;
          }
        }

        final List<String> targetPlayerList = map.get(TARGET_PLAYER_ELEMENT);
        if (!targetPlayerList.isEmpty()) {
          targetPlayer = targetPlayerList.get(0);
        }
      }
      catch (IOException ignore) {/*ignore*/ }
    }

    final String outputFileName = outputPath == null ? null : PathUtil.getFileName(outputPath);
    final String outputFolderPath = outputPath == null ? null : PathUtil.getParentPath(outputPath);
    return new InfoFromConfigFile(mainClass, outputFileName, outputFolderPath, targetPlayer);
  }

  private static String getClassForOutputTagValue(final Module module, final String outputTagValue, final VirtualFile baseDir) {
    if (outputTagValue.isEmpty()) return "unknown";

    final VirtualFile file = VfsUtil.findRelativeFile(outputTagValue, baseDir);
    if (file == null) return FileUtil.getNameWithoutExtension(PathUtil.getFileName(outputTagValue));

    final VirtualFile sourceRoot = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getSourceRootForFile(file);
    if (sourceRoot == null) return file.getNameWithoutExtension();

    final String relativePath = VfsUtilCore.getRelativePath(file, sourceRoot, '/');
    return relativePath == null ? file.getNameWithoutExtension() : FileUtil.getNameWithoutExtension(relativePath).replace("/", ".");
  }
}
