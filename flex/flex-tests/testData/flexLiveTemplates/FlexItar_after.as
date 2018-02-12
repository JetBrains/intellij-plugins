package {
public class FlexItar{

    var j:Boolean;

    private function someFunction():void{
        var i:int;
        var object:Object;

        {var l:int;}

        for (var l:int = 0; ;) {}

        for (var l:int = 0; l < object.length; l++) {
            var objectElement:Object = object[l];
            <caret>
        }
    }

    private static function k():void{}
}
}
