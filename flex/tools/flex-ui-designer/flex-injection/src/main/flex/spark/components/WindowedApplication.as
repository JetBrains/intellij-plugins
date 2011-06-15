package spark.components
{
import flash.desktop.NativeApplication;
import flash.display.NativeWindow;
import flash.display.NativeWindowSystemChrome;
import flash.display.NativeWindowType;
import flash.events.Event;

import mx.controls.FlexNativeMenu;
import mx.core.IVisualElement;
import mx.core.IWindow;

import spark.components.supportClasses.TextBase;
import spark.components.windowClasses.TitleBar;

//--------------------------------------
//  Styles
//--------------------------------------

/**
 *  Alpha level of the color defined by the <code>backgroundColor</code>
 *  property.
 *
 *  @default 1.0
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Style(name="backgroundAlpha", type="Number", inherit="no")]

/**
 *  The background color of the application. This color is used as the stage color for the
 *  application and the background color for the HTML embed tag.
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Style(name="backgroundColor", type="uint", format="Color", inherit="no")]

/**
 *  Provides a margin of error around a window's border so a resize
 *  can be more easily started. A click on a window is considered a
 *  click on the window's border if the click occurs with the resizeAffordance
 *  number of pixels from the outside edge of the window.
 *
 *  @default 6
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Style(name="resizeAffordanceWidth", type="Number", format="length", inherit="no")]

//--------------------------------------
//  Events
//--------------------------------------

/**
 *  Dispatched when this application is activated.
 *
 *  @eventType mx.events.AIREvent.APPLICATION_ACTIVATE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="applicationActivate", type="mx.events.AIREvent")]

/**
 *  Dispatched when this application is deactivated.
 *
 *  @eventType mx.events.AIREvent.APPLICATION_DEACTIVATE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="applicationDeactivate", type="mx.events.AIREvent")]

/**
 *  Dispatched after this application window has been activated.
 *
 *  @eventType mx.events.AIREvent.WINDOW_ACTIVATE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="windowActivate", type="mx.events.AIREvent")]

/**
 *  Dispatched after this application window has been deactivated.
 *
 *  @eventType mx.events.AIREvent.WINDOW_DEACTIVATE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="windowDeactivate", type="mx.events.AIREvent")]

/**
 *  Dispatched after this application window has been closed.
 *
 *  @eventType flash.events.Event.CLOSE
 *
 *  @see flash.display.NativeWindow
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="close", type="flash.events.Event")]

/**
 *  Dispatched before the WindowedApplication window closes.
 *  Cancelable.
 *
 *  @eventType flash.events.Event.CLOSING
 *
 *  @see flash.display.NativeWindow
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="closing", type="flash.events.Event")]

/**
 *  Dispatched after the display state changes to minimize, maximize
 *  or restore.
 *
 *  @eventType flash.events.NativeWindowDisplayStateEvent.DISPLAY_STATE_CHANGE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="displayStateChange", type="flash.events.NativeWindowDisplayStateEvent")]

/**
 *  Dispatched before the display state changes to minimize, maximize
 *  or restore.
 *
 *  @eventType flash.events.NativeWindowDisplayStateEvent.DISPLAY_STATE_CHANGING
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="displayStateChanging", type="flash.events.NativeWindowDisplayStateEvent")]

/**
 *  Dispatched when an application is invoked.
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="invoke", type="flash.events.InvokeEvent")]

/**
 *  Dispatched before the WindowedApplication object moves,
 *  or while the WindowedApplication object is being dragged.
 *
 *  @eventType flash.events.NativeWindowBoundsEvent.MOVING
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="moving", type="flash.events.NativeWindowBoundsEvent")]

/**
 *  Dispatched when the computer connects to or disconnects from the network.
 *
 *  @eventType flash.events.Event.NETWORK_CHANGE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="networkChange", type="flash.events.Event")]

/**
 *  Dispatched before the WindowedApplication object is resized,
 *  or while the WindowedApplication object boundaries are being dragged.
 *
 *  @eventType flash.events.NativeWindowBoundsEvent.RESIZING
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="resizing", type="flash.events.NativeWindowBoundsEvent")]

/**
 *  Dispatched when the WindowedApplication completes its initial layout.
 *  By default, the WindowedApplication will be visible at this time.
 *
 *  @eventType mx.events.AIREvent.WINDOW_COMPLETE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="windowComplete", type="mx.events.AIREvent")]

/**
 *  Dispatched after the WindowedApplication object moves.
 *
 *  @eventType mx.events.FlexNativeWindowBoundsEvent.WINDOW_MOVE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="windowMove", type="mx.events.FlexNativeWindowBoundsEvent")]

/**
 *  Dispatched after the underlying NativeWindow object is resized.
 *
 *  @eventType mx.events.FlexNativeWindowBoundsEvent.WINDOW_RESIZE
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="windowResize", type="mx.events.FlexNativeWindowBoundsEvent")]

//--------------------------------------
//  Effects
//--------------------------------------

/**
 *  Played when the window is closed.
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Effect(name="closeEffect", event="windowClose")]

/**
 *  Played when the component is minimized.
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Effect(name="minimizeEffect", event="windowMinimize")]

/**
 *  Played when the component is unminimized.
 *
 *  @langversion 3.0
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Effect(name="unminimizeEffect", event="windowUnminimize")]

//--------------------------------------
//  Excluded APIs
//--------------------------------------

[Exclude(name="controlBarContent", kind="property")]
[Exclude(name="controlBarGroup", kind="property")]
[Exclude(name="controlBarLayout", kind="property")]
[Exclude(name="controlBarVisible", kind="property")]
[Exclude(name="moveEffect", kind="effect")]
[Exclude(name="scriptTimeLimit", kind="property")]

//--------------------------------------
//  SkinStates
//--------------------------------------

/**
 *  The application is enabled and inactive.
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[SkinState("normalAndInactive")]

/**
 *  The application is disabled and inactive.
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[SkinState("disabledAndInactive")]

public class WindowedApplication extends Application implements IWindow {

    /**
     *  The skin part that defines the gripper button used to resize the window.
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    [SkinPart (required="false")]
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
    [SkinPart (required = "false")]
    public var statusBar:IVisualElement;

    //----------------------------------
    //  statusText
    //----------------------------------

    /**
     *  The skin part that defines the display of the status bar's text.
     *
     *  @langversion 3.0
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    [SkinPart (required="false")]
    public var statusText:TextBase;

    //----------------------------------
    //  titleBar
    //----------------------------------

    /**
     *  The skin part that defines the display of the title bar.
     *
     *  @langversion 3.0
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    [SkinPart (required="false")]
    public var titleBar:TitleBar;

  public function get applicationID():String {
    return "faked";
  }

  private var _alwaysInFront:Boolean = false;
  public function get alwaysInFront():Boolean {
    return _alwaysInFront;
  }

  public function set alwaysInFront(value:Boolean):void {
    _alwaysInFront = value;
  }

  public function get autoExit():Boolean {
    return true;
  }

  public function set autoExit(value:Boolean):void {
  }

  private var _backgroundFrameRate:Number = -1;

  public function get backgroundFrameRate():Number {
    return _backgroundFrameRate;
  }

  public function set backgroundFrameRate(frameRate:Number):void {
    _backgroundFrameRate = frameRate;
  }

  public function get closed():Boolean {
    return false;
  }

  private var _dockIconMenu:FlexNativeMenu;
  public function get dockIconMenu():FlexNativeMenu {
    return _dockIconMenu;
  }

  public function set dockIconMenu(value:FlexNativeMenu):void {
    _dockIconMenu = value;
  }

  public function get maximizable():Boolean {
    return false;
  }

  public function get minimizable():Boolean {
    return false;
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

  public function get nativeWindow():NativeWindow {
    return null;
  }

  public function get resizable():Boolean {
    return false;
  }

  public function get nativeApplication():NativeApplication {
    return null;
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

    [Bindable("statusChanged")]
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
  public function get systemChrome():String {
    return _systemChrome;
  }

  private var _systemTrayIconMenu:FlexNativeMenu;
  public function get systemTrayIconMenu():FlexNativeMenu {
    return _systemTrayIconMenu;
  }

  public function set systemTrayIconMenu(value:FlexNativeMenu):void {
    _systemTrayIconMenu = value;
  }

  private var _title:String = "";
  private var titleChanged:Boolean = false;

  [Bindable("titleChanged")]
  public function get title():String {
    return _title;
  }

  public function set title(value:String):void {
    _title = value;
    titleChanged = true;

    invalidateProperties();
    invalidateSize();
    invalidateDisplayList();

    dispatchEvent(new Event("titleChanged"));
  }

  private var _titleIcon:Class;
  private var titleIconChanged:Boolean = false;
  [Bindable("titleIconChanged")]
  public function get titleIcon():Class {
    return _titleIcon;
  }

  public function set titleIcon(value:Class):void {
    _titleIcon = value;
    titleIconChanged = true;

    invalidateProperties();
    invalidateSize();
    invalidateDisplayList();

    dispatchEvent(new Event("titleIconChanged"));
  }

  public function get transparent():Boolean {
    return false;
  }

  public function get type():String {
    return NativeWindowType.NORMAL;
  }

  public var useNativeDragManager:Boolean = true;

  override protected function commitProperties():void {
    super.commitProperties();

    if (titleIconChanged) {
      if (titleBar) {
        titleBar.titleIcon = _titleIcon;
      }
      titleIconChanged = false;
    }

    if (titleChanged) {
      if (titleBar) {
        titleBar.title = _title;
      }
      titleChanged = false;
    }

    if (showStatusBarChanged) {
      if (statusBar) {
        statusBar.visible = _showStatusBar;
        statusBar.includeInLayout = _showStatusBar;
      }
      showStatusBarChanged = false;
    }

    if (statusChanged) {
      if (statusText) {
        statusText.text = status;
      }
      statusChanged = false;
    }
  }

    override protected function partAdded(partName:String, instance:Object):void
    {
        super.partAdded(partName, instance);

        if (instance == statusBar)
        {
            statusBar.visible = _showStatusBar;
            statusBar.includeInLayout = _showStatusBar;
            showStatusBarChanged = false;
        }
        else if (instance == titleBar)
        {
            if (!nativeWindow.closed)
            {
                if (_title == "" && systemManager.stage.nativeWindow.title != null)
                    _title = systemManager.stage.nativeWindow.title;
                else
                    systemManager.stage.nativeWindow.title = _title;
            }

            titleBar.title = _title;
            titleChanged = false;
        }
        else if (instance == statusText)
        {
            statusText.text = status;
            statusChanged = false;
        }
    }

  public function activate():void {
  }

  public function close():void {
  }

  public function exit():void {
  }

  public function maximize():void {
  }

  public function minimize():void {
  }

  public function restore():void {
  }

  public function orderInBackOf(window:IWindow):Boolean {
    return true;
  }

  public function orderInFrontOf(window:IWindow):Boolean {
    return true;
  }

  public function orderToBack():Boolean {
    return true;
  }

  public function orderToFront():Boolean {
    return true;
  }

  override protected function getCurrentSkinState():String {
    return enabled ? "normal" : "disabled";
  }
}
}
