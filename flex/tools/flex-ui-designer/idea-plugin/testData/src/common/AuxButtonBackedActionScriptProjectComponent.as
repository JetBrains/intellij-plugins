package {
import spark.components.Button;

public class AuxButtonBackedActionScriptProjectComponent extends Button {
  public var customField:String;

  public function AuxButtonBackedActionScriptProjectComponent() {
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
