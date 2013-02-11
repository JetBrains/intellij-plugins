package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.*;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.openapi.roots.impl.OrderEntryFactory;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.Function;
import com.intellij.util.containers.hash.HashSet;
import org.jdom.Element;

import java.io.File;
import java.util.Collection;
import java.util.Set;

class FlexLibrariesConverter extends ConversionProcessor<ProjectLibrariesSettings> {
  private final ConversionContext myContext;
  private final ConversionParams myParams;

  public FlexLibrariesConverter(final ConversionContext context, final ConversionParams params) {
    myContext = context;
    myParams = params;
  }

  public boolean isConversionNeeded(final ProjectLibrariesSettings projectLibrariesSettings) {
    Collection<String> projectLibrariesNames = getProjectLibrariesNames(projectLibrariesSettings);
    try {
      for (File moduleFile : myContext.getModuleFiles()) {
        ModuleSettings moduleSettings;
        if (!moduleFile.exists() ||
            !FlexModuleConverter.isConversionNeededStatic(moduleSettings = myContext.getModuleSettings(moduleFile))) {
          continue;
        }

        for (Element orderEntry : moduleSettings.getOrderEntries()) {
          String orderEntryType = orderEntry.getAttributeValue(OrderEntryFactory.ORDER_ENTRY_TYPE_ATTR);
          if ("library".equals(orderEntryType)) {
            String libraryName = orderEntry.getAttributeValue("name");
            String libraryLevel = orderEntry.getAttributeValue("level");
            if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryLevel) && projectLibrariesNames.contains(libraryName)) {
              return true;
            }
          }
        }
      }
      return false;
    }
    catch (CannotConvertException e) {
      return false;
    }
  }

  public void process(final ProjectLibrariesSettings projectLibrariesSettings) throws CannotConvertException {
  }

  public void preProcess(final ProjectLibrariesSettings projectLibrariesSettings) throws CannotConvertException {
    myParams.setProjectLibrariesNames(getProjectLibrariesNames(projectLibrariesSettings));
  }

  private Collection<String> getProjectLibrariesNames(final ProjectLibrariesSettings projectLibrariesSettings) {
    Set<String> librariesNames = new HashSet<String>();
    for (Element element : projectLibrariesSettings.getProjectLibraries()) {
      if (!FlexModuleConverter.isApplicableLibrary(element, new Function<String, String>() {
        @Override
        public String fun(final String s) {
          return myParams.expandPath(s);
        }
      })) {
        // ignore non-flex project library
        continue;
      }
      librariesNames.add(LIB_NAME_MAPPER.fun(element));
    }
    return librariesNames;
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
