package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.ComponentInfoProvider;
import com.intellij.flex.uiDesigner.DocumentDisplayManager;
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.ResourceBundleProvider;
import com.intellij.flex.uiDesigner.UiErrorHandler;
import com.intellij.flex.uiDesigner.designSurface.LayoutManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Stage;
import flash.events.Event;
import flash.events.EventPhase;
import flash.events.MouseEvent;
import flash.geom.Rectangle;
import flash.system.ApplicationDomain;
import flash.text.TextFormat;
import flash.utils.Dictionary;
import flash.utils.getQualifiedClassName;

import mx.core.EmbeddedFontRegistry;
import mx.core.FlexGlobals;
import mx.core.IChildList;
import mx.core.IFlexDisplayObject;
import mx.core.IFlexModule;
import mx.core.IInvalidating;
import mx.core.ILayoutElement;
import mx.core.IRawChildrenContainer;
import mx.core.IUIComponent;
import mx.core.IVisualElement;
import mx.core.IVisualElementContainer;
import mx.core.Singleton;
import mx.core.UIComponent;
import mx.core.UIComponentGlobals;
import mx.core.mx_internal;
import mx.effects.EffectManager;
import mx.events.DynamicEvent;
import mx.events.FlexEvent;
import mx.managers.CursorManager;
import mx.managers.DragManagerImpl;
import mx.managers.IFocusManager;
import mx.managers.IFocusManagerContainer;
import mx.managers.ILayoutManagerClient;
import mx.managers.ISystemManager;
import mx.managers.LayoutManager;
import mx.managers.PopUpManagerImpl;
import mx.managers.SystemManagerGlobals;
import mx.modules.ModuleManagerGlobals;
import mx.resources.ResourceManager;
import mx.styles.ISimpleStyleClient;
import mx.styles.IStyleClient;
import mx.styles.StyleManager;

import spark.components.Application;
import spark.components.SkinnableContainer;
import spark.components.supportClasses.GroupBase;
import spark.layouts.supportClasses.LayoutBase;

use namespace mx_internal;

// must be IFocusManagerContainer, it is only way how UIComponent can find focusManager (see UIComponent.focusManager)
public final class FlexDocumentDisplayManager extends FlexDocumentDisplayManagerBase implements ISystemManager, DocumentDisplayManager, IFocusManagerContainer {
  internal static const SYSTEM_MANAGER_CHILD_MANAGER:String = "mx.managers::ISystemManagerChildManager";

  private var flexModuleFactory:FlexModuleFactory;

  private var uiErrorHandler:UiErrorHandler;

  private var mainFocusManager:MainFocusManagerSB;

  private static var stageForAdobeDummies:Stage;

  private var fakeTopLevelApplication:FakeTopLevelApplication;

  public function get componentInfoProvider():ComponentInfoProvider {
    return FlexComponentInfoProvider.instance;
  }

  public function get sharedInitialized():Boolean {
    return UIComponentGlobals.layoutManager != null;
  }

  override public function get stage():Stage {
    return stageForAdobeDummies;
  }

  private var _realStage:Stage;
  public function get realStage():Stage {
    return _realStage || super.stage;
  }

  // flex can cause addEventListener (our fake addEventListener impl can requires real stage) before we have been added to stage
  private function addedToStageHandler(event:Event):void {
    removeEventListener(Event.ADDED_TO_STAGE, addedToStageHandler);
    _realStage = null;
  }

