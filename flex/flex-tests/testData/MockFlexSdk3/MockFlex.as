native public function trace(... rest):void;

package flash.display {
import flash.events.EventDispatcher

[Event(name="mouseDown", type="flash.events.Event")]
[Event(name="mouseUp", type="flash.events.Event")]
[Event(name="mouseMove", type="flash.events.Event")]
public class Sprite extends InteractiveObject {
  [Inspectable(category="General")]
  public native function set layout(s:String);

  [Inspectable(category="General")]
  public native function set currentState(s:String):void;
}

[Event(name="click",type="flash.events.MouseEvent")]
public class InteractiveObject extends DisplayObject{}
public class DisplayObject extends EventDispatcher {
  public native function set width(_:uint):void;
  public native function get width():uint;
}
}

package barxxx {
  class HTTPService {
    [Inspectable(category="General", defaultValue="object", enumeration="object,array,xml,flashvars,text,e4x")]
    native public function set resultFormat(_79:Object):void;
  }
}

package flash.filters {
public class BitmapFilter { }
public class BitmapFilterQuality { }
}

package mx.events {
public class FlexEvent extends flash.events.Event{}
public class DividerEvent extends flash.events.Event{}
}

package flash.events {
public class Event {
  native public function Event(type:String,bubbles:Boolean = false,cancelable:Boolean = false):*;
  public var data;
}

public class KeyboardEvent extends Event {
  public static var TYPED:String = "xxx";
}
public class MouseEvent extends Event {
  public static var CLICK:String = "click";
  public static var DOUBLE_CLICK:String = "doubleClick";
  public static var MOUSE_UP:String = "mouseUp";
  public static var MOUSE_DOWN:String = "mouseDown";
  public static var MOUSE_MOVE:String = "mouseMove";
  public static var MOUSE_IN:String = "mouseIn";
  public static var MOUSE_OUT:String = "mouseOut";
  public static var MOUSE_WHEEL:String = "mouseWheel";
}

public interface IEventDispatcher {
  native function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false) :void
}

public class EventDispatcher implements IEventDispatcher{
  public native function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false) :void
  public native function removeEventListener(type:String, listener:Function, useCapture:Boolean = false) :void
  public native function dispatchEvent(event:flash.events.Event):Boolean
}
}

