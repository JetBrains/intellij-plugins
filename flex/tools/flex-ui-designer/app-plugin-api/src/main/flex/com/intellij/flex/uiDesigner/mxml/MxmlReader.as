package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.BitmapDataManager;
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.ModuleContextEx;
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.css.CssDeclarationImpl;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.CssSkinClassDeclaration;
import com.intellij.flex.uiDesigner.css.InlineCssRuleset;
import com.intellij.flex.uiDesigner.css.StyleDeclarationProxy;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.AmfExtendedTypes;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.events.Event;
import flash.system.ApplicationDomain;
import flash.utils.ByteArray;
import flash.utils.IDataInput;

public final class MxmlReader implements DocumentReader {
  private static const FLEX_EVENT_CLASS_NAME:String = "mx.events.FlexEvent";

  private static const COLOR_STYLE_MARKER:int = 42;

  private var input:IDataInput;
  
  private var stringRegistry:StringRegistry;
  private var bitmapDataManager:BitmapDataManager;
  private var swfDataManager:EmbedSwfManager;

  private const stateReader:StateReader = new StateReader();
  //noinspection JSFieldCanBeLocal
  private const injectedASReader:InjectedASReader = new InjectedASReader();

  private var moduleContext:ModuleContext;
  internal var context:DocumentReaderContext;

  private var deferredMxContainers:Vector.<DisplayObjectContainer>;
  internal var objectTable:Vector.<Object>;
  
  internal var factoryContext:DeferredInstanceFromBytesContext;

  public function MxmlReader(stringRegistry:StringRegistry, bitmapDataManager:BitmapDataManager, swfDataManager:EmbedSwfManager) {
    this.stringRegistry = stringRegistry;
    this.bitmapDataManager = bitmapDataManager;
    this.swfDataManager = swfDataManager;
  }

  private const deferredSetStyleProxyPool:Vector.<DeferredSetStyleProxy> = new Vector.<DeferredSetStyleProxy>();

  private function createDeferredSetStyleProxy():DeferredSetStyleProxy {
    var deferredSetStyleProxy:DeferredSetStyleProxy;
    if (deferredSetStyleProxyPool.length == 0) {
      deferredSetStyleProxy = new DeferredSetStyleProxy();
    }
    else {
      deferredSetStyleProxy = deferredSetStyleProxyPool.pop();
    }

    deferredSetStyleProxy.file = context.file;
    return deferredSetStyleProxy;
  }

  public function readDeferredInstanceFromBytes(input:IDataInput, factoryContext:DeferredInstanceFromBytesContext):Object {
    this.input = input;
    
    var objectTableSize:int = readObjectTableSize();

    context = factoryContext.readerContext;
    moduleContext = context.moduleContext;

    var object:Object;
    switch (input.readByte()) {
      case Amf3Types.OBJECT:
        object = readObjectFromClass(stringRegistry.read(input));
        break;

      case AmfExtendedTypes.DOCUMENT_REFERENCE:
        object = readObjectFromFactory(readDocumentFactory().newInstance());
        break;

      default:
        throw new ArgumentError("unknown property type");
    }

    assert(this.factoryContext == null && objectTableSize == (objectTable == null ? 0 : objectTable.length));

    context = null;
    moduleContext = null;
    this.input = null;
    
    if (input is ByteArray) {
      assert(input.bytesAvailable == 0);
      ByteArray(input).position = 0;
    }

    return object;
  }

