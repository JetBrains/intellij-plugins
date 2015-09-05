__AS3__$vec final dynamic class Vector$object extends Object{
  native public function Vector$object(length:uint = 0,fixed:Boolean = false):*;
  native public function set length(value:uint):*;
  native public function set fixed(f:Boolean):*;
  native AS3 function concat(... rest):Vector$object;
  native AS3 function reverse():Vector$object;
  static native private const AS3:* = "http://adobe.com/AS3/2006/builtin";
}

native public function trace(... rest):void;

package flash.display {
import flash.events.EventDispatcher;
[Event(name="mouseDown",type="flash.events.MouseEvent")]
[Event(name="mouseUp",type="flash.events.MouseEvent")]
[Event(name="mouseMove",type="flash.events.MouseEvent")]
public class Sprite extends EventDispatcher{
    native public function Sprite():*;
    native public function set buttonMode(value:Boolean):void;
    native public function get name():String;
    native public function set name(value:String):void;
}
}

import mypackage.ListCollectionView;

package flash.events {
public interface IEventDispatcher{
    native function addEventListener(type:String,listener:Function,useCapture:Boolean = false,priority:int = 0,useWeakReference:Boolean = false):void;
    native function removeEventListener(type:String,listener:Function,useCapture:Boolean = false):void;
}
public class EventDispatcher extends Object implements IEventDispatcher{
    native public function addEventListener(type:String,listener:Function,useCapture:Boolean = false,priority:int = 0,useWeakReference:Boolean = false):void;
    native public function removeEventListener(type:String,listener:Function,useCapture:Boolean = false):void;
}
public class Event{}
public class MouseEvent extends Event{}
public class ErrorEvent extends Event{
    static native public const ERROR:String = "error";
}
public class AccelerometerEvent extends Event{}
}

package mx.styles {
public interface IStyleClient{}
}

package mx.core {
import flash.display.Sprite;
import flash.events.EventDispatcher;
import mx.styles.IStyleClient;

public interface IVisualElementContainer{}                  // base interface for spark containers (Flex 4)
public interface IVisualElement extends EventDispatcher{    // base interface for spark elements (Flex 4)
    function set depth(value:Number):void;
}
public interface IUIComponent extends EventDispatcher{}     // base interface for Flex 3 components
public interface IContainer extends IUIComponent{}          // base interface for Flex 3 containers
public interface IDeferredInstance{}
public interface IRepeater{}
public interface IRepeaterClient{}
public interface IMXMLObject {}
[Event(name="initialize",type="mx.events.FlexEvent")]
[Event(name="creationComplete", type="mx.events.FlexEvent")]
[Effect(name="resizeEffect", event="resize")]
[Style(name="left", type="String", inherit="no")]
[Style(name="right", type="String", inherit="no")]
[Style(name="top", type="String", inherit="no")]
[Style(name="bottom", type="String", inherit="no")]
public class UIComponent extends Sprite implements IUIComponent, IVisualElement, IRepeaterClient, IStyleClient {
  public var states:Array /* of State */ = [];
  public function set transitions(value:Array):void{}
  public function get id():String{}
  public function set id(value:String):void{}
  native public function set currentState(value:String);
    
  native public function get left():Object;
  native public function set left(value:Object):void;
  
  native public function get right():Object;
  native public function set right(value:Object):void;
  
  native public function get top():Object;
  native public function set top(value:Object):void;
  
  native public function get bottom():Object;
  native public function set bottom(value:Object):void;

  [PercentProxy("percentWidth")]
  native public function get width():Number;
  native public function set width(value:Number):void;

  native public function get percentWidth():Number;
  native public function set percentWidth(value:Number):void;

  [PercentProxy("percentHeight")]
  native public function get height():Number;
  native public function set height(value:Number):void;

  native public function get percentHeight():Number;
  native public function set percentHeight(value:Number):void;

  native public function get horizontalCenter():Number;
  native public function set horizontalCenter(value:Number):void;

  native public function get verticalCenter():Number;
  native public function set verticalCenter(value:Number):void;
}
public class Application extends Container{}
public class Repeater extends UIComponent implements IRepeater{}
include "TextStyles.as"
public class Container extends UIComponent implements IContainer, IVisualElementContainer{
}
public interface IFactory {
    function newInstance():*;
}
public class ClassFactory implements IFactory {
    public function ClassFactory(generator:Class = null){}
    public function newInstance():* {}
}
public interface IFlexModuleFactory{}
public class DesignLayer extends EventDispatcher{
  public function set visible(value:Boolean):void{}
}
}