package mypackage {
import mx.core.UIComponent;
import mx.core.IFactory;
import mx.controls.listClasses.BaseListData;

public function getDefinitionByName(s:String) {}
public function getQualifiedClassName(s:String) {}
public function getClassByQualifiedClassName(s:String):Class {}

  public class ListCollectionView {}
  public class ArrayCollection extends ListCollectionView {
    public function set source(s:Array):void{}
  }

interface IEffect {}

[DefaultProperty("effect")]
class Transition {
    public var effect:IEffect;

    [Inspectable(category="General")]
    public var fromState:String = "*";

    [Inspectable(category="General")]
    public var toState:String = "*";
}

class TweenEffect implements IEffect {
    public var easingFunction:Function = null;
}

class Resize extends TweenEffect {}

interface IFill {}
class SolidColor implements IFill {
  public var color:uint;
}
class Rect {
  public var fill:IFill;
}

interface IOverride {}
interface IDeferredInstance {}

[DefaultProperty("targetFactory")]
class AddChild implements IOverride {
    [Inspectable(category="General")]

    public function get targetFactory():IDeferredInstance
    { return null;}

    /**
     *  @private
     */
    public function set targetFactory(value:IDeferredInstance):void {}
}

public interface IList {}
public interface ICollectionView {}

class SetProperty implements IOverride {
    [Inspectable(category="General")]

    public var name:String;

    //----------------------------------
    //  target
    //----------------------------------

    [Inspectable(category="General")]

    public var target:Object;

    //----------------------------------
    //  value
    //----------------------------------

    [Inspectable(category="General")]

    public var value:*;

}

[DefaultProperty("overrides")]
class State {

    public var basedOn:String;

    [Inspectable(category="General")]

    public var name:String;

    public var overrides:Array /* of IOverride */ = [];

}

[Event(name="initialize", type="flash.events.Event")]
[Event(name="xxx", type="flash.events.KeyboardEvent")]
[Style(name="disabledSkin")]
[Style(name="disabledColor")]
[Style(name="upSkin")]
  public class Application extends mx.core.Container {
      [Inspectable(arrayType="mypackage.Transition")]
        [ArrayElementType("mypackage.Transition")]

        public var transitions:Array /* of Transition */ = [];

      [Inspectable(arrayType="mypackage.State")]
        [ArrayElementType("mypackage.State")]

        public var states:Array /* of State */ = [];
  }

  [Style(name="verticalAlign", type="String", enumeration="bottom,middle,top", inherit="no")]
  public class HBox extends mx.core.Container {
    [Inspectable(category="General", defaultValue="false", enumeration="true,false")]
    public native function set selected(_:Object):void;
  }

  [Style(name="kerning", type="Boolean", inherit="yes")]
  class VBox extends mx.core.Container {
    public native function set selected(_:Boolean):void;
  }

  public interface ResourceBundle {
    function getString(resourceName:String):String;
  }

  public class RegExpValidator {
    public var expression:String;
  }

  public interface IResourceManager {
      function findResourceBundleWithResource(
                        bundleName:String,
                        resourceName:String):*;
      function getString(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):String;
      function getObject(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):*;
      function getClass(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Class;
      function getStringArray(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Array /* of String */;

      function getNumber(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Number;

      function getInt(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):int;
      function getUint(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):uint;

      function getBoolean(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Boolean;

      function getResourceBundle(locale:String,
                               bundleName:String):*;
  }

  public class ResourceManager {
    public static native function getInstance():IResourceManager;
  }

/**
 * tabStyleName doc
 */
  [Style(name="tabStyleName", type="String", inherit="no")]
  class TabBar extends mx.core.Container {

  }

  class HGroup extends UIComponent {
    public native override function set width(_:uint):void;
  }

  class CheckBox extends UIComponent {
    public var label:String;
  }

  class ListBase extends UIComponent {
    [Inspectable(enumeration="off,on,auto", defaultValue="auto")]
    public native function get verticalScrollPolicy():String;
    public native function set verticalScrollPolicy(value:String):void;

    [Inspectable(category="Data")]
    public function set itemRenderer(value:IFactory):void{}
  }

  public class List extends ListBase {
    public override native function set verticalScrollPolicy(value:String);
  }

  class Canvas extends UIComponent {
    [Inspectable(category="General")]
    public native function set hints(_:String):void;
  }

  [Exclude(name="resizeEffect", kind="effect")]
  class Text extends UIComponent {
    [Inspectable(category="General", defaultValue="")]
    public native function set text(_:String):void;

    public native function set htmlText(_:String):void;

    // Fake properties
    [Inspectable(category="General")]
    public native function set target(_:DisplayObject):void;

    [Inspectable(category="General")]
    public native function set relativeTo(_:UIComponent);

    [Inspectable(category="General")]
    public native function set relativeTo2(_:Object);
  }

  public class Tree extends UIComponent{}
  public class TextInput extends UIComponent {
    public native function set count(_:Number);
    public native function set text(_:String);
    public native function get text():String;
  }
  class TextArea extends UIComponent{
      public native function set text(_:String);
    public native function get text():String;
  }

  [Style(name="headerColors",type="Array",arrayType="uint",format="Color",inherit="yes")]
  [Style(name="borderAlpha",type="Number",inherit="no")]
  public class Panel extends mx.core.Container {
    public native function set title(_:String):void;
    public native function set statusStyleName(_:String):void;
  }

  [DefaultProperty("dataProvider")]
  class DataGrid extends ListBase {
    [Bindable("columnsChanged")]
    [Inspectable(arrayType="mypackage.dataGridClasses.DataGridColumn")]
    public native function get columns():Array;
    public native function set columns(_:Array);

    [Bindable("collectionChange")]
    [Inspectable(category="Data", defaultValue="undefined")]
    public native function set dataProvider(o:Object);
  }

  /**
   * Item click comment
   */

  [Event(name = "itemClick")]
  [Event(name = "change")]
  [Style(name="disabledColor",type="uint",format="Color",inherit="yes")]
  [Style(name="disabledSkin",type="Class",inherit="no")]
  [Style(name="upSkin",type="Class",inherit="no")]

  /**
   * Button's border color
   */
  [Style(name="borderColor",type="uint",format="Color",inherit="no")]
  [Style(name="borderAlpha")]
  public class Button extends UIComponent {
    public function Button() {}

    [Inspectable(category="General", defaultValue="")]
    public native function set label(_:String);

    [Bindable("click")]
    [Bindable("valueCommit")]
    [Inspectable(category="General", defaultValue="false")]
    public native function set selected(_:Boolean):void;

    [Bindable("dataChange")]
    [Inspectable(environment="none")]
    public native function set listData(value:BaseListData):void;
  }

  public class Label extends UIComponent {
    public function Label():* {}
    public native function set text(_:String);
  }

  public class Accordition extends UIComponent {
    public function set headerRenderer(value:IFactory):void{}
  }

  public class Image extends UIComponent {
    [Inspectable(category="General", defaultValue="", format="File")]
    public native function set source(value:Object);
  }

  class EmailValidator extends UIComponent {
    public native function set source(_:String);
  }

  class WebService {}

  public class StrangeUIComponentOutOfComponentList extends UIComponent {}

  class HTTPService extends barxxx.HTTPService {
      public var url:String;
      public var request:Object;
  }

  class Operation {
    native public function set request(_79:Object):void;
    native public function set name(_79:String):void;
  }

  class RemoteObject {
    native public function set destination(_79:Object):void;
    native public function set fault(_79:Object):void;
    native public function set result(_79:Object):void;
    native public function set source(s:String):void;
  }

  [Style(name="fontWeight",type="String",enumeration="normal,bold",inherit="yes")]

  class RemoteObjectOperation {
    native public function set name(_79:String):void;
    native public function send():void;
    native public function set arguments(a:Array):void;
    native public function get lastResult():Object;
  }

  public class MyEvent extends flash.events.Event {
    public static var TYPED:String = "yyy";
  }

  public class Alert {
    public static function show(s:String):void {}
  }

  public class CSSStyleDeclaration {
      public var defaultFactory:Function;
  }

  class FxContainer {
      public var skinClass:Array;
  }
  class Skin {}
  public class ClassFactory {
    public function ClassFactory(generator:Class) {}
  }
}

package mypackage.dataGridClasses {
import mx.core.IFactory;

  public class DataGridColumn {
    var ccc;

    // heh
    [Inspectable(category="General", defaultValue="")]
    /**
     * AAA
     */
    public var dataField:String;
    public var itemEditor:IFactory;
    public var width:int;
    public var headerText:String;
    public var itemRenderer:IFactory;
    public var headerStyleName:String;
    public var labelFunction:Function;
  }
}

package mx.rpc {
    public class AbstractService {}

    public class AbstractInvoker {}

    public class AbstractOperation {}

}

package mx.rpc.events {
    public class AbstractEvent {}
    public class FaultEvent {}
    public class HeaderEvent {}

}

package mx.rpc.soap {
    public class AbstractWebService {}
}

package mx.utils {
    public class Base64Encoder {}
    public class Base64Decoder {}
}

package mx.messaging.messages {
    public class AbstractMessage {}
}

package mx.messaging {
    public class AbstractProducer {}
    public class AbstractConsumer{}
    public class AbstractMessageStore {}
}

package mx.effects.easing {
    public class Back{}
}

package mx.charts.series {
    public class BarSeries {}
}

package mx.charts {
    public class BarChart {}
}

package mx.binding {
    public class Binding {}
}

package flash.utils {
public class Dictionary {}
}

package mx.core {
    public namespace mx_internal = "http://www.adobe.com/2006/flex/mx/internal";

  public interface IUIComponent {}
  public interface IContainer extends IUIComponent{}

  include "AnchorStyles2.as";
  /**
   * special event
   */
  [Event(name = "creationComplete")]
  include "AnchorStyles.as";
  /** Color of text in the component, including the component label.
   *  @default 0x0B333C
   */
  [Effect(name="resizeEffect", event="resize")]
  [Event(name="xxx")]
  [Event(name="yyy")]
  [Event(name = "onclick")]
  [Style(name="titleBackgroundSkin", type="Class", inherit="no")]
  /**
   * UI component's border color
   */
  [Style(name="borderColor", type="Number")]
  [Event(name="add", type="mx.events.FlexEvent")]
  public class UIComponent extends flash.display.Sprite implements IUIComponent{
    public function UIComponent() {}

    public var resourceManager:mypackage.IResourceManager

    public function get id():String{}
    public function set id(value:String):void{}

    [Inspectable(category="General")]
    [PercentProxy]
    public native function get height():uint;
    public native function set height(_:uint):void;

    private native function set fakePrivateProperty(_:uint):void;
    private native function get fakePrivateProperty():uint;

    static native function set fakeStaticProperty(_:uint):void;
    static native function get fakeStaticProperty():uint;

    final native function set fakeFinalProperty(_:uint):void;
    final native function get fakeFinalProperty():uint;

    [Inspectable(category="General")]
    [PercentProxy]
    public native function set width(_:uint):void;
    public native function get width():uint;

    [Inspectable(category="General")]
    public native function set name(_:String):void;
    public native function get name():String;

    public native function set backgroundColor(_:String):void
    public native function set enabled(_:Boolean):void

    public native function setStyle(styleProp:String, newValue:*):void;

    public native function set styleName(value:Object):void;

    import mx.core.mx_internal;

    mx_internal function updateCallbacks():void {}
  }


  /**
   * Container's border color
   */
  [Style(name="borderColor",type="uint",format="Color",inherit="no")]
  class Container extends UIComponent implements IContainer {
    private var _data:Object;
    public function get data():Object {
      return _data;
    }

    public function set data(value:Object):void {
      _data = value;
    }
  }

public interface IUITextField {}

public interface IFactory {
    function newInstance():*;
}

public class ClassFactory implements IFactory {
    public function ClassFactory(generator:Class = null){}
    public function newInstance():* {}
}

public class DeferredInstanceFromClass {
    public function DeferredInstanceFromClass(generator:Class) {}
}
}

package mx.controls.listClasses {
import mx.core.IUIComponent;
public class BaseListData {
}
}
package mx.controls.treeClasses {
    import mx.core.mx_internal;
    import mx.controls.listClasses.BaseListData;

    public class TreeItemRenderer {
        mx_internal function getLabel():int
        {
        }
    }
    public class TreeListData extends BaseListData {}
}

package flash.ui {
    public class ContextMenu {
        public function clone() {}
    }
}

package mx.controls {
import flash.events.EventDispatcher;
import mx.core.UIComponent;
[Style(name="borderColor",type="uint",format="Color",inherit="no")]
    public class Button extends UIComponent{}
    public class RadioButtonGroup extends EventDispatcher{}
}

[DefaultProperty("source")]
public class mx.collections.XMLListCollection{
  public function set source(s:XMLList):void{}
}

public class flash.utils.Proxy extends Object
{

  native public function Proxy():*;
  native flash_proxy function deleteProperty(name:*):Boolean;
  native flash_proxy function isAttribute(name:*):Boolean;
  native flash_proxy function callProperty(name:*,... rest):*;
  native flash_proxy function nextNameIndex(index:int):int;

  native flash_proxy function nextName(index:int):String;
  native flash_proxy function getDescendants(name:*):*;
  native flash_proxy function getProperty(name:*):*;
  native flash_proxy function nextValue(index:int):*;
  native flash_proxy function setProperty(name:*,value:*):void;

  native flash_proxy function hasProperty(name:*):Boolean;
}

native public const flash.utils.flash_proxy:* = "http://www.adobe.com/2006/actionscript/flash/proxy";

package flash.net {
public function navigateToURL() {}
}

package flash.data {
public class SQLStatement {
    public native function set text(value:String);
}
}

public final dynamic class XML {
    native AS3 function copy():XML;
}

public class flash.xml.XMLNode {
    native public function hasChildNodes():Boolean;
}

native public const NaN:Number;
native public const Infinity:Number;

public interface org.flexunit.runner.IRunner{}

package flash.external {
public class ExternalInterface {
    static native public function call(functionName:String,... rest):*;
}
}