  // must be call after readDeferredInstanceFromBytes(). read() — for not DeferredInstanceFromBytes document factory — objectTable will be cleared after read automatically,
  // readDeferredInstanceFromBytes() — for DeferredInstanceFromBytes — if we have static objects in our deferred parent,
  // we may need refer to it — as example, DESTINATION for other dynamic (i. e., included/excluded from some state) parent child (AFTER, as example)
  // or state-specific properties:
  // <VGroup includeIn="A, B">
  //   <Label text.A="A" text.B="B"/>
  // </VGroup>
  public function getObjectTableForDeferredInstanceFromBytes():Vector.<Object> {
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
    var objectTableSize:int = AmfUtil.readUInt29(input);
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

  public function read(input:IDataInput, documentReaderContext:DocumentReaderContext, restorePrevContextAfterRead:Boolean = false):Object {
    const oldInput:IDataInput = this.input;
    this.input = input;
    
    const objectTableSize:int = readObjectTableSize();

    const oldContext:DocumentReaderContext = this.context;
    context = documentReaderContext;
    moduleContext = context.moduleContext;
    var object:Object = readObjectFromClass(stringRegistry.read(input), true);
    stateReader.read(this, input, object);
    injectedASReader.read(input, this);

    context = oldContext;
    moduleContext = oldContext == null ? null : oldContext.moduleContext;

    if (objectTableSize != 0) {
      objectTable.fixed = false;
      objectTable.length = 0;
    }

    var t:DeferredInstanceFromBytesContext = factoryContext;
    factoryContext = null;
    stateReader.reset(t);

    if (input is ByteArray) {
      assert(input.bytesAvailable == 0);
      ByteArray(input).position = 0;
    }
    this.input = oldInput;
    return object;
  }
  
  internal function readObjectReference():Object {
    var o:Object;
    if ((o = objectTable[AmfUtil.readUInt29(input)]) == null) {
      throw new ArgumentError("must be not null");
    }

    return o;
  }
  
  private function readConstructor(objectClass:Class):Object {
    switch (input.readByte()) {
      case AmfExtendedTypes.CLASS_REFERENCE:
        return new objectClass(moduleContext.applicationDomain.getDefinition(stringRegistry.read(input)));

      case PropertyClassifier.VECTOR_OF_DEFERRED_INSTANCE_FROM_BYTES:
        return new objectClass(stateReader.readVectorOfDeferredInstanceFromBytes(this, input), getOrCreateFactoryContext());

      case PropertyClassifier.ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES:
        return new objectClass(stateReader.readArrayOfDeferredInstanceFromBytes(this, input), getOrCreateFactoryContext());

      case Amf3Types.BYTE_ARRAY:
        return new objectClass(readBytes(), getOrCreateFactoryContext());
      
      case Amf3Types.ARRAY:
        return new objectClass(readArray([]));

      default:
        throw new ArgumentError("unknown property classifier");
    }
  }

  internal function readObjectFromClass(className:String, setDocument:Boolean = false):Object {
    var clazz:Class = moduleContext.getClass(className);
    var reference:int = input.readUnsignedShort();
    var propertyName:String = stringRegistry.read(input);
    var object:Object;
    var objectDeclarationTextOffset:int;
    if (propertyName == "1") {
      object = readConstructor(clazz);
      propertyName = null;
    }
    else {
      object = new clazz();
      if (setDocument) {
        // perfomance, early set document, avoid recursive set later (see UIComponent.document setter)
        object.document = object;
      }
      if (propertyName == "$fud_position") {
        objectDeclarationTextOffset = AmfUtil.readUInt29(input);
        context.registerObjectDeclarationPosition(object, objectDeclarationTextOffset);
        propertyName = stringRegistry.read(input);
      }
    }

    return initObject(object, reference, propertyName, objectDeclarationTextOffset);
  }

  private function readObjectFromFactory(object:Object):Object {
    var reference:int = input.readUnsignedShort();
    var propertyName:String = stringRegistry.read(input);
    var objectDeclarationTextOffset:int;
    if (propertyName == "$fud_position") {
      objectDeclarationTextOffset = AmfUtil.readUInt29(input);
      context.registerObjectDeclarationPosition(object, objectDeclarationTextOffset);
      propertyName = stringRegistry.read(input);
    }

    return initObject(object, reference, propertyName, objectDeclarationTextOffset);
  }

  private function initObject(object:Object, reference:int, propertyName:String, objectDeclarationTextOffset:int):Object {
    if (reference != 0) {
      if (objectTable[reference - 1] != null) {
        throw new ArgumentError("must be null");
      }
      objectTable[reference - 1] = object;
    }

    const hasDeferredSetStyles:Boolean = "deferredSetStyles" in object;
    var deferredSetStyleProxy:DeferredSetStyleProxy;
    var explicitInlineCssDeclarationSourceCreated:Boolean;
    if (hasDeferredSetStyles) {
      deferredSetStyleProxy = createDeferredSetStyleProxy();
      deferredSetStyleProxy.objectDeclarationTextOffset = objectDeclarationTextOffset;
      object.deferredSetStyles = deferredSetStyleProxy;
    }
    
    var propertyHolder:Object = object;
    var cssPropertyDescriptor:CssDeclarationImpl;
    var o:Object;
    for (; propertyName != null; propertyName = stringRegistry.read(input)) {
      switch (input.readByte()) {
        case PropertyClassifier.PROPERTY:
          break;

        case PropertyClassifier.STYLE:
          if (deferredSetStyleProxy == null) {
            deferredSetStyleProxy = createDeferredSetStyleProxy();
          }
            
          if (deferredSetStyleProxy.inlineCssDeclarationSource == null) {
            explicitInlineCssDeclarationSourceCreated = true;
            deferredSetStyleProxy.inlineCssDeclarationSource = InlineCssRuleset.createInline(
                AmfUtil.readUInt29(input), AmfUtil.readUInt29(input), context.file);
          }
          else if (!explicitInlineCssDeclarationSourceCreated) {
            explicitInlineCssDeclarationSourceCreated = true;
            // skip line and text offset
            AmfUtil.readUInt29(input);
            AmfUtil.readUInt29(input);
          }

          const flags:int = input.readUnsignedByte();
          if ((flags & 1) != 0) {
            deferredSetStyleProxy.inlineCssDeclarationSource.declarations.push(new CssSkinClassDeclaration(readDocumentFactory(), CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT));
            continue;
          }

          cssPropertyDescriptor = CssDeclarationImpl.create(propertyName, CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT);
          deferredSetStyleProxy.inlineCssDeclarationSource.declarations.push(cssPropertyDescriptor);
          propertyHolder = cssPropertyDescriptor;
          if ((flags & 2) != 0) {
            moduleContext.effectManagerClass[new QName(getMxNs(), "setStyle")](propertyName, object);
          }
          propertyName = "value";
          break;

        case PropertyClassifier.ID:
          propertyHolder.id = AmfUtil.readUtf(input);
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

      switch (input.readByte()) {
        case Amf3Types.STRING:
          propertyHolder[propertyName] = AmfUtil.readUtf(input);
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.STRING;
          }
          break;

        case Amf3Types.DOUBLE:
          propertyHolder[propertyName] = input.readDouble();
          if (cssPropertyDescriptor != null) {
            cssPropertyDescriptor.type = CssPropertyType.NUMBER;
          }
          break;

        case Amf3Types.INTEGER:
          propertyHolder[propertyName] = (AmfUtil.readUInt29(input) << 3) >> 3;
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
          propertyHolder[propertyName] = readObjectFromClass(stringRegistry.read(input));
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
            propertyHolder[propertyName] = input.readObject();
          }
          else {
            cssPropertyDescriptor.type = input.readByte();
            if (cssPropertyDescriptor.type == CssPropertyType.COLOR_STRING) {
              cssPropertyDescriptor.colorName = stringRegistry.read(input);
            }
            cssPropertyDescriptor.value = input.readObject();
          }
          break;

        case Amf3Types.OBJECT_REFERENCE:
          if ((o = objectTable[AmfUtil.readUInt29(input)]) == null) {
            throw new ArgumentError("must be not null");
          }
          propertyHolder[propertyName] = o;
          break;
        
        case AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE:
          propertyHolder[propertyName] = readDocumentFactory();
          break;

        case AmfExtendedTypes.DOCUMENT_REFERENCE:
          propertyHolder[propertyName] = readObjectFromFactory(readDocumentFactory().newInstance());
          break;

        case Amf3Types.BITMAP:
          propertyHolder[propertyName] = readBitmapData();
          break;
        
        case Amf3Types.SWF:
          readSwfData(propertyHolder, propertyName);
          break;
        
        case AmfExtendedTypes.STRING_REFERENCE:
          propertyHolder[propertyName] = stringRegistry.read(input);
          break;
        
        case AmfExtendedTypes.CLASS_REFERENCE:
          propertyHolder[propertyName] = moduleContext.applicationDomain.getDefinition(stringRegistry.read(input));
          break;

        default:
          throw new ArgumentError("unknown property type");
      }

      if (cssPropertyDescriptor != null) {
        cssPropertyDescriptor = null;
        propertyHolder = object;
      }
    }

    if (deferredSetStyleProxy != null) {
      if (deferredSetStyleProxy.inlineCssDeclarationSource != null) {
        object.styleDeclaration = new moduleContext.inlineCssStyleDeclarationClass(deferredSetStyleProxy.inlineCssDeclarationSource,
            moduleContext.styleManager.styleValueResolver);
      }

      deferredSetStyleProxy.inlineCssDeclarationSource = null;
      deferredSetStyleProxy.file = null;
      deferredSetStyleProxyPool.push(deferredSetStyleProxy);
    }

    if (hasDeferredSetStyles) {
      object.deferredSetStyles = null;
    }
    
    return object;
  }

