package {
import spark.components.Button;

public class AuxActionScriptProjectComponent extends Button {
  public var customField:String;

  public function AuxActionScriptProjectComponent() {
    label = "I am amazing button";
  }

  private var _customProperty:String;
  public function get customProperty():String {
    return _customProperty;
  }

  public function set customProperty(value:String):void {
    _customProperty = value;
  }
}
}
