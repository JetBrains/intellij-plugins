package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PropertyKind;
import com.intellij.javascript.flex.FlexReferenceContributor.StateReference;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http://opensource.adobe.com/wiki/display/flexsdk/Enhanced+States+Syntax
 */
class StateWriter {
  int ITEMS_FACTORY;
  private int DESTINATION;
  private int RELATIVE_TO;
  private int POSITION;
  private int FIRST;
  private int AFTER;

  int VALUE;
  private int OVERRIDES;

  int NAME;
  int TARGET;

  private int STATIC_INSTANCE_REFERENCE_IN_DEFERRED_PARENT_INSTANCE;
  int ADD_ITEMS;

  private final ArrayList<State> states = new ArrayList<State>();
  private final Map<String, List<State>> nameToState = new THashMap<String, List<State>>();
  private BaseWriter writer;

  private SetPropertyOrStyle pendingFirstSetProperty;

  private boolean autoItemDestruction;

  private boolean namesInitialized;

  public StateWriter(BaseWriter writer) {
    this.writer = writer;
  }

  int statesSize() {
    return states.size();
  }

  private void initNames() {
    if (!namesInitialized) {
      ITEMS_FACTORY = writer.getNameReference("itemsFactory");
      DESTINATION = writer.getNameReference("destination");
      RELATIVE_TO = writer.getNameReference("relativeTo");
      POSITION = writer.getNameReference("position");
      FIRST = writer.getNameReference("first");
      AFTER = writer.getNameReference("after");

      NAME = writer.getNameReference("name");
      TARGET = writer.getNameReference("target");

      VALUE = writer.getNameReference("value");
      OVERRIDES = writer.getNameReference("overrides");

      STATIC_INSTANCE_REFERENCE_IN_DEFERRED_PARENT_INSTANCE = writer.getNameReference(
          "com.intellij.flex.uiDesigner.flex.states.StaticInstanceReferenceInDeferredParentInstance");
      ADD_ITEMS = writer.getNameReference("com.intellij.flex.uiDesigner.flex.states.AddItems");

      namesInitialized = true;
    }
  }

  public void readDeclaration(XmlTag parentTag) {
    initNames();

    XmlTag[] tags = parentTag.getSubTags();
    states.ensureCapacity(tags.length);
    for (XmlTag tag : tags) {
      State state = new State(this, states.size());
      states.add(state);
      for (XmlAttribute attribute : tag.getAttributes()) {
        if (attribute.getLocalName().equals(FlexStateElementNames.NAME)) {
          state.name = attribute.getDisplayValue();
          addNameToStateMap(state.name, state);
        }
        else if (attribute.getLocalName().equals(FlexStateElementNames.STATE_GROUPS)) {
          XmlAttributeValue valueElement = attribute.getValueElement();
          assert valueElement != null;
          for (PsiReference reference : valueElement.getReferences()) {
            addNameToStateMap(reference.getCanonicalText(), state);
          }
        }
      }
    }
  }

  private void addNameToStateMap(String name, State state) {
    List<State> states = nameToState.get(name);
    if (states == null) {
      states = new ArrayList<State>(5);
      states.add(state);
      nameToState.put(name, states);
    }
    else {
      if (states.contains(state)) {
        // IDEA-73547
        MxmlWriter.LOG.warn("State " + state + " already added to map for " + name);
      }
      else {
        states.add(state);
      }
    }
  }

  public void applyItemAutoDestruction(@Nullable Context context, Context parentContext) {
    if (context == null) {
      autoItemDestruction = true;
      return;
    }

    for (State state : states) {
      state.applyItemAutoDestruction(context, parentContext, writer);
    }
  }

