package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.NanoXmlUtil;
import org.jdom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FlexCompilerConfigFileUtil {

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

  public static Collection<FlexBuildConfiguration.NamespaceAndManifestFileInfo> getNamespaceAndManifestFileInfoFromCustomConfig(final VirtualFile configFile) {
    if (configFile == null || !configFile.isValid() || configFile.isDirectory()) {
      return Collections.emptyList();
    }

    final Collection<FlexBuildConfiguration.NamespaceAndManifestFileInfo> result =
      new ArrayList<FlexBuildConfiguration.NamespaceAndManifestFileInfo>();

    try {
      final NamespacesXmlBuilder builder = new NamespacesXmlBuilder();
      NanoXmlUtil.parse(configFile.getInputStream(), builder);

      final Collection<String> includedInSwcNamespaces = builder.getIncludedNamespaces();

      for (final Pair<String, String> namespaceAndManifest : builder.getNamespacesAndManifests()) {
        final FlexBuildConfiguration.NamespaceAndManifestFileInfo info = new FlexBuildConfiguration.NamespaceAndManifestFileInfo();
        info.NAMESPACE = namespaceAndManifest.first;
        info.MANIFEST_FILE_PATH = namespaceAndManifest.second;
        info.INCLUDE_IN_SWC = includedInSwcNamespaces.contains(info.NAMESPACE);
        result.add(info);
      }
    }
    catch (IOException ignored) {
    }

    return result;
  }
}