  public function initShared(stageForAdobeDummies:Stage, resourceBundleProvider:ResourceBundleProvider, uiErrorHandler:UiErrorHandler):void {
    FlexDocumentDisplayManager.stageForAdobeDummies = stageForAdobeDummies;

    UIComponentGlobals.designMode = true;
    UIComponentGlobals.catchCallLaterExceptions = true;
    SystemManagerGlobals.topLevelSystemManagers[0] = new TopLevelSystemManagerProxy();
    SystemManagerGlobals.bootstrapLoaderInfoURL = "app:/_Main.swf";

    Singleton.registerClass(LAYOUT_MANAGER_FQN, mx.managers.LayoutManager);
    UIComponentGlobals.layoutManager = new mx.managers.LayoutManager(uiErrorHandler);

    new ResourceManager(resourceBundleProvider);
    ModuleManagerGlobals.managerSingleton = new ModuleManager();

    Singleton.registerClass(POP_UP_MANAGER_FQN, PopUpManagerImpl);
    Singleton.registerClass(TOOL_TIP_MANAGER_FQN, ToolTipManager);
    Singleton.registerClass("mx.resources::IResourceManager", ResourceManager);
    Singleton.registerClass("mx.managers::ICursorManager", CursorManager);
    Singleton.registerClass("mx.managers::IDragManager", DragManagerImpl);
    Singleton.registerClass("mx.managers::IHistoryManager", HistoryManagerImpl);
    Singleton.registerClass("mx.managers::IBrowserManager", BrowserManagerImpl);
    if (ApplicationDomain.currentDomain.hasDefinition("mx.core::TextFieldFactory")) {
      Singleton.registerClass("mx.core::ITextFieldFactory", Class(getDefinitionByName("mx.core::TextFieldFactory")));
    }

    Singleton.registerClass("mx.core::IEmbeddedFontRegistry", EmbeddedFontRegistry);

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

  public function getDefinitionByName(name:String):Object {
    return ApplicationDomain.currentDomain.getDefinition(name);
  }

  override public function init(stage:Stage, moduleFactory:Object, uiErrorHandler:UiErrorHandler, mainFocusManager:MainFocusManagerSB,
                                documentFactory:Object):void {
    if (super.stage == null) {
      super.addEventListener(Event.ADDED_TO_STAGE, addedToStageHandler);
      _realStage = stage;
    }

    super.init(stage, moduleFactory, uiErrorHandler, mainFocusManager, documentFactory);

    this.mainFocusManager = mainFocusManager;
    this.uiErrorHandler = uiErrorHandler;

    mx.managers.LayoutManager(UIComponentGlobals.layoutManager).waitFrame();

    super.addEventListener(INITIALIZE_ERROR_EVENT_TYPE, uiInitializeOrCallLaterErrorHandler);
    super.addEventListener("callLaterError", uiInitializeOrCallLaterErrorHandler);

    flexModuleFactory = FlexModuleFactory(moduleFactory);
    implementations[SYSTEM_MANAGER_CHILD_MANAGER] = this;
    _focusManager = new DocumentFocusManager(this);
  }

  public function get isProxy():Boolean {
    // must be false, otherwise UIComponet will ignore our specified actual size
    // see validateDisplayList

    // but must be true if we want to keep existing systemManager (see get systemManager)

    // Adobe, burn in hell
    return !mx.managers.LayoutManager(UIComponentGlobals.layoutManager).adobePleaseUseSpecifiedActualSize;
  }

  public function setDocumentBounds(w:int, h:int):void {
    ILayoutElement(_document).setLayoutBoundsSize(w, h);
  }

  public function getImplementation(interfaceName:String):Object {
    var r:Object = implementations[interfaceName];
    // PopUpManager requires IActiveWindowManager, IDEA-73806
    if (r == null && interfaceName == "mx.managers::IActiveWindowManager") {
      r = new ActiveWindowManagerForAdobeDummies();
      implementations[interfaceName] = r;
    }
    return r;
  }

  private function uiInitializeOrCallLaterErrorHandler(event:DynamicEvent):void {
    var source:Object = event.source;
    const isInitError:Boolean = event.type == INITIALIZE_ERROR_EVENT_TYPE;
    uiErrorHandler.handleUiError(event.error, source, source == null ? null : "Can't " + (isInitError ? "initialize" : "call callLater handler") + " " + source.toString());

    if (isInitError) {
      var visualElement:IVisualElement = source as IVisualElement;
      if (visualElement != null) {
        if (visualElement is ILayoutManagerClient) {
          ILayoutManagerClient(visualElement).nestLevel = 0; // skip from layout
        }

        var visualElementContainer:IVisualElementContainer = visualElement.parent as IVisualElementContainer;
        if (visualElementContainer != null) {
          // cannot remove right now, because we cannot cancel all other actions executed by Group after addDisplayObjectToDisplayList
          // (as example, notifyListeners may dispatch ElementExistenceEvent.ELEMENT_ADD)
          removeInvalidVisualElementLater(visualElement, visualElementContainer);
        }
        else {
          visualElement.parent.removeChild(DisplayObject(visualElement));
        }
      }
    }
  }

  private static function removeInvalidVisualElementLater(visualElement:IVisualElement,
                                                          visualElementContainer:IVisualElementContainer):void {
    UIComponent(visualElementContainer).callLater(function ():void {
      visualElementContainer.removeElement(visualElement);
    });
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

  override public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    noTopMostIndex++;

    var oldParent:DisplayObjectContainer = child.parent;
    if (oldParent) {
      oldParent.removeChild(child);
    }

    return addRawChildAt(child, index + OFFSET);
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

  internal function addRawChildAt(child:DisplayObject, index:int):DisplayObject {
    addingChild(child);
    super.addChildAt(child, index);
    childAdded(child);
    return child;
  }

  private static function childAdded(child:DisplayObject):void {
    if (child.hasEventListener(FlexEvent.ADD)) {
      child.dispatchEvent(new FlexEvent(FlexEvent.ADD));
    }

    if (child is IUIComponent) {
      IUIComponent(child).initialize();
    }
  }

  internal function addRawChild(child:DisplayObject):DisplayObject {
    addingChild(child);
    super.addChild(child);
    childAdded(child);
    return child;
  }

  override internal function removeRawChild(child:DisplayObject):DisplayObject {
    if (child.hasEventListener(FlexEvent.REMOVE)) {
      child.dispatchEvent(new FlexEvent(FlexEvent.REMOVE));
    }

    $removeChild(child);

    if (child is IUIComponent) {
      IUIComponent(child).parentChanged(null);
    }

    return child;
  }

  public function setStyleManagerForTalentAdobeEngineers(value:Boolean):void {
    StyleManager.tempStyleManagerForTalentAdobeEngineers = value ? flexModuleFactory.styleManager : null;
  }

  public function setDocument(object:DisplayObject):void {
    removeEventHandlers();
    
    if (_document != null) {
      removeRawChild(_document);
    }
    
    _document = object;
    
    // early set, before activated()
    var topLevelApplication:Object = object;
    if (object is Application) {
      topLevelApplication = object;
      if (fakeTopLevelApplication != null) {
        fakeTopLevelApplication = null;
      }
    }
    else {
      if (fakeTopLevelApplication == null) {
        fakeTopLevelApplication = new FakeTopLevelApplication(this, flexModuleFactory);
      }

      topLevelApplication = fakeTopLevelApplication;
      fakeTopLevelApplication.setUIComponent(object as UIComponent);
    }
    FlexGlobals.topLevelApplication = topLevelApplication;

    var topLevelSystemManagerProxy:TopLevelSystemManagerProxy = TopLevelSystemManagerProxy(SystemManagerGlobals.topLevelSystemManagers[0]);
    topLevelSystemManagerProxy.activeSystemManager = this;

    if (object is IUIComponent) {
      UIComponent(object).focusManager = _focusManager;

      var documentUI:IUIComponent = IUIComponent(_document);
      _explicitDocumentWidth = initialExplicitDimension(documentUI.explicitWidth);
      _explicitDocumentHeight = initialExplicitDimension(documentUI.explicitHeight);
    }

    try {
      addRawChildAt(object, 0);

      var v:IInvalidating = document as IInvalidating;
      if (v != null) {
        v.validateNow();
      }
    }
    catch (e:Error) {
      if (super.contains(_document)) {
        removeRawChild(_document);
      }

      _document = null;
      if (fakeTopLevelApplication != null) {
        fakeTopLevelApplication.setUIComponent(null);
      }

      if (FlexGlobals.topLevelApplication == topLevelApplication) {
        FlexGlobals.topLevelApplication = null;
      }
      if (topLevelSystemManagerProxy.activeSystemManager == this) {
        topLevelSystemManagerProxy.activeSystemManager = null;
      }

      throw e;
    }

    var viewNavigatorApplicationBaseClass:Class = ModuleContext(documentFactory.moduleContext).getClassIfExists("spark.components.supportClasses.ViewNavigatorApplicationBase");
    if (viewNavigatorApplicationBaseClass != null && object is viewNavigatorApplicationBaseClass) {
      var navigator:Object = Object(object).navigator;
      if (navigator != null && navigator.activeView != null && !navigator.activeView.isActive) {
        navigator.activeView.setActive(true);
      }
    }
  }

  private function getMinSize(horizontal:Boolean):int {
    var layout:LayoutBase = getLayout();
    if (layout == null || getQualifiedClassName(layout).indexOf("MigLayout") == -1) {
      return horizontal ? ILayoutElement(_document).getPreferredBoundsWidth() : ILayoutElement(_document).getPreferredBoundsHeight();
    }
    else {
      return horizontal ? ILayoutElement(_document).getMinBoundsWidth() : ILayoutElement(_document).getMinBoundsHeight();
    }
  }

  public function get minDocumentWidth():int {
    return getMinSize(true);
  }

  public function get minDocumentHeight():int {
    return getMinSize(false);
  }

  public function get actualDocumentWidth():int {
    return ILayoutElement(_document).getLayoutBoundsWidth();
  }

  public function get actualDocumentHeight():int {
    return ILayoutElement(_document).getLayoutBoundsHeight();
  }

  public function added():void {
    _focusManager.activate();
  }

  public function activated():void {
    mainFocusManager.activeDocumentFocusManager = _focusManager;
    
    FlexGlobals.topLevelApplication = _document as Application || fakeTopLevelApplication;
    TopLevelSystemManagerProxy(SystemManagerGlobals.topLevelSystemManagers[0]).activeSystemManager = this;
  }

  public function deactivated():void {
    mainFocusManager.activeDocumentFocusManager = null;

    // We can not leave empty FlexGlobals.topLevelApplication, as example, VideoPlayer uses FlexGlobals.topLevelApplication as parent when opening fullscreen
    // but we can not set it as SystemManager, because it wants UIComponent (for style)
    FlexGlobals.topLevelApplication = null;

    TopLevelSystemManagerProxy(SystemManagerGlobals.topLevelSystemManagers[0]).activeSystemManager = null;
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

  public function callInContext(fn:Function, thisArg:Object, argArray:Array, returns:Boolean = true):* {
    if (returns) {
      return fn.apply(thisArg, argArray);
    }
    else {
      fn.apply(thisArg, argArray);
    }
  }

  public function create(... params):Object {
    return flexModuleFactory.create(params);
  }

  private const EMPTY_INFO:Object = {};
  public function info():Object {
    // we must return not-null, see spark.components.Application applicationDPI getter
    return EMPTY_INFO;
  }

  private var _rawChildren:SystemRawChildrenList;
  public function get rawChildren():IChildList {
    if (_rawChildren == null) {
      _rawChildren = new SystemRawChildrenList(this);
    }
    
    return _rawChildren;
  }

  public function get topLevelSystemManager():ISystemManager {
    return this;
  }

  public function isFontFaceEmbedded(tf:TextFormat):Boolean {
    return false;
  }

  flex::gt_4_1
  public function getVisibleApplicationRect(bounds:Rectangle = null, skipToSandboxRoot:Boolean = false):Rectangle {
    return commonGetVisibleApplicationRect(bounds);
  }

  flex::v4_1
  public function getVisibleApplicationRect(bounds:Rectangle = null):Rectangle {
    return commonGetVisibleApplicationRect(bounds);
  }

  private function commonGetVisibleApplicationRect(bounds:Rectangle):Rectangle {
    if (bounds == null) {
      bounds = getBounds(realStage);
    }

    return bounds;
  }

  // mx.managers::ISystemManagerChildManager, ChildManager, "cm.notifyStyleChangeInChildren(styleProp, true);" in CSSStyleDeclaration
  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function notifyStyleChangeInChildren(styleProp:String, recursive:Boolean):void {
  }

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
          realStage.addEventListener(rawType, proxyEventHandler, true);
        }
        else {
          realStage.addEventListener(rawType, proxyEventHandler);
        }
      }

      if (listeners.indexOf(listener) == -1) {
        //trace("ADDED", type,  useCapture);
        listeners.push(listener);
      }
    }
    else if (!(type in skippedEvents)) {
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
    var map:Dictionary = useCapture ? proxiedListenersInCapture : proxiedListeners;
    var listeners:Vector.<Function> = map == null ? null : map[type];
    if (listeners == null) {
      if (!(type in skippedEvents)) {
        super.removeEventListener(type, listener, useCapture);
      }
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
      realStage.removeEventListener(getRawEventType(type), proxyEventHandler, useCapture);
    }
  }

