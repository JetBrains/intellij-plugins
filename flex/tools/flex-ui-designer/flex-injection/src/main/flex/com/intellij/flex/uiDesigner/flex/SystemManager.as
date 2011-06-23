package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.ResourceBundleProvider;
import com.intellij.flex.uiDesigner.UiErrorHandler;
import com.intellij.flex.uiDesigner.css.RootStyleManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Shape;
import flash.display.Sprite;
import flash.display.Stage;
import flash.events.Event;
import flash.events.EventPhase;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.geom.Rectangle;
import flash.geom.Transform;
import flash.system.ApplicationDomain;
import flash.text.TextFormat;
import flash.utils.Dictionary;

import mx.core.FlexGlobals;
import mx.core.IChildList;
import mx.core.IFlexDisplayObject;
import mx.core.IFlexModule;
import mx.core.IRawChildrenContainer;
import mx.core.IUIComponent;
import mx.core.Singleton;
import mx.core.UIComponent;
import mx.core.UIComponentGlobals;
import mx.core.mx_internal;
import mx.effects.EffectManager;
import mx.events.DynamicEvent;
import mx.events.FlexEvent;
import mx.managers.DragManagerImpl;
import mx.managers.FocusManager;
import mx.managers.IFocusManager;
import mx.managers.IFocusManagerContainer;
import mx.managers.ILayoutManagerClient;
import mx.managers.ISystemManager;
import mx.managers.LayoutManager;
import mx.managers.PopUpManagerImpl;
import mx.managers.SystemManagerGlobals;
import mx.managers.ToolTipManagerImpl;
import mx.managers.systemClasses.ActiveWindowManager;
import mx.modules.ModuleManagerGlobals;
import mx.resources.ResourceManager;
import mx.styles.ISimpleStyleClient;
import mx.styles.IStyleClient;
import mx.styles.StyleManager;

use namespace mx_internal;

public class SystemManager extends Sprite implements ISystemManager, SystemManagerSB, IFocusManagerContainer {
  // offset due: 0 child of system manager is application
  internal static const OFFSET:int = 1;

  private static const LAYOUT_MANAGER_FQN:String = "mx.managers::ILayoutManager";
  private static const POP_UP_MANAGER_FQN:String = "mx.managers::IPopUpManager";
  private static const TOOL_TIP_MANAGER_FQN:String = "mx.managers::IToolTipManager2";
  internal static const SYSTEM_MANAGER_CHILD_MANAGER:String = "mx.managers::ISystemManagerChildManager";

  private var flexModuleFactory:FlexModuleFactory;

  private var uiErrorHandler:UiErrorHandler;

  private const implementations:Dictionary = new Dictionary();

  public function init(moduleFactory:Object, stage:Stage, uiErrorHandler:UiErrorHandler, resourceBundleProvider:ResourceBundleProvider):void {
    var layoutManager:LayoutManager = LayoutManager(UIComponentGlobals.layoutManager);
    if (layoutManager == null) {
      UIComponentGlobals.designMode = true;
      UIComponentGlobals.catchCallLaterExceptions = true;
      SystemManagerGlobals.topLevelSystemManagers[0] = new TopLevelSystemManager(stage);

      Singleton.registerClass(LAYOUT_MANAGER_FQN, LayoutManager);
      layoutManager = new LayoutManager(stage.getChildAt(0), uiErrorHandler);
      UIComponentGlobals.layoutManager = layoutManager;

      new ResourceManager(resourceBundleProvider);
    }

    layoutManager.waitFrame();

    this.uiErrorHandler = uiErrorHandler;
    addRealEventListener("initializeError", uiInitializeOrCallLaterErrorHandler);
    addRealEventListener("callLaterError", uiInitializeOrCallLaterErrorHandler);

    flexModuleFactory = FlexModuleFactory(moduleFactory);

    //  if not null — ModuleManagerGlobals class is shareable for this Document
    if (ModuleManagerGlobals.managerSingleton == null) {
      ModuleManagerGlobals.managerSingleton = new ModuleManager(flexModuleFactory);
    }

    Singleton.registerClass(POP_UP_MANAGER_FQN, PopUpManagerImpl);
    Singleton.registerClass(TOOL_TIP_MANAGER_FQN, ToolTipManagerImpl);

    implementations["mx.managers::IActiveWindowManager"] = new ActiveWindowManager();
    implementations[SYSTEM_MANAGER_CHILD_MANAGER] = this;

    Singleton.registerClass("mx.styles::IStyleManager2", RootStyleManager);
    Singleton.registerClass("mx.resources::IResourceManager", ResourceManager);
    Singleton.registerClass("mx.managers::IDragManager", DragManagerImpl);
    Singleton.registerClass("mx.managers::IHistoryManager", HistoryManagerImpl);
    Singleton.registerClass("mx.managers::IBrowserManager", BrowserManagerImpl);

    if (ApplicationDomain.currentDomain.hasDefinition("mx.core::TextFieldFactory")) {
      Singleton.registerClass("mx.core::ITextFieldFactory", Class(getDefinitionByName("mx.core::TextFieldFactory")));
    }

    // investigate, how we can add support for custom components — patch EffectManager or use IntellIJ IDEA index for effect annotations (the same as compiler — CompilationUnit)
    EffectManager.registerEffectTrigger("addedEffect", "added");
    EffectManager.registerEffectTrigger("creationCompleteEffect", "creationComplete");
    EffectManager.registerEffectTrigger("focusInEffect", "focusIn");
    EffectManager.registerEffectTrigger("focusOutEffect", "focusOut");
    EffectManager.registerEffectTrigger("hideEffect", "hide");
    EffectManager.registerEffectTrigger("mouseDownEffect", "mouseDown");
    EffectManager.registerEffectTrigger("mouseUpEffect", "mouseUp");
    EffectManager.registerEffectTrigger("moveEffect", "move");
    EffectManager.registerEffectTrigger("removedEffect", "removed");
    EffectManager.registerEffectTrigger("resizeEffect", "resize");
    EffectManager.registerEffectTrigger("rollOutEffect", "rollOut");
    EffectManager.registerEffectTrigger("rollOverEffect", "rollOver");
    EffectManager.registerEffectTrigger("showEffect", "show");
  }