  public void includeInOrExcludeFrom(XmlAttributeValue xmlAttributeValue, Context parentContext, DynamicObjectContext context, boolean excludeFrom) {
    // currently, all references in includeIn/exludeFrom attribute value are StateReference, so, we skip instanceof StateReference
    final PsiReference[] references = xmlAttributeValue.getReferences();
    assert context.includeInStates.isEmpty();
    if (excludeFrom) {
      context.includeInStates.addAll(states);
      for (PsiReference reference : references) {
        context.includeInStates.removeAll(nameToState.get(reference.getCanonicalText()));
      }
    }
    else {
      for (PsiReference reference : references) {
        context.includeInStates.addAll(nameToState.get(reference.getCanonicalText()));
      }
    }

    includeIn(parentContext, context, context.includeInStates);

    autoItemDestruction = false;
    pendingFirstSetProperty = null;
  }

  private void includeIn(Context parentContext, DynamicObjectContext context, List<State> includedStates) {
    for (State state : includedStates) {
      // lazy reset state.activeAddItems
      AddItems override = state.getValidActiveAddItems(parentContext, autoItemDestruction);
      if (override != null) {
        override.getItemDeferredInstances().add(context);
      }
      else {
        state.addAddItems(createAddItems(context, parentContext, autoItemDestruction), parentContext, pendingFirstSetProperty);
      }
    }
  }

  AddItems createAddItems(DynamicObjectContext context, Context parentContext, boolean autoDestruction) {
    AddItems override = new AddItems(writer.getBlockOut().startRange(), context, autoDestruction);

    DynamicObjectContext parentScopeOwner = parentContext.getScope().getOwner();
    if (parentScopeOwner != null && parentScopeOwner != parentContext) {
      writeDeferredInstanceFromObjectReference(DESTINATION, parentContext, parentContext);
    }
    else {
      writer.property(DESTINATION).objectReference(parentContext);
    }

    final int position;
    if (parentContext.getBackSibling() == null) {
      position = FIRST;
    }
    else {
      position = AFTER;
      if (parentContext.ownerIsDynamic()) {
        writeDeferredInstanceFromObjectReference(RELATIVE_TO, parentContext.getBackSibling(), parentContext);
      }
      else {
        writer.property(RELATIVE_TO).objectReference(parentContext.getBackSibling());
      }
    }

    writer.property(POSITION).stringReference(position);
    
    writer.getBlockOut().endRange(override.dataRange);
    return override;
  }

  public boolean checkStateSpecificPropertyValue(MxmlWriter mxmlWriter, PropertyProcessor propertyProcessor, XmlElement element,
                                                 XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor,
                                                 @Nullable Context context, @NotNull MxmlObjectReferenceProvider objectReferenceProvider) {
    PsiReference[] references = element.getReferences();
    if (references.length < 2) {
      return false;
    }

    List<State> states = null;
    for (int i = references.length - 1; i > -1; i--) {
      PsiReference psiReference = references[i];
      if (psiReference instanceof StateReference) {
        // resolve is expensive for StateReference, so, we use string key (states name) instead of object key (states tag)
        states = nameToState.get(psiReference.getCanonicalText());
        break;
      }
    }

    if (states == null) {
      return false;
    }

    final List<State> filteredStates;
    if (context instanceof DynamicObjectContext) {
      final ArrayList<State> includeInStates = ((DynamicObjectContext)context).includeInStates;
      filteredStates = new ArrayList<State>(states.size());
      for (State state : states) {
        if (includeInStates.contains(state)) {
          filteredStates.add(state);
        }
        else {
          MxmlWriter.LOG.warn("Skip " + element.getText() + " from " + state.name + " " +
                              "due to element parent object included only in " + includeInStates);
        }
      }

      if (filteredStates.isEmpty()) {
        return true;
      }
    }
    else {
      filteredStates = states;
    }

    ValueWriter valueWriter = null;
    try {
      valueWriter = propertyProcessor.process(element, valueProvider, descriptor, objectReferenceProvider);
    }
    catch (InvalidPropertyException ignored) {

    }
    if (valueWriter == null) {
      // binding is not yet supported for state specific
      return true;
    }

    final PrimitiveAmfOutputStream out = writer.getOut();
    SetPropertyOrStyle override = new SetPropertyOrStyle(writer.getBlockOut().startRange());
    writer.classOrPropertyName(propertyProcessor.isStyle()
                               ? "com.intellij.flex.uiDesigner.flex.states.SetStyle"
                               : "com.intellij.flex.uiDesigner.flex.states.SetProperty");
    writer.property(NAME).stringReference(propertyProcessor.getName());

    writer.property(VALUE);
    PropertyKind propertyKind;
    try {
      propertyKind = valueWriter.write(descriptor, valueProvider, out, writer, false, context);
    }
    catch (InvalidPropertyException invalidProperty) {
      // todo handle invalidProperty for state
      throw new UnsupportedOperationException("");
    }

    if (propertyKind.isComplex()) {
      assert context != null;
      mxmlWriter.processPropertyTagValue((XmlTag)element, context, propertyKind);
    }

    override.targetId = writer.getObjectOrFactoryId(context);
    if (pendingFirstSetProperty == null && context == null) {
      pendingFirstSetProperty = override;
    }

    writer.getBlockOut().endRange(override.dataRange);

    for (State state : filteredStates) {
      state.overrides.add(override);
    }

    return true;
  }

