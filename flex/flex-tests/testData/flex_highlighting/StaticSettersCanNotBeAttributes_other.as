package {
public class StaticSettersCanNotBeAttributes_other extends mx.core.Container{
    public static function set staticSetter(s:String) {}
    public function set dynamicSetter(s:String) {}
    public function set direction(value : String) : void {} // overrides [Style(name="direction" ...] in mx.core.Container
}
}