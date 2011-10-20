package spark.components {

import flash.display.DisplayObject;
import flash.display.NativeWindow;
import flash.display.NativeWindowResize;
import flash.display.NativeWindowSystemChrome;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.geom.Rectangle;

import mx.controls.FlexNativeMenu;
import mx.core.IVisualElement;
import mx.core.IWindow;
import mx.core.UIComponent;
import mx.core.mx_internal;

import spark.components.supportClasses.TextBase;
import spark.components.windowClasses.TitleBar;

use namespace mx_internal;

public class Window extends SkinnableContainer implements IWindow {

  /**
   *  Returns the Window to which a component is parented.
   *
   *  @param component The component whose Window you wish to find.
   *
   *  @return An IWindow object.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public static function getWindow(component:UIComponent):IWindow {
    return null;
  }

  /**
   *  The skin part that defines the gripper button used to resize the window.
   *
   *  @langversion 3.0
   *  @playerversion Flash 10
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  [SkinPart(required="false")]
  public var gripper:Button;

  //----------------------------------
  //  statusBar
  //----------------------------------

  /**
   *  The skin part that defines the display of the status bar.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  [SkinPart(required="false")]
  public var statusBar:IVisualElement;

  /**
   *  The skin part that defines the display of the status bar's text.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  [SkinPart(required="false")]
  public var statusText:TextBase;

  /**
   *  The skin part that defines the title bar.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  [SkinPart(required="false")]
  public var titleBar:TitleBar;

  private var _alwaysInFront:Boolean = false;

  /**
   *  Determines whether the underlying NativeWindow is always in front
   *  of other windows (including those of other applications). Setting
   *  this property sets the <code>alwaysInFront</code> property of the
   *  underlying NativeWindow. See the <code>NativeWindow.alwaysInFront</code>
   *  property description for details of how this affects window stacking
   *  order.
   *
   *  @see flash.display.NativeWindow#alwaysInFront
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function get alwaysInFront():Boolean {
    return _alwaysInFront;
  }

  public function set alwaysInFront(value:Boolean):void {
    _alwaysInFront = value;
  }

  protected function get bounds():Rectangle {
    return null;
  }

  protected function set bounds(value:Rectangle):void {
  }

  public function get closed():Boolean {
    return false;
  }

  [Inspectable(enumeration="default,off,on", defaultValue="default")]
  public function get colorCorrection():String {
    return null;
  }

  public function set colorCorrection(value:String):void {
  }

  private var _maximizable:Boolean = true;
  public function get maximizable():Boolean {
    return _maximizable;
  }

  public function set maximizable(value:Boolean):void {
    _maximizable = value;
  }

  private var _menu:FlexNativeMenu;

  private var menuChanged:Boolean = false;

  public function get menu():FlexNativeMenu {
    return _menu;
  }

  public function set menu(value:FlexNativeMenu):void {
    if (_menu) {
      _menu.automationParent = null;
      _menu.automationOwner = null;
    }

    _menu = value;
    menuChanged = true;

    if (_menu) {
      menu.automationParent = this;
      menu.automationOwner = this;
    }
  }

  private var _minimizable:Boolean = true;


  public function get minimizable():Boolean {
    return _minimizable;
  }

  public function set minimizable(value:Boolean):void {
    _minimizable = value;
  }


  public function get nativeWindow():NativeWindow {
    return null;
  }

  private var _resizable:Boolean = true;
  public function get resizable():Boolean {
    return _resizable;
  }

  public function set resizable(value:Boolean):void {
    _resizable = value;
  }

  private var _showStatusBar:Boolean = true;
  private var showStatusBarChanged:Boolean = true;

  public function get showStatusBar():Boolean {
    return _showStatusBar;
  }

  public function set showStatusBar(value:Boolean):void {
    if (_showStatusBar == value) {
      return;
    }

    _showStatusBar = value;
    showStatusBarChanged = true;

    invalidateProperties();
    invalidateDisplayList();
  }

  private var _status:String = "";

  private var statusChanged:Boolean = false;

  public function get status():String {
    return _status;
  }

  public function set status(value:String):void {

    _status = value;
    statusChanged = true;

    invalidateProperties();
    invalidateSize();

    dispatchEvent(new Event("statusChanged"));
  }

  private var _systemChrome:String = NativeWindowSystemChrome.STANDARD;

  [Inspectable(enumeration="none,standard", defaultValue="standard")]

  /**
   *  Specifies the type of system chrome (if any) the window has.
   *  The set of possible values is defined by the constants
   *  in the NativeWindowSystemChrome class.
   *
   *  <p>This property is read-only once the window has been opened.</p>
   *
   *  <p>The default value is <code>NativeWindowSystemChrome.STANDARD</code>.</p>
   *
   *  @see flash.display.NativeWindowSystemChrome
   *  @see flash.display.NativeWindowInitOptions#systemChrome
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function get systemChrome():String {
    return _systemChrome;
  }

  /**
   *  @private
   */
  public function set systemChrome(value:String):void {
      _systemChrome = value;
  }

  private var _title:String = "";


  private var titleChanged:Boolean = false;

  [Bindable("titleChanged")]

  public function get title():String {
    return _title;
  }

  /**
   *  @private
   */
  public function set title(value:String):void {
    titleChanged = true;
    _title = value;

    invalidateProperties();
    invalidateSize();
    invalidateDisplayList();

    dispatchEvent(new Event("titleChanged"));
  }

  private var _titleIcon:Class;

  /**
   *  @private
   */
  private var titleIconChanged:Boolean = false;

  [Bindable("titleIconChanged")]

  /**
   *  The Class (usually an image) used to draw the title bar icon.
   *
   *  @default null
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function get titleIcon():Class {
    return _titleIcon;
  }

  /**
   *  @private
   */
  public function set titleIcon(value:Class):void {
    _titleIcon = value;
    titleIconChanged = true;

    invalidateProperties();
    invalidateSize();
    invalidateDisplayList();

    dispatchEvent(new Event("titleIconChanged"));
  }

  private var _transparent:Boolean = false;

  /**
   *  Specifies whether the window is transparent. Setting this
   *  property to <code>true</code> for a window that uses
   *  system chrome is not supported.
   *
   *  <p>This property is read-only after the window has been opened.</p>
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function get transparent():Boolean {
    return _transparent;
  }

  /**
   *  @private
   */
  public function set transparent(value:Boolean):void {
      _transparent = value;
  }

  private var _type:String = "normal";

  /**
   *  Specifies the type of NativeWindow that this component
   *  represents. The set of possible values is defined by the constants
   *  in the NativeWindowType class.
   *
   *  <p>This property is read-only once the window has been opened.</p>
   *
   *  <p>The default value is <code>NativeWindowType.NORMAL</code>.</p>
   *
   *  @see flash.display.NativeWindowType
   *  @see flash.display.NativeWindowInitOptions#type
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function get type():String {
    return _type;
  }

  /**
   *  @private
   */
  public function set type(value:String):void {
      _type = value;
  }

  /**
   *  @private
   */
  override protected function commitProperties():void {
    // Create and open window.

    // Moved the super.commitProperites() to here to allow the Window subclass to be
    // initialized. Part of the initialization is loading the skin of the Window subclass.
    // At this point we can call into SkinnableComponent.commitProperties without getting
    // a "skin was not found" error.
    super.commitProperties();

    // AIR won't allow you to set the min width greater than the current
    // max width (same is true for height). You also can't set the max
    // width less than the current min width (same is true for height).
    // This makes the updating of the new minSize and maxSize a bit tricky.

    // minimum width and height

    // maximum width and height


    if (menuChanged && !nativeWindow.closed) {
      menuChanged = false;

      if (menu == null) {
        if (NativeWindow.supportsMenu) {
          nativeWindow.menu = null;
        }
      }
      else if (menu.nativeMenu) {
        if (NativeWindow.supportsMenu) {
          nativeWindow.menu = menu.nativeMenu;
        }
      }

      dispatchEvent(new Event("menuChanged"));
    }

    if (titleIconChanged) {
      if (titleBar) {
        titleBar.titleIcon = _titleIcon;
      }
      titleIconChanged = false;
    }

    if (titleChanged) {
      if (!nativeWindow.closed) {
        //systemManager.stage.nativeWindow.title = _title;
      }
      if (titleBar) {
        titleBar.title = _title;
      }
      titleChanged = false;
    }

    if (showStatusBarChanged) {
      if (statusBar) {
        statusBar.visible = _showStatusBar;
      }
      showStatusBarChanged = false;
    }

    if (statusChanged) {
      if (statusText) {
        statusText.text = _status;
      }
      statusChanged = false;
    }


  }


  override protected function partAdded(partName:String, instance:Object):void {
    super.partAdded(partName, instance);

    if (instance == statusBar) {
      statusBar.visible = _showStatusBar;
      statusBar.includeInLayout = _showStatusBar;
      showStatusBarChanged = false;
    }
    else if (instance == titleBar) {
      //if (!nativeWindow.closed) {
      //  // If the initial title is the default and the native window is set
      //  // from the initial window settings,
      //  // then use the initial window settings title.
      //  if (_title == "" && systemManager.stage.nativeWindow.title != null) {
      //    _title = systemManager.stage.nativeWindow.title;
      //  }
      //  else {
      //    systemManager.stage.nativeWindow.title = _title;
      //  }
      //}

      titleBar.title = _title;
      titleChanged = false;
    }
    else if (instance == statusText) {
      statusText.text = status;
      statusChanged = false;
    }
    else if (instance == gripper) {
      gripper.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
    }
  }

  /**
   *  @private
   */
  override protected function partRemoved(partName:String, instance:Object):void {
    super.partRemoved(partName, instance);

    if (instance == gripper) {
      gripper.removeEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
    }
  }

  //--------------------------------------------------------------------------
  //
  //  Methods
  //
  //--------------------------------------------------------------------------

  /**
   *  Closes the window. This action is cancelable.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function close():void {

  }


  /**
   *  Maximizes the window, or does nothing if it's already maximized.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function maximize():void {
  }

  /**
   *  Minimizes the window.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function minimize():void {
  }

  /**
   *  Restores the window (unmaximizes it if it's maximized, or
   *  unminimizes it if it's minimized).
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function restore():void {
  }

  /**
   *  Activates the underlying NativeWindow (even if this Window's application
   *  is not currently active).
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function activate():void {
  }

  /**
   *  Creates the underlying NativeWindow and opens it.
   *
   *  After being closed, the Window object is still a valid reference, but
   *  accessing most properties and methods will not work.
   *  Closed windows cannot be reopened.
   *
   *  @param  openWindowActive specifies whether the Window opens
   *  activated (that is, whether it has focus). The default value
   *  is <code>true</code>.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function open(openWindowActive:Boolean = true):void {
  }

  /**
   *  Orders the window just behind another. To order the window behind
   *  a NativeWindow that does not implement IWindow, use this window's
   *  nativeWindow's <code>orderInBackOf()</code> method.
   *
   *  @param window The IWindow (Window or WindowedAplication)
   *  to order this window behind.
   *
   *  @return <code>true</code> if the window was successfully sent behind;
   *          <code>false</code> if the window is invisible or minimized.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function orderInBackOf(window:IWindow):Boolean {
    return false;
  }

  /**
   *  Orders the window just in front of another. To order the window
   *  in front of a NativeWindow that does not implement IWindow, use this
   *  window's nativeWindow's  <code>orderInFrontOf()</code> method.
   *
   *  @param window The IWindow (Window or WindowedAplication)
   *  to order this window in front of.
   *
   *  @return <code>true</code> if the window was successfully sent in front;
   *          <code>false</code> if the window is invisible or minimized.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function orderInFrontOf(window:IWindow):Boolean {
      return false;
  }

  /**
   *  Orders the window behind all others in the same application.
   *
   *  @return <code>true</code> if the window was successfully sent to the back;
   *  <code>false</code> if the window is invisible or minimized.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function orderToBack():Boolean {
      return false;
  }

  /**
   *  Orders the window in front of all others in the same application.
   *
   *  @return <code>true</code> if the window was successfully sent to the front;
   *  <code>false</code> if the window is invisible or minimized.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  public function orderToFront():Boolean {
      return false;
  }
  
  /**
   *  @private
   *  Returns the name of the state to be applied to the skin. For example, a
   *  Button component could return the String "up", "down", "over", or "disabled"
   *  to specify the state.
   *
   *  <p>A subclass of SkinnableComponent must override this method to return a value.</p>
   *
   *  @return A string specifying the name of the state to apply to the skin.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  override protected function getCurrentSkinState():String {
    return enabled ? "normal" : "disabled";
  }

  /**
   *  @private
   *  Manages mouse down events on the window border.
   *
   *  @langversion 3.0
   *  @playerversion AIR 1.5
   *  @productversion Flex 4
   */
  protected function mouseDownHandler(event:MouseEvent):void {
  }

  /**
   *   @private
   *
   *   Perform a hit test to determine if an edge or corner of the application
   *   was clicked.
   *
   *   @param event The mouse event to hit test.
   *
   *   @return If an edge or corner was click return one of the constants from
   *   the NativeWindowResize to indicate the edit or corner that was clicked. If
   *   no edge or corner were clicked then return NativeWindowResize.NONE.
   */
  mx_internal function hitTestResizeEdge(event:MouseEvent):String {
    // If we clicked on a child of the contentGroup, then don't resize
    if (event.target is DisplayObject && event.target != contentGroup) {
      var o:DisplayObject = DisplayObject(event.target);
      while (o && o != contentGroup && o != this) {
        o = o.parent;
      }

      if (o == null || o == contentGroup) {
        return NativeWindowResize.NONE;
      }
    }

    var hitTestResults:String = NativeWindowResize.NONE;
    var resizeAfforanceWidth:Number = getStyle("resizeAffordanceWidth");
    var borderWidth:int = resizeAfforanceWidth;
    var cornerSize:int = resizeAfforanceWidth * 2;

    if (event.stageY < borderWidth) {
      if (event.stageX < cornerSize) {
        hitTestResults = NativeWindowResize.TOP_LEFT;
      }
      else if (event.stageX > width - cornerSize) {
        hitTestResults = NativeWindowResize.TOP_RIGHT;
      }
      else {
        hitTestResults = NativeWindowResize.TOP;
      }
    }
    else if (event.stageY > (height - borderWidth)) {
      if (event.stageX < cornerSize) {
        hitTestResults = NativeWindowResize.BOTTOM_LEFT;
      }
      else if (event.stageX > width - cornerSize) {
        hitTestResults = NativeWindowResize.BOTTOM_RIGHT;
      }
      else {
        hitTestResults = NativeWindowResize.BOTTOM;
      }
    }
    else if (event.stageX < borderWidth) {
      if (event.stageY < cornerSize) {
        hitTestResults = NativeWindowResize.TOP_LEFT;
      }
      else if (event.stageY > height - cornerSize) {
        hitTestResults = NativeWindowResize.BOTTOM_LEFT;
      }
      else {
        hitTestResults = NativeWindowResize.LEFT;
      }
    }
    else if (event.stageX > width - borderWidth) {
      if (event.stageY < cornerSize) {
        hitTestResults = NativeWindowResize.TOP_RIGHT;
      }
      else if (event.stageY > height - cornerSize) {
        hitTestResults = NativeWindowResize.BOTTOM_RIGHT;
      }
      else {
        hitTestResults = NativeWindowResize.RIGHT;
      }
    }

    return hitTestResults;
  }
}
}
