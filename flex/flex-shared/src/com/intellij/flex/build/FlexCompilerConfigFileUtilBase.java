// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.build;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class FlexCompilerConfigFileUtilBase {
  public static final String FLEX_CONFIG = "flex-config";
  public static final String COMPILER = "compiler";
  public static final String EXTERNAL_LIBRARY_PATH = "external-library-path";
  public static final String INCLUDE_LIBRARIES = "include-libraries";
  public static final String LIBRARY = "library";
  public static final String LIBRARY_PATH = "library-path";
  public static final String PATH_ELEMENT = "path-element";
  public static final String APPEND = "append";
  private static final String[] ELEMENTS_TO_REMOVE =
    {
      // these 8 options are for SWC compilation only, not applicable for SWF compilation
      "compute-digest", "directory", "include-classes", "include-file",
      "include-lookup-only", "include-namespaces", "include-sources", "include-stylesheet",

      // main class and output path are taken according to run configuration, i.e. as set in generated config file text
      /*"debug",*/ "file-specs", "output",

      // link-report option in custom config file is actual only for main app. Need to remove it for RLMs, tests, etc.
      "link-report",

      // load-externs option should not be used because it can lead to runtime errors like IDEA-70155
      "load-externs"
    };
  private static final String[] OPTIONS_CONTAINING_PATHS =
    {"path-element", "manifest", "defaults-css-url", "filename", "link-report", "load-externs", "services", "resource-bundle-list"};
  private static final String[] NON_REPEATABLE_OPTIONS_THAT_CAN_BE_IN_GENERATED_FILE =
    {"mobile", "preloader", "warn-no-constructor", "accessible", "keep-generated-actionscript", "services", "context-root",
      "defaults-css-url", "debug", "target-player", "swf-version", "static-link-runtime-shared-libraries",
      "date", "title", "language", "contributor", "creator", "publisher", "description", "manager-class"};

  public static String mergeWithCustomConfigFile(final String generatedConfigText,
                                                 final String additionalConfigFilePath,
                                                 final boolean makeExternalLibsMerged,
                                                 final boolean makeIncludedLibsMerged) {
    final File additionalConfigFile = new File(additionalConfigFilePath);
    if (!additionalConfigFile.isFile()) {
      return generatedConfigText;
    }

    final Element rootElement;
    try {
      rootElement = JDOMUtil.load(additionalConfigFile);
    }
    catch (JDOMException | IOException e) {
      return generatedConfigText;
    }

    if (!FLEX_CONFIG.equals(rootElement.getName())) {
      return generatedConfigText;
    }

    removeSwcSpecificElementsRecursively(rootElement);
    makeLibrariesMergedIntoCode(rootElement, makeExternalLibsMerged, makeIncludedLibsMerged);

    try {
      final Element otherRootElement = JDOMUtil.load(generatedConfigText);
      assert FLEX_CONFIG.equals(rootElement.getName()) : JDOMUtil.writeElement(rootElement);

      appendDocument(rootElement, otherRootElement);
    }
    catch (IOException | JDOMException e) {
      assert false : e.getMessage() + "\n" + generatedConfigText;
    }

    return JDOMUtil.writeElement(rootElement);
  }

  private static void removeSwcSpecificElementsRecursively(final Element element) {
    for (final String elementName : ELEMENTS_TO_REMOVE) {
      element.removeChildren(elementName, element.getNamespace());
    }

    for (final Element child : element.getChildren()) {
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

    final Collection<String> result = new ArrayList<>();

    for (Element compilerElement : rootElement.getChildren(COMPILER, namespace)) {
      if (removeExternal) {
        for (Element externalLibraryPathElement : compilerElement.getChildren(EXTERNAL_LIBRARY_PATH, namespace)) {
          final Collection<Element> pathElementsToRemove = new ArrayList<>();
          for (Element pathElement : externalLibraryPathElement.getChildren(PATH_ELEMENT, namespace)) {
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
        for (Element includeLibrariesElement : compilerElement.getChildren(INCLUDE_LIBRARIES, namespace)) {
          final Collection<Element> libraryElementsToRemove = new ArrayList<>();
          for (Element libraryElement : includeLibrariesElement.getChildren(LIBRARY, namespace)) {
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

    for (final Element otherElement : otherRootElement.getChildren()) {
      rootElement.addContent(otherElement.clone());
    }
  }

  // required to avoid setting the same option values twice because it may lead to compilation failure (e.g. if the same locale is listed twice)
  private static Collection<Element> findDuplicateElementsRecursively(final Element existingElement, final Element otherElement) {
    final Collection<Element> result = new HashSet<>();

    for (Element potentialChild : otherElement.getChildren()) {
      final List existingChildren = existingElement.getChildren(potentialChild.getName(), existingElement.getNamespace());
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
}
