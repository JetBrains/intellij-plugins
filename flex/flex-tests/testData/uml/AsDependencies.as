package {
 var v__ = new Foo();

    class As<caret expected="">Dependencies {
        var f : Foo;
        var v : Vector.<Bar>;

        var t : Zz = new Zz();
        static var t = new Zz2();

        var we: Vector.<AsDependencies>;

    var f1 = new A8();
    var f2: Vector.<A8>;
    var f3: A8;

    var f5: Vector.<A9>;
    var f6: A9;

    var xx = A10.foo();
    var xx2 = A10.foo();

    function aa2(o: Oo, v123: Vector.<W>): Pp {
    }
    function aa(o: Oo, v123: Vector.<W>): Pp {
            var u: Abc = new Def();
            var u2: Rt;

            var v3 = new UI();
            var v4 = new UI();

            new Inner1();

            v = aa(new Inner2());

            var ttt = new Vector.<A1>();

        var x235: Vector.<A2>;
        var x234: A2;

            var me: AsDependencies;
            new AsDependencies();
            var v : NotShown;

        var x336 = new A3();
        var x335: Vector.<A3>;
        var x334: A3;

        var x436 = new A4();
        var x434: A4;
        var x435: Vector.<A4>;

        var static = A10.foo();
        new Foo(A10.foo());
    }
    include "AsDependencies_2.as"
}

class Foo {}
class Bar {}
class Zz {}
class Zz2{
    public function Zz2() {
    }
}
class Pp {}
class Oo {}
class W {}
class Abc {}
class Def {}
class Rt {}
class UI {}
class A1 {}
class A2 {}
class A3 {}
class A4 {}
class A5 {}
class A6 {}
class A7 {}
class A8 {}
class A9 {}
class NotShown {}
class A10{ public static function foo():void {} }
}

class Inner1 {}
class Inner2 {}
