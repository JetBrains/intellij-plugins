package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionProcessor;
import com.intellij.conversion.ProjectLibrariesSettings;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.util.Function;
import com.intellij.util.containers.hash.HashSet;
import org.jdom.Element;

import java.util.Set;

class FlexLibrariesConverter extends ConversionProcessor<ProjectLibrariesSettings> {
  private final ConversionParams myParams;

  public FlexLibrariesConverter(final ConversionParams params) {
    myParams = params;
  }

  public boolean isConversionNeeded(final ProjectLibrariesSettings projectLibrariesSettings) {
    return true;
  }

  public void process(final ProjectLibrariesSettings projectLibrariesSettings) throws CannotConvertException {
  }

  public void preProcess(final ProjectLibrariesSettings projectLibrariesSettings) throws CannotConvertException {
    Set<String> librariesNames = new HashSet<String>();
    for (Element element : projectLibrariesSettings.getProjectLibraries()) {
      if (!FlexModuleConverter.isApplicableLibrary(element)) {
        // ignore non-flex project library
        continue;
      }
      librariesNames.add(LIB_NAME_MAPPER.fun(element));
    }
    myParams.setProjectLibrariesNames(librariesNames);
  }

  public void postProcess(final ProjectLibrariesSettings projectLibrariesSettings) throws CannotConvertException {
    final Set<String> librariesToMakeFlex = myParams.getProjectLibrariesToMakeFlex();
    for (Element libraryElement : projectLibrariesSettings.getProjectLibraries()) {
      if (librariesToMakeFlex.contains(LIB_NAME_MAPPER.fun(libraryElement))) {
        libraryElement.setAttribute(LibraryImpl.LIBRARY_TYPE_ATTR, FlexLibraryType.FLEX_LIBRARY.getKindId());
      }
    }
  }

  private static final Function<Element, String> LIB_NAME_MAPPER = new Function<Element, String>() {
    public String fun(final Element element) {
      return element.getAttributeValue(LibraryImpl.LIBRARY_NAME_ATTR);
    }
  };
}
