package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.InlineCssRuleset;
import com.intellij.flex.uiDesigner.css.StyleDeclarationProxy;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;
import com.intellij.flex.uiDesigner.flex.DocumentReader;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.events.Event;
import flash.system.ApplicationDomain;
import flash.utils.ByteArray;
import flash.utils.IDataInput;

public final class MxmlReader implements DocumentReader {
  private static const FLEX_EVENT_CLASS_NAME:String = "mx.events.FlexEvent";
  
  private static const CLASS_MARKER:int = 43;
  private static const COLOR_STYLE_MARKER:int = 42;
  private static const STRING_REFERENCE:int = 44;
  
  private var stringRegistry:StringRegistry;
  private var documentFactoryManager:DocumentFactoryManager;

  private const stateReader:StateReader = new StateReader();
  private const injectedASReader:InjectedASReader = new InjectedASReader();

  private var context:ModuleContext;
  private var documentFile:VirtualFile;
  private var styleManager:Object;

  private var deferredMxContainers:Vector.<DisplayObjectContainer>;
  internal var objectTable:Vector.<Object>;
  
  internal var byteFactoryContext:DeferredInstanceFromBytesContext;

  public function MxmlReader(stringRegistry:StringRegistry, documentFactoryManager:DocumentFactoryManager) {
    this.stringRegistry = stringRegistry;
    this.documentFactoryManager = documentFactoryManager;
  }
  
  private var _input:IDataInput;
  public function set input(value:IDataInput):void {
    _input = value;
  }

  internal function getClass(name:String):Class {
    return Class(context.applicationDomain.getDefinition(name));
  }

  public function read2(bytes:ByteArray, context:DeferredInstanceFromBytesContext):Object {
    var oldInput:IDataInput = _input;
    _input = bytes;
    var objectTableSize:int = readObjectTableSize();

    this.context = context.moduleContext;
    this.styleManager = context.styleManager;
    this.documentFile = context.documentFile;
    var object:Object = readObject(stringRegistry.read(_input));
    _input = oldInput;

    assert(stateReader.cleared && byteFactoryContext == null && objectTableSize == (objectTable == null ? 0 : objectTable.length));

    this.context = null;
    this.styleManager = null;
    return object;
  }

  public function getLocalObjectTable():Vector.<Object> {
    if (objectTable != null && objectTable.length != 0) {
      var o:Vector.<Object> = objectTable;
      objectTable = null;
      return o;
    }
    else {
      return null;
    }
  }

  private function readObjectTableSize():int {
    var objectTableSize:int = AmfUtil.readUInt29(_input);
    if (objectTableSize != 0) {
      if (objectTable == null) {
        objectTable = new Vector.<Object>(objectTableSize, true);
      }
      else {
        objectTable.length = objectTableSize;
        objectTable.fixed = true;
      }
    }
    
    return objectTableSize;
  }

  public function read(documentFile:VirtualFile, styleManager:Object, context:ModuleContext):Object {
    const objectTableSize:int = readObjectTableSize();

    this.context = context;
    this.styleManager = styleManager;
    this.documentFile = documentFile;
    var object:Object = readObject(stringRegistry.read(_input));
    stateReader.read(this, _input, object);
    injectedASReader.read(_input, this);

    this.context = null;
    this.styleManager = null;
    this.documentFile = null;

    if (objectTableSize != 0) {
      objectTable.fixed = false;
      objectTable.length = 0;
    }

    var t:DeferredInstanceFromBytesContext = byteFactoryContext;
    byteFactoryContext = null;
    stateReader.reset(t);

    return object;
  }
  
  internal function readObjectReference():Object {
    var o:Object;
    if ((o = objectTable[AmfUtil.readUInt29(_input)]) == null) {
      throw new ArgumentError("must be not null");
    }

    return o;
  }
  
  private function readConstructor(objectClass:Class):Object {
    switch (_input.readByte()) {
      case CLASS_MARKER:
        return new objectClass(context.applicationDomain.getDefinition(stringRegistry.read(_input)));

      case PropertyClassifier.VECTOR_OF_DEFERRED_INSTANCE_FROM_BYTES:
        initByteFactoryContext();
        return new objectClass(stateReader.readVectorOfDeferredInstanceFromBytes(this, _input), byteFactoryContext);

      case PropertyClassifier.ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES:
        initByteFactoryContext();
        return new objectClass(stateReader.readArrayOfDeferredInstanceFromBytes(this, _input), byteFactoryContext);

      case Amf3Types.BYTE_ARRAY:
        initByteFactoryContext();
        return new objectClass(readBytes(), byteFactoryContext);
      
      case Amf3Types.ARRAY:
        return new objectClass(readArray([]));

      default:
        throw new ArgumentError("unknown property classifier");
    }
  }