  private function proxyEventHandler(event:Event):void {
    //if (event.type != MouseEvent.MOUSE_MOVE) {
      //trace("EXECUTED", event, event.eventPhase == EventPhase.CAPTURING_PHASE);
    //}
    var listeners:Vector.<Function>;
    if (event.eventPhase == EventPhase.CAPTURING_PHASE) {
      listeners = proxiedListenersInCapture[event.type];
    }
    else {
      listeners = proxiedListeners[event.type];
    }

    try {
      // IDEA-73488
      setStyleManagerForTalentAdobeEngineers(true);
      for each (var listener:Function in
        listeners.slice() /* copy, because may be removed in removeEventListener (side effect by call listener) */) {
        listener(event);
      }
    }
    finally {
      setStyleManagerForTalentAdobeEngineers(false);
    }
  }

  public function removeEventHandlers():void {
    if (realStage != null) {
      removeProxyEventHandlers2(proxiedListeners, false);
      removeProxyEventHandlers2(proxiedListenersInCapture, true);
    }
  }

  private function removeProxyEventHandlers2(map:Dictionary, useCapture:Boolean):void {
    if (map != null) {
      for (var type:String in map) {
        realStage.removeEventListener(type, proxyEventHandler, useCapture);
        map[type].length = 0;
      }
    }
  }

