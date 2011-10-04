package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;
import com.intellij.flex.uiDesigner.flex.states.InstanceProvider;
import com.intellij.flex.uiDesigner.flex.states.StaticInstanceReferenceInDeferredParentInstanceBase;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.IDataInput;

public class InjectedASReader {
  private var deferredReferenceClass:Class;
  
  public function read(input:IDataInput, reader:MxmlReader):void {
    readDeclarations(input, reader);
    readBinding(input, reader);
  }

  //noinspection JSMethodCanBeStatic
  private function readDeclarations(input:IDataInput, reader:MxmlReader):void {
    const length:int = input.readUnsignedShort();
    if (length == 0) {
      return;
    }
    
    var vector:Vector.<Object> = new Vector.<Object>(length, true);
    reader.readArrayOrVector(vector, length); // result vector is ignored
  }
  
  private function readBinding(input:IDataInput, reader:MxmlReader):void {
    const size:int = input.readUnsignedShort();
    if (size == 0) {
      return;
    }

    for (var i:int = 0; i < size; i++) {
      var o:Object = null;
      var target:Object = readMxmlObjectReference(input, reader);
      var propertyName:String = reader.readClassOrPropertyName();
      var type:int = input.readByte();
      var isStyle:Boolean = (type & 1) != 0;
      type >>= 1;

      var targetBinding:PropertyBindingTarget = null;
      var deferredParentInstance:DeferredInstanceFromBytesBase = null;
      if (target is StaticInstanceReferenceInDeferredParentInstanceBase) {
        targetBinding = new PropertyBindingTarget(target, propertyName, isStyle);
        deferredParentInstance = StaticInstanceReferenceInDeferredParentInstanceBase(target).deferredParentInstance;
      }
      else if (target is DeferredInstanceFromBytesBase) {
        deferredParentInstance = DeferredInstanceFromBytesBase(target);
        targetBinding = new PropertyBindingDeferredTarget(deferredParentInstance, propertyName, isStyle);
      }

      switch (type) {
        case BindingType.MXML_OBJECT:
        case BindingType.MXML_OBJECT_WRAP_TO_ARRAY:
          o = readMxmlObjectReference(input, reader);
          // if target can be get only via binding, execute it
          // 1) on deferredParentInstance creation if value is static
          // 2) on value creation if value is dynamic
          if (o is DeferredInstanceFromBytesBase) {
            DeferredInstanceFromBytesBase(o).registerBinding(targetBinding == null ? new PropertyBindingTarget(target, propertyName, isStyle) : targetBinding);
          }
          else if (type == BindingType.MXML_OBJECT_WRAP_TO_ARRAY) {
            o = [o];
          }
          break;

        case BindingType.VARIABLE:
          o = readVariableReference(input, reader);
          break;

        case BindingType.EXPRESSION:
          var amfType:int = input.readByte();
          if (amfType == ExpressionMessageTypes.MXML_OBJECT_CHAIN) {
            readMxmlObjectChain(reader, input, target, propertyName, targetBinding, isStyle);
            if (targetBinding == null) {
              continue;
            }
          }
          else {
            o = reader.readExpression(amfType);
            if (targetBinding != null && o == null) {
              targetBinding.staticValue = o;
            }
          }
          break;

        default:
          throw new ArgumentError("unknown binding type " + type);
      }

      if (targetBinding == null) {
        if (isStyle) {
          target.setStyle(propertyName, o);
        }
        else {
          target[propertyName] = o;
        }
      }
      else {
        if (o != null) {
          targetBinding.staticValue = o;
        }
        deferredParentInstance.registerBinding(targetBinding);
      }
    }
    
    deferredReferenceClass = null;
  }
  
  private static function createChangeWatcherHandler(target:Object, propertyName:String, changeWatcher:Object, isStyle:Boolean):Function {
    return function(event:*):void {
      if (!isStyle) {
        target[propertyName] = changeWatcher.getValue();
      }
      else {
        target.setStyle(propertyName, changeWatcher.getValue());
      }
    };
  }

  private function readMxmlObjectChain(reader:MxmlReader, input:IDataInput, target:Object,
                                       propertyName:String, targetBinding:PropertyBindingTarget,
                                       isStyle:Boolean):void {
    var changeWatcher:Object = watchMxmlObjectChain(reader.context.moduleContext.getClass("mx.binding.utils.ChangeWatcher"),
                                                    reader.stringRegistry, input);
    var host:Object = readMxmlObjectReference(input, reader);
    if (targetBinding == null) {
      if (host is InstanceProvider) {
        var propertyBindingTarget:PropertyBindingTarget = new PropertyBindingTarget(target, propertyName, isStyle);
        InstanceProvider(host).bindingExecutor.registerBinding(propertyBindingTarget);
        // if host is DeferredInstanceFromBytesBase, we can get host via value on call execute(value:Object):void
        propertyBindingTarget.initChangeWatcher(changeWatcher, host is DeferredInstanceFromBytesBase ? null : host);
      }
      else {
        changeWatcher.reset(host);
        var handler:Function = createChangeWatcherHandler(target, propertyName, changeWatcher, isStyle);
        changeWatcher.setHandler(handler);
        handler(null);
      }
    }
    else {
      if (host is DeferredInstanceFromBytesBase) {
        DeferredInstanceFromBytesBase(host).registerBinding(targetBinding);
      }

      PropertyBindingTarget(targetBinding).initChangeWatcher(changeWatcher, host);
    }
  }

  private static function watchMxmlObjectChain(changeWatcherClass:Class, stringRegistry:StringRegistry,
                                               input:IDataInput):Object {
    var propertyName:String = stringRegistry.read(input);
    return propertyName == null ? null : new changeWatcherClass(propertyName, null, false,
                                                                watchMxmlObjectChain(changeWatcherClass, stringRegistry, input));
  }

  //noinspection JSMethodCanBeStatic
  internal function readVariableReference(input:IDataInput, reader:MxmlReader):Object {
    var o:Object;
    var id:int = AmfUtil.readUInt29(input);
    if ((id & 1) == 0) {
      o = reader.readExpression(input.readByte());
    }

    id = id >> 1;
    if (id != 0) {
      if (o == null) {
        o = reader.objectTable[id - 1];
      }
      else {
        reader.saveReferredObject(id - 1, o);
      }
    }
    
    return o;
  }

  // todo trace("unsupported DeferredInstanceFromBytesBase in readExpression");
  internal function readMxmlObjectReference(input:IDataInput, reader:MxmlReader):Object {
    var id:int = AmfUtil.readUInt29(input);
    // is object reference or StaticInstanceReferenceInDeferredParentInstance input
    if ((id & 1) == 0) {
      return reader.objectTable[id >> 1];
    }
    else {
      if (deferredReferenceClass == null) {
        deferredReferenceClass = reader.context.moduleContext.getClass("com.intellij.flex.uiDesigner.flex.states.StaticInstanceReferenceInDeferredParentInstance");
      }
      var o:StaticInstanceReferenceInDeferredParentInstanceBase = new deferredReferenceClass();
      o.reference = id >> 1;
      o.deferredParentInstance = DeferredInstanceFromBytesBase(reader.readObjectReference());
      reader.saveReferredObject(AmfUtil.readUInt29(input), o);
      return o;
    }
  }
}
}

final class BindingType {
  public static const MXML_OBJECT:int = 0;
  public static const MXML_OBJECT_WRAP_TO_ARRAY:int = 1;
  public static const VARIABLE:int = 2;
  public static const EXPRESSION:int = 3;
}