  internal function readObject(className:String):Object {
    var clazz:Class = Class(context.applicationDomain.getDefinition(className));
    var reference:int = _input.readUnsignedShort();
    var propertyName:String = stringRegistry.read(_input);
    var object:Object;
    if (propertyName == "1") {
      object = readConstructor(clazz);
      propertyName = null;
    }
    else {
      object = new clazz();
    }

    if (reference != 0) {
      if (objectTable[reference - 1] != null) {
        throw new ArgumentError("must be null");
      }
      objectTable[reference - 1] = object;
    }
    
    var propertyHolder:Object = object;
    var inlineCssDeclarationSource:CssRuleset;
    var cssPropertyDescriptor:CssDeclaration;
    var o:Object;
    for (; propertyName != null; propertyName = stringRegistry.read(_input)) {      
      switch (_input.readByte()) {
        case PropertyClassifier.PROPERTY:
          break;

        case PropertyClassifier.STYLE:
          if (inlineCssDeclarationSource == null) {
            inlineCssDeclarationSource = InlineCssRuleset.createInline(AmfUtil.readUInt29(_input), AmfUtil.readUInt29(_input), documentFile);
          }
          cssPropertyDescriptor = new CssDeclaration();
          cssPropertyDescriptor.name = propertyName;
          cssPropertyDescriptor.textOffset = AmfUtil.readUInt29(_input);
          inlineCssDeclarationSource.declarations.push(cssPropertyDescriptor);
          propertyHolder = cssPropertyDescriptor;
          propertyName = "value";

          if (_input.readBoolean()) {
            context.effectManagerClass[new QName(getMxNs(), "setStyle")](cssPropertyDescriptor.name, object);
          }
          break;

        case PropertyClassifier.ID:
          propertyHolder.id = _input.readUTFBytes(AmfUtil.readUInt29(_input));
          continue;
        
        case PropertyClassifier.MX_CONTAINER_CHILDREN:
          readChildrenMxContainer(DisplayObjectContainer(propertyHolder));
          continue;

        case PropertyClassifier.FIXED_ARRAY:
          propertyHolder[propertyName] = readFixedArray();
          continue;

        default:
          throw new ArgumentError("unknown property classifier");
      }

      switch (_input.readByte()) {
        case Amf3Types.STRING:
          propertyHolder[propertyName] = _input.readUTFBytes(AmfUtil.readUInt29(_input));
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.STRING;
          }
          break;

        case Amf3Types.DOUBLE:
          propertyHolder[propertyName] = _input.readDouble();
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.NUMBER;
          }
          break;

        case Amf3Types.INTEGER:
          propertyHolder[propertyName] = (AmfUtil.readUInt29(_input) << 3) >> 3;
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.NUMBER;
          }
          break;

