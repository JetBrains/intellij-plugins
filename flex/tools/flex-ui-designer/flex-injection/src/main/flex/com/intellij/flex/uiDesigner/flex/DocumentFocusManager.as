package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.InteractiveObject;
import flash.display.Sprite;
import flash.display.Stage;
import flash.events.Event;
import flash.events.FocusEvent;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.system.Capabilities;
import flash.system.IME;
import flash.text.TextField;
import flash.ui.Keyboard;

import mx.core.FlexSprite;
import mx.core.IButton;
import mx.core.IIMESupport;
import mx.core.IRawChildrenContainer;
import mx.core.IToggleButton;
import mx.core.IUIComponent;
import mx.core.IVisualElement;
import mx.managers.IFocusManager;
import mx.managers.IFocusManagerComponent;
import mx.managers.IFocusManagerGroup;
import mx.utils.OnDemandEventDispatcher;

public class DocumentFocusManager extends OnDemandEventDispatcher implements IFocusManager, DocumentFocusManagerSB {
  private static const FROM_INDEX_UNSPECIFIED:int = -2;

  private var systemManager:SystemManager;
  private var lastFocus:IFocusManagerComponent;
  private var calculateCandidates:Boolean = true;

  //noinspection JSMismatchedCollectionQueryUpdate
  private var focusableObjects:Vector.<IFocusManagerComponent>;
  private var focusableCandidates:Vector.<IFocusManagerComponent>;

  public function DocumentFocusManager(systemManager:SystemManager) {
    this.systemManager = systemManager;
  }

  public function activate():void {
    systemManager.addRealEventListener(FocusEvent.FOCUS_IN, focusInHandler, true);
    systemManager.addRealEventListener(MouseEvent.MOUSE_DOWN, mouseDownCaptureHandler, true);
    systemManager.addRealEventListener(KeyboardEvent.KEY_DOWN, defaultButtonKeyHandler);
    systemManager.addRealEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler, true);

