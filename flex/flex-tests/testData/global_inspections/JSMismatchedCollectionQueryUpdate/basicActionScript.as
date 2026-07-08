class Test {
    private var <warning descr="Contents of collection 'v0' are queried, but never written">v0</warning>: Vector.<String> = new Vector.<String>();
    private var <warning descr="Contents of collection 'a0' are queried, but never written">a0</warning>: Array = new Array();
    private var <warning descr="Contents of collection 'v1' are updated, but never queried">v1</warning>: Vector.<String> = new Vector.<String>();
    private var <warning descr="Contents of collection 'a1' are updated, but never queried">a1</warning>: Array = new Array();
    internal var v2: Vector.<String> = new Vector.<String>();
    internal var a2: Array = new Array();

    public function f1(): void {
        a1[0] = 1;
        v1[0] = "a";
        a2[0] = 2;
        v2[0] = "b";
        var a = v0[0];
        var b = a0[0];
    }

    private function f2(): void {
        var v3: Vector.<String> = Vector.<String>(["Bob", "Larry", "Sarah"]);
        var a3: Array = [1, 2, 3];
        var a: String = v3[0];
        var b: int = a3[0];
    }

    public function f3(): void {
        var <warning descr="Contents of collection 'v4' are updated, but never queried">v4</warning>: Vector.<int> = new Vector.<int>(10);
        var <warning descr="Contents of collection 'a4' are updated, but never queried">a4</warning>: Array = new Array();
        v4[0] = 1;
        a4[0] = 2;
    }

    public function f5(): void {
        var <warning descr="Contents of collection 'v5' are queried, but never written">v5</warning>: Vector.<int> = new Vector.<int>(10);
        var <warning descr="Contents of collection 'a5' are queried, but never written">a5</warning>: Array = new Array();
        var a = v5[0];
        var b = a5[0];
    }

    public function f6(): void {
        var v6: Vector.<int> = new Vector.<int>(10);
        var a6: Array = new Array();
        v6[0] = 3;
        a6[0] = 3;
        for (var i: int in v6) {
            trace(i);
        }
        for (var j: int in a6) {
            trace(i)
        }

        var foo:Array = [function():void {}];
        foo[0]();
    }

    public function f7(container: *): * {
        var c: Array = container[0];
        for each (var i in c) {
            foo(i)
        }
    }

    public function f8(): void {
        var vec:Vector3D = new Vector3D();
        trace(vec.clone());
    }

    public function f9(): void {
        var v:Vector.<ShaderParameterType> = new <ShaderParameterType>[];
    }

    private var _maps:Vector.<LocalEventMap>;
    public function set maps(value:Vector.<LocalEventMap>):void {
        _maps = value;
    }

    public function set contentView(component:Component):void {
        if (_maps != null) {
            _maps[0].dispatcher = _contentView;
        }
    }

  private var v3: Vector.<String> = new Vector.<String>();
  private function foo(v:Vector.<String>) {
    v[0] += "1";
    var v2:Vector.<String> = v3;
    foo(v2);
  }

    private function test():Boolean {
        var array:Array = [];

        var matches:Array = "TestString".match(new RegExp("pattern"));
        return matches && matches.length > 0
    }

    internal var a3: Array = new Array();
    private function test2() {
        for each (var phase :Array in a3) {
            for each (var piece in phase) {
            }
        }

        var ar:Array = [];
        ar.push("");
        var obj:Object = {"array" : ar};
    }
}
