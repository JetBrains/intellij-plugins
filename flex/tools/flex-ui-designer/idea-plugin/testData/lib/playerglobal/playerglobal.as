package {
public final class String {
  native public function String(value:* = ""):*;
}

public final class Number {
  native public function Number(value:* = 0):*;
}

public final class Date {
  native public function Date(year:* = null, month:* = null, date:* = null, hours:* = null, minutes:* = null, seconds:* = null,
                              ms:* = null):*;
}
}

package flash.display {
public class Sprite {
  native public function set name(value:String):void;
  
  native public function get buttonMode():Boolean;
  native public function set buttonMode(value:Boolean):void;
}
}