package mx.controls{
import flash.display.Sprite;
import mx.core.Container;
import mx.core.UIComponent;
import flash.events.EventDispatcher;
import mx.core.IFactory;

public class Alert extends Panel{
  static native public final function show(text:String = "", title:String = "", flags:uint = 4, parent:Sprite = null, closeHandler:Function = null, iconClass:Class = null, defaultButtonFlag:uint = 4):Alert;
}

[Event(name="click",type="flash.events.MouseEvent")]
[Style(name="color")]
public class Button extends UIComponent{
    native public function set label(value:String):void;

    [Inspectable(category="General", enumeration="left,right,top,bottom", defaultValue="right")]
    native public function get labelPlacement():String;
    native public function set labelPlacement(value:String):void;
}

public class LinkButton extends Button{}
public class CheckBox extends Button{}
public class TextInput extends UIComponent{}
public class DataGrid extends UIComponent{
  [Inspectable(category="General", arrayType="mx.controls.dataGridClasses.DataGridColumn")]
  public function get columns():Array{}
  public function set columns(value:Array):void{}
  public function set itemRenderer(value:IFactory):void{}
  public native function get dataProvider():Object;
  public native function set dataProvider(value:Object):void;
  public native function get columns():Array;
  public native function set columns(value:Array):void;
}
public class TextArea extends UIComponent{
  public function set text(value:String):void{}
}

public class RadioButtonGroup extends EventDispatcher {}

public class Tree extends UIComponent {}

public class Text extends UIComponent {
    public native function get text():String;
    public native function set text(s:String):void;
    [Inspectable(category="General")]
    public native function set target(_:String):void;
}
public class Image extends UIComponent {
    public native function get source():Object;
    public native function set source(p:Object):void;
}
}

package mx.controls.dataGridClasses {
import mx.core.IFactory;

public class DataGridColumn{
  public function get itemRenderer():IFactory{}
  public function set itemRenderer(value:IFactory):void{}
  public var itemEditor:IFactory;
  public native function get dataField():String;
  public native function set dataField(value:String):void;
}
}

package mx.containers{
import mx.core.Container;

public class Panel extends Container{}
public class VBox extends Container{}
public class HBox extends Container{}
public class Accordion extends Container{}
}

package mx.collections{
[DefaultProperty("source")]
public class XMLListCollection{
  public function set source(s:XMLList):void{}
}
public class ListCollectionView {}

public class ArrayCollection extends ListCollectionView{}
}

package mx.states{
public class State{
    public var name:String;

    [ArrayElementType("String")]
    [Inspectable(category="General")]
    public var stateGroups:Array /* of String */ = [];
}

[DefaultProperty("effect")]
class Transition {
    public var effect:IEffect;

    [Inspectable(category="General")]
    public var fromState:String = "*";

    [Inspectable(category="General")]
    public var toState:String = "*";
}
}

package spark.components {
import mx.core.IDeferredInstance;
import mx.core.IVisualElementContainer;
import mx.core.UIComponent;
import mx.core.IFactory;
import spark.components.supportClasses.ButtonBase;
import spark.components.supportClasses.SkinnableComponent;

public class Application extends SkinnableContainer{
    public var pageTitle:String;
}

public class ViewNavigator extends SkinnableContainer {
    public function pushView(viewClass:Class, data:Object = null, context:Object = null, transition:Object = null):void {}
}

public class View extends SkinnableContainer{
    public function get navigator():ViewNavigator{}
}

public class SkinnableDataContainer extends SkinnableComponent {
    public native function get itemRenderer():IFactory;
    public native function set itemRenderer(value:IFactory):void;
}

[Style(name="verticalScrollPolicy", type="String", inherit="no", enumeration="off,on,auto")]
public class List extends SkinnableDataContainer {
  public function set selectedIndices(value:Vector.<int>):void{}
}

[DefaultProperty("navigationStack")]
public class ViewNavigatorApplication extends Application {
    private function get navigationStack():NavigationStack{
        return null;
    }

    private function set navigationStack(value:NavigationStack):void{}
    public function set firstView(value:Class):void{}
}

[Event(name="click",type="flash.events.MouseEvent")]
[Style(name="color", type="uint", format="Color", inherit="yes")]
[Style(name="alignmentBaseline", type="String", enumeration="useDominantBaseline,roman,ascent,descent,ideographicTop,ideographicCenter,ideographicBottom", inherit="yes")]
[Style(name="borderAlpha")]
public class Button extends ButtonBase{
    native public function set label(value:String):void;
}

[DefaultProperty("mxmlContent")]
public class Group extends UIComponent implements IVisualElementContainer{
  [ArrayElementType("mx.core.IVisualElement")]
  public function set mxmlContent(value:Array):void{}
  override public function set width(value:Number):void{}
}

public class HGroup extends Group {}

public class DataGroup extends UIComponent{
    public function set itemRenderer(value:IFactory):void{}
}

public class ButtonBar extends SkinnableComponent {

}

public class ButtonBarButton extends SkinnableComponent {

}

public class TextInput extends SkinnableComponent {
    public native function get text():String;
    public native function set text(value:String):void;
}

public class CheckBox extends SkinnableComponent {
    public native function get text():String;
    public native function set text(value:String):void;
    public native function get label():String;
    public native function set label(value:String):void;
}

[SkinState("normal")]
[SkinState("disabled")]
[DefaultProperty("mxmlContentFactory")]
public class SkinnableContainer extends SkinnableComponent implements IVisualElementContainer{
    [InstanceType("Array")]
    [ArrayElementType("mx.core.IVisualElement")]
    public function set mxmlContentFactory(value:IDeferredInstance):void{}
}
}

