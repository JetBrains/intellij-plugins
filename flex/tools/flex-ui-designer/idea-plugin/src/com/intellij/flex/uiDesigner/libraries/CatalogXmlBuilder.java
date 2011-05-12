package com.intellij.flex.uiDesigner.libraries;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilder;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

class CatalogXmlBuilder implements XmlBuilder {
  static final String PROPERTIES_EXTENSION = ".properties";
  private static final int PROPERTIES_EXTENSION_LENGTH = PROPERTIES_EXTENSION.length();
  private static final String LOCALE_PREFIX = "locale/";
  private static final int LOCALE_PREFIX_LENGTH = LOCALE_PREFIX.length();

  private boolean processDependencies;
  private boolean processFiles;
  private boolean collectResourceBundles;

  private Definition definition;
  private final ArrayList<CharSequence> dependencies = new ArrayList<CharSequence>();
  private CharSequence mod;

  private LibrarySetItem library;
  private final Map<CharSequence, Definition> definitionMap;

  public CatalogXmlBuilder(final Map<CharSequence, Definition> definitionMap) {
    this.definitionMap = definitionMap;
  }

  public void setLibrary(LibrarySetItem item) {
    this.library = item;
    collectResourceBundles = item.library.resourceBundles.isEmpty();
  }

  @Override
  public void doctype(@Nullable CharSequence publicId, @Nullable CharSequence systemId, int startOffset, int endOffset) {
  }

  @Override
  public ProcessingOrder startTag(CharSequence localName, String namespace, int startoffset, int endoffset, int headerEndOffset) {
    if (localName.equals("script")) {
      return ProcessingOrder.TAGS_AND_ATTRIBUTES;
    }
    else if (localName.equals("def")) {
      definition = new Definition(library);
      return ProcessingOrder.TAGS_AND_ATTRIBUTES;
    }
    else if (definition != null) {
      return ProcessingOrder.TAGS_AND_ATTRIBUTES;
    }
    else if (collectResourceBundles && localName.equals("file")) {
      processFiles = true;
      return ProcessingOrder.TAGS_AND_ATTRIBUTES;
    }
    else {
      return ProcessingOrder.TAGS;
    }
  }

  @Override
  public void endTag(CharSequence localName, String namespace, int startoffset, int endoffset) {
    if (processDependencies && localName.equals("script")) {
      if (!dependencies.isEmpty()) {
        definition.dependencies = dependencies.toArray(new CharSequence[dependencies.size()]);
        dependencies.clear();
      }
      else {
        definition.hasUnresolvedDependencies = Definition.UnresolvedState.NO;
      }
      definition = null;
      processDependencies = false;
    }
  }

  @Override
  public void attribute(CharSequence name, CharSequence value, int startoffset, int endoffset) {
    if (name.equals("mod")) {
      mod = value;
    }
    else if (processDependencies) {
      if (name.equals("id") &&
          !(StringUtil.startsWith(value, "flash.") || value.charAt(0) == '_' || !StringUtil.contains(value, 1, value.length() - 1, ':'))) {
        dependencies.add(value);
      }
    }
    else if (definition != null) {
      if (value.charAt(0) != '_') {
        Definition oldDefinition = definitionMap.get(value);
        if (oldDefinition == null) {
          definition.setTimeAsCharSequence(mod);
        }
        else {
          definition.time = Long.parseLong(mod.toString());
          // todo see http://confluence.jetbrains.net/display/IDEA/Topological+sort+External+Dependencies+and+filter+unresolved+definitions last note
          //if (definition.time > oldDefinition.getTime()) {
          //noinspection ConstantIfStatement
          if (false) {
            oldDefinition.markAsUnresolved(value);
          }
          else {
            definition = null;
            // filter library, remove definition abc bytecode from lib swf
            library.unresolvedDefinitions.add(value);
            return;
          }
        }

        definitionMap.put(value, definition);
        library.definitionCounter++;
        processDependencies = true;
      }
      else {
        definition = null;
      }
    }
    else if (processFiles && name.equals("path")) {
      if (StringUtil.startsWith(value, LOCALE_PREFIX) && StringUtil.endsWith(value, PROPERTIES_EXTENSION)) {
        final int vlength = value.length();
        final int secondSlashPosition = StringUtil.lastIndexOf(value, '/', LOCALE_PREFIX_LENGTH + 2, vlength - PROPERTIES_EXTENSION_LENGTH - 1);
        final String locale = value.subSequence(LOCALE_PREFIX_LENGTH, secondSlashPosition).toString();
        THashSet<String> bundles = library.library.resourceBundles.get(locale);
        if (bundles == null) {
          bundles = new THashSet<String>();
          library.library.resourceBundles.put(locale, bundles);
        }
        bundles.add(value.subSequence(secondSlashPosition + 1, vlength - PROPERTIES_EXTENSION_LENGTH).toString());
      }
    }
  }

  @Override
  public void textElement(CharSequence display, CharSequence physical, int startoffset, int endoffset) {
  }

  @Override
  public void entityRef(CharSequence ref, int startOffset, int endOffset) {
  }

  @Override
  public void error(String message, int startOffset, int endOffset) {
  }
}
