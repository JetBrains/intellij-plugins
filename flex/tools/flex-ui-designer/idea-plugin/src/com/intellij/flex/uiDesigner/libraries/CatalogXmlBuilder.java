package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.libraries.Definition.ResolvedState;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.NanoXmlUtil.IXMLBuilderAdapter;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

class CatalogXmlBuilder extends IXMLBuilderAdapter {
  private static final int PROPERTIES_EXTENSION_LENGTH = LibraryManager.PROPERTIES_EXTENSION.length();
  private static final String LOCALE_PREFIX = "locale/";
  private static final int LOCALE_PREFIX_LENGTH = LOCALE_PREFIX.length();

  private boolean defProcessing;
  private boolean dependenciesProcessing;
  private boolean filesProcessing;

  private boolean collectResourceBundles;

  private Definition definition;
  private final ArrayList<String> dependencies = new ArrayList<String>();

  private String mod;
  private String dep;

  @Nullable
  private LibrarySetItem library;
  private final Map<CharSequence, Definition> definitionMap;
  private final Condition<String> skipDependency;
  private final Condition<String> skipDefinition;

  CatalogXmlBuilder(Map<CharSequence, Definition> definitionMap, Condition<String> skipDependency, Condition<String> skipDefinition) {
    this.definitionMap = definitionMap;
    this.skipDependency = skipDependency;
    this.skipDefinition = skipDefinition;
  }

  CatalogXmlBuilder(Map<CharSequence, Definition> definitionMap, Condition<String> skip) {
    this(definitionMap, skip, skip);
  }

  public void setLibrary(@NotNull LibrarySetItem item) {
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
    if (dependenciesProcessing && name.equals("script")) {
      if (dependencies.isEmpty()) {
        definition.resolved = ResolvedState.YES;
      }
      else {
        if (dependencies.size() == 1) {
          definition.dependency = dependencies.get(0);
        }
        else {
          definition.dependencies = dependencies.toArray(new String[dependencies.size()]);
        }

        dependencies.clear();
      }

      definition = null;
      dependenciesProcessing = false;
      defProcessing = false;
      dep = null;
    }
  }

  @Override
  public void addAttribute(String name, String nsPrefix, String nsURI, String value, String type) throws Exception {
    if (name.charAt(0) == 'm') {
      mod = value;
    }
    else if (dependenciesProcessing) {
      if (name.charAt(0) == 'i') {
        dep = value;
      }
      else if (name.charAt(0) == 't' && (value.charAt(0) == 'i' || value.charAt(0) == 'n') && !skipDependency.value(dep)) {
        dependencies.add(dep); 
      }
    }
    else if (defProcessing) {
      if (skipDefinition.value(value)) {
        System.out.print(value + " skipped\n");
        return;
      }

      Definition oldDefinition = definitionMap.get(value);
      long time = -1;
      if (oldDefinition != null) {
        if (library == null || (time = Long.parseLong(mod)) > oldDefinition.getTime()) {
          oldDefinition.markAsUnresolved();
        }
        else {
          return;
        }
      }

      definition = new Definition(library);
      //definition.name = value;
      if (time == -1) {
        definition.setTimeAsString(mod);
      }
      else {
        definition.time = time;
      }

      definitionMap.put(value, definition);
      if (library != null) {
        library.definitionCounter++;
      }
      dependenciesProcessing = true;
    }
    else if (filesProcessing && name.equals("path")) {
      if (value.startsWith(LOCALE_PREFIX) && value.endsWith(LibraryManager.PROPERTIES_EXTENSION)) {
        final int vlength = value.length();
        final int secondSlashPosition = StringUtil.lastIndexOf(value, '/', LOCALE_PREFIX_LENGTH + 2, vlength - PROPERTIES_EXTENSION_LENGTH - 1);
        final String locale = value.substring(LOCALE_PREFIX_LENGTH, secondSlashPosition);
        @SuppressWarnings("ConstantConditions")
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