  private function uiInitializeOrCallLaterErrorHandler(event:DynamicEvent):void {
    uiErrorHandler.handleUiError(event.error, event.source);
  }

  private var _document:DisplayObject;
  public function get document():Object {
    return _document;
  }

  public function set document(value:Object):void {
    throw new Error("forbidden");
  }

  private var _toolTipChildren:SystemChildList;
  public function get toolTipChildren():IChildList {
    if (_toolTipChildren == null) {
      _toolTipChildren = new SystemChildList(this, "topMostIndex", "toolTipIndex");
    }

    return _toolTipChildren;
  }

  private var _popUpChildren:SystemChildList;
  public function get popUpChildren():IChildList {
    if (_popUpChildren == null) {
      _popUpChildren = new SystemChildList(this, "noTopMostIndex", "topMostIndex");
    }

    return _popUpChildren;
  }

  private var _cursorChildren:SystemChildList;
  public function get cursorChildren():IChildList {
    if (_cursorChildren == null) {
      _cursorChildren = new SystemChildList(this, "toolTipIndex", "cursorIndex");
    }

    return _cursorChildren;
  }

  // The index of the highest child that is a cursor
  private var _cursorIndex:int = 0;
  internal function get cursorIndex():int {
    return _cursorIndex;
  }

  internal function set cursorIndex(value:int):void {
    _cursorIndex = value;
  }

  override public function setChildIndex(child:DisplayObject, index:int):void {
    super.setChildIndex(child, OFFSET + index);
  }
  
  public function $setChildIndex(child:DisplayObject, index:int):void {
    super.setChildIndex(child, index);
  }

  override public function getChildIndex(child:DisplayObject):int {
    return super.getChildIndex(child) - OFFSET;
  }

  public function $getChildIndex(child:DisplayObject):int {
    return super.getChildIndex(child);
  }

  override public function addChild(child:DisplayObject):DisplayObject {
    var addIndex:int = numChildren;
    if (child.parent == this) {
      addIndex--;
    }

    return addChildAt(child, addIndex);
  }

  override public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    noTopMostIndex++;

    var oldParent:DisplayObjectContainer = child.parent;
    if (oldParent) {
      oldParent.removeChild(child);
    }

