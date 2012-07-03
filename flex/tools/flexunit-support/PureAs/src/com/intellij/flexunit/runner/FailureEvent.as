/**
 * Created with IntelliJ IDEA.
 * User: Kirill.Safonov
 * Date: 6/7/12
 * Time: 8:23 PM
 * To change this template use File | Settings | File Templates.
 */
package com.intellij.flexunit.runner {
import flash.events.Event;

public class FailureEvent extends Event {
    public static const TYPE:String = "failure";
    private var _message:String;
    private var _callback:Function;
    private var _title:String;

    public function FailureEvent(message:String, title:String, callback:Function) {
        super(TYPE, false, false);
        _message = message;
        _title = title;
        _callback = callback;
    }

    public function get message():String {
        return _message;
    }

    public function get title():String {
        return _title;
    }

    public function get callback():Function {
        return _callback;
    }
}
}
