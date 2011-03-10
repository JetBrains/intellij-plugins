package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AddItems extends OverrideBase {
  private final List<DynamicObjectContext> itemDeferredInstances;
  private boolean autoDestruction;

  AddItems(ByteRange dataRange, DynamicObjectContext itemDeferredInstance, boolean autoDestruction) {
    super(dataRange);
    this.autoDestruction = autoDestruction;
    itemDeferredInstances = new ArrayList<DynamicObjectContext>();
    itemDeferredInstances.add(itemDeferredInstance);
  }

  @Override
  void write(BaseWriter writer, StateWriter stateWriter) {
    writer.addMarker(dataRange);

    if (autoDestruction) {
      writer.writeStringReference("destructionPolicy", "auto");
      
      writer.writeObjectHeader(stateWriter.ITEMS_FACTORY, "com.intellij.flex.uiDesigner.flex.states.TransientArrayOfDeferredInstanceFromBytes");
      writer.write("deferredInstances");
      writer.getOut().write(PropertyClassifier.VECTOR_OF_DEFERRED_INSTANCE_FROM_BYTES);
    }
    else {
      writer.writeObjectHeader(stateWriter.ITEMS_FACTORY, "com.intellij.flex.uiDesigner.flex.states.PermanentArrayOfDeferredInstanceFromBytes");
      writer.getOut().writeUInt29(writer.ARRAY);
      writer.getOut().write(PropertyClassifier.ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES);
    }
    
    writer.getOut().write(itemDeferredInstances.size());
    for (DynamicObjectContext itemDeferredInstance : itemDeferredInstances) {
      stateWriter.writeDeferredInstance(itemDeferredInstance);
    }
    writer.getOut().write(MxmlWriter.EMPTY_CLASS_OR_PROPERTY_NAME);
  }

  public List<DynamicObjectContext> getItemDeferredInstances() {
    return itemDeferredInstances;
  }

  public boolean isAutoDestruction() {
    return autoDestruction;
  }

  public void setAutoDestruction(boolean autoDestruction) {
    this.autoDestruction = autoDestruction;
  }
}
