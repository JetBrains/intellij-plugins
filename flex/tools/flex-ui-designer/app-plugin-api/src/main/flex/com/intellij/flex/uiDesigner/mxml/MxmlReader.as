package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.EmbedImageManager;
import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.ModuleContextEx;
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssDeclarationImpl;
import com.intellij.flex.uiDesigner.css.CssEmbedImageDeclaration;
import com.intellij.flex.uiDesigner.css.CssEmbedSwfDeclaration;
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

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.events.Event;
import flash.system.ApplicationDomain;
import flash.utils.ByteArray;
import flash.utils.IDataInput;
import flash.utils.Proxy;

public final class MxmlReader implements DocumentReader {
  private static const FLEX_EVENT_CLASS_NAME:String = "mx.events.FlexEvent";

  private static const COLOR_STYLE_MARKER:int = 42;

  private var input:IDataInput;
  
  private var stringRegistry:StringRegistry;
  private var embedImageManager:EmbedImageManager;
  private var embedSwfManager:EmbedSwfManager;

  private const stateReader:StateReader = new StateReader();
  //noinspection JSFieldCanBeLocal
  private const injectedASReader:InjectedASReader = new InjectedASReader();

  private var moduleContext:ModuleContextEx;
  internal var context:DocumentReaderContext;

  private var deferredMxContainers:Vector.<DisplayObjectContainer>;
  internal var objectTable:Vector.<Object>;
  
  internal var factoryContext:DeferredInstanceFromBytesContext;

  public function MxmlReader(stringRegistry:StringRegistry, embedImageManager:EmbedImageManager, embedSwfManager:EmbedSwfManager) {
    this.stringRegistry = stringRegistry;
    this.embedImageManager = embedImageManager;
    this.embedSwfManager = embedSwfManager;
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
    moduleContext = ModuleContextEx(context.moduleContext);

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

    var oldObjectTable:Vector.<Object>;
    if (restorePrevContextAfterRead) {
      oldObjectTable = objectTable;
      objectTable = null;
    }
    
    const objectTableSize:int = readObjectTableSize();

    const oldContext:DocumentReaderContext = this.context;
    context = documentReaderContext;
    moduleContext = ModuleContextEx(context.moduleContext);
    var object:Object = readObjectFromClass(stringRegistry.read(input), true);
    stateReader.read(this, input, object);
    injectedASReader.read(input, this);

    context = oldContext;
    moduleContext = oldContext == null ? null : ModuleContextEx(oldContext.moduleContext);

    if (restorePrevContextAfterRead) {
      objectTable = oldObjectTable;
    }
    else if (objectTableSize != 0) {
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
        return new objectClass(readArray());

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

    const hasDeferredSetStyles:Boolean = !(object is Proxy) && "deferredSetStyles" in object;
    var deferredSetStyleProxy:DeferredSetStyleProxy;
    var explicitInlineCssRulesetCreated:Boolean;
    if (hasDeferredSetStyles) {
      deferredSetStyleProxy = createDeferredSetStyleProxy();
      deferredSetStyleProxy.objectDeclarationTextOffset = objectDeclarationTextOffset;
      object.deferredSetStyles = deferredSetStyleProxy;
    }
    
    var propertyHolder:Object = object;
    var cssDeclaration:CssDeclarationImpl;
    var o:Object;
    for (; propertyName != null; propertyName = stringRegistry.read(input)) {
      switch (input.readByte()) {
        case PropertyClassifier.PROPERTY:
          break;

        case PropertyClassifier.STYLE:
          if (deferredSetStyleProxy == null) {
            deferredSetStyleProxy = createDeferredSetStyleProxy();
          }
            
          if (deferredSetStyleProxy.inlineCssRuleset == null) {
            explicitInlineCssRulesetCreated = true;
            deferredSetStyleProxy.inlineCssRuleset = InlineCssRuleset.createInline(AmfUtil.readUInt29(input), AmfUtil.readUInt29(input),
                                                                                   context.file);
          }
          else if (!explicitInlineCssRulesetCreated) {
            explicitInlineCssRulesetCreated = true;
            // skip line and text offset
            AmfUtil.readUInt29(input);
            AmfUtil.readUInt29(input);
          }

          //noinspection JSMismatchedCollectionQueryUpdate
          var cssDeclarations:Vector.<CssDeclaration> = deferredSetStyleProxy.inlineCssRuleset.declarations;
          const flags:int = input.readUnsignedByte();
          if ((flags & StyleFlags.SKIN_IN_PROJECT) != 0) {
            cssDeclarations.push(new CssSkinClassDeclaration(readDocumentFactory(), CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT));
            continue;
          }

          if ((flags & StyleFlags.EMBED_IMAGE) != 0) {
            cssDeclarations.push(CssEmbedImageDeclaration.create(propertyName, CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT,
                                                                 AmfUtil.readUInt29(input)));
            continue;
          }
          else if ((flags & StyleFlags.EMBED_SWF) != 0) {
            cssDeclarations.push(CssEmbedSwfDeclaration.create2(propertyName, CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT, input));
            continue;
          }
          else {
            cssDeclaration = CssDeclarationImpl.create(propertyName, CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT);
          }

          cssDeclarations.push(cssDeclaration);
          propertyHolder = cssDeclaration;
          if ((flags & StyleFlags.EFFECT) != 0) {
            moduleContext.effectManagerClass[new QName(getMxNs(), "setStyle")](propertyName, object);
          }
          propertyName = "value";
          break;

        case PropertyClassifier.ID:
          propertyHolder.id = AmfUtil.readString(input);
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
          propertyHolder[propertyName] = AmfUtil.readString(input);
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.STRING;
          }
          break;

        case Amf3Types.DOUBLE:
          propertyHolder[propertyName] = input.readDouble();
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.NUMBER;
          }
          break;

        case Amf3Types.INTEGER:
          propertyHolder[propertyName] = (AmfUtil.readUInt29(input) << 3) >> 3;
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.NUMBER;
          }
          break;

        case Amf3Types.TRUE:
          propertyHolder[propertyName] = true;
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.BOOL;
          }
          break;

