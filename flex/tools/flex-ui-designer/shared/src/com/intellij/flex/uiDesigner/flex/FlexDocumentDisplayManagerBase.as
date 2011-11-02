package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.AbstractDocumentDisplayManager;

import flash.geom.Rectangle;
import flash.utils.Dictionary;

[Abstract]
internal class FlexDocumentDisplayManagerBase extends AbstractDocumentDisplayManager {
  protected static const INITIALIZE_ERROR_EVENT_TYPE:String = "initializeError";

  protected static const LAYOUT_MANAGER_FQN:String = "mx.managers::ILayoutManager";
  protected static const POP_UP_MANAGER_FQN:String = "mx.managers::IPopUpManager";
  protected static const TOOL_TIP_MANAGER_FQN:String = "mx.managers::IToolTipManager2";

  protected static const skippedEvents:Dictionary = new Dictionary();
  skippedEvents.cursorManagerRequest = true;
  skippedEvents.dragManagerRequest = true;
  skippedEvents.initManagerRequest = true;
  skippedEvents.systemManagerRequest = true;
  skippedEvents.tooltipManagerRequest = true;

  protected const implementations:Dictionary = new Dictionary();

  protected var proxiedListeners:Dictionary;
  protected var proxiedListenersInCapture:Dictionary;

  public function getImplementation(interfaceName:String):Object {
    return implementations[interfaceName];
  }

  public function registerImplementation(interfaceName:String, impl:Object):void {
    throw new Error("");
  }

  public function get isProxy():Boolean {
    return true; // so, UIComponent will "keep the existing proxy", see UIComponent#get systemManager
  }

  public function get numModalWindows():int {
    return 0;
  }

  public function set numModalWindows(value:int):void {
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
}
}