  private function readDocumentFactory():Object {
    var id:int = AmfUtil.readUInt29(input);
    var factory:Object = moduleContext.getDocumentFactory(id);
    if (factory == null) {
      var documentFactory:DocumentFactory = DocumentFactoryManager.getInstance(ModuleContextEx(moduleContext).project).get2(id, DocumentFactory(context));
      factory = new moduleContext.documentFactoryClass(documentFactory, new DeferredInstanceFromBytesContext(documentFactory, this));
      moduleContext.putDocumentFactory(id, factory);
    }
    
    return factory;
  }
  
  private function readBitmapData():BitmapData {
    return bitmapDataManager.get(AmfUtil.readUInt29(input));
  }

  private function readSwfData(propertyHolder:Object, propertyName:String):void {
    const symbolLength:int = AmfUtil.readUInt29(input);
    var symbol:String = symbolLength == 0 ? null : input.readUTFBytes(symbolLength);
    swfDataManager.assign(AmfUtil.readUInt29(input), symbol, propertyHolder, propertyName);
  }

  private function getOrCreateFactoryContext():DeferredInstanceFromBytesContext {
    if (factoryContext == null) {
      factoryContext = new DeferredInstanceFromBytesContext(context, this);
    }
    
    return factoryContext;
  }

  internal function readBytes():ByteArray {
    var bytes:ByteArray = new ByteArray();
    input.readBytes(bytes, 0, AmfUtil.readUInt29(input));
    return bytes;
  }
  