        case Amf3Types.FALSE:
          propertyHolder[propertyName] = false;
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.BOOL;
          }
          break;

        case Amf3Types.OBJECT:
          propertyHolder[propertyName] = readObjectFromClass(stringRegistry.readNotNull(input));
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.EFFECT;
          }
          break;

        case Amf3Types.ARRAY:
          propertyHolder[propertyName] = readArray();
          break;

        case Amf3Types.VECTOR_OBJECT:
          propertyHolder[propertyName] = readVector();
          break;

        case COLOR_STYLE_MARKER:
          if (cssDeclaration == null) {
            // todo property inspector
            propertyHolder[propertyName] = input.readObject();
          }
          else {
            cssDeclaration.type = input.readByte();
            if (cssDeclaration.type == CssPropertyType.COLOR_STRING) {
              cssDeclaration.colorName = stringRegistry.read(input);
            }
            cssDeclaration.value = input.readObject();
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

        case AmfExtendedTypes.IMAGE:
          propertyHolder[propertyName] = embedImageManager.get(AmfUtil.readUInt29(input), moduleContext.imageAssetContainerClassPool,
                                                               moduleContext.project);
          break;

        case AmfExtendedTypes.SWF:
          propertyHolder[propertyName] = embedSwfManager.get(AmfUtil.readUInt29(input), moduleContext.swfAssetContainerClassPool,
                                                             moduleContext.project);
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.EMBED;
          }
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

      if (cssDeclaration != null) {
        cssDeclaration = null;
        propertyHolder = object;
      }
    }

    if (deferredSetStyleProxy != null) {
      if (deferredSetStyleProxy.inlineCssRuleset != null) {
        object.styleDeclaration = new moduleContext.inlineCssStyleDeclarationClass(deferredSetStyleProxy.inlineCssRuleset,
            moduleContext.styleManager.styleValueResolver);
      }

      deferredSetStyleProxy.inlineCssRuleset = null;
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
      var documentFactory:DocumentFactory = DocumentFactoryManager.getInstance(moduleContext.project).get2(id, DocumentFactory(context));
      factory = new moduleContext.documentFactoryClass(documentFactory, new DeferredInstanceFromBytesContext(documentFactory, this));
      moduleContext.putDocumentFactory(id, factory);
    }
    
    return factory;
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
    const length:int = input.readUnsignedShort();
    var array:Array = new Array(length);
    var mxNs:Namespace = getMxNs();
    container[new QName(mxNs, "setActualCreationPolicies")]("none");
    container[new QName(mxNs, "createdComponents")] = array;
    if (deferredMxContainers == null) {
      deferredMxContainers = new Vector.<DisplayObjectContainer>();
    }
    deferredMxContainers.push(container);

    readArrayOrVector(array, length);
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

  internal function readArray():Object {
    const length:int = input.readUnsignedShort();
    return readArrayOrVector(new Array(length), length);
  }

  private function readVector():Object {
    var vectorClass:Class = moduleContext.getVectorClass(stringRegistry.readNotNull(input));
    const fixed:Boolean = input.readBoolean();
    const length:int = input.readUnsignedShort();
    return readArrayOrVector(new vectorClass(length, fixed), length);
  }

  // support only object array without null
  internal function readArrayOrVector(array:Object, length:int):Object {
    var i:int = 0;
    while (i < length) {
      const amfType:int = input.readByte();
      switch (amfType) {
        case Amf3Types.OBJECT:
          array[i++] = readObjectFromClass(stringRegistry.readNotNull(input));
          break;

        case Amf3Types.STRING:
          array[i++] = AmfUtil.readString(input);
          break;

        case AmfExtendedTypes.STRING_REFERENCE:
          array[i++] = stringRegistry.read(input);
          break;

        case Amf3Types.DOUBLE:
          array[i++] = input.readDouble();
          break;

        case Amf3Types.FALSE:
          array[i++] = false;
          break;

        case Amf3Types.TRUE:
          array[i++] = true;
          break;

        case Amf3Types.ARRAY:
          array[i++] = readArray();
          break;

        case AmfExtendedTypes.DOCUMENT_REFERENCE:
          array[i++] = readObjectFromFactory(readDocumentFactory().newInstance());
          break;

        default:
          throw new ArgumentError("unknown property type " + amfType);
      }
    }

    return array;
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
import com.intellij.flex.uiDesigner.css.CssDeclaration;
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
  internal var inlineCssRuleset:CssRuleset;
  internal var objectDeclarationTextOffset:int;

  override flash_proxy function setProperty(name:*, value:*):void {
    if (inlineCssRuleset == null) {
      inlineCssRuleset = InlineCssRuleset.createInline(0, objectDeclarationTextOffset, file);
    }

    var cssDeclaration:CssDeclarationImpl = CssDeclarationImpl.create(name, CssRuleset.GUESS_TEXT_OFFSET_BY_PARENT);
    cssDeclaration.value = value;
    inlineCssRuleset.declarations.push(cssDeclaration);
  }

  override flash_proxy function getProperty(name:*):* {
    return inlineCssRuleset == null ? undefined : CssDeclaration(inlineCssRuleset.declarationMap[name]).value;
  }
}