  public StaticObjectContext createContextForStaticBackSibling(boolean allowIncludeInExludeFrom, int referencePosition,
                                                               @Nullable Context parentContext, @Nullable MxmlObjectReference mxmlObjectReference) {
    assert referencePosition != -1;
    if (allowIncludeInExludeFrom) {
      assert parentContext != null;
      int backSiblingId = (writer.isIdPreallocated() && !parentContext.ownerIsDynamic()) ? writer.getPreallocatedId() : -1;
      // reset due to new backsibling
      resetActiveAddItems(parentContext.activeAddItems);
      if (parentContext.getBackSibling() == null) {
        parentContext.setBackSibling(new StaticObjectContext(referencePosition, writer.getOut(), backSiblingId,
                                                             parentContext.getScope()));
      }
      else {
        parentContext.getBackSibling().reinitialize(referencePosition, backSiblingId);
        resetActiveAddItems(parentContext.getBackSibling().activeAddItems);
      }
      return parentContext.getBackSibling();
    }
    else {
      return writer.createStaticContext(parentContext, referencePosition);
    }
  }

  public void finalizeStateSpecificAttributesForStaticContext(@NotNull StaticObjectContext context, @Nullable Context parentContext,
                                                              MxmlWriter mxmlWriter) {
    // 1
    if (!writer.isIdPreallocated()) {
      assert pendingFirstSetProperty == null;
      return;
    }

    if (parentContext == null || !parentContext.ownerIsDynamic()) {
      pendingFirstSetProperty = null;
      return;
    }

    final int objectInstance = parentContext.getScope().referenceCounter++;
    final int deferredParentInstance = parentContext.getScope().getOwner().getOrAllocateId();
    final int referenceInstance = writer.getPreallocatedId();

    // 2
    if (pendingFirstSetProperty != null) {
      ByteRange byteRange = writer.getBlockOut().startRange();
      writeDeferredInstanceFromObjectReference(TARGET, objectInstance, deferredParentInstance, referenceInstance);
      writer.getBlockOut().endRange(byteRange);

      assert pendingFirstSetProperty.targetId == referenceInstance;
      pendingFirstSetProperty.setTargetRange(byteRange);
      pendingFirstSetProperty = null;
    }
    else {
      // 3
      StaticInstanceReferenceInDeferredParentInstance staticInstanceReferenceInDeferredParentInstance =
        new StaticInstanceReferenceInDeferredParentInstance(objectInstance, deferredParentInstance);
      mxmlWriter.setDeferredReferenceForObjectWithExplicitIdOrBinding(staticInstanceReferenceInDeferredParentInstance,
                                                                            referenceInstance);

      context.setStaticInstanceReferenceInDeferredParentInstance(staticInstanceReferenceInDeferredParentInstance);
    }

    context.setId(objectInstance);
    context.referenceInitialized();
    context.setId(referenceInstance);
  }

  private static void resetActiveAddItems(AddItems[] activeAddItems) {
    if (activeAddItems != null) {
      for (int i = 0; i < activeAddItems.length; i++) {
        activeAddItems[i] = null;
      }
    }
  }

