package {
import flash.display.Sprite;
import flash.events.AccelerometerEvent;
import flash.events.ErrorEvent;
import flash.events.Event;

import starling.display.Quad;
import starling.events.Event;
import starling.events.ResizeEvent;
import starling.events.TouchEvent;

import com.acme.MyEventDispatcher;

[Event(type="starling.events.Event")]
[Event(type="starling.events.TouchEvent")]
[Event(type="starling.events.<error descr="Expected class flash.events.Event, starling.events.Event or descendant">NotEvent</error>")]
[Event(type="starling.events.<error descr="Unresolved variable or type 'NotClass'">NotClass</error>")]
[Event(type="starling.<error descr="Unresolved variable or type 'notPackage'">notPackage</error>.<error descr="Unresolved variable or type 'NotClass'">NotClass</error>")]
[Event(type="flash.events.Event")]
[Event(type="flash.events.MouseEvent")]

[Event(name="MyEvent", type="flash.events.ErrorEvent")]
[Event(name="MyStarlingEvent", type="starling.events.TouchEvent")]
public class StarlingEvent extends Sprite{
    public function foo0():void {}
    public function foo1(e:AccelerometerEvent):void {}
    public function foo2(e:ErrorEvent):void {}
    public function foo3(e:flash.events.Event):void {}
    public function foo4(e:flash.events.Event, b:Boolean=true, ...rest):void {}

    public function bar0():void {}
    public function bar1(e:ResizeEvent):void {}
    public function bar2(e:TouchEvent):void {}
    public function bar3(e:starling.events.Event):void {}
    public function bar4(e:starling.events.Event, b:Boolean=true, ...rest):void {}

    public function StarlingEvent() {
        addEventListener("SomeEvent", <weak_warning descr="Callback should have single parameter with event type">foo0</weak_warning>); //red
        addEventListener("SomeEvent", foo1);
        addEventListener("SomeEvent", foo2);
        addEventListener("SomeEvent", foo3);
        addEventListener("SomeEvent", foo4);
        addEventListener("SomeEvent", function<weak_warning descr="Callback should have single parameter with event type">()</weak_warning>:void{});  //red
        addEventListener("SomeEvent", function(e:AccelerometerEvent):void{});
        addEventListener("SomeEvent", function(e:ErrorEvent):void{});
        addEventListener("SomeEvent", function(e:flash.events.Event):void{});
        addEventListener("SomeEvent", function(e:flash.events.Event, b:Boolean=true, ...rest):void{});
        addEventListener("MyEvent", <weak_warning descr="Callback should have single parameter with event type">foo0</weak_warning>); //red
        addEventListener("MyEvent", <weak_warning descr="Callback should have single parameter with flash.events.ErrorEvent type">foo1</weak_warning>); //red
        addEventListener("MyEvent", foo2);
        addEventListener("MyEvent", foo3);
        addEventListener("MyEvent", foo4);
        addEventListener("MyEvent", function<weak_warning descr="Callback should have single parameter with event type">()</weak_warning>:void{});  //red
        addEventListener("MyEvent", function(<weak_warning descr="Callback should have single parameter with flash.events.ErrorEvent type">e:AccelerometerEvent</weak_warning>):void{});  //red
        addEventListener("MyEvent", function(e:ErrorEvent):void{});
        addEventListener("MyEvent", function(e:flash.events.Event):void{});
        addEventListener("MyEvent", function(e:flash.events.Event, b:Boolean=true, ...rest):void{});
        var sprite:Sprite;
        sprite.addEventListener(ErrorEvent.ERROR, <weak_warning descr="Callback should have single parameter with event type">foo0</weak_warning>); //red
        sprite.addEventListener(ErrorEvent.ERROR, <weak_warning descr="Callback should have single parameter with flash.events.ErrorEvent type">foo1</weak_warning>); //red
        sprite.addEventListener(ErrorEvent.ERROR, foo2);
        sprite.addEventListener(ErrorEvent.ERROR, foo3);
        sprite.addEventListener(ErrorEvent.ERROR, foo4);
        sprite.addEventListener(ErrorEvent.ERROR, function<weak_warning descr="Callback should have single parameter with event type">()</weak_warning>:void{}); //red
        sprite.addEventListener(ErrorEvent.ERROR, function(<weak_warning descr="Callback should have single parameter with flash.events.ErrorEvent type">e:AccelerometerEvent</weak_warning>):void{}); //red
        sprite.addEventListener(ErrorEvent.ERROR, function(e:ErrorEvent):void{});
        sprite.addEventListener(ErrorEvent.ERROR, function(e:flash.events.Event):void{});
        sprite.addEventListener(ErrorEvent.ERROR, function(e:flash.events.Event, b:Boolean=true, ...rest):void{});

        addEventListener("SomeStarlingEvent", <weak_warning descr="Callback should have single parameter with event type">bar0</weak_warning>);  //red
        addEventListener("SomeStarlingEvent", bar1);
        addEventListener("SomeStarlingEvent", bar2);
        addEventListener("SomeStarlingEvent", bar3);
        addEventListener("SomeStarlingEvent", bar4);
        addEventListener("SomeStarlingEvent", function<weak_warning descr="Callback should have single parameter with event type">()</weak_warning>:void{});  //red
        addEventListener("SomeStarlingEvent", function(e:ResizeEvent):void{});
        addEventListener("SomeStarlingEvent", function(e:TouchEvent):void{});
        addEventListener("SomeStarlingEvent", function(e:starling.events.Event):void{});
        addEventListener("SomeStarlingEvent", function(e:starling.events.Event, b:Boolean=true, ...rest):void{});
        addEventListener("MyStarlingEvent", <weak_warning descr="Callback should have single parameter with event type">bar0</weak_warning>); //red
        addEventListener("MyStarlingEvent", <weak_warning descr="Callback should have single parameter with starling.events.TouchEvent type">bar1</weak_warning>); //red
        addEventListener("MyStarlingEvent", bar2);
        addEventListener("MyStarlingEvent", bar3);
        addEventListener("MyStarlingEvent", bar4);
        addEventListener("MyStarlingEvent", function<weak_warning descr="Callback should have single parameter with event type">()</weak_warning>:void{}); //red
        addEventListener("MyStarlingEvent", function(<weak_warning descr="Callback should have single parameter with starling.events.TouchEvent type">e:ResizeEvent</weak_warning>):void{});  //red
        addEventListener("MyStarlingEvent", function(e:TouchEvent):void{});
        addEventListener("MyStarlingEvent", function(e:starling.events.Event):void{});
        addEventListener("MyStarlingEvent", function(e:starling.events.Event, b:Boolean=true, ...rest):void{});
        var quad : Quad;
        quad.addEventListener(TouchEvent.TOUCH, <weak_warning descr="Callback should have single parameter with event type">bar0</weak_warning>); //red
        quad.addEventListener(TouchEvent.TOUCH, <weak_warning descr="Callback should have single parameter with starling.events.TouchEvent type">bar1</weak_warning>); //red
        quad.addEventListener(TouchEvent.TOUCH, bar2);
        quad.addEventListener(TouchEvent.TOUCH, bar3);
        quad.addEventListener(TouchEvent.TOUCH, bar4);
        quad.addEventListener(TouchEvent.TOUCH, function<weak_warning descr="Callback should have single parameter with event type">()</weak_warning>:void{}); //red
        quad.addEventListener(TouchEvent.TOUCH, function(<weak_warning descr="Callback should have single parameter with starling.events.TouchEvent type">e:ResizeEvent</weak_warning>):void{}); //red
        quad.addEventListener(TouchEvent.TOUCH, function(e:TouchEvent):void{});
        quad.addEventListener(TouchEvent.TOUCH, function(e:starling.events.Event):void{});
        quad.addEventListener(TouchEvent.TOUCH, function(e:starling.events.Event, b:Boolean=true, ...rest):void{});

        new MyEventDispatcher().addEventListener("foo", function(one:String, two:int, three:Boolean):void {});
    }
}
}

package <error>starling.events</error>{
    public class <error><error>Event</error></error>{}
    public class <error><error>TouchEvent</error></error> extends Event {
        public static const TOUCH:String = "touch";
    }
    public class <error><error>ResizeEvent</error></error> extends Event {}
    public class <error><error>NotEvent</error></error> {}
    public class <error><error>EventDispatcher</error></error> {
        public function addEventListener(type:String, listener:Function):void{}
    }
}

package <error>starling.display</error>{
    import starling.events.EventDispatcher;
    public class <error>Quad</error> extends starling.events.EventDispatcher {
        override public function addEventListener(type:String, listener:Function):void{}
    }
}

package <error>com.acme</error>{
    public class <error>MyEventDispatcher</error> {
        public function addEventListener(type:String, listener:Function):void{}
    }
}