  flex::gt_4_1 {
    include '../../../../../../baseFlexModuleFactoryImpl45.as';
  }

  private var _focusManager:DocumentFocusManager;
  public function get focusManager():IFocusManager {
    return _focusManager;
  }

  public function set focusManager(value:IFocusManager):void {
    trace("skip illegal set focus manager");
  }

  public function get defaultButton():IFlexDisplayObject {
    return null;
  }

  public function set defaultButton(value:IFlexDisplayObject):void {
    trace("skip illegal set defaultButton");
  }

  public function get systemManager():ISystemManager {
    return this;
  }

  public function get flexLayoutManager():Object {
    return UIComponentGlobals.layoutManager;
  }

  private var _layoutManager:com.intellij.flex.uiDesigner.designSurface.LayoutManager;

  public function get layoutManager():com.intellij.flex.uiDesigner.designSurface.LayoutManager {
    if (_layoutManager == null) {
      _layoutManager = createLayoutManager();
    }
    return _layoutManager;
  }

  private function getLayout():LayoutBase {
    if (document is GroupBase) {
      return GroupBase(document).layout;
    }
    else if (document is SkinnableContainer) {
      return SkinnableContainer(document).layout;
    }
    else {
      return null;
    }
  }

  private function createLayoutManager():com.intellij.flex.uiDesigner.designSurface.LayoutManager {
    return null;
    //var migLayout:MigLayout;
    //if (document is GroupBase) {
    //  migLayout = GroupBase(document).layout as MigLayout;
    //}
    //else if (document is SkinnableContainer) {
    //  migLayout = SkinnableContainer(document).layout as MigLayout;
    //}
    //
    //if (migLayout == null) {
    //  return null;
    //}
    //
    //LayoutUtil.designTimeEmptySize = 15;
    //
    //return new MigLayoutManager(migLayout);
  }

  public function prepareSnapshot(setActualSize:Boolean):void {
    if (setActualSize) {
      var w:int = explicitDocumentWidth;
      var h:int = explicitDocumentHeight;
      if (w == -1) {
        w = Math.max(500, minDocumentWidth);
      }
      if (h == -1) {
        h = Math.max(400, minDocumentHeight);
      }

      ILayoutElement(document).setLayoutBoundsSize(w, h);
      if (document is IInvalidating) {
        IInvalidating(document).validateNow();
      }
    }
  }
}
}

import mx.managers.IFocusManagerContainer;
import mx.managers.systemClasses.ActiveWindowManager;

// see ugly StyleableStageText — it uses concrete class instead of interface
final class ActiveWindowManagerForAdobeDummies extends ActiveWindowManager {
  override public function addFocusManager(f:IFocusManagerContainer):void {
  }

  override public function removeFocusManager(f:IFocusManagerContainer):void {
  }

  override public function activate(f:Object):void {
  }

  override public function deactivate(f:Object):void {
  }

  override public function get numModalWindows():int {
    return 0;
  }

  override public function set numModalWindows(value:int):void {
  }
}

