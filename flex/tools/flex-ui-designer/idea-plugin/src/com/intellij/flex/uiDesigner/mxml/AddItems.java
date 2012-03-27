package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;

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
    itemDeferredInstance.overrideUserCount++;
  }

  @Override
  void write(BaseWriter writer, StateWriter stateWriter) {
    writer.objectHeader(stateWriter.ADD_ITEMS);
    writer.getBlockOut().append(dataRange);

    if (autoDestruction) {
      writer.classOrPropertyName("destructionPolicy");
      writer.stringReference("auto");

      writer.property(stateWriter.ITEMS_FACTORY).typeMarker(AmfExtendedTypes.TRANSIENT_ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES);
    }
    else {
      writer.property(stateWriter.ITEMS_FACTORY).typeMarker(AmfExtendedTypes.PERMANENT_ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES);
    }

    writer.getOut().write(itemDeferredInstances.size());
    for (DynamicObjectContext itemDeferredInstance : itemDeferredInstances) {
      stateWriter.writeDeferredInstance(itemDeferredInstance);
    }

    writer.endObject();
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