    if (hasEventListener("activateFM")) {
      dispatchEvent(new Event("activateFM"));
    }
  }

  public function deactivate():void {
  }

  public function restoreFocusToLastControl():InteractiveObject {
    if (lastFocus != null) {
      setFocus(lastFocus);
      return null;
    }
    else {
      return systemManager;
    }
  }

  private var _defaultButton:IButton;
  public function get defaultButton():IButton {
    return _defaultButton;
  }

  public function set defaultButton(value:IButton):void {
    var button:IButton = value ? IButton(value) : null;
    if (button != _defaultButton) {
      if (_defaultButton != null) {
        _defaultButton.emphasized = false;
      }

      _defaultButton = button;

      if (button != null) {
        button.emphasized = true;
      }
    }
  }

  private var _defaultButtonEnabled:Boolean;
  public function get defaultButtonEnabled():Boolean {
    return _defaultButtonEnabled;
  }

  public function set defaultButtonEnabled(value:Boolean):void {
    _defaultButtonEnabled = value;
  }

  private var _focusPane:Sprite;
  public function get focusPane():Sprite {
    if (_focusPane == null) {
      _focusPane = new FlexSprite();
      _focusPane.name = "focusPane";
    }

    return _focusPane;
  }

  public function set focusPane(value:Sprite):void {
    _focusPane = value;
  }

  public function get nextTabIndex():int {
    return getMaxTabIndex() + 1;
  }

  private function getMaxTabIndex():int {
    var maxTabIndex:int = 0;
    var n:int = focusableObjects.length;
    var tabIndex:int;
    for (var i:int = 0; i < n; i++) {
      if ((tabIndex = focusableObjects[i].tabIndex) > maxTabIndex) {
        maxTabIndex = tabIndex;
      }
    }

    return maxTabIndex;
  }

  private var _showFocusIndicator:Boolean;
  public function get showFocusIndicator():Boolean {
    return _showFocusIndicator;
  }

  public function set showFocusIndicator(value:Boolean):void {
    if (_showFocusIndicator == value) {
      return;
    }

    _showFocusIndicator = value;

    if (hasEventListener("showFocusIndicator")) {
      dispatchEvent(new Event("showFocusIndicator"));
    }
  }

  public function getFocus():IFocusManagerComponent {
    var stage:Stage = systemManager.stage;
    if (stage == null) {
      return null;
    }

    return findFocusManagerComponent(stage.focus);
  }

  public function setFocus(o:IFocusManagerComponent):void {
    o.setFocus();
    if (hasEventListener("setFocus")) {
      dispatchEvent(new Event("setFocus"));
    }
  }

  public function showFocus():void {
    if (!showFocusIndicator) {
      showFocusIndicator = true;
      if (lastFocus) {
        lastFocus.drawFocus(true);
      }
    }
  }

  public function hideFocus():void {
    if (showFocusIndicator) {
      showFocusIndicator = false;
      if (lastFocus) {
        lastFocus.drawFocus(false);
      }
    }
  }

  public function findFocusManagerComponent(o:InteractiveObject):IFocusManagerComponent {
    while (o) {
      if ((o is IFocusManagerComponent && IFocusManagerComponent(o).focusEnabled)) {
        return IFocusManagerComponent(o);
      }

      o = o.parent;
    }

    return null;
  }

  public function getNextFocusManagerComponent(backward:Boolean = false):IFocusManagerComponent {
    return getNextFocusInfo(backward).displayObject as IFocusManagerComponent;
  }

  public function getNextFocusInfo(backward:Boolean = false):FocusInfo {
    if (focusableObjects.length == 0) {
      return null;
    }

    if (calculateCandidates) {
      sortFocusableObjects();
      calculateCandidates = false;
    }

    var fromIndex:int = FROM_INDEX_UNSPECIFIED;
    var i:int = fromIndex;
    if (fromIndex == FROM_INDEX_UNSPECIFIED) {
      // if there is no passed in object, then get the object that has the focus
      //var o:DisplayObject = fromObject;
      var o:DisplayObject;
      if (o == null) {
        o = systemManager.stage.focus;
      }

      o = DisplayObject(findFocusManagerComponent(InteractiveObject(o)));

      var g:String = "";
      if (o is IFocusManagerGroup) {
        var tg:IFocusManagerGroup = IFocusManagerGroup(o);
        g = tg.groupName;
      }
      i = getIndexOfFocusedObject(o);
    }

    // trace(" starting at " + i);
    var bSearchAll:Boolean = false;
    if (i == -1) {
      if (backward) {
        i = focusableCandidates.length;
      }
      bSearchAll = true;
      // trace("search all " + i);
    }

    var j:int = getIndexOfNextObject(i, backward, bSearchAll, g);
    // if we wrapped around, get if we have a parent we should pass
    // focus to.
    var wrapped:Boolean = false;
    if (backward) {
      if (j >= i) {
        wrapped = true;
      }
    }
    else if (j <= i) {
      wrapped = true;
    }

    var focusInfo:FocusInfo = new FocusInfo();

    focusInfo.displayObject = DisplayObject(findFocusManagerComponent(InteractiveObject(focusableCandidates[j])));
    focusInfo.wrapped = wrapped;

    return focusInfo;

  }

  private function getIndexOfFocusedObject(o:DisplayObject):int {
    if (o == null) {
      return -1;
    }

    var i:int = focusableCandidates.indexOf(o);
    if (i != -1) {
      return i;
    }

    // no match?  try again with a slower match for certain cases like DG editors
    for (i = 0; i < focusableCandidates.length; i++) {
      var iui:IUIComponent = focusableCandidates[i] as IUIComponent;
      if (iui != null && iui.owns(o)) {
        return i;
      }
    }

    return -1;
  }

  private function getIndexOfNextObject(i:int, shiftKey:Boolean, bSearchAll:Boolean, groupName:String):int {
    var n:int = focusableCandidates.length;
    var start:int = i;

    while (true) {
      if (shiftKey) {
        i--;
      }
      else {
        i++;
      }
      if (bSearchAll) {
        if (shiftKey && i < 0) {
          break;
        }
        if (!shiftKey && i == n) {
          break;
        }
      }
      else {
        i = (i + n) % n;
        // came around and found the original
        if (start == i) {
          break;
        }
        // if start is -1, set start to first valid value of i
        if (start == -1) {
          start = i;
        }
      }
      // trace("testing " + focusableCandidates[i]);
      if (isValidFocusCandidate(DisplayObject(focusableCandidates[i]), groupName)) {
        // trace(" stopped at " + i);
        var o:DisplayObject = DisplayObject(findFocusManagerComponent(InteractiveObject(focusableCandidates[i])));
        if (o is IFocusManagerGroup) {
          // look around to see if there's an enabled and visible
          // selected member in the tabgroup, otherwise use the first
          // one we found.
          var tg1:IFocusManagerGroup = IFocusManagerGroup(o);
          for (var j:int = 0; j < focusableCandidates.length; j++) {
            var obj:DisplayObject = DisplayObject(focusableCandidates[j]);
            if (obj is IFocusManagerGroup && isEnabledAndVisible(obj)) {
              var tg2:IFocusManagerGroup = IFocusManagerGroup(obj);
              if (tg2.groupName == tg1.groupName && tg2.selected) {
                // if objects of same group have different tab index
                // skip you aren't selected.
                if (InteractiveObject(obj).tabIndex != InteractiveObject(o).tabIndex && !tg1.selected) {
                  return getIndexOfNextObject(i, shiftKey, bSearchAll, groupName);
                }

                i = j;
                break;
              }
            }
          }

        }
        return i;
      }
    }
    return i;
  }

  private function isValidFocusCandidate(o:DisplayObject, g:String):Boolean {
    if (o is IFocusManagerComponent) {
      if (!IFocusManagerComponent(o).focusEnabled) {
        return false;
      }
    }

    if (!isEnabledAndVisible(o)) {
      return false;
    }

    if (o is IFocusManagerGroup) {
      // reject if it is in the same tabgroup
      var tg:IFocusManagerGroup = IFocusManagerGroup(o);
      if (g == tg.groupName) return false;
    }
    return true;
  }

  private function isEnabledAndVisible(o:DisplayObject):Boolean {
    var formParent:DisplayObjectContainer = systemManager;
    while (o != formParent) {
      if (o is IUIComponent) {
        if (!IUIComponent(o).enabled) {
          return false;
        }
      }

      if (o is IVisualElement) {
        if (IVisualElement(o).designLayer != null && !IVisualElement(o).designLayer.effectiveVisibility) {
          return false;
        }
      }

      if (!o.visible) {
        return false;
      }
      o = o.parent;

      // if no parent, then not on display list
      if (o == null) {
        return false;
      }
    }

    return true;
  }


  private function sortFocusableObjects():void {
    focusableCandidates = new Vector.<IFocusManagerComponent>();

    var fcc:int = 0;
    var n:int = focusableObjects.length;
    for (var i:int = 0; i < n; i++) {
      var c:IFocusManagerComponent = focusableObjects[i];
      if (c.tabIndex > 0) {
        sortFocusableObjectsTabIndex();
        return;
      }
      focusableCandidates[fcc++] = c;
    }

    focusableCandidates.sort(sortByDepth);
  }

  private function sortFocusableObjectsTabIndex():void {
    focusableCandidates.length = 0;

    var n:int = focusableObjects.length;
    var fcc:int = 0;
    for (var i:int = 0; i < n; i++) {
      var c:IFocusManagerComponent = focusableObjects[i];
      if (c.tabIndex > 0) {
        focusableCandidates[fcc++] = c;
      }
    }

    focusableCandidates.sort(sortByTabIndex);
  }

  private function sortByTabIndex(a:InteractiveObject, b:InteractiveObject):int {
    var aa:int = a.tabIndex;
    var bb:int = b.tabIndex;

    if (aa == -1) {
      aa = int.MAX_VALUE;
    }
    if (bb == -1) {
      bb = int.MAX_VALUE;
    }

    return (aa > bb ? 1 :
            aa < bb ? -1 : sortByDepth(DisplayObject(a), DisplayObject(b)));
  }

  private function sortByDepth(aa:DisplayObject, bb:DisplayObject):Number {
    var val1:String = "";
    var val2:String = "";
    var index:int;
    var tmp:String;
    var tmp2:String;
    var zeros:String = "0000";

    var a:DisplayObject = DisplayObject(aa);
    var b:DisplayObject = DisplayObject(bb);
    while (a != systemManager && a.parent != null) {
      index = getChildIndex(a.parent, a);
      tmp = index.toString(16);
      if (tmp.length < 4) {
        tmp2 = zeros.substring(0, 4 - tmp.length) + tmp;
      }
      val1 = tmp2 + val1;
      a = a.parent;
    }

    while (b != systemManager && b.parent != null) {
      index = getChildIndex(b.parent, b);
      tmp = index.toString(16);
      if (tmp.length < 4) {
        tmp2 = zeros.substring(0, 4 - tmp.length) + tmp;
      }
      val2 = tmp2 + val2;
      b = b.parent;
    }

    return val1 > val2 ? 1 : val1 < val2 ? -1 : 0;
  }

  private static function getChildIndex(parent:DisplayObjectContainer, child:DisplayObject):int {
    try {
      return parent.getChildIndex(child);
    }
    catch (e:Error) {
      if (parent is IRawChildrenContainer) {
        return IRawChildrenContainer(parent).rawChildren.getChildIndex(child);
      }
      throw e;
    }

    throw new Error("FocusManager.getChildIndex failed");
  }

  private function setFocusToNextObject(event:FocusEvent):void {
    //if (focusableObjects.length == 0) {
    //  return;
    //}
    //
    //var focusInfo:FocusInfo = getNextFocusManagerComponent2(event.shiftKey, fauxFocus);
    //// trace("winner = ", focusInfo.displayObject);
    //
    //// If we are about to wrap focus around, send focus back to the parent.
    //if (!popup && (focusInfo.wrapped || !focusInfo.displayObject)) {
    //  if (hasEventListener("focusWrapping")) {
    //    if (!dispatchEvent(new FocusEvent("focusWrapping", false, true, null, event.shiftKey))) {
    //      return;
    //    }
    //  }
    //}
    //
    //if (!focusInfo.displayObject) {
    //  event.preventDefault();
    //  return;
    //}
    //
    //setFocusToComponent(focusInfo.displayObject, event.shiftKey);
  }

  //private function isParent(p:DisplayObjectContainer, o:DisplayObject):Boolean {
  //  if (p == o) {
  //    return false;
  //  }
  //
  //  if (p is IRawChildrenContainer) {
  //    return IRawChildrenContainer(p).rawChildren.contains(o);
  //  }
  //
  //  return p.contains(o);
  //}

  private static function isParent(p:DisplayObjectContainer, o:DisplayObject):Boolean {
    if (p == o) {
      return false;
    }

    if (p is IRawChildrenContainer) {
      return IRawChildrenContainer(p).rawChildren.contains(o);
    }

    return p.contains(o);
  }


  private function focusInHandler(event:FocusEvent):void {
    var target:InteractiveObject = InteractiveObject(event.target);

    if (!isParent(DisplayObjectContainer(systemManager), target)) {
      return;
    }

    if (_defaultButton) {
      if (target is IButton && target != _defaultButton && !(target is IToggleButton)) {
        _defaultButton.emphasized = false;
      }
      else if (_defaultButtonEnabled) {
        _defaultButton.emphasized = true;
      }
    }

    // trace("FM " + this + " setting last focus " + target);
    lastFocus = findFocusManagerComponent(target);

    if (Capabilities.hasIME) {
      var usesIME:Boolean;
      if (lastFocus is IIMESupport) {
        var imeFocus:IIMESupport = IIMESupport(lastFocus);
        if (imeFocus.enableIME) {
          usesIME = true;
        }
      }
      //if (IMEEnabled) {
      IME.enabled = usesIME;
      //}
    }

    // handle default button here
    // we can't check for Button because of cross-versioning so
    // for now we just check for an emphasized property
    //if (_lastFocus is IButton && !(_lastFocus is IToggleButton)) {
    //  defButton = _lastFocus as IButton;
    //}
    //else {
    //  // restore the default button to be the original one
    //  if (defButton && defButton != _defaultButton) {
    //    defButton = _defaultButton;
    //  }
    //}
  }

  public function handleMouseDown(event:MouseEvent):Boolean {
    var o:DisplayObject = getTopLevelFocusTarget(InteractiveObject(event.target));
    if (o == null) {
      return false;
    }

    // trace("FocusManager mouseDownHandler on " + o);

    // We don't set focus to a TextField ever because the player already did and took care of where
    // the insertion point is, and we also don't call setfocus on a component that last the last focused object unless
    // the last action was just to activate the player and didn't involve tabbing or clicking on a component
    if ((o != lastFocus) && !(o is TextField)) {
      setFocus(IFocusManagerComponent(o));
    }

    if (hasEventListener("mouseDownFM")) {
      dispatchEvent(new FocusEvent("mouseDownFM", false, false, InteractiveObject(o)));
    }

    return true;
  }

  private function mouseDownCaptureHandler(event:MouseEvent):void {
    showFocusIndicator = false;
  }

  private function getTopLevelFocusTarget(o:InteractiveObject):InteractiveObject {
    const hasEventListenerForOverride:Boolean = hasEventListener("getTopLevelFocusTarget");
    while (o != systemManager) {
      if (o is IFocusManagerComponent && IFocusManagerComponent(o).focusEnabled && IFocusManagerComponent(o).mouseFocusEnabled &&
          (o is IUIComponent ? IUIComponent(o).enabled : true)) {
        return o;
      }

      if (hasEventListenerForOverride && !dispatchEvent(new FocusEvent("getTopLevelFocusTarget", false, true, o.parent))) {
        return null;
      }

      if ((o = o.parent) == null) {
        break;
      }
    }

    return null;
  }

  private function defaultButtonKeyHandler(event:KeyboardEvent):void {
    if (hasEventListener("defaultButtonKeyHandler")) {
      if (!dispatchEvent(new FocusEvent("defaultButtonKeyHandler", false, true))) {
        return;
      }
    }

    if (defaultButtonEnabled && event.keyCode == Keyboard.ENTER && defaultButton != null) {
      defaultButton.dispatchEvent(new MouseEvent(MouseEvent.CLICK));
    }
  }

  private function keyDownHandler(event:KeyboardEvent):void {
    if (hasEventListener("keyDownFM")) {
      if (!dispatchEvent(new FocusEvent("keyDownFM", false, true, InteractiveObject(event.target)))) {
        return;
      }
    }

//    if (event.keyCode == Keyboard.TAB) {
//      if (calculateCandidates) {
//        sortFocusableObjects();
//        calculateCandidates = false;
//      }
//    }
  }
}
}

import flash.display.DisplayObject;

final class FocusInfo {
  public var displayObject:DisplayObject;	// object to get focus
  public var wrapped:Boolean;				// true if focus wrapped around
}