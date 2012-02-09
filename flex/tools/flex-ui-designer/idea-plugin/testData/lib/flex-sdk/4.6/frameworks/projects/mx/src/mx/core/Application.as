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

package mx.core
{

import flash.display.DisplayObject;
import flash.display.InteractiveObject;
import flash.events.ContextMenuEvent;
import flash.events.ErrorEvent;
import flash.events.Event;
import flash.external.ExternalInterface;
import flash.net.URLRequest;
import flash.net.navigateToURL;
import flash.system.Capabilities;
import flash.ui.ContextMenu;
import flash.ui.ContextMenuItem;
import flash.utils.setInterval;

import mx.containers.utilityClasses.ApplicationLayout;
import mx.effects.EffectManager;
import mx.events.FlexEvent;
import mx.managers.FocusManager;
import mx.managers.IActiveWindowManager;
import mx.managers.ILayoutManager;
import mx.managers.ISystemManager;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleClient;
import mx.utils.LoaderUtil;

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
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
[Event(name="applicationComplete", type="mx.events.FlexEvent")]

/**
 *  Dispatched when an HTTPService call fails.
 * 
 *  @eventType flash.events.ErrorEvent.ERROR
 *  
 *  @langversion 3.0
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
[Event(name="error", type="flash.events.ErrorEvent")]

//--------------------------------------
//  Styles
//--------------------------------------

include "../styles/metadata/ModalTransparencyStyles.as";

/**
 *  Specifies the alpha transparency values used for the background gradient fill of the application.
 *  You should set this to an Array of two numbers.
 *  Elements 0 and 1 specify the start and end values for an alpha gradient.
 *
 *  @default [ 1.0, 1.0 ]
 *  
 *  @langversion 3.0
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
[Style(name="backgroundGradientAlphas", type="Array", arrayType="Number", inherit="no", theme="halo")]

/**
 *  Specifies the colors used to tint the background gradient fill of the application.
 *  You should set this to an Array of two uint values that specify RGB colors.
 *  Elements 0 and 1 specify the start and end values for a color gradient.
 *  For a solid-color background, set the same color value for elements 0 and 1.
 *  A value of <code>undefined</code> means background gradient is generated
 *  based on the <code>backgroundColor</code> property.
 *
 *  @default undefined 
 *  
 *  @langversion 3.0
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
[Style(name="backgroundGradientColors", type="Array", arrayType="uint", format="Color", inherit="no", theme="halo")]

/**
 *  Number of pixels between the application's bottom border
 *  and its content area.  
 *
 *  @default 24
 *  
 *  @langversion 3.0
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
[Style(name="paddingBottom", type="Number", format="Length", inherit="no")]

/**
 *  Number of pixels between the application's top border
 *  and its content area. 
 *
 *  @default 24
 *  
 *  @langversion 3.0
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
[Style(name="paddingTop", type="Number", format="Length", inherit="no")]

//--------------------------------------
//  Excluded APIs
//--------------------------------------

[Exclude(name="direction", kind="property")]
[Exclude(name="icon", kind="property")]
[Exclude(name="label", kind="property")]
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
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
[Frame(factoryClass="mx.managers.SystemManager")]

[ResourceBundle("core")]

[Alternative(replacement="spark.components.Application", since="4.0")]

/**
 *  Flex defines a default, or Application, container that lets you start
 *  adding content to your application without explicitly defining
 *  another container.
 *  Flex creates this container from the <code>&lt;mx:Application&gt;</code>
 *  tag, the first tag in an MXML application file.
 *  While you might find it convenient to use the Application container
 *  as the only  container in your application, in most cases you explicitly
 *  define at least one more container before you add any controls
 *  to your application.
 *
 *  <p>Applications support a predefined plain style that sets
 *  a white background, left alignment, and removes all margins.
 *  To use this style, do the following:</p>
 *
 *  <pre>
 *    &lt;mx:Application styleName="plain" /&gt;
 *  </pre>
 *
 *  <p>This is equivalent to setting the following style attributes:</p>
 *
 *  <pre>
 *    backgroundColor="0xFFFFFF"
 *    horizontalAlign="left"
 *    paddingLeft="0"
 *    paddingTop="0"
 *    paddingBottom="0"
 *    paddingRight="0"
 *  </pre>
 * 
 *  @mxml
 *
 *  <p>The <code>&lt;mx:Application&gt;</code> tag inherits all of the tag 
 *  attributes of its superclass and adds the following tag attributes:</p>
 *
 *  <pre>
 *  &lt;mx:Application
 *    <strong>Properties</strong>
 *    application="<i>No default</i>"
 *    controlBar="null"
 *    frameRate="24"
 *    historyManagementEnabled="true|false"
 *    layout="vertical|horizontal|absolute"
 *    pageTitle"<i>No default</i>"
 *    preloader="<i>No default</i>"
 *    resetHistory="false|true"
 *    scriptRecursionLimit="1000"
 *    scriptTimeLimit="60"
 *    usePreloader="true|false"
 *    viewSourceURL=""
 *    xmlns:<i>No default</i>="<i>No default</i>"
 * 
 *    <strong>Styles</strong> 
 *    backgroundGradientAlphas="[ 1.0, 1.0 ]"
 *    backgroundGradientColors="undefined"
 *    horizontalAlign="center|left|right"
 *    horizontalGap="8"
 *    modalTransparency="0.5"
 *    modalTransparencyBlur="3"
 *    modalTransparencyColor="#DDDDDD"
 *    modalTransparencyDuration="100"
 *    paddingBottom="24"
 *    paddingTop="24"
 *    verticalAlign="top|bottom|middle"
 *    verticalGap="6"
 *  
 *    <strong>Events</strong>
 *    applicationComplete="<i>No default</i>"
 *    error="<i>No default</i>"
 *  /&gt;
 *  </pre>
 *
 *  @includeExample examples/SimpleApplicationExample.mxml
 *  
 *  @see mx.managers.CursorManager
 *  @see mx.managers.LayoutManager
 *  @see mx.managers.SystemManager
 *  @see flash.events.EventDispatcher
 *  
 *  @langversion 3.0
 *  @playerversion Flash 9
 *  @playerversion AIR 1.1
 *  @productversion Flex 3
 */
public class Application extends LayoutContainer
{
    include "../core/Version.as";

    //--------------------------------------------------------------------------
    //
    //  Class variables
    //
    //--------------------------------------------------------------------------
    
    /**
     *  @private    
     */
    mx_internal static var useProgressiveLayout:Boolean = false;

    //--------------------------------------------------------------------------
    //
    //  Class properties
    //
    //--------------------------------------------------------------------------

    /**
     *  A reference to the top-level application.
     *
     *  <p>In general, there can be a hierarchy of Application objects,
     *  because an Application can use a SWFLoader control to dynamically
     *  load another Application.
     *  The <code>parentApplication</code> property of a UIComponent can be
     *  used to access the sub-Application in which that UIComponent lives,
     *  and to walk up the hierarchy to the top-level Application.</p>
     *  
     *  <p>This property has been deprecated starting in Flex4. Note that this
     *  property will still return applications of type mx.core.Application and 
     *  mx.core.WindowedApplication as in previous versions. Starting in Flex 4
     *  it will also return applications of type spark.components.Application or 
     *  spark.components.WindowedApplication.</p>
     * 
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    /*
     *  Note: here are two reasons that 'application' is typed as Object
     *  rather than as Application. The first is for consistency with
     *  the 'parentApplication' property of UIComponent. That property is not
     *  typed as Application because it would make UIComponent dependent
     *  on Application, slowing down compile times not only for SWCs
     *  for also for MXML and AS components. Second, if it were typed
     *  as Application, authors would not be able to access properties
     *  and methods in the <Script> of their <Application> without
     *  casting it to their application's subclass, as in
     *  MyApplication(Application.application).myAppMethod().
     *  Therefore we decided to dispense with strict typing for
     *  'application'.
     */
    [Deprecated(replacement="FlexGlobals.topLevelApplication", since="4.0")]
    
    public static function get application():Object
    {
        return FlexGlobals.topLevelApplication;
    }

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     *  Constructor.
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public function Application()
    {
        UIComponentGlobals.layoutManager = ILayoutManager(
            Singleton.getInstance("mx.managers::ILayoutManager"));
        UIComponentGlobals.layoutManager.usePhasedInstantiation = true;

        if (!FlexGlobals.topLevelApplication)
            FlexGlobals.topLevelApplication = this;

        super();

        layoutObject = new ApplicationLayout();
        layoutObject.target = this;
        boxLayoutClass = ApplicationLayout;
            
        showInAutomationHierarchy = true;
        // Flex's auto-generated init() override has set the
        // documentDescriptor property for the application object.
        // We get the id and the creationPolicy, which we want to
        // set very early, from that descriptor.
        
        initResizeBehavior();
    }

    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------

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
     *  Used in progressive layout.
     */
    private var creationQueue:Array = [];

    /**
     *  @private
     *  Used in progressive layout.
     */
    private var processingCreationQueue:Boolean = false;

    /**
     *  @private
     *  The application's view metrics.
     */
    private var _applicationViewMetrics:EdgeMetrics;
    
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
    private var synchronousResize:Boolean = false;
    
    /**
     * @private
     * (Possibly null) reference to the View Source context menu item,
     * so that we can update it for runtime localization.
     */
    private var viewSourceCMI:ContextMenuItem;
        
    //--------------------------------------------------------------------------
    //
    //  Compile-time pseudo-properties
    //
    //--------------------------------------------------------------------------

    // These declarations correspond to the MXML-compile-time attributes
    // allowed on the <mx:Application> tag. These attributes affect the MXML
    // compiler, but they aren't actually used in the runtime framework.
    // The declarations appear here in order to provide metadata about these
    // attributes for Flash Builder.

    //----------------------------------
    //  frameRate
    //----------------------------------

    [Inspectable(defaultValue="24")]

    /**
     *    Specifies the frame rate of the application.
     *    <p>Note: This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *    @default 24
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
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
     *    <p>Note: This property cannot be set by ActionScript code; it must be set in MXML code. 
     *    The value set in MXML code is designed to be used by a tool to update the HTML templates 
     *    provided with the SDK.</p>
     *
     *    @default ""
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
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
     *  <p>Use this property to specify the path of a component that 
     *  defines a custom progress indicator.
     *  To create a custom progress indicator, you typically create a subclass of the 
     *  DownloadProgressBar class, or create a subclass of 
     *  the flash.display.Sprite class that implements the 
     *  mx.preloaders.IPreloaderDisplay interface. </p>
     *
     *  <p><b>Note:</b> This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *  @see mx.preloaders.DownloadProgressBar 
     *  @see flash.display.Sprite
     *  @see mx.preloaders.IPreloaderDisplay
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public var preloader:Object;
    
    //----------------------------------
    //  preloaderChromeColor
    //----------------------------------
    
    [Inspectable(defaultValue="0xCCCCCC", format="Color")]
    
    /**
     *    Specifies the base color used by the default preloader component. This property
     *    has the same effect as the <code>chromeColor</code> style used by the Spark skins.
     *    Typically this property should be set to the same value as the <code>chromeColor</code>
     *    style used by the application.
     *    
     *    <p>Note: This property cannot be set by ActionScript code; it must be set in MXML code.</p>
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
     *    <p>Note: This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *    @default 1000
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
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
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public var scriptTimeLimit:Number;

    //----------------------------------
    //  usePreloader
    //----------------------------------

    [Inspectable(defaultValue="true")]

    /**
     *    If <code>true</code>, specifies to display the application preloader.
     *    <p>Note: This property cannot be set by ActionScript code; it must be set in MXML code.</p>
     *
     *    @default true
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public var usePreloader:Boolean;

    //--------------------------------------------------------------------------
    //
    //  Overridden properties (to block metadata from superclasses)
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  enabled
    //----------------------------------

    [Inspectable(category="General", enumeration="true,false", defaultValue="true")]

    /**
     *  @private
     */
    override public function set enabled(value:Boolean):void
    {
        super.enabled = value;

        // controlBar must be enabled/disabled when this container is.
        if (controlBar)
            controlBar.enabled = value;
    }

    //----------------------------------
    //  icon
    //----------------------------------

    [Inspectable(environment="none")]

    /**
     *  @private
     */
    override public function set icon(value:Class):void
    {
    }

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
    //  label
    //----------------------------------

    [Inspectable(environment="none")]

    /**
     *  @private
     */
    override public function set label(value:String):void
    {
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

    //----------------------------------
    //  viewMetrics
    //----------------------------------

    /**
     *  @private
     *  Returns the thickness of the edges of the object, including
     *  the border, title bar and scroll bars, if visible.
     *
     *  @return EdgeMetrics object with left, right, top, and bottom
     *  properties containing the edge thickness, in pixels.
     */
    override public function get viewMetrics():EdgeMetrics
    {
        // This function needs to return an object.
        // Rather than allocating a new one each time,
        // we'll allocate one once and then hold a reference to it.
        if (!_applicationViewMetrics)
            _applicationViewMetrics = new EdgeMetrics();
        var vm:EdgeMetrics = _applicationViewMetrics;

        var o:EdgeMetrics = super.viewMetrics;
        
        var thickness:Number = getStyle("borderThickness");

        vm.left = o.left;
        vm.top = o.top;
        vm.right = o.right;
        vm.bottom = o.bottom;

        if (controlBar && controlBar.includeInLayout)
        {
            vm.top -= thickness;
            vm.top += Math.max(controlBar.getExplicitOrMeasuredHeight(),
                               thickness);
        }

        return vm;
    }

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    /**
     *  The ApplicationControlBar for this Application. 
     *
     *  @see mx.containers.ApplicationControlBar
     *  @default null
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public var controlBar:IUIComponent;

    //----------------------------------
    //  historyManagementEnabled
    //----------------------------------

    [Inspectable(defaultValue="true")]

    /**
     *  If <code>false</code>, the history manager will be disabled.
     *  Setting to false is recommended when using the BrowserManager.
     *
     *  @default true
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public var historyManagementEnabled:Boolean = true;

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
     *  The parameters property returns an Object containing name-value
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
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public function get parameters():Object
    {
        return _parameters;
    }

    //----------------------------------
    //  resetHistory
    //----------------------------------

    [Inspectable(defaultValue="true")]

    /**
     *  If <code>true</code>, the application's history state is reset
     *  to its initial state whenever the application is reloaded.
     *  Applications are reloaded when any of the following occurs:
     *  <ul>
     *    <li>The user clicks the browser's Refresh button.</li>
     *    <li>The user navigates to another web page, and then clicks
     *    the browser's Back button to return to the Flex application.</li>
     *    <li>The user loads a Flex application from the browser's
     *    Favorites or Bookmarks menu.</li>
     *  </ul>
     *
     *  @default true
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public var resetHistory:Boolean = true;
    
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

    //----------------------------------
    //  url
    //----------------------------------

    /**
     *  @private
     *  Storage for the url property.
     *  This variable is set in the initialize() method.
     */
    mx_internal var _url:String;

    /**
     *  The URL from which this Application's SWF file was loaded.
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public function get url():String
    {
        return _url;
    }
    
    //----------------------------------
    //  usePadding
    //----------------------------------
    
    /**
     *  @private
     */
    override mx_internal function get usePadding():Boolean
    {
        // We use padding for all layouts except absolute.
        return layout != ContainerLayout.ABSOLUTE;
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
     *    &lt;mx:Application viewSourceURL="http://path/to/source"&gt;
     *      ...
     *    &lt;/mx:Application&gt;</pre>
     *
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
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
    //  Overridden methods: DisplayObjectContainer
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    override public function getChildIndex(child:DisplayObject):int
    {
        // For control bar focus management, return -1 to indicate
        // "before the first child".
        if (controlBar && child == controlBar)
            return -1;

        return super.getChildIndex(child);
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
        // trace("app initialize app");

        var sm:ISystemManager = systemManager;
        
        _url = LoaderUtil.normalizeURL(sm.loaderInfo);
        _parameters = sm.loaderInfo.parameters;

        initManagers(sm);
        _descriptor = null;

        if (documentDescriptor)
        {
            creationPolicy = documentDescriptor.properties.creationPolicy;
            if (creationPolicy == null || creationPolicy.length == 0)
                creationPolicy = ContainerCreationPolicy.AUTO;

            var properties:Object = documentDescriptor.properties;

            if (properties.width != null)
            {
                width = properties.width;
                delete properties.width;
            }
            if (properties.height != null)
            {
                height = properties.height;
                delete properties.height;
            }

            // Flex auto-generated code has already set up events.
            documentDescriptor.events = null;
        }

        // Setup the default context menu here. This allows the application
        // developer to override it in the initialize event, if desired.
        initContextMenu();

        super.initialize();

        addEventListener(Event.ADDED, addedHandler);
        
        // Stick a timer here so that we will execute script every 1.5s
        // no matter what.
        // This is strictly for the debugger to be able to halt.
        // Note: isDebugger is true only with a Debugger Player.
        if (sm.isTopLevelRoot() && Capabilities.isDebugger == true)
            setInterval(debugTickler, 1500);
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
     *  Calculates the preferred, mininum and maximum sizes of the
     *  Application. See the <code>UIComponent.measure()</code> method for more
     *  information.
     *  <p>
     *  The <code>measure()</code> method first calls
     *  <code>Box.measure()</code> method, then makes sure the
     *  <code>measuredWidth</code> and <code>measuredMinWidth</code>
     *  are wide enough to display the application's control bar.
     */
    override protected function measure():void
    {
        super.measure();

        var bm:EdgeMetrics = borderMetrics;

        if (controlBar && controlBar.includeInLayout)
        {
            var controlWidth:Number = controlBar.getExplicitOrMeasuredWidth() +
                                      bm.left + bm.right;

            measuredWidth = Math.max(measuredWidth, controlWidth);
            measuredMinWidth = Math.max(measuredMinWidth, controlWidth);
        }
    }

    /**
     *  @private
     */
    override protected function updateDisplayList(unscaledWidth:Number,
                                                  unscaledHeight:Number):void
    {
        super.updateDisplayList(unscaledWidth, unscaledHeight);

        // Wait to layout the border after all the children
        // have been positioned.
        createBorder();
    }

    /**
     *  @private
     */
    override public function styleChanged(styleProp:String):void
    {
        super.styleChanged(styleProp);
        
        if (styleProp == "backgroundColor" &&
            getStyle("backgroundImage") == getStyle("defaultBackgroundImage"))
        {
            clearStyle("backgroundImage");
        }
    }   

    /**
     *  @private
     *  Prepare the Object for printing.
     *
     *  @see mx.printing.FlexPrintJob
     */
    override public function prepareToPrint(target:IFlexDisplayObject):Object
    {
        var objData:Object = {};

        if (target == this)
        {
            objData.width = width;
            objData.height = height;

            objData.verticalScrollPosition = verticalScrollPosition;
            objData.horizontalScrollPosition = horizontalScrollPosition;

            objData.horizontalScrollBarVisible = (horizontalScrollBar != null);
            objData.verticalScrollBarVisible = (verticalScrollBar != null);
            
            objData.whiteBoxVisible = (whiteBox != null);
            
            setActualSize(measuredWidth, measuredHeight);

            horizontalScrollPosition = 0;
            verticalScrollPosition = 0;
            
            if (horizontalScrollBar)
                horizontalScrollBar.visible = false;

            if (verticalScrollBar)
                verticalScrollBar.visible = false;

            if (whiteBox)
                whiteBox.visible = false;

            updateDisplayList(unscaledWidth, unscaledHeight);
        }

        objData.scrollRect = super.prepareToPrint(target);

        return objData;
    }

    /**
     *  @private
     *  Should be called after printing is done for post-processing and clean up.
     *
     *  @see mx.printing.FlexPrintJob
     */
    override public function finishPrint(obj:Object, target:IFlexDisplayObject):void
    {
        if (target == this)
        {
            setActualSize(obj.width, obj.height);

            if (horizontalScrollBar)
                horizontalScrollBar.visible = obj.horizontalScrollBarVisible;
            if (verticalScrollBar)
                verticalScrollBar.visible = obj.verticalScrollBarVisible;

            if (whiteBox)
                whiteBox.visible = obj.whiteBoxVisible;

            horizontalScrollPosition = obj.horizontalScrollPosition;
            verticalScrollPosition = obj.verticalScrollPosition;

            updateDisplayList(unscaledWidth, unscaledHeight);
        }

        // obj is the Object created in prepare to print above, 
        // it just stores the scrollRect Rectangle object sent from Container
        // obj doesnt have to be DisplayObject
        super.finishPrint(obj.scrollRect, target);
    }

    /**
     *  @private
     *  Application also handles themeColor defined
     *  on the global selector.
     */
    override mx_internal function initThemeColor():Boolean
    {
        if (FlexVersion.compatibilityVersion >= FlexVersion.VERSION_4_0)
            return true;
        
        var result:Boolean = super.initThemeColor();
        
        if (!result)
        {
            var tc:Object;  // Can be number or string
            var rc:Number;
            var sc:Number;
            var globalSelector:CSSStyleDeclaration = 
                styleManager.getMergedStyleDeclaration("global");
            
            if (globalSelector)
            {
                tc = globalSelector.getStyle("themeColor");
                rc = globalSelector.getStyle("rollOverColor");
                sc = globalSelector.getStyle("selectionColor");
            }
            
            if (tc && isNaN(rc) && isNaN(sc))
            {
                setThemeColor(tc);
            }
            result = true;
        }
        
        return result;
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
            viewSourceCMI.caption = resourceManager.getString("core", "viewSource");
        }
    }
    
    //--------------------------------------------------------------------------
    //
    //  Overridden methods: Container
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    override protected function layoutChrome(unscaledWidth:Number,
                                             unscaledHeight:Number):void
    {
        super.layoutChrome(unscaledWidth, unscaledHeight);

        // When Container.autoLayout is false, updateDisplayList()
        // is not called, but layoutChrome() is still called.
        // In that case, we still need to position the border.
        if (!doingLayout)
            createBorder();

        // Remove the borderThickness from the border metrics,
        // since the header and control bar overlap any solid border.
        var bm:EdgeMetrics = borderMetrics;
        var thickness:Number = getStyle("borderThickness");

        var em:EdgeMetrics = new EdgeMetrics();

        em.left = bm.left - thickness;
        em.top = bm.top - thickness;
        em.right = bm.right - thickness;
        em.bottom = bm.bottom - thickness;

        if (controlBar && controlBar.includeInLayout)
        {
            if (controlBar is IInvalidating)
                IInvalidating(controlBar).invalidateDisplayList();
            controlBar.setActualSize(width - (em.left + em.right),
                    controlBar.getExplicitOrMeasuredHeight());
            controlBar.move(em.left, em.top);
        }
    }

    /**
     *  @private
     *  
     *  Container implements addChild in terms of addChildAt. 
     */
    override public function addChildAt(child:DisplayObject,
                                        index:int):DisplayObject
    {
        super.addChildAt(child, index);
        if (child == controlBar && 
            "dock" in child && child["dock"] &&
            "resetDock" in controlBar)
        {
            controlBar["resetDock"](true);
        }
        
        return child;
    }
    
    /**
     *  @private
     * 
     *  Container implements removeChildAt in terms of removeChild.
     */
    override public function removeChild(child:DisplayObject):DisplayObject
    {
        if (child == controlBar && 
            "dock" in child && child["dock"])
        {
            dockControlBar(IUIComponent(child), false);
        }
        
        return super.removeChild(child);
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
                InteractiveObject(systemManager).contextMenu = contextMenu;
            return;
        }
        
        var defaultMenu:ContextMenu = new ContextMenu();
        defaultMenu.hideBuiltInItems();
        defaultMenu.builtInItems.print = true;

        if (_viewSourceURL)
        {
            // don't worry! this gets updated in resourcesChanged()
            const caption:String = resourceManager.getString("core", "viewSource");
            
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
     *  Add a container to the Application's creation queue.
     *
     *  <p>Use this mechanism to instantiate and draw the contents
     *  of a container in an ordered manner.
     *  The container should have the <code>creationPolicy</code> property
     *  set to <code>"none"</code> prior to calling this function.</p>
     *
     *  @param id The id of the container to add to the queue or a 
     *  pointer to the container itself
     *
     *  @param preferredIndex (optional) A positive integer that determines
     *  the container's position in the queue relative to the other
     *  containers presently in the queue.
     *
     *  @param callbackFunc This parameter is ignored.
     *
     *  @param parent This parameter is ignored.
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    public function addToCreationQueue(id:Object, preferredIndex:int = -1,
                                       callbackFunc:Function = null,
                                       parent:IFlexDisplayObject = null):void
    {
        //trace("App.addToCreationQueue id",id,"index",preferredIndex,"parent",parent);

        var queueLength:int = creationQueue.length;
        var queueObj:Object = {};
        var insertedItem:Boolean = false;

        queueObj.id = id;
        queueObj.parent = parent;
        queueObj.callbackFunc = callbackFunc;
        queueObj.index = preferredIndex;

        //trace("addToCreationQueue id",id,"index",queueObj.index,"parent",queueObj.parent,"nestLevel",queueObj.parent.nestLevel,"initialized",initialized);

        var insertIndex:int;
        var pointerIndex:int;
        var pointerLevel:int;

        // Find out where to place this item
        for (var i:int = 0; i < queueLength; i++)
        {
            pointerIndex = creationQueue[i].index;
            pointerLevel = creationQueue[i].parent ? creationQueue[i].parent.nestLevel : 0;

            /*
            trace("addToCreationQueue queueItem",queueItemPointer.id,"queueItem.level",pointerLevel,"obj.level",queueObj.parent.nestLevel,
                    "queueItem.index",pointerIndex,"obj.index",queueObj.index);
            */
            // If our new item has a preferredIndex
            if (queueObj.index != -1)
            {
                // Place at index i if the queued index is -1 or if the new item's index is before the queued index
                if (pointerIndex == -1 || queueObj.index < pointerIndex)
                {
                    insertIndex = i;
                    insertedItem = true;
                    break;
                }
            }
            else
            {
                // Place at index i if queued index is -1 and the new item is deeper in the component tree
                // than the queued item. (Inner-most components should reveal first)
                var parentLevel:int = queueObj.parent ? queueObj.parent.nestLevel : 0;
                if (pointerIndex == -1 && pointerLevel < parentLevel)
                {
                    insertIndex = i;
                    insertedItem = true;
                    break;
                }
            }
        }

        if (!insertedItem)
        {
            creationQueue.push(queueObj); // Just add it to the end of the queue
            insertedItem = true;
        }
        else
        {
            creationQueue.splice(insertIndex, 0, queueObj); // Insert into a specific place in the queue
        }

        //printCreationQueue();

        // Check if we need to trigger queue processing
        if (initialized && !processingCreationQueue)
        {
            //trace("App.addToCreationQueue KICKING the queue processer");
            doNextQueueItem();
        }
    }

    /**
     *  @private
     */
    private function doNextQueueItem(event:FlexEvent = null):void
    {
        processingCreationQueue = true;

        Application.useProgressiveLayout = true;

        // The doNextQueueItem function is usually called
        // when a creationComplete event is dispatched.
        // Wait for other creationComplete listeners to fire
        // before we start processing the next item,
        // in case one of those listeners plays an effect.
        callLater(processNextQueueItem);
    }

    /**
     *  @private
     */
    private function processNextQueueItem():void
    {
        if (EffectManager.effectsPlaying.length > 0)
        {
            // Wait for effects to finish playing before processing the
            // next item in the queue.
            callLater(processNextQueueItem);
        }
        else if (creationQueue.length > 0)
        {
            //trace("processNextQueueItem START length", creationQueue.length);
            //printCreationQueue();

            var queueItem:Object = creationQueue.shift();
            try
            {
                var nextChild:IUIComponent = (queueItem.id is String
                                              ? document[queueItem.id]
                                              : queueItem.id);
                if (nextChild is Container)
                    Container(nextChild).createComponentsFromDescriptors(true);
                if (nextChild is Container && 
                        Container(nextChild).creationPolicy == ContainerCreationPolicy.QUEUED)
                    doNextQueueItem();
                else
                    nextChild.addEventListener("childrenCreationComplete", doNextQueueItem);
            }
            catch(e:Error)
            {
                //trace("Exception in processNextQueue",e);
                // Can't find the id in the document. Just move on to the next item.
                processNextQueueItem();
            }

            //trace("processNextQueueItem id", queueItem. id, "index", queueItem. index);
        }
        else
        {
            processingCreationQueue = false;
            Application.useProgressiveLayout = false;
        }
    }

    /**
     *  @private
     */
    private function printCreationQueue():void
    {
        var msg:String = "";

        var n:Number = creationQueue.length;
        for (var i:int = 0; i < n; i++)
        {
            var child:Object = creationQueue[i];
            msg += " [" + i + "] " + child.id + " " + child.index;
        }

        //trace("creationQueue =>" + msg);
    }

    /**
     *  @private
     */
    private function setControlBar(newControlBar:IUIComponent):void
    {
        if (newControlBar == controlBar)
            return;

        if (controlBar && controlBar is IStyleClient)
        {
            IStyleClient(controlBar).clearStyle("cornerRadius");
            IStyleClient(controlBar).clearStyle("docked");
        }

        controlBar = newControlBar;
        if (controlBar && controlBar is IStyleClient)
        {
            IStyleClient(controlBar).setStyle("cornerRadius", 0);
            IStyleClient(controlBar).setStyle("docked", true);
        }

        invalidateSize();
        invalidateDisplayList();
        invalidateViewMetricsAndPadding();
    }

    /**
     *  @private
     */
    mx_internal function dockControlBar(controlBar:IUIComponent,
            dock:Boolean):void
    {
        if (dock)
        {
            try
            {
                removeChild(DisplayObject(controlBar));
            }
            catch(e:Error)
            {
                return;
            }

            rawChildren.addChildAt(DisplayObject(controlBar), firstChildIndex);
            setControlBar(controlBar);
        }
        else // undock
        {
            try
            {
                rawChildren.removeChild(DisplayObject(controlBar));
            }
            catch(e:Error)
            {
                return;
            }

            setControlBar(null);
            addChildAt(DisplayObject(controlBar), 0);
        }
    }

    /**
     *  @private
     *  Check to see if we're able to synchronize our size with the stage
     *  immediately rather than deferring (dependent on WATSON 2200950).
     */
    private function initResizeBehavior():void
    {
        var version:Array = Capabilities.version.split(' ')[1].split(',');
        
        synchronousResize = (parseFloat(version[0]) > 10 || 
            (parseFloat(version[0]) == 10 && parseFloat(version[1]) >= 1))
            && (Capabilities.playerType != "Desktop");
    }
    
    //--------------------------------------------------------------------------
    //
    //  Event handlers
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *   Called after all children are drawn.
     */
    private function addedHandler(event:Event):void
    {
        //trace("creationComplete +++++++++ check if queue has any items", creationQueue.length, "initialized", initialized);
        if (event.target == this && creationQueue.length > 0)
            doNextQueueItem();
    }

    /**
     *  @private 
     *  Triggered by a resize event of the stage.
     *  Sets the new width and height.
     *  After the SystemManager performs its function,
     *  it is only necessary to notify the children of the change.
     */
    private function resizeHandler(event:Event):void
    {
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
                
                if (FlexVersion.compatibilityVersion >= FlexVersion.VERSION_4_0)
                    w = percentWidth * DisplayObject(systemManager).width/100;
                else
                    w = percentWidth * screen.width/100;
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
                
                if (FlexVersion.compatibilityVersion >= FlexVersion.VERSION_4_0)
                    h = percentHeight * DisplayObject(systemManager).height/100;
                else
                    h = percentHeight * screen.height/100;
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
}

}