    return addRawChildAt(child, index + OFFSET);
  }

  override public function getObjectsUnderPoint(point:Point):Array {
    var children:Array = [];
    // Get all the children that aren't tooltips and cursors.
    var n:int = _topMostIndex;
    for (var i:int = 0; i < n; i++) {
      var child:DisplayObject = super.getChildAt(i);
      if (child is DisplayObjectContainer) {
        var temp:Array = DisplayObjectContainer(child).getObjectsUnderPoint(point);
        if (temp != null) {
          children = children.concat(temp);
        }
      }
    }

    return children;
  }

  internal function $getObjectsUnderPoint(point:Point):Array {
    return super.getObjectsUnderPoint(point);
  }

  override public function contains(child:DisplayObject):Boolean {
    if (super.contains(child)) {
      if (child.parent == this) {
        var childIndex:int = super.getChildIndex(child);
        if (childIndex < _noTopMostIndex) {
          return true;
        }
      }
      else {
        for (var i:int = 0; i < _noTopMostIndex; i++) {
          var myChild:DisplayObject = super.getChildAt(i);
          if (myChild is IRawChildrenContainer) {
            if (IRawChildrenContainer(myChild).rawChildren.contains(child)) {
              return true;
            }
          }
          if (myChild is DisplayObjectContainer && DisplayObjectContainer(myChild).contains(child)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  internal function $contains(child:DisplayObject):Boolean {
    return super.contains(child);
  }

  private var _toolTipIndex:int = 1; // see comment for _noTopMostIndex init value
  internal function get toolTipIndex():int {
    return _toolTipIndex;
  }

  internal function set toolTipIndex(value:int):void {
    var delta:int = value - _toolTipIndex;
    _toolTipIndex = value;
    cursorIndex += delta;
  }

  private var _topMostIndex:int;
  internal function get topMostIndex():int {
    return _topMostIndex;
  }

  internal function set topMostIndex(value:int):void {
    var delta:int = value - _topMostIndex;
    _topMostIndex = value;
    toolTipIndex += delta;
  }

  private var _noTopMostIndex:int = 1; // flex sdk preloader set it as 1 for mouse catcher (missed in our case) and 2 as app (we add app directly)
  internal function get noTopMostIndex():int {
    return _noTopMostIndex;
  }

  //noinspection JSUnusedGlobalSymbols
  internal function set noTopMostIndex(value:int):void {
    var delta:int = value - _noTopMostIndex;
    _noTopMostIndex = value;
    topMostIndex += delta;
  }

  override public function get numChildren():int {
    return noTopMostIndex - OFFSET;
  }

  internal function addRawChildAt(child:DisplayObject, index:int):DisplayObject {
    addingChild(child);
    super.addChildAt(child, index);

    if (child.hasEventListener(FlexEvent.ADD)) {
      child.dispatchEvent(new FlexEvent(FlexEvent.ADD));
    }

    if (child is IUIComponent) {
      IUIComponent(child).initialize();
    }

    return child;
  }

  override public function removeChild(child:DisplayObject):DisplayObject {
    _noTopMostIndex--;
    return removeRawChild(child);
  }

  override public function removeChildAt(index:int):DisplayObject {
    _noTopMostIndex--;
    return $removeChildAt(index + OFFSET);
  }

  internal function removeRawChild(child:DisplayObject):DisplayObject {
    if (child.hasEventListener(FlexEvent.REMOVE)) {
      child.dispatchEvent(new FlexEvent(FlexEvent.REMOVE));
    }

    super.removeChild(child);

    if (child is IUIComponent) {
      IUIComponent(child).parentChanged(null);
    }

    return child;
  }

  internal function $removeChildAt(index:int):DisplayObject {
    return removeRawChild(super.getChildAt(index));
  }

  internal function $getChildAt(index:int):DisplayObject {
    return super.getChildAt(index);
  }

  public function setUserDocument(object:DisplayObject):void {
    removeEventHandlers();
    
    if (_document != null) {
      removeRawChild(_document);
    }
    
    _document = object;
    // We can not leave empty FlexGlobals.topLevelApplication, as example, VideoPlayer uses FlexGlobals.topLevelApplication as parent when opening fullscreen
    // but we can not set it in SystemManager, because it wants UIComponent (for style)
    FlexGlobals.topLevelApplication = object;

    if (object is IUIComponent) {
      var documentUI:IUIComponent = IUIComponent(_document);
      _explicitDocumentSize.width = documentUI.explicitWidth;
      _explicitDocumentSize.height = documentUI.explicitHeight;
    }
    
    try {
      StyleManager.tempStyleManagerForTalentAdobeEngineers = flexModuleFactory.styleManager;
      addRawChildAt(object, 0);
    }
    finally {
      StyleManager.tempStyleManagerForTalentAdobeEngineers = null;
    }
  }

  private const _explicitDocumentSize:Rectangle = new Rectangle();

  public function get explicitDocumentSize():Rectangle {
    return _explicitDocumentSize;
  }

  public function setActualDocumentSize(w:Number, h:Number):void {
    // originally set by setLayoutBoundsSize, but the Application without explicit size hangs on Stage and listen to resize - we can not change this behavior without the injection of the byte-code
    _document.width = w;
    _document.height = h;
  }

  internal function addingChild(object:DisplayObject):void {
    var uiComponent:IUIComponent = object as IUIComponent;
    if (uiComponent != null) {
      uiComponent.systemManager = this;
      if (uiComponent.document == null) {
        uiComponent.document = _document;
      }
    }

    if (object is IFlexModule && IFlexModule(object).moduleFactory == null) {
      IFlexModule(object).moduleFactory = flexModuleFactory;
    }

    // skip font context, not need for us

    if (object is ILayoutManagerClient) {
      ILayoutManagerClient(object).nestLevel = 2;
    }

		// skip doubleClickEnabled

    if (uiComponent != null) {
      uiComponent.parentChanged(this);
    }

    if (object is ISimpleStyleClient) {
      var isStyleClient:Boolean = object is IStyleClient;
      if (isStyleClient) {
        IStyleClient(object).regenerateStyleCache(true);
      }
      ISimpleStyleClient(object).styleChanged(null);
      if (isStyleClient) {
        IStyleClient(object).notifyStyleChangeInChildren(null, true);
      }

      if (object is UIComponent) {
        var ui:UIComponent = UIComponent(uiComponent);
        ui.initThemeColor();
        ui.stylesInitialized();
      }
    }
	}

  public function get preloadedRSLs():Dictionary {
    return null;
  }

  public function allowDomain(... rest):void {
  }

  public function allowInsecureDomain(... rest):void {
  }

  public function callInContext(fn:Function, thisArg:Object, argArray:Array, returns:Boolean = true):* {
    return null;
  }

  public function create(... params):Object {
    return flexModuleFactory.create(params);
  }

  public function getImplementation(interfaceName:String):Object {
    return implementations[interfaceName];
  }

  public function info():Object {
    return null;
  }

  public function registerImplementation(interfaceName:String, impl:Object):void {
    throw new Error("");
  }

  public function get embeddedFontList():Object {
    return null;
  }

  public function get focusPane():Sprite {
    return null;
  }

  public function set focusPane(value:Sprite):void {
  }

  public function get isProxy():Boolean {
    return true; // so, UIComponent will "keep the existing proxy", see UIComponent#get systemManager
  }

  public function get numModalWindows():int {
    return 0;
  }

  public function set numModalWindows(value:int):void {
  }

  public function get rawChildren():IChildList {
    return null;
  }

  private var _screen:Rectangle;
  public function get screen():Rectangle {
    if (_screen == null) {
      _screen = new Rectangle();
    }

    _screen.width = super.parent.width;
    _screen.height = super.parent.height;
    return _screen;
  }

  public function get topLevelSystemManager():ISystemManager {
    return this;
  }

  public function getDefinitionByName(name:String):Object {
    return ApplicationDomain.currentDomain.getDefinition(name);
  }

  public function isTopLevel():Boolean {
    return true;
  }

  public function isFontFaceEmbedded(tf:TextFormat):Boolean {
    return false;
  }

  public function isTopLevelRoot():Boolean {
    return true;
  }

  public function getTopLevelRoot():DisplayObject {
    return this;
  }

  public function getSandboxRoot():DisplayObject {
    return this;
  }

  flex::v4_5
  public function getVisibleApplicationRect(bounds:Rectangle = null, skipToSandboxRoot:Boolean = false):Rectangle {
    return commonGetVisibleApplicationRect(bounds);
  }

  flex::v4_1
  public function getVisibleApplicationRect(bounds:Rectangle = null):Rectangle {
    return commonGetVisibleApplicationRect(bounds);
  }
  
  private function commonGetVisibleApplicationRect(bounds:Rectangle):Rectangle {
    if (bounds == null) {
      bounds = getBounds(stage);
    }

    return bounds;
  }

  public function deployMouseShields(deploy:Boolean):void {
  }

  public function invalidateParentSizeAndDisplayList():void {
  }

  override public function get parent():DisplayObjectContainer {
    return null;
  }

  private static var fakeTransform:Transform;

  override public function get transform():Transform {
    if (fakeTransform == null) {
      fakeTransform = new Transform(new Shape());
    }
    return fakeTransform;
  }

  private var _focusManager:FocusManager;
  public function get focusManager():IFocusManager {
    if (_focusManager == null) {
      _focusManager = new FocusManager(this);
    }
    
    return _focusManager;
  }

  public function set focusManager(value:IFocusManager):void {
  }

  public function get defaultButton():IFlexDisplayObject {
    return null;
  }

  public function set defaultButton(value:IFlexDisplayObject):void {
  }

  public function get systemManager():ISystemManager {
    return this;
  }

  // mx.managers::ISystemManagerChildManager, ChildManager, "cm.notifyStyleChangeInChildren(styleProp, true);" in CSSStyleDeclaration
  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function notifyStyleChangeInChildren(styleProp:String, recursive:Boolean):void {
  }

  private var proxiedListeners:Dictionary;
  private var proxiedListenersInCapture:Dictionary;

  override public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0,
                                            useWeakReference:Boolean = false):void {
    if (type == MouseEvent.CLICK || type == MouseEvent.MOUSE_DOWN || type == MouseEvent.MOUSE_UP || type == MouseEvent.MOUSE_MOVE ||
      type == MouseEvent.MOUSE_OVER || type == MouseEvent.MOUSE_OUT || type == MouseEvent.ROLL_OUT || type == MouseEvent.ROLL_OVER ||
      type == MouseEvent.MIDDLE_CLICK || type == MouseEvent.MOUSE_WHEEL ||
      type == FlexEvent.RENDER || type == FlexEvent.ENTER_FRAME) {

      var map:Dictionary;
      if (useCapture) {
        if (proxiedListenersInCapture == null) {
          proxiedListenersInCapture = new Dictionary();
        }
        map = proxiedListenersInCapture;
      }
      else {
        if (proxiedListeners == null) {
          proxiedListeners = new Dictionary();
        }
        map = proxiedListeners;
      }

      const rawType:String = getRawEventType(type);

      var listeners:Vector.<Function> = map[rawType];
      if (listeners == null) {
        listeners = new Vector.<Function>();
        map[rawType] = listeners;
      }

      if (listeners.length == 0) {
        if (useCapture) {
          stage.addEventListener(rawType, proxyEventHandler, true);
        }
        else {
          stage.addEventListener(rawType, proxyEventHandler);
        }
      }

      if (listeners.indexOf(listener) == -1) {
        //trace("ADDED", type,  useCapture);
        listeners.push(listener);
      }
    }
    else if (!(type == "cursorManagerRequest" || type == "dragManagerRequest" || type == "initManagerRequest" || type == "systemManagerRequest" || type == "tooltipManagerRequest")) {
      super.addEventListener(type, listener, useCapture, priority, useWeakReference);
    }
  }

  private static function getRawEventType(type:String):String {
    if (type == FlexEvent.RENDER) {
      return Event.RENDER;
    }
    else if (type == FlexEvent.ENTER_FRAME) {
      return Event.ENTER_FRAME;
    }
    else {
      return type;
    }
  }

  override public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
    var map:Dictionary;
    if (useCapture) {
      if (proxiedListeners != null) {
        map = proxiedListenersInCapture;
      }
    }
    else if (proxiedListeners != null) {
      map = proxiedListeners;
    }

    var listeners:Vector.<Function> = map == null ? null : map[type];
    if (listeners == null) {
      super.removeEventListener(type, listener, useCapture);
      return;
    }

    var index:int = listeners.indexOf(listener);
    //trace("REMOVED", index, type, useCapture);
    if (index == -1) {
      return;
    }

    listeners.splice(index, 1);
    if (listeners.length == 0) {
      //trace("REMOVED proxyEventHandler", useCapture);
      stage.removeEventListener(getRawEventType(type), proxyEventHandler, useCapture);
    }
  }

  private function proxyEventHandler(event:Event):void {
    trace("EXECUTED", event, event.eventPhase == EventPhase.CAPTURING_PHASE);
    var listeners:Vector.<Function>;
    if (event.eventPhase == EventPhase.CAPTURING_PHASE) {
      listeners = proxiedListenersInCapture[event.type];
    }
    else {
      listeners = proxiedListeners[event.type];
    }

    for each (var listener:Function in listeners.slice() /* copy, because may be removed in removeEventListener (side effect by call listener) */) {
      listener(event);
    }
  }

  public function removeEventHandlers():void {
    if (stage != null) {
      removeProxyEventHandlers2(proxiedListeners, false);
      removeProxyEventHandlers2(proxiedListenersInCapture, true);
    }
  }

  private function removeProxyEventHandlers2(map:Dictionary, useCapture:Boolean):void {
    if (map != null) {
      for (var type:String in map) {
        stage.removeEventListener(type, proxyEventHandler, useCapture);
        map[type].length = 0;
      }
    }
  }

  public function addRealEventListener(type:String, listener:Function):void {
    super.addEventListener(type, listener);
  }

  public function removeRealEventListener(type:String, listener:Function):void {
    super.removeEventListener(type, listener);
  }

  flex::v4_5 {
    include 'baseFlexModuleFactoryImpl45.as';
  }
}
}