package com.intellij.lang.javascript.flex.debug;

import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class NodeClassInfo {

  public final String myFqn;
  public final boolean myIsDynamic;
  public final Map<String, Icon> myOwnStaticFields;
  public final Map<String, Icon> myOwnStaticProperties;
  public final Map<String, Icon> myOwnFields;
  public final Map<String, Icon> myOwnProperties;
  public final Map<String, Icon> myInheritedStaticFields;
  public final Map<String, Icon> myInheritedStaticProperties;
  public final Map<String, Icon> myInheritedFields;
  public final Map<String, Icon> myInheritedProperties;

  public NodeClassInfo(final String fqn,
                       final boolean dynamic,
                       final Map<String, Icon> ownStaticFields,
                       final Map<String, Icon> ownStaticProperties,
                       final Map<String, Icon> ownFields,
                       final Map<String, Icon> ownProperties,
                       final Map<String, Icon> inheritedStaticFields,
                       final Map<String, Icon> inheritedStaticProperties,
                       final Map<String, Icon> inheritedFields,
                       final Map<String, Icon> inheritedProperties) {
    myFqn = fqn;
    myIsDynamic = dynamic;
    myOwnStaticFields = ownStaticFields;
    myOwnStaticProperties = ownStaticProperties;
    myOwnFields = ownFields;
    myOwnProperties = ownProperties;
    myInheritedStaticFields = inheritedStaticFields;
    myInheritedStaticProperties = inheritedStaticProperties;
    myInheritedFields = inheritedFields;
    myInheritedProperties = inheritedProperties;
  }

  static NodeClassInfo getNodeClassInfo(final @NotNull JSClass jsClass) {
    final JSAttributeList classAttributes = jsClass.getAttributeList();
    final boolean dynamic = classAttributes != null && classAttributes.hasModifier(JSAttributeList.ModifierType.DYNAMIC);

    final Map<String, Icon> ownStaticFields = new HashMap<>();
    final Map<String, Icon> ownStaticProperties = new HashMap<>();
    final Map<String, Icon> ownFields = new HashMap<>();
    final Map<String, Icon> ownProperties = new HashMap<>();
    final Map<String, Icon> inheritedStaticFields = new HashMap<>();
    final Map<String, Icon> inheritedStaticProperties = new HashMap<>();
    final Map<String, Icon> inheritedFields = new HashMap<>();
    final Map<String, Icon> inheritedProperties = new HashMap<>();

    fillMapsForClass(jsClass, ownStaticFields, ownStaticProperties, ownFields, ownProperties);
    fillMapsForSupersRecursively(jsClass, new HashSet<>(), inheritedStaticFields, inheritedStaticProperties, inheritedFields,
                                 inheritedProperties);

    return new NodeClassInfo(normalizeIfVector(jsClass.getQualifiedName()), dynamic, ownStaticFields, ownStaticProperties, ownFields,
                             ownProperties, inheritedStaticFields, inheritedStaticProperties, inheritedFields, inheritedProperties);
  }

  private static String normalizeIfVector(final String qName) {
    return qName.startsWith("Vector$") ? "Vector" : qName;
  }

  private static void fillMapsForSupersRecursively(final JSClass jsClass,
                                                   final HashSet<? super JSClass> visited,
                                                   final Map<String, Icon> inheritedStaticFields,
                                                   final Map<String, Icon> inheritedStaticProperties,
                                                   final Map<String, Icon> inheritedFields,
                                                   final Map<String, Icon> inheritedProperties) {
    if (visited.contains(jsClass)) {
      return;
    }
    visited.add(jsClass);

    for (final JSClass superClass : jsClass.getSuperClasses()) {
      fillMapsForClass(superClass, inheritedStaticFields, inheritedStaticProperties, inheritedFields, inheritedProperties);
      fillMapsForSupersRecursively(superClass, visited, inheritedStaticFields, inheritedStaticProperties, inheritedFields,
                                   inheritedProperties);
    }
  }

  private static void fillMapsForClass(final JSClass jsClass,
                                       final Map<String, Icon> staticFields,
                                       final Map<String, Icon> staticProperties,
                                       final Map<String, Icon> fields,
                                       final Map<String, Icon> properties) {
    for (final JSField variable : jsClass.getFields()) {
      final JSAttributeList varAttributes = variable.getAttributeList();
      if (varAttributes != null && varAttributes.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        staticFields.put(variable.getName(), variable.getIcon(Iconable.ICON_FLAG_VISIBILITY));
      }
      else {
        fields.put(variable.getName(), variable.getIcon(Iconable.ICON_FLAG_VISIBILITY));
      }
    }

    for (final JSFunction function : jsClass.getFunctions()) {
      if (function.getKind() == JSFunction.FunctionKind.GETTER && function.getName() != null) {
        final JSAttributeList functionAttributes = function.getAttributeList();
        if (functionAttributes != null && functionAttributes.hasModifier(JSAttributeList.ModifierType.STATIC)) {
          staticProperties.put(function.getName(), function.getIcon(Iconable.ICON_FLAG_VISIBILITY));
        }
        else {
          properties.put(function.getName(), function.getIcon(Iconable.ICON_FLAG_VISIBILITY));
        }
      }
    }

    if (jsClass instanceof MxmlJSClass) {
      final PsiFile file = jsClass.getContainingFile();
      final XmlFile xmlFile = file instanceof XmlFile ? (XmlFile)file : null;
      final XmlTag rootTag = xmlFile == null ? null : xmlFile.getRootTag();
      if (rootTag != null) {
        processSubtagsRecursively(rootTag, tag -> {
          final String id = tag.getAttributeValue("id");
          if (id != null) {
            fields.put(id, tag.getIcon(Iconable.ICON_FLAG_VISIBILITY));
          }
          return !MxmlJSClass.isTagThatAllowsAnyXmlContent(tag);
        });
      }
    }
  }

  public static void processSubtagsRecursively(final XmlTag tag, final Processor<? super XmlTag> processor) {
    for (XmlTag subTag : tag.getSubTags()) {
      if (processor.process(subTag)) {
        processSubtagsRecursively(subTag, processor);
      }
    }
  }
}