package spark.components.supportClasses{
import spark.components.Group;
import mx.core.UIComponent;
import mx.core.IVisualElementContainer;
import mx.core.IDeferredInstance;

[Style(name="skinClass", type="Class")]
[Event(name="stateChangeComplete", type="mx.events.FlexEvent")]
public class SkinnableComponent extends UIComponent{
  public function get skin():UIComponent{}
}

public class Skin extends Group{}

[SkinState("disabled")]
[SkinState("down")]
[SkinState("over")]
[SkinState("up")]
[Event(name="buttonDown", type="mx.events.FlexEvent")]
public class ButtonBase extends SkinnableComponent{}
public class NavigationStack{}
}

package spark.collections {
import flash.events.EventDispatcher;

public class Sort extends EventDispatcher{
    [Inspectable(category="General", arrayType="adobe.typo.NonExistentClass")]
    public function set fields(value:Array):void{}
}
}

package flash.filters {
  public class BitmapFilterQuality {}
}

package mx.filters {
  public class BaseFilter {}
}

package spark.primitives.supportClasses {
import flash.events.EventDispatcher;
import mx.core.IVisualElement;

public class GraphicElement extends EventDispatcher implements IVisualElement{}
}

package spark.primitives {
import spark.primitives.supportClasses.GraphicElement;
import mx.graphics.IFill;

public class Rect extends GraphicElement {
    public function set fill(value:IFill):void{}
}
}

package mx.graphics{
import flash.events.EventDispatcher;

public interface IFill{}
public class SolidColor extends EventDispatcher implements IFill{
    public function set color(value:uint):void{}
}
}

package spark.core{
import mx.core.IVisualElement;
import flash.display.Sprite;

public class SpriteVisualElement extends Sprite implements IVisualElement{
    public function get id():String{}
    public function set id(value:String):void{}
    public function set depth(value:Number):void{}
}
}

package mx.rpc.soap.mxml{
import flash.events.EventDispatcher;
public class Operation extends EventDispatcher{
    public function set name(n:String):void{}
    public function set request(r:Object):void{}
}
}

package mx.rpc.http.mxml{
import flash.events.EventDispatcher;

public class HTTPService extends EventDispatcher{
    public function set request(r:Object):void{}
    [Inspectable(category="General", defaultValue="object", enumeration="object,array,xml,flashvars,text,e4x")]
    native public function set resultFormat(_79:Object):void;
}
}

package mx.rpc.soap.mxml{
import flash.events.IEventDispatcher;
public dynamic class WebService implements IEventDispatcher{}
}

public final dynamic class XML {}
public class flash.xml.XMLNode {}

package mx.rpc.remoting {
public class RemoteObject {
    [Inspectable(category="General")]
    public function get destination():String {}
   public function set destination(name:String):void {}
}
}

package mx.rpc.remoting.mxml {
public class Operation {
    native public function set name(value:String):void;
}
}

package mx.validators {
import mx.core.IMXMLObject;
    public class EmailValidator implements IMXMLObject {
        public native function get source():Object;
        public native function set source(p:Object):void;
    }
}

package flash.utils {
[native(cls="DictionaryClass",gc="exact",instance="DictionaryObject",methods="auto")]
public dynamic class Dictionary extends Object {
    native public function Dictionary(weakKeys:Boolean = false):*;
    native private function init(weakKeys:Boolean):void;
}
}