package {
public class SourceClass implements INotMoved, IMoved {
    function movedMethod() {
    }

    function notMovedMethod() {
    }

    public function methodFromIMoved() {
    }

    public function methodFromIMovedAndINotMoved() {
    }

    public function methodFromINotMoved() {
    }

    function get movedProp():int {
        return 0;
    }

    function set movedProp(value:int):void {
    }

    public function get notMovedProp():int {
        return 0;
    }

    public function set notMovedProp(value:int):void {
    }
}
}