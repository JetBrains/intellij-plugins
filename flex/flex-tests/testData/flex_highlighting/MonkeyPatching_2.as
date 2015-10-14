// ActionScript file
/*
   Copyright (c) 2007 - 2008 FlexLib Contributors.  See:
   http://code.google.com/p/flexlib/wiki/ProjectContributors

   Permission is hereby granted, free of charge, to any person obtaining a copy of
   this software and associated documentation files (the "Software"), to deal in
   the Software without restriction, including without limitation the rights to
   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
   of the Software, and to permit persons to whom the Software is furnished to do
   so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
 */
package flexlib.containers
{
import flash.events.MouseEvent;

import flexlib.events.WindowShadeEvent;

import mx.controls.Button;
import mx.core.EdgeMetrics;
import mx.core.IFactory;
import mx.core.LayoutContainer;
import mx.core.ScrollPolicy;
import mx.effects.Resize;
import mx.effects.easing.Cubic;
import mx.effects.effectClasses.ResizeInstance;
import mx.events.EffectEvent;
import mx.events.PropertyChangeEvent;
import mx.events.ResizeEvent;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.utils.StringUtil;

/**
 * This is the icon displayed on the headerButton when the WindowShade is in the open state.
 */
[Style(name="openIcon", type="Class", inherit="no")]

/**
 * This is the icon displayed on the headerButton when the WindowShade is in the closed state.
 */
[Style(name="closeIcon", type="Class", inherit="no")]

/**
 * The duration of the WindowShade opening transition, in milliseconds. The value 0 specifies no transition.
 *
 * @default 250
 */
[Style(name="openDuration", type="Number", format="Time", inherit="no")]

/**
 * The duration of the WindowShade closing transition, in milliseconds. The value 0 specifies no transition.
 *
 * @default 250
 */
[Style(name="closeDuration", type="Number", format="Time", inherit="no")]

/**
 * The class from which the headerButton will be instantiated. Must be mx.controls.Button
 * or a subclass.
 *
 * @default mx.controls.Button
 */
[Style(name="headerClass", type="Class", inherit="no")]

/**
 * Name of CSS style declaration that specifies styles for the headerButton.
 */
[Style(name="headerStyleName", type="String", inherit="no")]

/**
 * Alignment of text on the headerButton. The value set for this style is used as
 * the textAlign style on the headerButton. Valid values are "left", "center" and "right".
 *
 * @default "right"
 */
[Style(name="headerTextAlign", type="String", inherit="no")]

/**
 * If true, the value of the headerButton's <code>toggle</code> property will be set to true;
 * otherwise the <code>toggle</code> property will be left in its default state.
 *
 * @default false
 */
[Style(name="toggleHeader", type="Boolean", inherit="no")]



/**
 * Dispatched when the <code>opened</code> property is changed, either through user action
 * or programatically. This event is cancelable. When cancelled through a call to Event.preventDefault(),
 * the <code>opened</code> property will be restored to its previous state.
 *
 *  @eventType flexlib.events.WindowShadeEvent.OPENED_CHANGED
 */
[Event(name="openedChanged", type="flexlib.events.WindowShadeEvent")]

/**
 * Dispatched when the WindowShade is about to be opened.
 *
 * <p>In most cases, an event of this type will be followed by an event of type WindowShadeEvent.OPEN_END (<code>openEnd</code>); however,
 * if the user clicks the header button before the closing transition has run to completion, the <code>openEnd</code> event will
 * not be dispatched, since the WindowShade will not be left in the opened state.</p>
 *
 *  @eventType flexlib.events.WindowShadeEvent.OPEN_BEGIN
 */
[Event(name="openBegin", type="flexlib.events.WindowShadeEvent")]

/**
 * Dispatched when the WindowShade has finished opening. This event cannot be cancelled.
 *
 *  @eventType flexlib.events.WindowShadeEvent.OPEN_END
 */
[Event(name="openEnd", type="flexlib.events.WindowShadeEvent")]

/**
 * Dispatched when the WindowShade is about to be closed. This event cannot be cancelled.
 *
 * <p>In most cases, an event of this type will be followed by an event of type WindowShadeEvent.CLOSE_END (<code>closeEnd</code>); however,
 * if the user clicks the header button before the closing transition has run to completion, the <code>closeEnd</code> event will
 * not be dispatched, since the WindowShade will not be left in the closed state.</p>
 *
 *  @eventType flexlib.events.WindowShadeEvent.CLOSE_BEGIN
 */
[Event(name="closeBegin", type="flexlib.events.WindowShadeEvent")]

/**
 * Dispatched when the WindowShade has finished closing. This event cannot be cancelled.
 *
 *  @eventType flexlib.events.WindowShadeEvent.CLOSE_END
 */
[Event(name="closeEnd", type="flexlib.events.WindowShadeEvent")]

/**
 * This control displays a button, which when clicked, will cause a panel to "unroll" beneath
 * it like a windowshade being pulled down; or if the panel is already displayed it
 * will be "rolled up" like a windowshade being rolled up. When multiple WindowShades are stacked
 * in a VBox, the result will be similar to an mx.containers.Accordian container, except that multiple
 * WindowShades can be opened simultaneously whereas an Accordian acts like a tab navigator, with only
 * one panel visible at a time.
 */
public class WindowShade extends LayoutContainer
{

	[Embed(source="/images/embedded/arrow_round_expand.png")]
	private static var DEFAULT_CLOSE_ICON:Class;

	[Embed(source="/images/embedded/arrow_round_collapse.png")]
	private static var DEFAULT_OPEN_ICON:Class;


	private static var styleDefaults:Object = {
			openDuration: 250, closeDuration: 250, paddingTop: 10, headerClass: Button, headerTextAlign: "left", toggleHeader: false, headerStyleName: null, closeIcon: DEFAULT_CLOSE_ICON, openIcon: DEFAULT_OPEN_ICON
		};

	private static var classConstructed:Boolean = constructClass();

	private static function constructClass():Boolean
	{

		var css:CSSStyleDeclaration = StyleManager.getStyleDeclaration("WindowShade")
		var changed:Boolean = false;
		if (!css)
		{
			// If there is no CSS definition for WindowShade,
			// then create one and set the default value.
			css = new CSSStyleDeclaration();
			changed = true;
		}

		// make sure we have a valid values for each style. If not, set the defaults.
		for (var styleProp:String in styleDefaults)
		{
			if (!StyleManager.isValidStyleValue(css.getStyle(styleProp)))
			{
				css.setStyle(styleProp, styleDefaults[styleProp]);
				changed = true;
			}
		}

		if (changed)
		{
			StyleManager.setStyleDeclaration("WindowShade", css, true);
		}

		return true;
	}

	/**
	 * @private
	 * A reference to the Button that will be used for the header. Must always be a Button or subclass of Button.
	 */
	protected var _headerButton:Button = null;

	private var headerChanged:Boolean;

	/**
	 * @private
	 * The header renderer factory that will get used to create the header.
	 */
	private var _headerRenderer:IFactory;

	/**
	 * To control the header used on the WindowShade component you can either set the <code>headerClass</code> or the
	 * <code>headerRenderer</code>. The <code>headerRenderer</code> works similar to the itemRenderer of a List control.
	 * You can set this using MXML using any Button control. This would let you customize things like button skin. You could
	 * even combine this with the CanvasButton component to make complex headers.
	 */
	public function set headerRenderer(value:IFactory):void
	{
		_headerRenderer = value;

		headerChanged = true;
		invalidateProperties();
	}

	public function get headerRenderer():IFactory
	{
		return _headerRenderer;
	}

	/**
	 * @private
	 * Boolean dirty flag to let us know if we need to change the icon in the commitProperties method.
	 */
	private var _openedChanged:Boolean = false;


	public function WindowShade()
	{
		super();
		//automation
		this.showInAutomationHierarchy = true;


		//default scroll policies are off
		this.verticalScrollPolicy = ScrollPolicy.OFF;
		this.horizontalScrollPolicy = ScrollPolicy.OFF;

		addEventListener(EffectEvent.EFFECT_END, onEffectEnd);
	}

	protected function createOrReplaceHeaderButton():void
	{
		if (_headerButton)
		{
			_headerButton.removeEventListener(MouseEvent.CLICK, headerButton_clickHandler);

			if (rawChildren.contains(_headerButton))
			{
				rawChildren.removeChild(_headerButton);
			}
		}

		if (_headerRenderer)
		{
			_headerButton = _headerRenderer.newInstance() as Button;
		}
		else
		{
			var headerClass:Class = getStyle("headerClass");
			_headerButton = new headerClass();
		}

		applyHeaderButtonStyles(_headerButton);

		_headerButton.addEventListener(MouseEvent.CLICK, headerButton_clickHandler);

		rawChildren.addChild(_headerButton);

		// Fix for Issue #85
		_headerButton.tabEnabled = false;
	}

	protected function applyHeaderButtonStyles(button:Button):void
	{
		button.setStyle("textAlign", getStyle("headerTextAlign"));

		var headerStyleName:String = getStyle("headerStyleName");
		if (headerStyleName)
		{
			headerStyleName = StringUtil.trim(headerStyleName);
			button.styleName = headerStyleName;
		}

		button.toggle = getStyle("toggleHeader");
		button.label = label;

		if (_opened)
		{
			button.setStyle('icon', getStyle("openIcon"));
		}
		else
		{
			button.setStyle('icon', getStyle("closeIcon"));
		}

		if (button.toggle)
		{
			button.selected = _opened;
		}
	}

	/**
	 * The text that appears on the headerButton.
	 */
	override public function get label():String
	{
		// This override is here only to keep the ASDoc tool from incorrectly marking this a write-only property.
		return super.label;
	}

	/**
	 * @private
	 */
	override public function set label(value:String):void
	{
		super.label = value;

		if (_headerButton)
			_headerButton.label = value;
	}

	/**
	 * @private
	 */
	private var _opened:Boolean = true;

	/**
	 * Sets or gets the state of this WindowShade, either opened (true) or closed (false).
	 */
	public function get opened():Boolean
	{
		return _opened;
	}

	private var _headerLocation:String = "top";

	[Bindable]
	[Inspectable(enumeration="top,bottom", defaultValue="top")]
	/**
	 * Specifies where the header button is placed relative tot he content of this WindowShade. Possible
	 * values are <code>top</code> and <code>bottom</code>.
	 */
	public function set headerLocation(value:String):void
	{
		_headerLocation = value;
		invalidateSize();
		invalidateDisplayList();
	}

	public function get headerLocation():String
	{
		return _headerLocation;
	}

	/**
	 * @private
	 */
	[Bindable]
	public function set opened(value:Boolean):void
	{
		var old:Boolean = _opened;

		_opened = value;
		_openedChanged = _openedChanged || (old != _opened);

		if (_openedChanged && initialized)
		{
			// we only want to dispatch the WindowShadeEvent.OPENED_CHANGED when the property actually changes. The _openedChanged
			// flag may be set from a previous call with the same value. In that case we want to leave it set
			// for the commitProperties() method.
			if ((old != _opened) && willTrigger(WindowShadeEvent.OPENED_CHANGED))
			{
				var evt:WindowShadeEvent = new WindowShadeEvent(WindowShadeEvent.OPENED_CHANGED, false, true);
				dispatchEvent(evt);
				var cancelled:Boolean = evt.isDefaultPrevented();
				if (evt.isDefaultPrevented())
				{
					// restore the old setting. We use callLater so it happens after the PropertyChangeEvent fired
					// by the binding wrapper.
					callLater(restoreOpened, [old]);
					return;
				}
			}

			measure();
			runResizeEffect();

			invalidateProperties();
		}
	}


	/**
	 * @private
	 *
	 * This exists to allow us to restore a previous opened state when a WindowShadeEvent.OPENED_CHANGED event is
	 * cancelled, while bypassing the code that changes the visual state of the WindowShade and dispatches the WindowShadeEvent.
	 */
	protected function restoreOpened(value:Boolean):void
	{
		var old:Boolean = _opened;
		_opened = value;
		_openedChanged = _openedChanged || (old != _opened);
		if (_opened != old)
		{
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "opened", old, _opened));
		}
	}


	/**
	 * @private
	 */
	override public function styleChanged(styleProp:String):void
	{
		super.styleChanged(styleProp);

		if (styleProp == "headerClass")
		{
			headerChanged = true;
			invalidateProperties();
		}
		else if (styleProp == "headerStyleName" || styleProp == "headerTextAlign" || styleProp == "toggleHeader"
			|| styleProp == "openIcon" || styleProp == "closeIcon")
		{
			applyHeaderButtonStyles(_headerButton);
		}

		invalidateDisplayList();
	}

	/**
	 * @private
	 */
	override protected function createChildren():void
	{
		super.createChildren();

		createOrReplaceHeaderButton();
	}

	/**
	 * @private
	 */
	override protected function commitProperties():void
	{

		super.commitProperties();

		if (headerChanged)
		{
			createOrReplaceHeaderButton();
			headerChanged = false;
		}

		if (_openedChanged)
		{

			if (_opened)
			{
				_headerButton.setStyle('icon', getStyle("openIcon"));
			}
			else
			{
				_headerButton.setStyle('icon', getStyle("closeIcon"));
			}

			_openedChanged = false;
		}
	}


	/**
	 * @private
	 */
	override protected function updateDisplayList(w:Number, h:Number):void
	{
		super.updateDisplayList(w, h);

		if (_headerLocation == "top")
		{
			_headerButton.move(0, 0);
		}
		else if (_headerLocation == "bottom")
		{
			_headerButton.move(0, h - _headerButton.getExplicitOrMeasuredHeight());
		}

		_headerButton.setActualSize(w, _headerButton.getExplicitOrMeasuredHeight());
		this.dispatchEvent(new ResizeEvent(ResizeEvent.RESIZE, true));
	}

	/**
	 * @private
	 */
	private var _viewMetrics:EdgeMetrics;

	override public function get viewMetrics():EdgeMetrics
	{
		// The getViewMetrics function needs to return its own object.
		// Rather than allocating a new one each time, we'll allocate
		// one once and then hold a pointer to it.
		if (!_viewMetrics)
			_viewMetrics = new EdgeMetrics(0, 0, 0, 0);

		var vm:EdgeMetrics = _viewMetrics;

		var o:EdgeMetrics = super.viewMetrics;

		vm.left = o.left;
		vm.top = o.top;
		vm.right = o.right;
		vm.bottom = o.bottom;

		//trace(ObjectUtil.toString(vm));
		var hHeight:Number = _headerButton.getExplicitOrMeasuredHeight();
		if (!isNaN(hHeight))
		{
			if (_headerLocation == "top")
			{
				vm.top += hHeight;
			}
			else if (_headerLocation == "bottom")
			{
				vm.bottom += hHeight;
			}
		}


		return vm;
	}

	/**
	 * @private
	 */
	override protected function measure():void
	{
		super.measure();

		if (_opened)
		{
			//if this WindowShade is opened then we have to include the height of the header button
			//measuredHeight += _headerButton.getExplicitOrMeasuredHeight();
		}
		else
		{
			//if the WindowShade is closed then the height is only the height of the header button
			measuredHeight = _headerButton.getExplicitOrMeasuredHeight();
		}
	}

	/**
	 * @private
	 */
	private var resize:Resize;

	/**
	 * @private
	 */
	private var resizeInstance:ResizeInstance;

	/**
	 * @private
	 */
	private var resetExplicitHeight:Boolean;


	private var transitionCompleted:Boolean = true;

	/**
	 * @private
	 */
	protected function runResizeEffect():void
	{
		if (resize && resize.isPlaying)
		{

			// The user has clicked the header button before an open or close transition has run
			// to completion. We'll set the transitionCompleted flag to false to prevent the
			// completion event from being dispatched in onEffectEnd.
			transitionCompleted = false;

			// before the call to end() returns, the onEffectEnd method will have been called
			// for the currently playing resize.
			resize.end();
		}

		transitionCompleted = true;

		var beginEvent:String = _opened ? WindowShadeEvent.OPEN_BEGIN : WindowShadeEvent.CLOSE_BEGIN;
		if (willTrigger(beginEvent))
		{
			dispatchEvent(new WindowShadeEvent(beginEvent, false, false));
		}

		var duration:Number = _opened ? getStyle("openDuration") : getStyle("closeDuration");
		if (duration == 0)
		{
			this.setActualSize(getExplicitOrMeasuredWidth(), measuredHeight);

			invalidateSize();
			invalidateDisplayList();

			var endEvent:String = _opened ? WindowShadeEvent.OPEN_END : WindowShadeEvent.CLOSE_END;
			if (willTrigger(endEvent))
			{
				dispatchEvent(new WindowShadeEvent(endEvent, false, false));
			}

			return;
		}

		resize = new Resize(this);
		resize.easingFunction = Cubic.easeInOut;

		// If this WindowShade currently has no explicit height set, we want to
		// restore that state when the resize effect is finished, in the onEffectEnd method.
		// If it does, then the final height set by the effect will be retained.
		resetExplicitHeight = isNaN(explicitHeight);

		var resizeHeight:Number = measuredHeight;
		//hack to fix expanding too far -- kelly
		//var resizeHeight:Number = measuredHeight > this._headerButton.height ? measuredHeight - this._headerButton.height : measuredHeight;
		//end hack

		resize.heightTo = Math.min(maxHeight, resizeHeight);

		resize.duration = duration;

		var instances:Array = resize.play();
		if (instances && instances.length)
		{
			resizeInstance = instances[0];
		}
	}

	/**
	 * @private
	 */
	protected function onEffectEnd(evt:EffectEvent):void
	{
		// Make sure this is our effect ending
		if (evt.effectInstance == resizeInstance)
		{
			if (resetExplicitHeight)
				explicitHeight = NaN;
			resizeInstance = null;

			// the transitionCompleted flag will be false if the user clicked the headerButton
			// twice in succession. We only want to fire events for transitions that run
			// all the way to completion.
			if (transitionCompleted)
			{
				var endEvent:String = _opened ? WindowShadeEvent.OPEN_END : WindowShadeEvent.CLOSE_END;
				if (willTrigger(endEvent))
				{
					dispatchEvent(new WindowShadeEvent(endEvent, false, false));
						//dispatchEvent(new ResizeEvent(ResizeEvent.RESIZE));
				}
			}
		}
	}


	public function set headerAutomationName(value:String):void
	{
		this._headerButton.automationName = value;
	}


	/**
	 * @private
	 */
	protected function headerButton_clickHandler(event:MouseEvent):void
	{
		opened = !_opened;
	}
}
}

