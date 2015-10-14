package {
import flash.events.Event;

[Event("someEvent1")]
[Event("someEvent2")]
[Style(name="someStyle")]
[Effect("someEffect")]
public class MxmlAttrPreferResolveToEvent_2{
    public function set someEvent1(i:int):void {}
    public var someEvent2:int;
    public function set someStyle(i:int):void {}
    public var someEffect:int;

    [Bindable("textChanged")]
    public function get text():String {
        return "";
    }

    public function set text(t : String):void {
        dispatchEvent(new Event("textChanged"));
    }

}
}
