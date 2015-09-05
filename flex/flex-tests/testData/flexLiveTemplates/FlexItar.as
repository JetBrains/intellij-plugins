package {
public class FlexItar{

    var j:Boolean;

    private function someFunction():void{
        var i:int;
        var object:Object;

        {var l:int;}

        for (var l:int = 0; ;) {}

        <caret>
    }

    private static function k():void{}
}
}
