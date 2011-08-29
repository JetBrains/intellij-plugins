package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;
import com.intellij.flex.uiDesigner.io.AmfExtendedTypes;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.IDataInput;

internal final class StateReader {
  private var deferredInstancesForImmediateCreation:Vector.<DeferredInstanceFromBytesBase>;

  //noinspection JSMethodCanBeStatic
  public function read(reader:MxmlReader, data:IDataInput, object:Object):void {
    const size:int = data.readByte();
    if (size == 0) {
      return;
    }
   
    var states:Array = new Array(size);
    for (var i:int = 0; i < size; i++) {
      var state:Object = reader.readObjectFromClass("com.intellij.flex.uiDesigner.flex.states.State");
      state.context = reader.factoryContext;
      states[i] = state;
    }

    // may be explicitly specified (<local:DynamicParentWithDeepBackSibling currentState="A" currentState.A="B"/>)
    if (object.currentState == null) {
      object.setCurrentState(states[0].name, false); // as mxml compiler does
    }
    object.states = states;
  }
  
  public function readArrayOfDeferredInstanceFromBytes(reader:MxmlReader, data:IDataInput):Object {
    var n:int = data.readUnsignedByte();
    var array:Array = new Array(n);
    for (var i:int = 0; i < n; i++) {
      array[i] = readDeferredInstanceFromBytes(reader, data);
    }

    return array;
  }
  
  public function readVectorOfDeferredInstanceFromBytes(reader:MxmlReader, data:IDataInput):Object {
    var vClass:Class = reader.context.moduleContext.deferredInstanceFromBytesVectorClass;
    var n:int = data.readUnsignedByte();
    var v:Object = new vClass(n, true);
    for (var i:int = 0; i < n; i++) {
      v[i] = readDeferredInstanceFromBytes(reader, data);
    }

    return v;
  }
  
  public function readDeferredInstanceFromBytes(reader:MxmlReader, data:IDataInput):DeferredInstanceFromBytesBase {
    var kind:int = data.readByte();
    var immediateCreation:Boolean = (kind & 1) != 0;
    kind = kind >> 1;
    if (kind == AmfExtendedTypes.OBJECT_REFERENCE) {
      return DeferredInstanceFromBytesBase(reader.readObjectReference());
    }
    else {
      var o:DeferredInstanceFromBytesBase = new reader.context.moduleContext.deferredInstanceFromBytesClass(reader.readBytes());
      if (kind == 1) {
        var id:int = AmfUtil.readUInt29(data);
        if (reader.objectTable[id] != null) {
          throw new ArgumentError("must be null");
        }
        reader.objectTable[id] = o;
      }
      
      if (immediateCreation) {
        if (deferredInstancesForImmediateCreation == null) {
          deferredInstancesForImmediateCreation = new Vector.<DeferredInstanceFromBytesBase>();
        }
        deferredInstancesForImmediateCreation[deferredInstancesForImmediateCreation.length] = o;
      }
      return o;
    }
  }

  public function reset(byteFactoryContext:DeferredInstanceFromBytesContext):void {
    if (deferredInstancesForImmediateCreation != null && deferredInstancesForImmediateCreation.length != 0) {
      for each (var deferredInstanceFromBytesBase:DeferredInstanceFromBytesBase in deferredInstancesForImmediateCreation) {
        deferredInstanceFromBytesBase.create(byteFactoryContext);
      }
      
      deferredInstancesForImmediateCreation.length = 0;
    }
  }
}
}
