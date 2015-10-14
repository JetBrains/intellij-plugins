package foo {
import flash.events.Event;

import mx.rpc.AbstractService;

import mypackage.Alert;

public class To {
    public static function myHandleClick(event:Event):void {
        Alert.show("abc");
    }

    private static function abc(a:AbstractService):void {
    }

    private static function def(a:AbstractService):void {
        abc(null);
    }
}

}