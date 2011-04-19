package com.intellij.flex.uiDesigner.libraries;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

class CatalogXmlBuilder implements XmlBuilder {
  private boolean processDependencies;

  private Definition definition;
  private final ArrayList<CharSequence> dependencies = new ArrayList<CharSequence>();
  private CharSequence mod;

  private OriginalLibrary library;
  private final Map<CharSequence, Definition> definitionMap;

  public CatalogXmlBuilder(final Map<CharSequence, Definition> definitionMap) {
    this.definitionMap = definitionMap;
  }

  public void setLibrary(OriginalLibrary library) {
    this.library = library;
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
          !(StringUtil.startsWith(value, "flash.") ||
            value.charAt(0) == '_' ||
            !StringUtil.contains(value, 1, value.length() - 1, ':'))) {
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
          if (definition.time > oldDefinition.getTime()) {
            oldDefinition.getLibrary().definitionCounter--;
            oldDefinition.getLibrary().unresolvedDefinitions.add(value);
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
      else if (StringUtil.endsWith(value, "_mx_core_FlexModuleFactory")) {
        library.mxCoreFlexModuleFactoryClassName = value;
      }
      else {
        definition = null;
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
