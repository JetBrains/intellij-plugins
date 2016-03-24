function foo():void {
    var x = new Vector.<*>(5);
}

class MyClass {
    public static var BUFFER0:Vector.<*> = Vector.<*>(new Vector.<Object>);
    public static var BUFFER1:Vector.<*> = <error>new Vector.<Object></error>; // doesn't compile with ASC 2.0
    public static var BUFFER2:Vector.<*> = (new Vector.<Object>) as Vector.<*>;
    public static var BUFFER3:Vector.<Object> = new Vector.<Object>;
    public static var BUFFER4:Vector.<*> = BUFFER3 as Vector.<*>;
    public static var BUFFER5:Vector.<*> = <error>BUFFER3</error>; // doesn't compile

    function f() {
        var s : Vector.<String> = new Vector.<String>(1, true);
    }
}