package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

class MxmlObjectReference implements ValueReference {
  final int id;
  StaticInstanceReferenceInDeferredParentInstance staticReferenceInDeferredParentInstance;

  public MxmlObjectReference(int id) {
    this.id = id;
  }

  @Override
  public void write(PrimitiveAmfOutputStream out, BaseWriter writer, ValueReferenceResolver valueReferenceResolver) {
    if (staticReferenceInDeferredParentInstance == null || staticReferenceInDeferredParentInstance.isWritten()) {
      out.writeUInt29(id << 1);
    }
    else {
      out.writeUInt29((staticReferenceInDeferredParentInstance.getObjectInstance() << 1) | 1);
      out.writeUInt29(staticReferenceInDeferredParentInstance.getDeferredParentInstance());
      out.writeUInt29(id);

      staticReferenceInDeferredParentInstance.markAsWritten();
    }
  }
}
