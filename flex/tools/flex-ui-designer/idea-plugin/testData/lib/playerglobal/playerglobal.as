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

  native public function get fullYear():Number;

  native public function set fullYear(value:Number):*;

  native public function get month():Number;

  native public function set month(value:Number):*;

  native public function get date():Number;

  native public function set date(value:Number):*;

  native public function get hours():Number;

  native public function set hours(value:Number):*;

  native public function get minutes():Number;

  native public function set minutes(value:Number):*;

  native public function get seconds():Number;

  native public function set seconds(value:Number):*;

  native public function get milliseconds():Number;

  native public function set milliseconds(value:Number):*;

  native public function get fullYearUTC():Number;

  native public function set fullYearUTC(value:Number):*;

  native public function get monthUTC():Number;

  native public function set monthUTC(value:Number):*;

  native public function get dateUTC():Number;

  native public function set dateUTC(value:Number):*;

  native public function get hoursUTC():Number;

  native public function set hoursUTC(value:Number):*;

  native public function get minutesUTC():Number;

  native public function set minutesUTC(value:Number):*;

  native public function get secondsUTC():Number;

  native public function set secondsUTC(value:Number):*;

  native public function get millisecondsUTC():Number;

  native public function set millisecondsUTC(value:Number):*;

  native public function get time():Number;

  native public function set time(value:Number):*;

  native public function get timezoneOffset():Number;

  native public function get day():Number;

  native public function get dayUTC():Number;
}
}

package flash.display {
public class Sprite {
  native public function set name(value:String):void;
  
  native public function get buttonMode():Boolean;
  native public function set buttonMode(value:Boolean):void;
}
}