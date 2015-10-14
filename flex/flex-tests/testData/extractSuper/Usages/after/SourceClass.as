package {
public class SourceClass implements INotMoved, ISuper {
    public function movedMethod() {
    }

    function notMovedMethod() {
    }

    public function methodFromIMoved() {
    }

    public function methodFromIMovedAndINotMoved() {
    }

    public function methodFromINotMoved() {
    }

    public function get movedProp():int {
        return 0;
    }

    public function set movedProp(value:int):void {
    }

    public function get notMovedProp():int {
        return 0;
    }

    public function set notMovedProp(value:int):void {
    }
}
}