  private void writeDeferredInstanceFromObjectReference(int propertyName, Context context, Context parentContext) {
    int referenceInstanceReference = context.getId();
    if (referenceInstanceReference == -1) {
      int objectInstanceReference = parentContext.getScope().referenceCounter++;
      context.setId(objectInstanceReference);
      context.referenceInitialized();
      referenceInstanceReference = writer.getRootScope().referenceCounter++;
      context.setId(referenceInstanceReference);

      writeDeferredInstanceFromObjectReference(propertyName, objectInstanceReference,
                                               parentContext.getScope().getOwner().getOrAllocateId(), referenceInstanceReference);
    }
    else {
      StaticInstanceReferenceInDeferredParentInstance referenceInDeferredParentInstance = context.getStaticInstanceReferenceInDeferredParentInstance();
      if (referenceInDeferredParentInstance == null) {
        writer.property(propertyName).objectReference(referenceInstanceReference);
      }
      else {
        writeDeferredInstanceFromObjectReference(propertyName, referenceInDeferredParentInstance.getObjectInstance(),
                                                 referenceInDeferredParentInstance.getDeferredParentInstance(), referenceInstanceReference);
        referenceInDeferredParentInstance.markAsWritten();
        context.setStaticInstanceReferenceInDeferredParentInstance(null);
      }
    }
  }

  private void writeDeferredInstanceFromObjectReference(int propertyName, int objectInstanceReference, int deferredParentInstance,
                                                        int referenceInstanceReference) {
    writer.property(propertyName).referableHeader(referenceInstanceReference).objectHeader(
      STATIC_INSTANCE_REFERENCE_IN_DEFERRED_PARENT_INSTANCE);

    writer.property("reference").getOut().writeAmfInt(objectInstanceReference);
    writer.property("deferredParentInstance").objectReference(deferredParentInstance);
    
    writer.endObject();
  }

  void writeDeferredInstance(DynamicObjectContext instance) {
    PrimitiveAmfOutputStream out = writer.getOut();
    if (instance.isWritten()) {
      writeDeferredInstanceKind(AmfExtendedTypes.OBJECT_REFERENCE, instance);
      out.writeUInt29(instance.id);
    }
    else {
      // IDEA-72004
      if (instance.overrideUserCount > 0) {
        instance.getOrAllocateId();
      }

      if (instance.id == -1) {
        writeDeferredInstanceKind(ObjectMetadata.NEVER_REFERRED, instance);
      }
      else {
        writeDeferredInstanceKind(ObjectMetadata.REFERRED, instance);
        instance.markAsWritten();
      }

      final int referredObjectsCount = instance.getReferredObjectsCount();
      out.writeUInt29(writer.getBlockOut().getDataRangeOwnLength(instance.getDataRange()) + IOUtil.uint29SizeOf(referredObjectsCount));
      out.writeUInt29(referredObjectsCount);
      writer.getBlockOut().addMarker(new ByteRangeMarker(out.size(), instance.getDataRange()));

      if (instance.id != -1) {
        out.writeUInt29(instance.id);
      }
    }
  }

  private void writeDeferredInstanceKind(int kind, DynamicObjectContext instance) {
    writer.getOut().write((kind << 1) | (instance.isImmediateCreation() ? 1 : 0));
  }

  public void write() {
    final PrimitiveAmfOutputStream out = writer.getOut();
    out.write(states.size());
    if (states.isEmpty()) {
      return;
    }

    for (State state : states) {
      writer.property(NAME).string(state.name);

      if (!state.overrides.isEmpty()) {
        writer.property(OVERRIDES).arrayHeader(state.overrides.size());
        for (OverrideBase override : state.overrides) {
          override.write(writer, this);
        }
      }

      // object State footer
      writer.endObject();
    }

    reset();
  }

  public void reset() {
    states.clear();
    nameToState.clear();
    namesInitialized = false;
    pendingFirstSetProperty = null;
  }
}