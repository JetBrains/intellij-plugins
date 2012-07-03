/**
 * Created with IntelliJ IDEA.
 * User: Kirill.Safonov
 * Date: 6/7/12
 * Time: 7:19 PM
 * To change this template use File | Settings | File Templates.
 */
package com.intellij.flexunit.runner {
import flash.events.Event;

public class UpdateTextEvent extends Event {

    private var _newText:String;

    public function UpdateTextEvent(type:String, text:String) {
        super(type, false, false);
        _newText = text;
    }

    public function get newText():String {
        return _newText;
    }
}
}
