import flash.desktop.NativeApplication;

override public function get colorCorrection():String {
  return NativeApplication.nativeApplication.activeWindow.stage.colorCorrection;
}

override public function set colorCorrection(value:String):void {
}