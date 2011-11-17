package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.DocumentDisplayManager;
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.EmbedImageManager;
import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.ModuleContextEx;
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.UncaughtErrorManager;
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssDeclarationImpl;
import com.intellij.flex.uiDesigner.css.CssEmbedImageDeclaration;
import com.intellij.flex.uiDesigner.css.CssEmbedSwfDeclaration;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.CssSkinClassDeclaration;
import com.intellij.flex.uiDesigner.css.InlineCssRuleset;
import com.intellij.flex.uiDesigner.css.StyleDeclarationProxy;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;
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

  private var input:IDataInput;
  
  internal var stringRegistry:StringRegistry;
  private var embedImageManager:EmbedImageManager;
  private var embedSwfManager:EmbedSwfManager;

  private const stateReader:StateReader = new StateReader();
  //noinspection JSFieldCanBeLocal
  private const injectedASReader:InjectedASReader = new InjectedASReader();

  private var moduleContext:ModuleContextEx;
  internal var context:DocumentReaderContext;
  private var styleManager:StyleManagerEx;

  private var deferredMxContainers:Vector.<DisplayObjectContainer>;
  internal var objectTable:Vector.<Object>;
  
  internal var factoryContext:DeferredInstanceFromBytesContext;

  private var rootObject:Object;

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
        object = readMxmlObjectFromClass(stringRegistry.read(input));
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

  public function read(input:IDataInput, documentReaderContext:DocumentReaderContext, styleManager:StyleManagerEx, restorePrevContextAfterRead:Boolean = false):Object {
    const oldStyleManager:StyleManagerEx = this.styleManager;
    this.styleManager = styleManager;

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
    const oldRootObject:Object = rootObject;
    var object:Object = readMxmlObjectFromClass(stringRegistry.read(input), styleManager != null /* pure flash doesn't have styleManager */);
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
    stateReader.reset(t, styleManager);

    rootObject = oldRootObject;

    if (input is ByteArray) {
      assert(input.bytesAvailable == 0);
      ByteArray(input).position = 0;
    }
    this.input = oldInput;
    this.styleManager = oldStyleManager;

    return object;
  }
  
  internal function readObjectReference():Object {
    var o:Object;
    if ((o = objectTable[AmfUtil.readUInt29(input)]) == null) {
      throw new ArgumentError("must be not null");
    }

    return o;
  }

  internal function readMxmlObjectFromClass(className:String, setDocument:Boolean = false):Object {
    var clazz:Class = moduleContext.getClass(className);
    var reference:int = input.readUnsignedShort();
    var propertyName:String = stringRegistry.read(input);
    var object:Object;
    var objectDeclarationRangeMarkerId:int;

    object = new clazz();
    if (setDocument) {
      // perfomance, early set document, avoid recursive set later (see UIComponent.document setter)
      object.document = object;
      rootObject = object;
    }
    if (propertyName == "$fud_r") {
      objectDeclarationRangeMarkerId = AmfUtil.readUInt29(input);
      context.registerObjectDeclarationRangeMarkerId(object, objectDeclarationRangeMarkerId);
      propertyName = stringRegistry.read(input);
    }

    return initObject(object, reference, propertyName, objectDeclarationRangeMarkerId);
  }

  private function readObjectFromFactory(object:Object):Object {
    const reference:int = input.readUnsignedShort();
    var propertyName:String = stringRegistry.read(input);
    var objectDeclarationTextOffset:int;
    if (propertyName == "$fud_r") {
      objectDeclarationTextOffset = AmfUtil.readUInt29(input);
      context.registerObjectDeclarationRangeMarkerId(object, objectDeclarationTextOffset);
      propertyName = stringRegistry.read(input);
    }

    return initObject(object, reference, propertyName, objectDeclarationTextOffset);
  }

  private function initObject(object:Object, reference:int, propertyName:String, objectDeclarationTextOffset:int):Object {
    processReference(reference, object);
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
    var marker:int;
    for (; propertyName != null; propertyName = stringRegistry.read(input)) {
      switch ((marker = input.readByte())) {
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
          context.registerObjectWithId(propertyHolder.id, propertyHolder);
          continue;
        
        case PropertyClassifier.MX_CONTAINER_CHILDREN:
          readChildrenMxContainer(DisplayObjectContainer(propertyHolder));
          continue;

        default:
          throw new ArgumentError("unknown property \"" + propertyName + "\" classifier " + marker);
      }

      switch ((marker = input.readByte())) {
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
          propertyHolder[propertyName] = readMxmlObjectFromClass(stringRegistry.readNotNull(input));
          if (cssDeclaration != null) {
            cssDeclaration.type = CssPropertyType.EFFECT;
          }
          break;

        case Amf3Types.ARRAY:
          propertyHolder[propertyName] = readArray();
          break;

        case AmfExtendedTypes.ARRAY_IF_LENGTH_GREATER_THAN_1:
          var ta:Array = readArray() as Array;
          if (ta.length > 0) {
            propertyHolder[propertyName] = ta.length == 1 ? ta[0] : ta;
          }
          break;

        case Amf3Types.VECTOR_OBJECT:
          propertyHolder[propertyName] = readVector();
          break;

        case AmfExtendedTypes.MXML_ARRAY:
          propertyHolder[propertyName] = readMxmlArray();
          break;

        case AmfExtendedTypes.MXML_VECTOR:
          propertyHolder[propertyName] = readMxmlVector();
          break;

        case AmfExtendedTypes.COLOR_STYLE:
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

        case AmfExtendedTypes.OBJECT_REFERENCE:
          propertyHolder[propertyName] = readObjectReference();
          break;
        
        case AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE:
          propertyHolder[propertyName] = readDocumentFactory();
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

        default:
          propertyHolder[propertyName] = readExpression(marker);
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

  private function readXmlList():XMLList {
    var r:int = input.readUnsignedShort();
    var o:XMLList = new XMLList(AmfUtil.readString(input));
    processReference(r, o);
    return o;
  }

  private function readXml():XML {
    var r:int = input.readUnsignedShort();
    var o:XML = new XML(AmfUtil.readString(input));
    processReference(r, o);
    return o;
  }

  private function readReferable():Object {
    var r:int = input.readUnsignedShort();
    var o:Object = readExpression(input.readByte());
    processReference(r, o);
    return o;
  }

  private function processReference(reference:int, o:Object):void {
    if (reference != 0) {
      saveReferredObject(reference - 1, o);
    }
  }

  internal function saveReferredObject(id:int, o:Object):void {
    if (objectTable[id] != null) {
      throw new ArgumentError("must be null");
    }
    objectTable[id] = o;
  }

  private function readDocumentFactory():Object {
    var id:int = AmfUtil.readUInt29(input);
    var factory:Object = moduleContext.getDocumentFactory(id);
    if (factory == null) {
      var documentFactory:DocumentFactory = DocumentFactoryManager.getInstance().get2(id, DocumentFactory(context));
      factory = new moduleContext.documentFactoryClass(documentFactory, new DeferredInstanceFromBytesContext(documentFactory, this, styleManager));
      moduleContext.putDocumentFactory(id, factory);
    }

    return factory;
  }

  private function getOrCreateFactoryContext():DeferredInstanceFromBytesContext {
    if (factoryContext == null) {
      factoryContext = new DeferredInstanceFromBytesContext(context, this, styleManager);
    }
    
    return factoryContext;
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

  public function createDeferredMxContainersChildren(systemManager:ApplicationDomain):void {
    if (deferredMxContainers == null || deferredMxContainers.length == 0) {
      return;
    }

    var mxNs:Namespace = Namespace(systemManager.getDefinition("mx.core.mx_internal"));
    var createdComponentsQName:QName = new QName(mxNs, "createdComponents");
    var numChildrenCreatedQName:QName = new QName(mxNs, "numChildrenCreated");
    var flexEventClass:Class = Class(systemManager.getDefinition(FLEX_EVENT_CLASS_NAME));
    var controlBarClass:Class = Class(systemManager.getDefinition("mx.containers.ControlBar"));
    var panelClass:Class = Class(systemManager.getDefinition("mx.containers.Panel"));
    for each (var container:DisplayObjectContainer in deferredMxContainers) {
      // initialized equals false, because processedDescriptors equals false, so, we check inheritingStyles (if is StyleDeclarationProxy, so, already "initialized")
      if (container["inheritingStyles"] is StyleDeclarationProxy) {
        createMxContainerChildren(container, createdComponentsQName, numChildrenCreatedQName, flexEventClass, controlBarClass, container is panelClass);
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
    var sm:DocumentDisplayManager = DocumentDisplayManager(Object(container).systemManager);
    var mxNs:Namespace = Namespace(sm.getDefinitionByName("mx.core.mx_internal"));
    createMxContainerChildren(container, new QName(mxNs, "createdComponents"), new QName(mxNs, "numChildrenCreated"),
      Class(sm.getDefinitionByName(FLEX_EVENT_CLASS_NAME)), Class(sm.getDefinitionByName("mx.containers.ControlBar")),
      container is Class(sm.getDefinitionByName("mx.containers.Panel")));
  }

  private static function createMxContainerChildren(container:DisplayObjectContainer, createdComponentsQName:QName,
                                                    numChildrenCreatedQName:QName, flexEventClass:Class, controlBarClass:Class, isPanel:Boolean):void {
    var chidlren:Array = container[createdComponentsQName];
    for (var i:int = 0, n:int = chidlren.length == 1 ? 1 : chidlren.length - 1; i < n; i++) {
      container.addChild(chidlren[i]);
    }

    if (chidlren.length > 1) {
      var lastChild:DisplayObject = chidlren[i];
      if (isPanel && lastChild is controlBarClass) {
        container["rawChildren"].addChild(lastChild);
        container["setControlBar"](lastChild);
      }
      else {
        container.addChild(lastChild);
      }
    }
    
    container["processedDescriptors"] = true;
    container[numChildrenCreatedQName] = chidlren.length;
    container.dispatchEvent(new flexEventClass("contentCreationComplete"));
  }

  internal function readArray():Object {
    const length:int = input.readUnsignedShort();
    return readArrayOrVector(new Array(length), length);
  }

  internal function readMxmlArray():Object {
    const reference:int = input.readUnsignedShort();
    const length:int = input.readUnsignedShort();
    var o:Object = readArrayOrVector(new Array(length), length);
    processReference(reference, o);
    return o;
  }


  private function readVector():Object {
    var vectorClass:Class = moduleContext.getVectorClass(stringRegistry.readNotNull(input));
    const length:int = input.readUnsignedShort();
    return readArrayOrVector(new vectorClass(length), length);
  }

  private function readMxmlVector():Object {
    var vectorClass:Class = moduleContext.getVectorClass(stringRegistry.readNotNull(input));
    const fixed:Boolean = input.readBoolean();
    const reference:int = input.readUnsignedShort();
    const length:int = input.readUnsignedShort();
    var o:Object = readArrayOrVector(new vectorClass(length, fixed), length);
    processReference(reference, o);
    return o;
  }

  // support only object array without null
  internal function readArrayOrVector(array:Object, length:int):Object {
    var i:int = 0;
    while (i < length) {
      array[i++] = readExpression(input.readByte());
    }

    return array;
  }

  internal function readClassOrPropertyName():String {
    return stringRegistry.read(input);
  }

  internal function readExpression(amfType:int):* {
    switch (amfType) {
      case Amf3Types.OBJECT:
        return readMxmlObjectFromClass(stringRegistry.readNotNull(input));
        break;

      case ExpressionMessageTypes.SIMPLE_OBJECT:
        return readSimpleObject(new Object());

      case Amf3Types.STRING:
        return AmfUtil.readString(input);

      case AmfExtendedTypes.STRING_REFERENCE:
        return stringRegistry.read(input);

      case Amf3Types.INTEGER:
        return (AmfUtil.readUInt29(input) << 3) >> 3;

      case Amf3Types.DOUBLE:
        return input.readDouble();

      case Amf3Types.FALSE:
        return false;

      case Amf3Types.TRUE:
        return true;

      case Amf3Types.NULL:
        return null;

      case Amf3Types.ARRAY:
        return readArray();

      case ExpressionMessageTypes.CALL:
        return callFunction();

      case ExpressionMessageTypes.NEW:
        return constructObject();

      case AmfExtendedTypes.MXML_ARRAY:
        return readMxmlArray();

      case AmfExtendedTypes.MXML_VECTOR:
        return readMxmlVector();

      case ExpressionMessageTypes.MXML_OBJECT_REFERENCE:
        return injectedASReader.readMxmlObjectReference(input, this);

      case AmfExtendedTypes.OBJECT_REFERENCE:
        return readObjectReference();

      case AmfExtendedTypes.DOCUMENT_REFERENCE:
        return readObjectFromFactory(readDocumentFactory().newInstance());

      case ExpressionMessageTypes.VARIABLE_REFERENCE:
        return injectedASReader.readVariableReference(input, this);
        break;

      case AmfExtendedTypes.REFERABLE:
        return readReferable();
        break;

      case AmfExtendedTypes.XML_LIST:
        return readXmlList();
        break;

      case AmfExtendedTypes.XML:
        return readXml();
        break;

      case AmfExtendedTypes.OBJECT:
        return readSimpleObject(new (moduleContext.getClass(stringRegistry.readNotNull(input)))());
        break;

      case AmfExtendedTypes.CLASS_REFERENCE:
        return moduleContext.applicationDomain.getDefinition(stringRegistry.readNotNull(input));

      case AmfExtendedTypes.TRANSIENT_ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES:
        return new (moduleContext.getClass("com.intellij.flex.uiDesigner.flex.states.TransientArrayOfDeferredInstanceFromBytes"))(stateReader.readVectorOfDeferredInstanceFromBytes(this, input), getOrCreateFactoryContext());

      case AmfExtendedTypes.PERMANENT_ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES:
        return new (moduleContext.getClass("com.intellij.flex.uiDesigner.flex.states.PermanentArrayOfDeferredInstanceFromBytes"))(stateReader.readArrayOfDeferredInstanceFromBytes(this, input), getOrCreateFactoryContext());

      case AmfExtendedTypes.COLOR_STYLE:
        return input.readObject();

      default:
        throw new ArgumentError("unknown property type " + amfType);
    }    
  }

  private function callFunction():* {
    const dataLength:int = input.readUnsignedShort();
    const start:int = input.bytesAvailable;
    try {
      var qualifier:Object = rootObject;
      var qualifierName:String;
      try {
        while ((qualifierName = stringRegistry.read(input)) != null) {
          qualifier = qualifier[qualifierName];
        }
      }
      catch (e:Error) {
        // skip end
        input.readByte();

        if (e.errorID == 1069 && qualifierName == "resourceManager") {
          qualifier = moduleContext.getClass("mx.resources.ResourceManager")["getInstance"]();
        }
        else {
          //noinspection ExceptionCaughtLocallyJS
          throw e;
        }
      }

      const functionName:String = stringRegistry.readNotNull(input);
      const argumentsLength:int = input.readByte();
      // isGetter
      if (argumentsLength == -1) {
        return qualifier[functionName];
      }
      else if (argumentsLength == 0) {
        return qualifier[functionName]();
      }
      else {
        return (qualifier[functionName] as Function).apply(null, readArrayOrVector(new Array(argumentsLength), argumentsLength));
      }
    }
    catch (e:Error) {
      handleCallExpressionBindingError(e, dataLength, start);
    }

    return null;
  }

  private function constructObject():Object {
    var clazz:Class = moduleContext.getClass(stringRegistry.readNotNull(input));
    var argumentsLength:int = input.readByte();

    var dataLength:int;
    var start:int;
    if ((argumentsLength & 1) != 0) {
      dataLength = input.readUnsignedShort();
      start = input.bytesAvailable;
    }

    argumentsLength >>= 1;

    try {
      if (argumentsLength == 0) {
        return new clazz();
      }
      else {
        switch (argumentsLength) {
          case 1:
            return new clazz(readExpression(input.readByte()));
          case 2:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()));
          case 3:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()));
          case 4:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()));
          case 5:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()));
          case 6:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()));
          case 7:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()),
                             readExpression(input.readByte()));
          case 8:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()),
                             readExpression(input.readByte()), readExpression(input.readByte()));
          case 9:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()),
                             readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()));
          case 10:
            return new clazz(readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()),
                             readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()), readExpression(input.readByte()));
        }

        UncaughtErrorManager.instance.logWarning("Can't execute binding, constructorArguments is too long");
      }
    }
    catch (e:Error) {
      if (dataLength == 0) {
        throw e;
      }
      else {
        handleCallExpressionBindingError(e, dataLength, start);
      }
    }

    return null;
  }

  private function handleCallExpressionBindingError(e:Error, dataLength:int, start:int):void {
    UncaughtErrorManager.instance.logWarning3("Can't execute binding", e);

    var unreadLength:int = dataLength - (start - input.bytesAvailable);
    if (unreadLength > 0) {
      input.readBytes(new ByteArray(), 0, unreadLength);
    }
  }
  
  internal function readSimpleObject(o:Object):Object {
    var propertyName:String;
    while ((propertyName = stringRegistry.read(input)) != null) {
      if (propertyName == "2") {
        saveReferredObject(AmfUtil.readUInt29(input), o);
      }
      else {
        o[propertyName] = readExpression(input.readByte());
      }
    }
    
    return o;
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

final class PropertyClassifier {
  public static const PROPERTY:int = 0;
  public static const STYLE:int = 1;
  
  public static const ID:int = 2;

  public static const MX_CONTAINER_CHILDREN:int = 4;
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
    if (inlineCssRuleset == null) {
      return undefined;
    }
    else {
      var declaration:* = inlineCssRuleset.declarationMap[name];
      return declaration === undefined ? undefined : CssDeclaration(declaration).value;
    }
  }
}