package com.intellij.lang.javascript.flex.debug;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.util.Iconable;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

public class NodeClassInfo {

  public final boolean isDynamic;
  public final Map<String, Icon> myOwnStaticFields;
  public final Map<String, Icon> myOwnStaticProperties;
  public final Map<String, Icon> myOwnFields;
  public final Map<String, Icon> myOwnProperties;
  public final Map<String, Icon> myInheritedStaticFields;
  public final Map<String, Icon> myInheritedStaticProperties;
  public final Map<String, Icon> myInheritedFields;
  public final Map<String, Icon> myInheritedProperties;

  public NodeClassInfo(final boolean dynamic,
                       final Map<String, Icon> ownStaticFields,
                       final Map<String, Icon> ownStaticProperties,
                       final Map<String, Icon> ownFields,
                       final Map<String, Icon> ownProperties,
                       final Map<String, Icon> inheritedStaticFields,
                       final Map<String, Icon> inheritedStaticProperties,
                       final Map<String, Icon> inheritedFields,
                       final Map<String, Icon> inheritedProperties) {
    isDynamic = dynamic;
    this.myOwnStaticFields = ownStaticFields;
    this.myOwnStaticProperties = ownStaticProperties;
    this.myOwnFields = ownFields;
    this.myOwnProperties = ownProperties;
    this.myInheritedStaticFields = inheritedStaticFields;
    this.myInheritedStaticProperties = inheritedStaticProperties;
    this.myInheritedFields = inheritedFields;
    this.myInheritedProperties = inheritedProperties;
  }

  static NodeClassInfo getNodeClassInfo(final @NotNull JSClass jsClass) {
    final JSAttributeList classAttributes = jsClass.getAttributeList();
    final boolean dynamic = classAttributes != null && classAttributes.hasModifier(JSAttributeList.ModifierType.DYNAMIC);

    final Map<String, Icon> ownStaticFields = new THashMap<String, Icon>();
    final Map<String, Icon> ownStaticProperties = new THashMap<String, Icon>();
    final Map<String, Icon> ownFields = new THashMap<String, Icon>();
    final Map<String, Icon> ownProperties = new THashMap<String, Icon>();
    final Map<String, Icon> inheritedStaticFields = new THashMap<String, Icon>();
    final Map<String, Icon> inheritedStaticProperties = new THashMap<String, Icon>();
    final Map<String, Icon> inheritedFields = new THashMap<String, Icon>();
    final Map<String, Icon> inheritedProperties = new THashMap<String, Icon>();

    fillMapsForClass(jsClass, ownStaticFields, ownStaticProperties, ownFields, ownProperties);
    fillMapsForSupersRecursively(jsClass, new THashSet<JSClass>(), inheritedStaticFields, inheritedStaticProperties, inheritedFields,
                                 inheritedProperties);

    return new NodeClassInfo(dynamic, ownStaticFields, ownStaticProperties, ownFields, ownProperties, inheritedStaticFields,
                             inheritedStaticProperties, inheritedFields, inheritedProperties);
  }

  private static void fillMapsForSupersRecursively(final JSClass jsClass,
                                                   final THashSet<JSClass> visited,
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
    for (final JSVariable variable : jsClass.getFields()) {
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
  }
}
