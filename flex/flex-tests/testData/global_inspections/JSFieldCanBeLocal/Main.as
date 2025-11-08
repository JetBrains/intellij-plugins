package {
internal class Main {
  private var a:int = 5;
  private var <warning descr="Field can be converted to a local variable">b</warning>:int = 2;
  private var <warning descr="Field can be converted to a local variable">c</warning>:*;
  private static var d:* = 0;
  private var e:* = 12;
  private var f:* = a * 5;
  private var <warning descr="Field can be converted to a local variable">g</warning>:* = [1, a];
  private var <warning descr="Field can be converted to a local variable">h</warning>:* = 23;
  private const describeCache:Dictionary = new Dictionary();
  [Embed(source='<error descr="Cannot resolve file 'a'">a</error>')]
  private var k:Class;
  private static var instance:Main = new Main();
  private var m:int = 5;
  private var n:int = 7;

  function Main() {
    d = d + 1;
    c = ++d;
    e = 5;
    trace(k.toString());
  }

  public static function getInstance():Main {
    return instance;
  }

  private function foo():void {
      var other:Main = new Main();
      other.m = 6;

      if (false) n = 8;
      trace(n);

    a = 23;
    g = 7;
    f += g;
    f = 55;
  }

  public function bar():void {
    c = 7;
    e >>= 2;
    b = e;
    fff(h);
  }

  public function handleSockedData(messageSize:int, methodNameSize:int):void {
    var clazz:Class = Socket;

    var methodInfo:Dictionary = describeCache[clazz];
    if (methodInfo == null) {
      methodInfo = new Dictionary();
      describeCache[clazz] = methodInfo;
    }
  }

  private var anObject:Object = new Object();
  private var <warning descr="Field can be converted to a local variable">intValue</warning>: int = 4;
  private var initialized:Boolean = false;

  internal function anObjectIsUsed(p:Object):void {
    var x:Object  = anObject;
    p.foo = 1;
    anObjectIsUsed(x);
  }

  internal function foo2():void {
    var x:* = intValue;
    anObjectIsUsed(x);
  }

  internal function init():void {
    if (initialized) return;
    initialized = true;

    A = 5;
    ++cnt;
    t(cnt);
  }

  private var cnt:int = 0;
  /**
   * qqqq
   */
  private static const A: int = 0;

  private const urlRequest:Object = {};

  public function t(o:Object):void {
    urlRequest.url = "d";
    if (false) t(urlRequest);
  }
}
}

class Test {
    private var test1;
    private var test2;
    private var test3;

    public function getTest(key:String):String {
        getTest(test3["ss"]);
        test2["erre"] = 0;
        if (!test1[key]) {
            test1[key] = 123;
        }
        return test1[key];
    }

    [Inject]
    private var k2:Class;

    function foo() {
        trace(k2);
    }

}
