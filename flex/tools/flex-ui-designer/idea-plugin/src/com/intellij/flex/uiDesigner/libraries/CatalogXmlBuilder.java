package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.libraries.Definition.ResolvedState;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.NanoXmlUtil.IXMLBuilderAdapter;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Map;

class CatalogXmlBuilder extends IXMLBuilderAdapter {
  static final String PROPERTIES_EXTENSION = ".properties";
  private static final int PROPERTIES_EXTENSION_LENGTH = PROPERTIES_EXTENSION.length();
  private static final String LOCALE_PREFIX = "locale/";
  private static final int LOCALE_PREFIX_LENGTH = LOCALE_PREFIX.length();

  private boolean defProcessing;
  private boolean processDependencies;
  private boolean filesProcessing;
  private boolean collectResourceBundles;

  private Definition definition;
  private final ArrayList<String> dependencies = new ArrayList<String>();
  private String mod;

  private LibrarySetItem library;
  private final Map<CharSequence, Definition> definitionMap;
  private final Condition<String> isExternal;

  public CatalogXmlBuilder(Map<CharSequence, Definition> definitionMap, Condition<String> isExternal) {
    this.definitionMap = definitionMap;
    this.isExternal = isExternal;
  }

  public void setLibrary(LibrarySetItem item) {
    library = item;
    collectResourceBundles = item.library.resourceBundles.isEmpty();
  }

  @Override
  public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
    if (name.equals("def")) {
      defProcessing = true;
      definition = null;
    }
    else {
      defProcessing = false;
      if (collectResourceBundles && name.equals("file")) {
        filesProcessing = true;
      }
    }
  }

  @Override
  public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
    if (processDependencies && name.equals("script")) {
      if (dependencies.isEmpty()) {
        definition.resolved = ResolvedState.YES;
      }
      else {
        definition.dependencies = dependencies.toArray(new String[dependencies.size()]);
        dependencies.clear();
      }
      definition = null;
      processDependencies = false;
      defProcessing = false;
    }
  }

  @Override
  public void addAttribute(String name, String nsPrefix, String nsURI, String value, String type) throws Exception {
    if (name.charAt(0) == 'm') {
      mod = value;
    }
    else if (processDependencies) {
      if (name.charAt(0) == 'i' && !isExternal.value(value)) {
        dependencies.add(value);
      }
    }
    else if (defProcessing) {
      Definition oldDefinition = definitionMap.get(value);
      long time = -1;
      if (oldDefinition != null) {
        time = Long.parseLong(mod);
        if (time > oldDefinition.getTime()) {
          oldDefinition.markAsUnresolved();
        }
        else {
          return;
        }
      }

      definition = new Definition(library);
      definition.name = value;
      if (time == -1) {
        definition.setTimeAsString(mod);
      }
      else {
        definition.time = time;
      }

      definitionMap.put(value, definition);
      library.definitionCounter++;
      processDependencies = true;
    }
    else if (filesProcessing && name.equals("path")) {
      if (value.startsWith(LOCALE_PREFIX) && value.endsWith(PROPERTIES_EXTENSION)) {
        final int vlength = value.length();
        final int secondSlashPosition = StringUtil.lastIndexOf(value, '/', LOCALE_PREFIX_LENGTH + 2, vlength - PROPERTIES_EXTENSION_LENGTH - 1);
        final String locale = value.substring(LOCALE_PREFIX_LENGTH, secondSlashPosition);
        THashSet<String> bundles = library.library.resourceBundles.get(locale);
        if (bundles == null) {
          bundles = new THashSet<String>();
          library.library.resourceBundles.put(locale, bundles);
        }
        bundles.add(value.substring(secondSlashPosition + 1, vlength - PROPERTIES_EXTENSION_LENGTH));
      }
    }
  }
}
