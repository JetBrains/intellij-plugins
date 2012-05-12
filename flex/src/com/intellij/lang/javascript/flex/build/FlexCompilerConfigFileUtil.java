package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.text.CharSequenceReader;
import com.intellij.util.xml.NanoXmlUtil;
import gnu.trove.THashSet;
import org.jdom.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class FlexCompilerConfigFileUtil {

  private static final Key<Pair<Long, Collection<NamespacesInfo>>> MOD_STAMP_TO_NAMESPACES_INFOS =
    Key.create("MOD_STAMP_TO_NAMESPACES_INFOS");
  private static final Key<Pair<Long, InfoFromConfigFile>> MOD_STAMP_AND_INFO_FROM_CONFIG_FILE =
    Key.create("MOD_STAMP_AND_INFO_FROM_CONFIG_FILE");

  private static final String TARGET_PLAYER_ELEMENT = "<flex-config><target-player>";
  private static final String FILE_SPEC_ELEMENT = "<flex-config><file-specs><path-element>";
  private static final String OUTPUT_ELEMENT = "<flex-config><output>";

  public static final String FLEX_CONFIG = "flex-config";
  public static final String COMPILER = "compiler";

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

      // main class and output path are taken according to run configuration, i.e. as set in generated config file text
      /*"debug",*/ "file-specs", "output",

      // load-externs option should not be used because it can lead to runtime errors like IDEA-70155
      "load-externs"
    };

  private static final String[] OPTIONS_CONTAINING_PATHS =
    {"path-element", "manifest", "defaults-css-url", "filename", "link-report", "load-externs", "services", "resource-bundle-list"};
  private static final String[] NON_REPEATABLE_OPTIONS_THAT_CAN_BE_IN_GENERATED_FILE =
    {"mobile", "preloader", "warn-no-constructor", "accessible", "keep-generated-actionscript", "services", "context-root",
      "defaults-css-url", "debug", "target-player", "swf-version", "static-link-runtime-shared-libraries",
      "date", "title", "language", "contributor", "creator", "publisher", "description"};

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

  public static String mergeWithCustomConfigFile(final String generatedConfigText,
                                                 final String additionalConfigFilePath,
                                                 final boolean makeExternalLibsMerged,
                                                 final boolean makeIncludedLibsMerged) {
    final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
    if (additionalConfigFile == null) {
      return generatedConfigText;
    }

    final Document document;
    try {
      document = JDOMUtil.loadDocument(additionalConfigFile.getInputStream());
    }
    catch (JDOMException e) {
      return generatedConfigText;
    }
    catch (IOException e) {
      return generatedConfigText;
    }

    final Element rootElement = document.getRootElement();
    if (!FLEX_CONFIG.equals(rootElement.getName())) {
      return generatedConfigText;
    }

    removeSwcSpecificElementsRecursively(rootElement);
    makeLibrariesMergedIntoCode(rootElement, makeExternalLibsMerged, makeIncludedLibsMerged);

    try {
      final Element otherRootElement = JDOMUtil.loadDocument(generatedConfigText).getRootElement();
      assert otherRootElement != null && FLEX_CONFIG.equals(rootElement.getName()) : JDOMUtil.writeDocument(document, "\n");

      appendDocument(rootElement, otherRootElement);
    }
    catch (IOException e) {
      assert false : e.getMessage() + "\n" + generatedConfigText;
    }
    catch (JDOMException e) {
      assert false : e.getMessage() + "\n" + generatedConfigText;
    }

    return JDOMUtil.writeDocument(document, "\n");
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

  private static void appendDocument(final Element rootElement, final Element otherRootElement) {
    final Collection<Element> toRemove = findDuplicateElementsRecursively(rootElement, otherRootElement);
    for (Element duplicateElement : toRemove) {
      final Element parentElement = duplicateElement.getParentElement();
      parentElement.removeContent(duplicateElement);
      if (parentElement.getChildren().isEmpty()) {
        parentElement.getParentElement().removeContent(parentElement);
      }
    }

    //noinspection unchecked
    for (final Element otherElement : ((Iterable<Element>)otherRootElement.getChildren())) {
      rootElement.addContent((Element)otherElement.clone());
    }
  }

  // required to avoid setting the same option values twice because it may lead to compilation failure (e.g. if the same locale is listed twice)
  private static Collection<Element> findDuplicateElementsRecursively(final Element existingElement, final Element otherElement) {
    final Collection<Element> result = new THashSet<Element>();

    //noinspection unchecked
    for (Element potentialChild : (Iterable<Element>)otherElement.getChildren()) {
      final List existingChildren = existingElement.getChildren(potentialChild.getName(), existingElement.getNamespace());
      //noinspection unchecked
      for (Element existingChild : (Iterable<Element>)existingChildren) {
        final String potentialChildContent = potentialChild.getTextTrim();
        if (potentialChildContent.isEmpty()) {
          result.addAll(findDuplicateElementsRecursively(existingChild, potentialChild));
        }
        else {
          final String existingElementContent = existingChild.getTextTrim();
          if (ArrayUtil.contains(existingChild.getName(), NON_REPEATABLE_OPTIONS_THAT_CAN_BE_IN_GENERATED_FILE)) {
            result.add(potentialChild);
          }
          else if (areOptionValuesEqual(existingChild.getName(), potentialChildContent, existingElementContent)) {
            // remove only similar repeatable values, do not remove equal values of <policy-file-url/> that are for different <runtime-shared-library-path/> elements.
            if (existingElement.getChildren().size() == existingChildren.size()) {
              result.add(potentialChild);
            }
          }
        }
      }
    }

    return result;
  }

  private static boolean areOptionValuesEqual(final String optionName, final String value1, final String value2) {
    if (value1.equals(value2)) return true;
    if (ArrayUtil.contains(optionName, OPTIONS_CONTAINING_PATHS)) {
      if (FileUtil.toSystemIndependentName(value1).equals(FileUtil.toSystemIndependentName(value2))) return true;
    }
    return false;
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

  @NotNull
  public static InfoFromConfigFile getInfoFromConfigFile(final String configFilePath) {
    final VirtualFile configFile = configFilePath.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(configFilePath);
    if (configFile == null) {
      return InfoFromConfigFile.DEFAULT;
    }

    Pair<Long, InfoFromConfigFile> data = configFile.getUserData(MOD_STAMP_AND_INFO_FROM_CONFIG_FILE);

    final FileDocumentManager documentManager = FileDocumentManager.getInstance();
    final com.intellij.openapi.editor.Document cachedDocument = documentManager.getCachedDocument(configFile);
    final Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : configFile.getModificationCount();
    final Long cachedTimestamp = data == null ? null : data.first;

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      data = null;
      configFile.putUserData(MOD_STAMP_AND_INFO_FROM_CONFIG_FILE, data);

      final List<String> xmlElements = Arrays.asList(FILE_SPEC_ELEMENT, OUTPUT_ELEMENT, TARGET_PLAYER_ELEMENT);

      String mainClassPath = null;
      String outputPath = null;
      String targetPlayer = null;

      try {
        final InputStream inputStream =
          cachedDocument == null ? configFile.getInputStream() : new ByteArrayInputStream(cachedDocument.getText().getBytes());
        final Map<String, List<String>> map = FlexUtils.findXMLElements(inputStream, xmlElements);

        final List<String> fileSpecList = map.get(FILE_SPEC_ELEMENT);
        if (!fileSpecList.isEmpty()) {
          mainClassPath = fileSpecList.get(0);
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

      final String outputFileName = outputPath == null ? null : PathUtil.getFileName(outputPath);
      final String outputFolderPath = outputPath == null ? null : PathUtil.getParentPath(outputPath);
      data =
        Pair.create(currentTimestamp, new InfoFromConfigFile(configFile, mainClassPath, outputFileName, outputFolderPath, targetPlayer));
      configFile.putUserData(MOD_STAMP_AND_INFO_FROM_CONFIG_FILE, data);
    }

    return data.second;
  }
}
