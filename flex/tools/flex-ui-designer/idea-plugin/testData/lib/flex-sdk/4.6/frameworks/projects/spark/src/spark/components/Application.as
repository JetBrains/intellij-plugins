////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2003-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package spark.components
{

import flash.display.DisplayObject;
import flash.display.InteractiveObject;
import flash.events.ContextMenuEvent;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.SoftKeyboardEvent;
import flash.events.UncaughtErrorEvent;
import flash.external.ExternalInterface;
import flash.geom.Rectangle;
import flash.net.URLRequest;
import flash.net.navigateToURL;
import flash.system.Capabilities;
import flash.ui.ContextMenu;
import flash.ui.ContextMenuItem;
import flash.utils.Dictionary;
import flash.utils.setInterval;

import mx.core.EventPriority;
import mx.core.FlexGlobals;
import mx.core.IInvalidating;
import mx.core.InteractionMode;
import mx.core.Singleton;
import mx.core.UIComponentGlobals;
import mx.core.mx_internal;
import mx.managers.FocusManager;
import mx.managers.IActiveWindowManager;
import mx.managers.ILayoutManager;
import mx.managers.ISystemManager;
import mx.managers.SystemManager;
import mx.managers.ToolTipManager;
import mx.utils.BitFlagUtil;
import mx.utils.DensityUtil;
import mx.utils.LoaderUtil;

import spark.layouts.supportClasses.LayoutBase;

use namespace mx_internal;

//--------------------------------------
//  Events
//--------------------------------------

/**
 *  Dispatched after the Application has been initialized,
 *  processed by the LayoutManager, and attached to the display list.
 *
 *  @eventType mx.events.FlexEvent.APPLICATION_COMPLETE
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="applicationComplete", type="mx.events.FlexEvent")]

/**
 *  Dispatched when an HTTPService call fails.
 *
 *  @eventType flash.events.ErrorEvent.ERROR
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Event(name="error", type="flash.events.ErrorEvent")]

/**
 *  Dispatched when an uncaught error is caught by the Global Exception Handler
 *
 *  @eventType flash.events.UncaughtErrorEvent.UNCAUGHT_ERROR
 *
 *  @langversion 3.0
 *  @playerversion Flash 10.1
 *  @playerversion AIR 2.0
 *  @productversion Flex 4.5
 */
[Event(name="uncaughtError", type="flash.events.UncaughtErrorEvent")]

//--------------------------------------
//  Styles
//--------------------------------------

/**
 *  The background color of the application. This color is used as the stage color for the
 *  application and the background color for the HTML embed tag.
 *
 *  @default 0x464646
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Style(name="backgroundColor", type="uint", format="Color", inherit="no")]

//--------------------------------------
//  Excluded APIs
//--------------------------------------

[Exclude(name="direction", kind="property")]
[Exclude(name="tabIndex", kind="property")]
[Exclude(name="toolTip", kind="property")]
[Exclude(name="x", kind="property")]
[Exclude(name="y", kind="property")]

//--------------------------------------
//  Other metadata
//--------------------------------------

/**
 *  The frameworks must be initialized by SystemManager.
 *  This factoryClass will be automatically subclassed by any
 *  MXML applications that don't explicitly specify a different
 *  factoryClass.
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
[Frame(factoryClass="mx.managers.SystemManager")]

[ResourceBundle("components")]

/**
 *  Flex defines a default, or Application, container that lets you start
 *  adding content to your application without explicitly defining
 *  another container.
 *
 *  <p>The Application container has the following default characteristics:</p>
 *     <table class="innertable">
 *        <tr>
 *           <th>Characteristic</th>
 *           <th>Description</th>
 *        </tr>
 *        <tr>
 *           <td>Default size</td>
 *           <td>375 pixels high and 500 pixels wide in the Standalone Flash Player,
 *               and all available space in a browser</td>
 *        </tr>
 *        <tr>
 *           <td>Minimum size</td>
 *           <td>0 pixels wide and 0 pixels high</td>
 *        </tr>
 *        <tr>
 *           <td>Maximum size</td>
 *           <td>No limit</td>
 *        </tr>
 *        <tr>
 *           <td>Default skin class</td>
 *           <td>spark.skins.spark.ApplicationSkin</td>
 *        </tr>
 *     </table>
 *
 *  @mxml
 *
 *  <p>The <code>&lt;s:Application&gt;</code> tag inherits all of the tag
 *  attributes of its superclass and adds the following tag attributes:</p>
 *
 *  <pre>
 *  &lt;s:Application
 *    <strong>Properties</strong>
 *    applicationDPI=<i>Device dependent</i>"
 *    backgroundColor="0xFFFFFF"
 *    colorCorrection="default"
 *    controlBarContent="null"
 *    controlBarLayout="HorizontalLayout"
 *    controlBarVisible="true"
 *    frameRate="24"
 *    pageTitle""
 *    preloader="<i>No default</i>"
 *    preloaderChromeColor="<i>No default</i>"
 *    resizeForSoftKeyboard=true"
 *    runtimeDPIProvider="RuntimeDPIProvider"
 *    scriptRecursionLimit="1000"
 *    scriptTimeLimit="60"
 *    splashScreenImage=""
 *    splashScreenMinimumDisplayTime="1000"
 *    splashScreenScaleMode="none"
 *    usePreloader="true"
 *    viewSourceURL=""
 *    xmlns:<i>No default</i>="<i>No default</i>"
 *
 *    <strong>Events</strong>
 *    applicationComplete="<i>No default</i>"
 *    error="<i>No default</i>"
 *  /&gt;
 *  </pre>
 *
 *  @see spark.skins.spark.ApplicationSkin
 *  @includeExample examples/ApplicationContainerExample.mxml
 *
 *  @langversion 3.0
 *  @playerversion Flash 10
 *  @playerversion AIR 1.5
 *  @productversion Flex 4
 */
public class Application extends SkinnableContainer
{
    include "../core/Version.as";

    //--------------------------------------------------------------------------
    //
    //  Class constants
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    private static const CONTROLBAR_PROPERTY_FLAG:uint = 1 << 0;

    /**
     *  @private
     */
    private static const LAYOUT_PROPERTY_FLAG:uint = 1 << 1;

    /**
     *  @private
     */
    private static const VISIBLE_PROPERTY_FLAG:uint = 1 << 2;

    //--------------------------------------------------------------------------
    //
    //  Class properties
    //
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     *  Constructor.
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public function Application()
    {
        UIComponentGlobals.layoutManager = ILayoutManager(
            Singleton.getInstance("mx.managers::ILayoutManager"));
        UIComponentGlobals.layoutManager.usePhasedInstantiation = true;

        if (!FlexGlobals.topLevelApplication)
            FlexGlobals.topLevelApplication = this;

        super();

        showInAutomationHierarchy = true;

        initResizeBehavior();
    }

    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------
    
    /**
     *  @private
     *  Variable that determines whether this application is running on iOS.
     */
    private var isIOS:Boolean = false;
    
    /**
     *  @private
     *  This variable stores the last object that received the SOFT_KEYBOARD_ACTIVATE
     *  event.
     */
    private var softKeyboardTarget:Object = null;
    
    /**
     *  @private
     *  Flag set to true if the application has temporarily set its explicit
     *  width and height to deal with orientation.
     */ 
    private var explicitSizingForOrientation:Boolean = false;
    
    /**
     *  @private
     *  Caches the application's width and height values for better resizing
     *  of the screen during orientation changes. Keys are composed of the width and
     *  height values in a string of the form "w:h", e.g. "1004:768" and represent
     *  the screen dimensions before the orientation change. Values are
     *  objects containing width and height properties that represent the screen dimensions
     *  after the orientation change.
     */
    private var cachedDimensions:Dictionary;
    
    /**
     *  @private
     *  Previous width of the application prior to an orientation change 
     */    
    private var previousWidth:Number;
    
    /**
     *  @private
     *  Previous height of the application prior to an orientation change 
     */    
    private var previousHeight:Number;
    
    /**
     *  @private
     *  Flag to determine if the keyboard is active during an orientation change,
     *  to minimize renders during the orientation change.
     */
    private var keyboardActiveInOrientationChange:Boolean = false;
    
    /**
     *  @private
     */
    private var resizeHandlerAdded:Boolean = false;

    /**
     *  @private
     */
    private var percentBoundsChanged:Boolean;

    /**
     *  @private
     *  Placeholder for Preloader object reference.
     */
    private var preloadObj:Object;

    /**
     *  @private
     *  This flag indicates whether the width of the Application instance
     *  can change or has been explicitly set by the developer.
     *  When the stage is resized we use this flag to know whether the
     *  width of the Application should be modified.
     */
    private var resizeWidth:Boolean = true;

    /**
     *  @private
     *  This flag indicates whether the height of the Application instance
     *  can change or has been explicitly set by the developer.
     *  When the stage is resized we use this flag to know whether the
     *  height of the Application should be modified.
     */
    private var resizeHeight:Boolean = true;
    
    /**
     *  @private
     */
    private static var _softKeyboardBehavior:String = null;
    
    /**
     *  @private
     */
    mx_internal var isSoftKeyboardActive:Boolean = false;
    
    /**
     *  @private
     */
    private var synchronousResize:Boolean = false;

    /**
     * @private
     * (Possibly null) reference to the View Source context menu item,
     * so that we can update it for runtime localization.
     */
    private var viewSourceCMI:ContextMenuItem;

    /**
     * @private 
     * Return the density scaling factor for the application 
     */    
    private function get scaleFactor():Number
    {
        if (systemManager)
            return (systemManager as SystemManager).densityScale;

        return 1;
    }
    
    //----------------------------------
    //  colorCorrection
    //----------------------------------

    [Inspectable(enumeration="default,off,on", defaultValue="default" )]

   /**
    *  The value of the stage's <code>colorCorrection</code> property.
    *  If this application does not have access to the stage's <code>colorCorrection</code> property,
    *  the value of the <code>colorCorrection</code> property is <code>null</code>.
    *
    *  <p>Only the main application is allowed to set the <code>colorCorrection</code>
    *  property. If a nested application's needs to set the color correction property, it
    *  must set it by referencing the main application's instance.</p>
    *
    *  @default ColorCorrection.DEFAULT
    *
    *  @see flash.display.ColorCorrection
    *
    *  @langversion 3.0
    *  @playerversion Flash 10
    *  @playerversion AIR 1.5
    *  @productversion Flex 4
    */
    public function get colorCorrection():String
    {
        try
        {
            var sm:ISystemManager = systemManager;
            if (sm && sm.stage)
                return sm.stage.colorCorrection;
        }
        catch (e:SecurityError)
        {
            // ignore error if this application is not allow
            // to view the colorCorrection property.
        }

        return null;
    }

    /**
     * @private
     */
    public function set colorCorrection(value:String):void
    {
        // Since only the main application is allowed to change the value this property, there
        // is no need to catch security violations like in the getter.
        var sm:ISystemManager = systemManager;
        if (sm && sm.stage && sm.isTopLevelRoot())
            sm.stage.colorCorrection = value;
    }

    /**
     *  @private
     *  Several properties are proxied to controlBarGroup.  However, when controlBarGroup
     *  is not around, we need to store values set on SkinnableContainer.  This object
     *  stores those values.  If controlBarGroup is around, the values are stored
     *  on the controlBarGroup directly.  However, we need to know what values
     *  have been set by the developer on the SkinnableContainer (versus set on
     *  the controlBarGroup or defaults of the controlBarGroup) as those are values
     *  we want to carry around if the controlBarGroup changes (via a new skin).
     *  In order to store this info effeciently, controlBarGroupProperties becomes
     *  a uint to store a series of BitFlags.  These bits represent whether a
     *  property has been explicitely set on this SkinnableContainer.  When the
     *  controlBarGroup is not around, controlBarGroupProperties is a typeless
     *  object to store these proxied properties.  When controlBarGroup is around,
     *  controlBarGroupProperties stores booleans as to whether these properties
     *  have been explicitely set or not.
     */
    private var controlBarGroupProperties:Object = { visible: true };

    //----------------------------------
    //  controlBarGroup
    //----------------------------------

    [SkinPart(required="false")]

    /**
     *  The skin part that defines the appearance of the
     *  control bar area of the container.
     *  By default, the ApplicationSkin class defines the control bar area to appear at the top
     *  of the content area of the Application container with a grey background.
     *
     *  @see spark.skins.spark.ApplicationSkin
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public var controlBarGroup:Group;

    //----------------------------------
    //  controlBarContent
    //----------------------------------

    [ArrayElementType("mx.core.IVisualElement")]

    /**
     *  The set of components to include in the control bar area of the
     *  Application container.
     *  The location and appearance of the control bar area of the Application container
     *  is determined by the spark.skins.spark.ApplicationSkin class.
     *  By default, the ApplicationSkin class defines the control bar area to appear at the top
     *  of the content area of the Application container with a grey background.
     *  Create a custom skin to change the default appearance of the control bar.
     *
     *  @default null
     *
     *  @see spark.skins.spark.ApplicationSkin
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public function get controlBarContent():Array
    {
        if (controlBarGroup)
            return controlBarGroup.getMXMLContent();
        else
            return controlBarGroupProperties.controlBarContent;
    }

    /**
     *  @private
     */
    public function set controlBarContent(value:Array):void
    {
        if (controlBarGroup)
        {
            controlBarGroup.mxmlContent = value;
            controlBarGroupProperties = BitFlagUtil.update(controlBarGroupProperties as uint,
                                                        CONTROLBAR_PROPERTY_FLAG, value != null);
        }
        else
            controlBarGroupProperties.controlBarContent = value;

        invalidateSkinState();
    }

    //----------------------------------
    //  controlBarLayout
    //----------------------------------

    /**
     *  Defines the layout of the control bar area of the container.
     *
     *  @default HorizontalLayout
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public function get controlBarLayout():LayoutBase
    {
        return (controlBarGroup)
            ? controlBarGroup.layout
            : controlBarGroupProperties.layout;
    }

    public function set controlBarLayout(value:LayoutBase):void
    {
        if (controlBarGroup)
        {
            controlBarGroup.layout = value;
            controlBarGroupProperties = BitFlagUtil.update(controlBarGroupProperties as uint,
                                                        LAYOUT_PROPERTY_FLAG, true);
        }
        else
            controlBarGroupProperties.layout = value;
    }

    //----------------------------------
    //  controlBarVisible
    //----------------------------------

    /**
     *  If <code>true</code>, the control bar is visible.
     *  The flag has no affect if there is no value set for
     *  the <code>controlBarContent</code> property.
     *
     *  <p><b>Note:</b> The Application container does not monitor the
     *  <code>controlBarGroup</code> property.
     *  If other code makes it invisible, the Application container might
     *  not update correctly.</p>
     *
     *  @default true
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public function get controlBarVisible():Boolean
    {
        return (controlBarGroup)
            ? controlBarGroup.visible
            : controlBarGroupProperties.visible;
    }

    public function set controlBarVisible(value:Boolean):void
    {
        if (controlBarGroup)
        {
            controlBarGroup.visible = value;
            controlBarGroupProperties = BitFlagUtil.update(controlBarGroupProperties as uint,
                                                        VISIBLE_PROPERTY_FLAG, value);
        }
        else
            controlBarGroupProperties.visible = value;

        invalidateSkinState();
        if (skin)
            skin.invalidateSize();
    }

    //--------------------------------------------------------------------------
    //
    //  Compile-time pseudo-properties
    //
    //--------------------------------------------------------------------------

    // These declarations correspond to the MXML-compile-time attributes
    // allowed on the <Application> tag. These attributes affect the MXML
    // compiler, but they aren't actually used in the runtime framework.
    // The declarations appear here in order to provide metadata about these
    // attributes for Flash Builder.

    //----------------------------------
    //  frameRate
    //----------------------------------

    [Inspectable(defaultValue="24")]

    /**
     *    Specifies the frame rate of the application.
     *
     *    <p><b>Note:</b> This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *    @default 24
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public var frameRate:Number;

    //----------------------------------
    //  pageTitle
    //----------------------------------

    /**
     *    Specifies a string that appears in the title bar of the browser.
     *    This property provides the same functionality as the
     *    HTML <code>&lt;title&gt;</code> tag.
     *
     *    <p><b>Note:</b> This property cannot be set by ActionScript code; it must be set in MXML code.
     *    The value set in MXML code is designed to be used by a tool to update the HTML templates
     *    provided with the SDK.</p>
     *
     *    @default ""
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public var pageTitle:String;

    //----------------------------------
    //  preloader
    //----------------------------------

    [Inspectable(defaultValue="mx.preloaders.DownloadProgressBar")]

    /**
     *  The application container supports an application preloader that 
     *  uses a download progress bar to show the download and initialization progress 
     *  of an application SWF file. 
     *  By default, the application preloader is enabled. 
     *  The preloader tracks how many bytes have been downloaded and continually 
     *  updates the progress bar. 
     *
     *  <p>Use this property to specify the path of a 
     *  component that defines a custom progress indicator.
     *  To create a custom progress indicator, you typically create a subclass of the 
     *  SparkDownloadProgressBar or DownloadProgressBar class, or create a subclass of 
     *  the flash.display.Sprite class that implements the 
     *  mx.preloaders.IPreloaderDisplay interface. </p>
     *
     *  <p><b>Note:</b> This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *  @see mx.preloaders.SparkDownloadProgressBar 
     *  @see mx.preloaders.DownloadProgressBar 
     *  @see flash.display.Sprite
     *  @see mx.preloaders.IPreloaderDisplay
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public var preloader:Object;

    //----------------------------------
    //  preloaderChromeColor
    //----------------------------------

    [Inspectable(defaultValue="0xCCCCCC", format="Color")]

    /**
     *  Specifies the chrome color used by the default preloader component. This property
     *  has the same effect as the <code>chromeColor</code> style used by Spark skins.
     *  Typically this property should be set to the same value as the
     *  Application container's <code>chromeColor</code> style property.
     *
     *  <p>Note: This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */

    /* This property is not directly read by the download progress bar (preloader)
     * component. It is here so that it gets picked up by the compiler and included
     * in the info() structure for the generated system manager. The download progress bar
     * grabs the value directly from the info() structure. */
    public var preloaderChromeColor:uint;

    //----------------------------------
    //  scriptRecursionLimit
    //----------------------------------

    [Inspectable(defaultValue="1000")]

    /**
     *    Specifies the maximum depth of Flash Player or AIR
     *    call stack before the player stops.
     *    This is essentially the stack overflow limit.
     *
     *    <p><b>Note:</b> This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *    @default 1000
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public var scriptRecursionLimit:int;

    //----------------------------------
    //  scriptTimeLimit
    //----------------------------------

    [Inspectable(defaultValue="60")]

    /**
     *    Specifies the maximum duration, in seconds, that an ActionScript
     *    event handler can execute before Flash Player or AIR assumes
     *    that it is hung, and aborts it.
     *    The maximum allowable value that you can set is 60 seconds.
     *
     *  @default 60
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public var scriptTimeLimit:Number;

    //----------------------------------
    //  splashScreenImage
    //----------------------------------

    /**
     *  The image class for the SplashScreen preloader.
     *  Typically you set this property to either an embedded resource
     *  or the name of a <code>SplashScreenImage</code> class defined in a separate MXML file.
     *  Example of setting splashScreenImage to an embedded image:
     *
     *  <pre>splashScreenImage="&#64;Embed('Default.png')"</pre>
     *
     *  <p><b>Note:</b> The property has effect only when the <code>preloader</code>
     *  property is set to spark.preloaders.SplashScreen.
     *  The spark.preloaders.SplashScreen class is the default preloader for Mobile Flex applications.
     *  This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *  <p><b>Note:</b> You must add the frameworks\libs\mobile\mobilecomponents.swc to the 
     *  library path of the application to support the splash screen in a desktop application.</p>
     *
     *  @see spark.preloaders.SplashScreen
     *  @see #splashScreenScaleMode
     *  @see #splashScreenMinimumDisplayTime
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */
    public function get splashScreenImage():Class
    {
        // When set through mxml, the compiler uses the value in the generated loader class.
        return systemManager.info()["splashScreenImage"];
    }
    
    /**
     *  @private
     */
    public function set splashScreenImage(value:Class):void
    {
        systemManager.info()["splashScreenImage"] = value;
    }

    //----------------------------------
    //  splashScreenScaleMode
    //----------------------------------

    [Inspectable(enumeration="none,letterbox,stretch,zoom", defaultValue="none")]

    /**
     *  The splash screen image scale mode:
     *  
     *  <ul>
     *      <li>A value of <code>none</code> implies that the image size is set 
     *      to match its intrinsic size.</li>
     *
     *      <li>A value of <code>stretch</code> sets the width and the height of the image to the
     *      stage width and height, possibly changing the content aspect ratio.</li>
     *
     *      <li>A value of <code>letterbox</code> sets the width and height of the image 
     *      as close to the stage width and height as possible while maintaining aspect ratio.  
     *      The image is stretched to a maximum of the stage bounds,
     *      with spacing added inside the stage to maintain the aspect ratio if necessary.</li>
     *
     *      <li>A value of <code>zoom</code> is similar to <code>letterbox</code>, except 
     *      that <code>zoom</code> stretches the image past the bounds of the stage, 
     *      to remove the spacing required to maintain aspect ratio.
     *      This setting has the effect of using the entire bounds of the stage, but also 
     *      possibly cropping some of the image.</li>
     *  </ul>
     *
     *  <p>The portion of the stage that is not covered by the image shows 
     *  in the Application container's <code>backgroundColor</code>.</p>
     *
     *  <p><b>Note:</b> The property has effect only when the <code>splashScreenImage</code> property 
     *  is set and the <code>preloader</code> property is set to spark.preloaders.SplashScreen.
     *  The spark.preloaders.SplashScreen class is the default preloader for Mobile Flex applications.
     *  This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *  <p><b>Note:</b> You must add the frameworks\libs\mobile\mobilecomponents.swc to the 
     *  library path of the application to support the splash screen in a desktop application.</p>
     *
     *  @default "none"
     *  @see #splashScreenImage
     *  @see #splashScreenMinimumDisplayTime
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */
    public var splashScreenScaleMode:String;

    //----------------------------------
    //  splashScreenMinimumDisplayTime
    //----------------------------------

    /**
     *  Minimum amount of time, in milliseconds, to show the splash screen image.
     *  Specify the splash screen image by using the <code>splashScreenImage</code> property.
     *
     *  <p><b>Note:</b> The property has effect only when the <code>splashScreenImage</code> property 
     *  is set and the <code>preloader</code> property is set to spark.preloaders.SplashScreen.
     *  The spark.preloaders.SplashScreen class is the default preloader for Mobile Flex applications.
     *  This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *  <p><b>Note:</b> You must add the frameworks\libs\mobile\mobilecomponents.swc to the 
     *  library path of the application to support the splash screen in a desktop application.</p>
     *
     *  @default 1000
     * 
     *  @see #splashScreenImage
     *  @see #splashScreenScaleMode
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */
    public var splashScreenMinimumDisplayTime:Number;
    
    //----------------------------------
    //  applicationDPI
    //----------------------------------
    
    /**
     *  Storage for the applicationDPI property.
     * 
     *  @private
     */
    private var _applicationDPI:Number = NaN;
    
    [Inspectable(category="General", enumeration="160,240,320")]
    
    /**
     *  The DPI of the application.
     *  
     *  By default, this is the DPI of the device that the application is currently running on.
     * 
     *  When set in MXML, Flex will scale the Application to match its DPI to the
     *  <code>runtimeDPI</code>.
     *  
     *  This property cannot be set by ActionScript code; it must be set in MXML code.
     * 
     *  @see #runtimeDPI
     *  @see mx.core.DPIClassification
     *
     *  @langversion 3.0
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */
    public function get applicationDPI():Number
    {
        if (isNaN(_applicationDPI))
        {
            var value:String = systemManager.info()["applicationDPI"];
            _applicationDPI = value ? Number(value) : runtimeDPI;
        }
        
        return _applicationDPI;
    }
    
    /**
     *  @private
     */
    public function set applicationDPI(value:Number):void
    {
        // Can't change at run-time so do nothing here.
        // The compiler will propagate the MXML value to the
        // systemManager's info object.
    }
    
    //----------------------------------
    //  runtimeDPI
    //----------------------------------
    
    /**
     *  The DPI of the device the application is currently running on.
     *
     *  Flex rounds the value to one of the <code>DPIClassification</code> choices. 
     *   
     *  @see #applicationDPI
     *  @see mx.core.DPIClassification
     *
     *  @langversion 3.0
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */  
    public function get runtimeDPI():Number
    {
        return DensityUtil.getRuntimeDPI();
    }
    
    //----------------------------------
    //  runtimeDPIProvider
    //----------------------------------
    
    /**
     *  A class that extends RuntimeDPIProvider and overrides the default Flex
     *  calculations for <code>runtimeDPI</code>.
     * 
     *  <p>Flex's default mappings are:
     *     <table class="innertable">
     *        <tr><td>160 DPI</td><td>&lt;200 DPI</td></tr>
     *        <tr><td>240 DPI</td><td>&gt;=200 DPI and &lt;280 DPI</td></tr>
     *        <tr><td>320 DPI</td><td>&gt;=280 DPI</td></tr>
     *     </table>
     *  </p>
     * 
     *  This property cannot be set by ActionScript code; it must be set in MXML code.
     * 
     *  @default spark.components.RuntimeDPIProvider
     * 
     *  @see #applicationDPI
     *  @see #runtimeDPI
     *  @see mx.core.DPIClassification
     *  @see mx.core.RuntimeDPIProvider
     *
     *  @langversion 3.0
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */
    public function get runtimeDPIProvider():Class
    {
        return systemManager.info()["runtimeDPIProvider"];
    }
    
    /**
     *  @private
     */
    public function set runtimeDPIProvider(value:Class):void
    {
        // Can't change at run-time so do nothing here.
        // The compiler will propagate the MXML value to the
        // systemManager's info object.
    }
        
    //----------------------------------
    //  usePreloader
    //----------------------------------

    [Inspectable(defaultValue="true")]

    /**
     *    If <code>true</code>, specifies to display the application preloader.
     *
     *    <p><b>Note:</b> This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *    @default true
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public var usePreloader:Boolean;

    //--------------------------------------------------------------------------
    //
    //  Overridden properties (to block metadata from superclasses)
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  id
    //----------------------------------

    [Inspectable(environment="none")]

    /**
     *  @private
     */
    override public function get id():String
    {
        if (!super.id &&
            this == FlexGlobals.topLevelApplication &&
            ExternalInterface.available)
        {
            return ExternalInterface.objectID;
        }

        return super.id;
    }

    //----------------------------------
    //  percentHeight
    //----------------------------------

    /**
     *  @private
     */
    override public function set percentHeight(value:Number):void
    {
        if (value != super.percentHeight)
        {
            super.percentHeight = value;
            percentBoundsChanged = true;
            invalidateProperties();
        }
    }

    //----------------------------------
    //  percentWidth
    //----------------------------------

    /**
     *  @private
     */
    override public function set percentWidth(value:Number):void
    {
        if (value != super.percentWidth)
        {
            super.percentWidth = value;
            percentBoundsChanged = true;
            invalidateProperties();
        }
    }

    //----------------------------------
    //  tabIndex
    //----------------------------------

    [Inspectable(environment="none")]

    /**
     *  @private
     */
    override public function set tabIndex(value:int):void
    {
    }

    //----------------------------------
    //  toolTip
    //----------------------------------

    [Inspectable(environment="none")]

    /**
     *  @private
     */
    override public function set toolTip(value:String):void
    {
    }

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  aspectRatio
    //----------------------------------
    
    /**
     *  Returns the aspect ratio of the top level stage based on its width
     *  and height.  If the width of the stage is greater than the height,
     *  the stage is considered to be in "landscape" orientation.  Otherwise,
     *  portrait is returned.
     *  
	 *  @return This method will return "landscape" if the application is
	 *  in a landsacpe orientation, and "portrait" otherwise.
	 * 
     *  @langversion 3.0
     *  @playerversion Flash 10.2
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */
    public function get aspectRatio():String
    {
		var actualHeight:Number = height;

		// If the application has resized itself in response to the
		// softKeyboard becoming visible, we need to compare against
		// the height without the keyboard active
		if (isSoftKeyboardActive && softKeyboardBehavior == "none" && resizeForSoftKeyboard)
			actualHeight += stage.softKeyboardRect.height;
		
        return width > actualHeight ? "landscape" : "portrait";
    }
    
    //----------------------------------
    //  parameters
    //----------------------------------

    /**
     *  @private
     *  Storage for the parameters property.
     *  This variable is set in the initialize() method of SystemManager.
     */
    mx_internal var _parameters:Object;

    /**
     *  An Object containing name-value
     *  pairs representing the parameters provided to this Application.
     *
     *  <p>You can use a for-in loop to extract all the names and values
     *  from the parameters Object.</p>
     *
     *  <p>There are two sources of parameters: the query string of the
     *  Application's URL, and the value of the FlashVars HTML parameter
     *  (this affects only the main Application).</p>
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public function get parameters():Object
    {
        return _parameters;
    }

    //----------------------------------
    //  resizeForSoftKeyboard
    //----------------------------------
    private var _resizeForSoftKeyboard:Boolean = false;
    
    /**
     *  Some devices do not support a hardware keyboard. 
     *  Instead, these devices use a keyboard that opens on 
     *  the screen when necessary. 
     *  The screen keyboard, also called a soft or virtual keyboard, 
     *  closes after the user enters the information, or when the user cancels the operation.
     *  A value of <code>true</code> means the application 
     *  is resized when the soft keyboard is open or
     *  closed.  
     *
     *  <p>To enable application resizing, you must also set the 
     *  <code>&lt;softKeyboardBehavior&gt;</code> attribute in the 
     *  application's xml descriptor file to <code>none</code>.</p>
     * 
     *  @default false
     *
     *  @langversion 3.0
     *  @playerversion AIR 2.5
     *  @productversion Flex 4.5
     */ 
    public function get resizeForSoftKeyboard():Boolean
    {
        return _resizeForSoftKeyboard;
    }
    
    public function set resizeForSoftKeyboard(value:Boolean):void
    {
        if (_resizeForSoftKeyboard != value)
        {
            _resizeForSoftKeyboard = value;
        }
    }
    
    //----------------------------------
    //  url
    //----------------------------------

    /**
     *  @private
     *  Storage for the url property.
     *  This variable is set in the initialize().
     */
    mx_internal var _url:String;

    /**
     *  The URL from which this Application's SWF file was loaded.
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public function get url():String
    {
        return _url;
    }

    //----------------------------------
    //  viewSourceURL
    //----------------------------------

    /**
     *  @private
     *  Storage for viewSourceURL property.
     */
    private var _viewSourceURL:String;

    /**
     *  URL where the application's source can be viewed. Setting this
     *  property inserts a "View Source" menu item into the application's
     *  default context menu.  Selecting the menu item opens the
     *  <code>viewSourceURL</code> in a new window.
     *
     *  <p>You must set the <code>viewSourceURL</code> property
     *  using MXML, not using ActionScript, as the following example shows:</p>
     *
     *  <pre>
     *    &lt;Application viewSourceURL="http://path/to/source"&gt;
     *      ...
     *    &lt;/Application&gt;</pre>
     *
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    public function get viewSourceURL():String
    {
        return _viewSourceURL;
    }

    /**
     *  @private
     */
    public function set viewSourceURL(value:String):void
    {
        _viewSourceURL = value;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden methods: UIComponent
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    override protected function invalidateParentSizeAndDisplayList():void
    {
        if (!includeInLayout)
            return;

        var p:IInvalidating = parent as IInvalidating;
        if (!p)
        {
            if (parent is ISystemManager)
                ISystemManager(parent).invalidateParentSizeAndDisplayList();

            return;
        }

        super.invalidateParentSizeAndDisplayList();
    }

    /**
     *  @private
     */
    override public function initialize():void
    {
        // trace("FxApp initialize FxApp");

        var sm:ISystemManager = systemManager;

        // add listener if one is already attached
        if (hasEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR))
            systemManager.loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorRedispatcher);

        // Determine if we are running on an iOS device
        isIOS = Capabilities.version.indexOf("IOS") == 0;
        
        // To prevent a flicker described in SDK-30133, a flex application listens
        // for orientationChanging events dispatched by iOS AIR applications.
        // In the handler, the stage's width and height are swapped, and a validation
        // pass is forced to allow the application to resize and re-layout itself before the
        // orientation change animation occurs
        if (isIOS && sm && sm.stage && sm.isTopLevelRoot())
        {
            sm.stage.addEventListener("orientationChanging", stage_orientationChangingHandler);
            sm.stage.addEventListener("orientationChange", stage_orientationChange);
        }
        
        _url = LoaderUtil.normalizeURL(sm.loaderInfo);
        _parameters = sm.loaderInfo.parameters;

        initManagers(sm);

        // Setup the default context menu here. This allows the application
        // developer to override it in the initialize event, if desired.
        initContextMenu();

        super.initialize();

        // Stick a timer here so that we will execute script every 1.5s
        // no matter what.
        // This is strictly for the debugger to be able to halt.
        // Note: isDebugger is true only with a Debugger Player.
        if (sm.isTopLevel() && Capabilities.isDebugger == true)
            setInterval(debugTickler, 1500);
    }
    
    /**
     *  @private
     *  This is the event handler for the stage's orientationChanging event.  It
     *  cancels the orientation animation and manually swaps the width and height
     *  of the application to allow the application to validate itself before
     *  the orientation change occurs.  The orientaitonChanging is only listened
     *  for on iOS devices.
     */
    private function stage_orientationChangingHandler(event:Event):void
    {
        var sm:ISystemManager = systemManager;
        
        // Manually update orientation width and height if the application's explicit
        // sizes aren't set.  If they are, we assume the application will handle
        // orientation on their own.
        // SDK-30625: check stage for null since the Application may not be on-screen yet
        // if orientation changes during start-up.
        if (!stage || !isNaN(explicitWidth) || !isNaN(explicitHeight))
            return;

        if (!cachedDimensions)
            cachedDimensions = new Dictionary();
        
        // remember the current dimensions
        previousWidth = width;
        previousHeight = height;
        
        var key:String = width + ":" + height;
        
        // On some platforms (e.g. iOS and Playbook) if the soft keyboard is up
        // it deactivates before orientation change and reactivates after it; 
        // the value of isSoftKeyboardActive thus changes during orientation change.
        // We are remembering the initial value of isSoftKeyboardActivate in this
        // situation for use in avoiding excessive resizing during the orientation change.
        keyboardActiveInOrientationChange = isSoftKeyboardActive;

        // if we're rotating 180 degrees don't do any screen resizing
        var beforeOrientation:String = event["beforeOrientation"];
        var afterOrientation:String = event["afterOrientation"];
        
        if ((beforeOrientation == "default" && afterOrientation == "upsideDown") ||
            (beforeOrientation == "upsideDown" && afterOrientation == "default") ||
            (beforeOrientation == "rotatedLeft" && afterOrientation == "rotatedRight") ||
            (beforeOrientation == "rotatedRight" && afterOrientation == "rotatedLeft"))
            return;

        var newWidth:Number;
        var newHeight:Number;

        // if we have a cached value, use it
        if (cachedDimensions[key])
        {
            newWidth = cachedDimensions[key].width;
            newHeight = cachedDimensions[key].height;
        }
        else // no cached value; just swap the numbers for now
        {
            // use stageHeight as the new width if you can get it
            newWidth = stage ? stage.stageHeight / scaleFactor : height;
            newHeight = width;
        }
        
        setActualSize(newWidth, newHeight);
        
        // Indicate that the width and height have changed because of orientation 
        explicitSizingForOrientation = true;
        
        // Force a validation
        validateNow();
    }
    
    /**
     *  @private
     *  Handler for the stage orientation change event.  At this point, we need
     *  to undo the explicit width and height that was set when the application is
     *  reoriented on an iOS device.  See stage_orientationChangingHandler for more
     *  information.
     */
    private function stage_orientationChange(event:Event):void
    {
        // SDK-30625: check stage for null since the Application may not be on-screen yet
        // if orientation changes during start-up.
        if (!stage || !explicitSizingForOrientation)
            return;

        // update cache if keyboard was not previously active
        if (!keyboardActiveInOrientationChange)
        {
            updateScreenSizeCache(stage.stageWidth / scaleFactor, stage.stageHeight / scaleFactor);
        }
        
        explicitSizingForOrientation = false;
    }

    /**
     *  @private 
     *  Update the screen size cache with the new values. The previous values are
     *  pulled from the values currently set in the cache.
     *  Note that if the old and new width/height values are unchanged no values are
     *  cached under the assumption that this was a 180 degree rotation.
     * 
     */
    private function updateScreenSizeCache(newWidth:Number, newHeight:Number):void
    {
        // same dimensions, probably 180 degree rotation; don't cache
        if (previousWidth == newWidth && previousHeight == newHeight)
            return;
        
        var key:String = previousWidth + ":" + previousHeight;
        
        if (cachedDimensions[key])
        {
            // update the cached values if they are different
            cachedDimensions[key].width = newWidth;
            cachedDimensions[key].height = newHeight;
        }
        else
        {
            var orientationChangeKey:String = previousWidth + ":" + previousHeight;
            var reverseOrientationChangeKey:String = newWidth + ":" + newHeight;
            
            // cache values both ways, i.e. old -> new and new -> old
            cachedDimensions[orientationChangeKey] = {
                width:newWidth,
                height:newHeight
            };
            cachedDimensions[reverseOrientationChangeKey] = {
                width:previousWidth,
                height:previousHeight
            };
        }
    }
    
    
    /**
     *  @private
     */
    override protected function createChildren():void
    {
        super.createChildren();
        
        // Only listen for softKeyboard events 
        // if the runtime supports a soft keyboard
        if (softKeyboardBehavior != "")
        {
            addEventListener(SoftKeyboardEvent.SOFT_KEYBOARD_ACTIVATE, 
                softKeyboardActivateHandler, true, 
                EventPriority.DEFAULT, true);
            addEventListener(SoftKeyboardEvent.SOFT_KEYBOARD_DEACTIVATE, 
                softKeyboardDeactivateHandler, true, 
                EventPriority.DEFAULT, true);
            
            // Listen for the deactivate event so we can close the softKeyboard
            var nativeApp:Object = FlexGlobals.topLevelApplication.
                systemManager.getDefinitionByName("flash.desktop.NativeApplication");
            
            if (nativeApp && nativeApp["nativeApplication"])
                EventDispatcher(nativeApp["nativeApplication"]).
                    addEventListener(Event.DEACTIVATE, nativeApplication_deactivateHandler);
        }
    }

    /**
     *  @private
     */
    override protected function commitProperties():void
    {
        super.commitProperties();

        resizeWidth = isNaN(explicitWidth);
        resizeHeight = isNaN(explicitHeight);

        if (resizeWidth || resizeHeight)
        {
            resizeHandler(new Event(Event.RESIZE));

            if (!resizeHandlerAdded)
            {
                // weak reference
                systemManager.addEventListener(Event.RESIZE, resizeHandler, false, 0, true);
                resizeHandlerAdded = true;
            }
        }
        else
        {
            if (resizeHandlerAdded)
            {
                systemManager.removeEventListener(Event.RESIZE, resizeHandler);
                resizeHandlerAdded = false;
            }
        }

        if (percentBoundsChanged)
        {
            updateBounds();
            percentBoundsChanged = false;
        }
    }

    /**
     *  @private
     */
    override protected function resourcesChanged():void
    {
        super.resourcesChanged();

        // "View Source" on the context menu
        if (viewSourceCMI)
        {
            viewSourceCMI.caption = resourceManager.getString("components", "viewSource");
        }
    }

    /**
     *  @private
     */
    override protected function partAdded(partName:String, instance:Object):void
    {
        super.partAdded(partName, instance);

        if (instance == controlBarGroup)
        {
            // copy proxied values from controlBarGroupProperties (if set) to contentGroup
            var newControlBarGroupProperties:uint = 0;

            if (controlBarGroupProperties.controlBarContent !== undefined)
            {
                controlBarGroup.mxmlContent = controlBarGroupProperties.controlBarContent;
                newControlBarGroupProperties = BitFlagUtil.update(newControlBarGroupProperties,
                                                               CONTROLBAR_PROPERTY_FLAG, true);
            }

            if (controlBarGroupProperties.layout !== undefined)
            {
                controlBarGroup.layout = controlBarGroupProperties.layout;
                newControlBarGroupProperties = BitFlagUtil.update(newControlBarGroupProperties,
                                                               LAYOUT_PROPERTY_FLAG, true);
            }

            if (controlBarGroupProperties.visible !== undefined)
            {
                controlBarGroup.visible = controlBarGroupProperties.visible;
                newControlBarGroupProperties = BitFlagUtil.update(newControlBarGroupProperties,
                                                               VISIBLE_PROPERTY_FLAG, true);
            }

            controlBarGroupProperties = newControlBarGroupProperties;
        }
    }

    /**
     *  @private
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    override protected function partRemoved(partName:String, instance:Object):void
    {
        super.partRemoved(partName, instance);

        if (instance == controlBarGroup)
        {
            // copy proxied values from contentGroup (if explicitely set) to contentGroupProperties

            var newControlBarGroupProperties:Object = {};

            if (BitFlagUtil.isSet(controlBarGroupProperties as uint, CONTROLBAR_PROPERTY_FLAG))
                newControlBarGroupProperties.controlBarContent = controlBarGroup.getMXMLContent();

            if (BitFlagUtil.isSet(controlBarGroupProperties as uint, LAYOUT_PROPERTY_FLAG))
                newControlBarGroupProperties.layout = controlBarGroup.layout;

            if (BitFlagUtil.isSet(controlBarGroupProperties as uint, VISIBLE_PROPERTY_FLAG))
                newControlBarGroupProperties.visible = controlBarGroup.visible;

            controlBarGroupProperties = newControlBarGroupProperties;

            controlBarGroup.mxmlContent = null;
            controlBarGroup.layout = null;
        }
    }

    /**
     *  @private
     *
     *  @langversion 3.0
     *  @playerversion Flash 10
     *  @playerversion AIR 1.5
     *  @productversion Flex 4
     */
    override protected function getCurrentSkinState():String
    {
        var state:String = enabled ? "normal" : "disabled";
        if (controlBarGroup)
        {
            if (BitFlagUtil.isSet(controlBarGroupProperties as uint, CONTROLBAR_PROPERTY_FLAG) &&
                BitFlagUtil.isSet(controlBarGroupProperties as uint, VISIBLE_PROPERTY_FLAG))
                state += "WithControlBar";
        }
        else
        {
            if (controlBarGroupProperties.controlBarContent &&
                controlBarGroupProperties.visible)
                state += "WithControlBar";
        }

        return state;
    }
    
    
    /**
     *  @private 
     */
    override public function styleChanged(styleProp:String):void
    {
        super.styleChanged(styleProp);
        
        if (!styleProp || styleProp == "styleName" || styleProp == "interactionMode")
        {
            // Turn off tooltip support for all mobile applications
            ToolTipManager.enabled = getStyle("interactionMode") != InteractionMode.TOUCH;
        }
    }

    //----------------------------------
    //  unscaledHeight
    //----------------------------------

    /**
     *  @private
     */
    override mx_internal function setUnscaledHeight(value:Number):void
    {
        // we invalidate so we can properly add/remove the resize
        // event handler (SDK-12664)
        invalidateProperties();

        super.setUnscaledHeight(value);
    }

    //----------------------------------
    //  unscaledWidth
    //----------------------------------

    /**
     *  @private
     */
    override mx_internal function setUnscaledWidth(value:Number):void
    {
        // we invalidate so we can properly add/remove the resize
        // event handler (SDK-12664)
        invalidateProperties();

        super.setUnscaledWidth(value);
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  This is here so we get the this pointer set to Application.
     */
    private function debugTickler():void
    {
        // We need some bytes of code in order to have a place to break.
        var i:int = 0;
    }

    /**
     *  @private
     */
    private function initManagers(sm:ISystemManager):void
    {
        if (sm.isTopLevel())
        {
            focusManager = new FocusManager(this);
            var awm:IActiveWindowManager =
                IActiveWindowManager(sm.getImplementation("mx.managers::IActiveWindowManager"));
            if (awm)
                awm.activate(this);
            else
                focusManager.activate();
        }
    }

    /**
     *  @private
     *  Disable all the built-in items except "Print...".
     */
    private function initContextMenu():void
    {
        // context menu already set
        // nothing to init
        if (flexContextMenu != null)
        {
            // make sure we set it back on systemManager b/c it may have been overridden by now
            if (systemManager is InteractiveObject)
                InteractiveObject(systemManager).contextMenu = contextMenu as ContextMenu;
            return;
        }

        var defaultMenu:ContextMenu = new ContextMenu();
        defaultMenu.hideBuiltInItems();
        defaultMenu.builtInItems.print = true;

        if (_viewSourceURL)
        {
            // don't worry! this gets updated in resourcesChanged()
            const caption:String = resourceManager.getString("components", "viewSource");

            viewSourceCMI = new ContextMenuItem(caption, true);
            viewSourceCMI.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, menuItemSelectHandler);

            // Append custom option, validating customItems first as in the 
            // mobile context this is null.
            if (defaultMenu.customItems)
                defaultMenu.customItems.push(viewSourceCMI);
        }

        contextMenu = defaultMenu;

        if (systemManager is InteractiveObject)
            InteractiveObject(systemManager).contextMenu = defaultMenu;
    }

    /**
     *  @private
     *  Check to see if we're able to synchronize our size with the stage
     *  immediately rather than deferring (dependent on WATSON 2200950).
     */
    private function initResizeBehavior():void
    {
        var version:Array = Capabilities.version.split(' ')[1].split(',');
        var versionPrefix:String = Capabilities.version.substr(0, 3).toLowerCase();
        var runningOnMobile:Boolean = (versionPrefix != "win" && 
                                       versionPrefix != "mac" && 
                                       versionPrefix != "lnx");
        
        synchronousResize = (parseFloat(version[0]) > 10 ||
                             (parseFloat(version[0]) == 10 && parseFloat(version[1]) >= 1)) && (Capabilities.playerType != "Desktop" || runningOnMobile);
    }
    
    /**
     *  @private
     *  Called if resizeForSoftKeyboard is true and the softKeyboard
     *  has been activated. 
     */    
    private function softKeyboardActivateHandler(event:SoftKeyboardEvent):void
    {        
        if (this === FlexGlobals.topLevelApplication)
        {
            if (softKeyboardTarget && softKeyboardTarget != event.target)
                clearSoftKeyboardTarget();
            
            softKeyboardTarget = event.target;
            
            // If the display object that activates the softkeyboard is removed without
            // losing focus, the runtime may not dispatch a deactivate event.  So the
            // framework adds a REMOVE_FROM_STAGE event listener to the target and manually
            // clears the focus.
            softKeyboardTarget.addEventListener(Event.REMOVED_FROM_STAGE, 
                                                softKeyboardTarget_removeFromStageHandler, 
                                                false, EventPriority.DEFAULT, true);
            
            // On iOS, if the softKeyboard target is removed from the stage as a result
            // of a user input with another focusable component, the application will not
            // receive a SOFT_KEYBOARD_DEACTIVATE event, only the target will.
            if (isIOS)
            {
                softKeyboardTarget.addEventListener(SoftKeyboardEvent.SOFT_KEYBOARD_DEACTIVATE, 
                                                    softKeyboardDeactivateHandler, false, 
                                                    EventPriority.DEFAULT, true);
            }
            
            // Get the keyboard size
            var keyboardRect:Rectangle = stage.softKeyboardRect;
     
            if (keyboardRect.height > 0)
                isSoftKeyboardActive = true;
            
            if (softKeyboardBehavior == "none" && resizeForSoftKeyboard)
            {
                var appHeight:Number = (stage.stageHeight - keyboardRect.height) / scaleFactor;
                var appWidth:Number = stage.stageWidth / scaleFactor;
                
                if (appHeight != height || appWidth != width)
                {
                    setActualSize(appWidth, appHeight);
                    validateNow(); // Validate so that other listeners like Scroller get the updated dimensions
                }
                
                // update the screen size cache
                if (keyboardActiveInOrientationChange)
                {
                    keyboardActiveInOrientationChange = false;
                    updateScreenSizeCache(appWidth, appHeight);
                }
            }
        }
    }
    
    /**
     *  @private
     *  Called if resizeForSoftKeyboard is true and the softKeyboard
     *  has been deactivated. 
     */ 
    private function softKeyboardDeactivateHandler(event:SoftKeyboardEvent):void
    {
        if (this === FlexGlobals.topLevelApplication && isSoftKeyboardActive)
        {            
            if (softKeyboardTarget)
                clearSoftKeyboardTarget();
            
            isSoftKeyboardActive = false;
            
            if (softKeyboardBehavior == "none" && resizeForSoftKeyboard && !keyboardActiveInOrientationChange)
            {
                // Restore the original values
                setActualSize(stage.stageWidth / scaleFactor, stage.stageHeight / scaleFactor);
                
                validateNow(); // Validate so that other listeners like Scroller get the updated dimensions
            }
        }
    }
    
    /**
     *  @private
     */
    private function nativeApplication_deactivateHandler(event:Event):void
    {
        // Close the softKeyboard if we deactivate the application. This works
        // around an iOS bug where the SoftKeyboard.DEACTIVATE event isn't
        // dispatched when the application is deactivated. 
        if (isSoftKeyboardActive)
        {
            stage.focus = null;
            softKeyboardDeactivateHandler(null);
        }
    }
    
    /**
     *  @private
     *  Called when a softKeyboard activation target is removed from the
     *  stage.  If the target has stage focus, then the focus is set to null.
     *  This will cause a SOFT_KEYBOARD_DEACTIVATE event to be dispatched.
     */ 
    private function softKeyboardTarget_removeFromStageHandler(event:Event):void
    {
        if (stage.focus == softKeyboardTarget)
            stage.focus = null;
        
        // clearSoftKeyboardTarget() is called in response to the SOFT_KEYBOARD_DEACTIVATE
        // event and will clear the removeFromStage listener and softKeyboardDeactivate 
        // events from the target.
    }
    
    /**
     *  @private
     *  This method clears the cached softKeyboard target and removes the
     *  removeFromStage handler that is added in the softKeyboardActivateHandler
     *  method.
     */
    private function clearSoftKeyboardTarget():void
    {
        if (softKeyboardTarget)
        {
            if (isIOS)
            {
                softKeyboardTarget.removeEventListener(SoftKeyboardEvent.SOFT_KEYBOARD_DEACTIVATE, 
                                                       softKeyboardDeactivateHandler);
            }
            
            softKeyboardTarget.removeEventListener(Event.REMOVED_FROM_STAGE, softKeyboardTarget_removeFromStageHandler);
            softKeyboardTarget = null;
        }
    }
    
    /**
     *  Helper function to get the AIR application descriptor attribute called "softKeyboardBehavior". 
     */  
    mx_internal static function get softKeyboardBehavior():String
    {
        if (_softKeyboardBehavior != null)
        {
            return _softKeyboardBehavior;
        }
        else
        {
            // Since we might not be running on AIR, need to get the class by name. 
            // Also, make sure to cache the value so we only run this once
            var nativeApp:Object = FlexGlobals.topLevelApplication.systemManager.getDefinitionByName("flash.desktop.NativeApplication");
            
            if (nativeApp)
            {
                try
                {
                    var appXML:XML = XML(nativeApp["nativeApplication"]["applicationDescriptor"]);
                    var ns:Namespace = appXML.namespace();
                    
                    _softKeyboardBehavior = String(appXML..ns::softKeyboardBehavior);
                }
                catch (e:Error)
                {
                    // TODO (aharui): Marshall this someday?
                    _softKeyboardBehavior = "";
                }
            }
            else
            {
                _softKeyboardBehavior = "";
            }
            
            return _softKeyboardBehavior;
        }
    }

    //--------------------------------------------------------------------------
    //
    //  Event handlers
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Triggered by a resize event of the stage.
     *  Sets the new width and height.
     *  After the SystemManager performs its function,
     *  it is only necessary to notify the children of the change.
     */
    private function resizeHandler(event:Event):void
    {
        // don't run while keyboard is up and orientation is changing
        if (keyboardActiveInOrientationChange)
            return;
        
        // If we're already due to update our bounds on the next
        // commitProperties pass, avoid the redundancy.
        if (!percentBoundsChanged)
        {
            updateBounds();

            // Update immediately when stage resizes so that we may appear
            // in synch with the stage rather than visually "catching up".
            if (synchronousResize)
                UIComponentGlobals.layoutManager.validateNow();
        }
    }

    /**
     *  @private
     *  Called when the "View Source" item in the application's context menu is
     *  selected.
     */
    protected function menuItemSelectHandler(event:Event):void
    {
        navigateToURL(new URLRequest(_viewSourceURL), "_blank");
    }

    /**
     *  @private
     *  Sets the new width and height after the Stage has resized
     *  or when percentHeight or percentWidth has changed.
     */
    private function updateBounds():void
    {
        // When user has not specified any width/height,
        // application assumes the size of the stage.
        // If developer has specified width/height,
        // the application will not resize.
        // If developer has specified percent width/height,
        // application will resize to the required value
        // based on the current SystemManager's width/height.
        // If developer has specified min/max values,
        // then application will not resize beyond those values.
        
        // ignore updateBounds while orientation is changing
        if (keyboardActiveInOrientationChange)
            return;
        
        var w:Number;
        var h:Number

        if (resizeWidth)
        {
            if (isNaN(percentWidth))
            {
                w = DisplayObject(systemManager).width;
            }
            else
            {
                super.percentWidth = Math.max(percentWidth, 0);
                super.percentWidth = Math.min(percentWidth, 100);
                w = percentWidth*DisplayObject(systemManager).width/100;
            }

            if (!isNaN(explicitMaxWidth))
                w = Math.min(w, explicitMaxWidth);

            if (!isNaN(explicitMinWidth))
                w = Math.max(w, explicitMinWidth);
        }
        else
        {
            w = width;
        }

        if (resizeHeight)
        {
            if (isNaN(percentHeight))
            {
                h = DisplayObject(systemManager).height;
            }
            else
            {
                super.percentHeight = Math.max(percentHeight, 0);
                super.percentHeight = Math.min(percentHeight, 100);
                h = percentHeight*DisplayObject(systemManager).height/100;
            }

            if (!isNaN(explicitMaxHeight))
                h = Math.min(h, explicitMaxHeight);

            if (!isNaN(explicitMinHeight))
                h = Math.max(h, explicitMinHeight);
        }
        else
        {
            h = height;
        }

        if (w != width || h != height)
        {
            invalidateProperties();
            invalidateSize();
        }

        setActualSize(w, h);

        invalidateDisplayList();
    }

    /**
     * @private
     */
    override public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void
    {
        // this can get called before we know our systemManager.  Hook it up later in initialize()
        if (type == UncaughtErrorEvent.UNCAUGHT_ERROR && systemManager)
            systemManager.loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorRedispatcher);
        super.addEventListener(type, listener, useCapture, priority, useWeakReference)
    }

    /**
     * @private
     */
    override public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void
    {
        super.removeEventListener(type, listener, useCapture);
        if (type == UncaughtErrorEvent.UNCAUGHT_ERROR && systemManager)
            systemManager.loaderInfo.uncaughtErrorEvents.removeEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorRedispatcher);
    }

    private function uncaughtErrorRedispatcher(event:Event):void
    {
        if (!dispatchEvent(event))
            event.preventDefault();
    }
}

}
