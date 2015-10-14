package {
 class Te<caret expected="">st {
     var v : Vector.<Foo>;
     var v2: Vector;
     var v3: Vector.<Vector.<Bar>>;
     var v4: Vector$object;
     var v5: Vector$int;
     var v6: Vector$uint;
 }
}

class Foo {}
class Bar {}