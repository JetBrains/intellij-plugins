package {

class StaticBlock {

  var a:int;
  <error descr="Instance member is not accessible">a</error> = 0;
  <error descr="Instance member is not accessible">a</error> = 0;

  var aa = a;
  static var aaa = <error descr="Instance member is not accessible">a</error>;

  for (var i:int = 0; <error descr="Instance member is not accessible">i</error> < 5; <error descr="Instance member is not accessible">i</error>++) {

  }

  {
    <error descr="Instance member is not accessible">a</error> = 5;
    var b:String;
    var bb = b;
    static var bbb = <error descr="Instance member is not accessible">b</error>;

    <error descr="Instance member is not accessible">b</error> = "abc";

    for (var k:int = 0; <error descr="Instance member is not accessible">k</error> < 5; <error descr="Instance member is not accessible">k</error>++) {

    }
  }

  function foo() {}
  <error descr="Instance member is not accessible">foo</error>();
  <error descr="Instance member is not accessible">foo</error>();

  {
    <error descr="Instance member is not accessible">foo</error>();
    function bar() {}
    <error descr="Instance member is not accessible">bar</error>();
  }
}
}