  private function getMxNs():Namespace {
    return Namespace(moduleContext.applicationDomain.getDefinition("mx.core.mx_internal"));
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

  private static function createMxContainerChildren(container:DisplayObjectContainer, createdComponentsQName:QName,
                                                    numChildrenCreatedQName:QName, flexEventClass:Class):void {
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
      const amfType:int = input.readByte();
      switch (amfType) {
        case Amf3Types.OBJECT:
          array[count++] = readObjectFromClass(stringRegistry.read(input));
          break;

        case 0:
          return array;

        case Amf3Types.STRING:
          array[count++] = AmfUtil.readUtf(input);
          break;

        case AmfExtendedTypes.STRING_REFERENCE:
          array[count++] = stringRegistry.read(input);
          break;

        case Amf3Types.DOUBLE:
          array[count++] = input.readDouble();
          break;

        case Amf3Types.FALSE:
          array[count++] = false;
          break;

        case Amf3Types.TRUE:
          array[count++] = true;
          break;

        case AmfExtendedTypes.DOCUMENT_REFERENCE:
          array[count++] = readObjectFromFactory(readDocumentFactory().newInstance());
          break;

        default:
          throw new ArgumentError("unknown property type " + amfType);
      }
    }

    // *** Adobe
    //noinspection UnreachableCodeJS
    throw new ArgumentError();
  }

  private function readFixedArray():Array {
    var n:int = input.readUnsignedByte();
    var array:Array = new Array(n);
    for (var i:int = 0; i < n; i++) {
      array[i] = readObjectFromClass(stringRegistry.read(input));
    }

    return array;
  }

  internal function readClassOrPropertyName():String {
    return stringRegistry.read(input);
  }
}
}

import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.CssDeclarationImpl;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.InlineCssRuleset;

import flash.utils.Proxy;
import flash.utils.flash_proxy;

class PropertyClassifier {
  public static const PROPERTY:int = 0;

  public static const STYLE:int = 1;

  public static const ID:int = 2;

  public static const MX_CONTAINER_CHILDREN:int = 4;

  public static const ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES:int = 6;
  public static const VECTOR_OF_DEFERRED_INSTANCE_FROM_BYTES:int = 7;

  public static const FIXED_ARRAY:int = 8;
}

use namespace flash_proxy;

// IDEA-72366
final class DeferredSetStyleProxy extends Proxy {
  internal var file:VirtualFile;
  internal var inlineCssDeclarationSource:CssRuleset;
  internal var objectDeclarationTextOffset:int;

  override flash_proxy function setProperty(name:*, value:*):void {
    if (inlineCssDeclarationSource == null) {
      inlineCssDeclarationSource = InlineCssRuleset.createInline(0, objectDeclarationTextOffset, file);
    }

    var cssDeclaration:CssDeclarationImpl = CssDeclarationImpl.create(name, CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT);
    cssDeclaration.value = value;
    inlineCssDeclarationSource.declarations.push(cssDeclaration);
  }
}