        case Amf3Types.TRUE:
          propertyHolder[propertyName] = true;
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.BOOL;
          }
          break;

        case Amf3Types.FALSE:
          propertyHolder[propertyName] = false;
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.BOOL;
          }
          break;

        case Amf3Types.OBJECT:
          propertyHolder[propertyName] = readObject(stringRegistry.read(_input));
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.EFFECT;
          }
          break;

        case Amf3Types.ARRAY:
          propertyHolder[propertyName] = readArray([]);
          break;

        case COLOR_STYLE_MARKER:
          if (cssPropertyDescriptor == null) {
            // todo property inspector
            propertyHolder[propertyName] = _input.readObject();
          }
          else {
            cssPropertyDescriptor.type = _input.readByte();
            if (cssPropertyDescriptor.type == CssPropertyType.COLOR_STRING) {
              cssPropertyDescriptor.colorName = stringRegistry.read(_input);
            }
            cssPropertyDescriptor.value = _input.readObject();
          }
          break;

        case Amf3Types.OBJECT_REFERENCE:
          if ((o = objectTable[AmfUtil.readUInt29(_input)]) == null) {
            throw new ArgumentError("must be not null");
          }
          propertyHolder[propertyName] = o;
          break;
        
        case Amf3Types.DOCUMENT_FACTORY_REFERENCE:
          propertyHolder[propertyName] = readDocumentFactory();
          break;
        
        case STRING_REFERENCE:
          propertyHolder[propertyName] = stringRegistry.read(_input);
          break;
        
        case CLASS_MARKER:
          propertyHolder[propertyName] = context.applicationDomain.getDefinition(stringRegistry.read(_input));
          break;

        default:
          throw new ArgumentError("unknown property type");
      }

      if (cssPropertyDescriptor != null) {
        cssPropertyDescriptor = null;
        propertyHolder = object;
      }
    }

    if (inlineCssDeclarationSource != null) {
      clazz = context.inlineCssStyleDeclarationClass;
      object.styleDeclaration = new clazz(inlineCssDeclarationSource, styleManager);
    }

    return object;
  }
  
  private function readDocumentFactory():Object {
    var id:int = AmfUtil.readUInt29(_input);
    var factory:Object = context.getDocumentFactory(id);
    if (factory == null) {
      initByteFactoryContext();
      factory = new context.documentFactoryClass(documentFactoryManager.get(id), byteFactoryContext);
      context.putDocumentFactory(id, factory);
    }
    
    return factory;
  }

  private function initByteFactoryContext():void {
    if (stateReader.deferredInstanceFromBytesClass == null) {
      stateReader.deferredInstanceFromBytesClass = getClass(StateReader.DIFB_CLASS_NAME);
      byteFactoryContext = new DeferredInstanceFromBytesContext(documentFile, this, styleManager, context);
    }
  }

  internal function readBytes():ByteArray {
    var bytes:ByteArray = new ByteArray();
    _input.readBytes(bytes, 0, AmfUtil.readUInt29(_input));
    return bytes;
  }
  
  private function getMxNs():Namespace {
    return Namespace(context.applicationDomain.getDefinition("mx.core.mx_internal"));
  }

  private function readChildrenMxContainer(container:DisplayObjectContainer):void {
    var array:Array = [];
    var mxNs:Namespace = getMxNs();
    container[new QName(mxNs, "setActualCreationPolicies")]("none");
    container[new QName(mxNs, "createdComponents")] = array;
    if (deferredMxContainers == null) {
      deferredMxContainers = new Vector.<DisplayObjectContainer>();
    }
    deferredMxContainers.push(container);

    readArray(array);
  }

  public function createDeferredMxContainersChildren(applicationDomain:ApplicationDomain):void {
    if (deferredMxContainers == null || deferredMxContainers.length == 0) {
      return;
    }

    var mxNs:Namespace = Namespace(applicationDomain.getDefinition("mx.core.mx_internal"));
    var createdComponentsQName:QName = new QName(mxNs, "createdComponents");
    var numChildrenCreatedQName:QName = new QName(mxNs, "numChildrenCreated");
    var flexEventClass:Class = Class(applicationDomain.getDefinition(FLEX_EVENT_CLASS_NAME));
    for each (var container:DisplayObjectContainer in deferredMxContainers) {
      // initialized equals false, because processedDescriptors equals false, so, we check inheritingStyles (if is StyleDeclarationProxy, so, already "initialized")
      if (container["inheritingStyles"] is StyleDeclarationProxy) {
        createMxContainerChildren(container, createdComponentsQName, numChildrenCreatedQName, flexEventClass);
      }
      else {
        container.addEventListener("preinitialize", mxContainerPreinitializeHandler);
      }
    }

    deferredMxContainers.length = 0;
  }
  
  private static function mxContainerPreinitializeHandler(event:Event):void {
    var container:DisplayObjectContainer = DisplayObjectContainer(event.target);
    container.removeEventListener("preinitialize", mxContainerPreinitializeHandler);
    var sm:SystemManagerSB = SystemManagerSB(Object(container).systemManager);
    var mxNs:Namespace = Namespace(sm.getDefinitionByName("mx.core.mx_internal"));
    createMxContainerChildren(container, new QName(mxNs, "createdComponents"), new QName(mxNs, "numChildrenCreated"), Class(sm.getDefinitionByName(FLEX_EVENT_CLASS_NAME)));
  }

  private static function createMxContainerChildren(container:DisplayObjectContainer, createdComponentsQName:QName, numChildrenCreatedQName:QName, flexEventClass:Class):void {
    var chidlren:Array = container[createdComponentsQName];
    for each (var child:DisplayObject in chidlren) {
      container.addChild(child);
    }
    container["processedDescriptors"] = true;
    container[numChildrenCreatedQName] = chidlren.length;
    container.dispatchEvent(new flexEventClass("contentCreationComplete"));
  }

  // support only object array without null
  internal function readArray(array:Array):Array {
    var count:int = 0;
    while (true) {
      var className:String = stringRegistry.read(_input);
      if (className == null) {
        return array;
      }
      else {
        switch (className) {
          case "String":
            array[count++] = _input.readUTFBytes(AmfUtil.readUInt29(_input));
            break;

          case "Number":
            array[count++] = _input.readDouble();
            break;

          case "Boolean":
            array[count++] = _input.readBoolean();
            break;

          default:
            array[count++] = readObject(className);
            break;
        }
      }
    }

    // *** Adobe
    //noinspection UnreachableCodeJS
    throw new ArgumentError();
  }

  private function readFixedArray():Array {
    var n:int = _input.readUnsignedByte();
    var array:Array = new Array(n);
    for (var i:int = 0; i < n; i++) {
      array[i] = readObject(stringRegistry.read(_input));
    }

    return array;
  }

  internal function readClassOrPropertyName():String {
    return stringRegistry.read(_input);
  }
}
}

class PropertyClassifier {
  public static const PROPERTY:int = 0;

  public static const STYLE:int = 1;

  public static const ID:int = 2;

  public static const MX_CONTAINER_CHILDREN:int = 4;

  public static const ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES:int = 6;
  public static const VECTOR_OF_DEFERRED_INSTANCE_FROM_BYTES:int = 7;

  public static const FIXED_ARRAY:int = 8;
}