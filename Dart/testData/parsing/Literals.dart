@Foo(bar)
library;

class NumberSyntax {
  f() {
    1; 12; 123;
    1.0; 12.0; 123.0;
    .1; .12; .123;
    1.0; 12.12; 123.123;

    1e1; 12e12; 123e123;
    1e+1; 12e+12; 123e+123;
    1e-1; 12e-12; 123e-123;

    1.0e1; 12.0e12; 123.0e123;
    1.0e+1; 12.0e+12; 123.0e+123;
    1.0e-1; 12.0e-12; 123.0e-123;

    .1e1; .12e12; .123e123;
    .1e+1; .12e+12; .123e+123;
    .1e-1; .12e-12; .123e-123;

    1.0e1; 12.12e12; 123.123e123;
    1.0e+1; 12.12e+12; 123.123e+123;
    1.0e-1; 12.12e-12; 123.123e-123;

    1.1234e+444;

    100__000_000__000_000__000_000;  // one hundred million million millions!
    0x4000_FABC_0000_0000;
    0.000_000_000_01;
    0x00_14_22_01_23_45;  // MAC address
    555_123_4567;  // US Phone number

    // Invalid literals:
    100_;
    0x_00_14_22_01_23_45;
    0._000_000_000_1;
    0_.000_000_000_1;
    100_.1;
    1.2e_3;

    // incorrect, but anyway parsed as numbers
    1e;
    1e+;
    1e-;
    0x;

    o.d();
  }
}

class SymbolLiterals {
  void f() {
    f(
        #~,
        #[], #[]=,
        #*, #/, #%, #~/,
        #+, #-,
        #<<, #>>, #>>>,
        #>=, #>, #<=, #<,
        #&, #^, #|,
        #name, #q.n.a.m.e
    );
    x = #~;
    x = #[]; #[]=;
    x = #*; x = #/; x = #%; x = #~/;
    x = #+; x = #-;
    x = #<<; x = #>>;
    x = #>=; x = #>; x = #<=; #<;
    x = #&; x = #^; x = #|;
    x = #name; #q.n.a.m.e; #void;
  }
}

class ArrayLiteralSyntax {
  void f() {
    var a0 = [];
    var a1 = [12];
    var a2 = [null];
    var a3 = [f(),o2];
    var a4 = [(){return 42;},o2];
    var a5 = [()=>42,o2];
    var a6 = const <int> [ 12, 18 ];
    var a7 = <int> [ 12, 18 ];
    var q0 = "abc"[1];
    var q1 = [[]][index][subindex];
    var q2 = const [1, 2][getIndex()]();
    var q3 = <String> [][1];
  }
  String toString() => const<String>['',[]][index];
}

class MapLiteralSyntax {
  void f() {
    var o0 = {};
    var o1 = {"a":12};
    var o2 = {"a":null,};
    var o3 = {"a":f(),"b":o2};
    var o4 = {"a":(){return 42;},"b":o2,};
    var o4 = {"a":()=>42,"b":o2,};
    var o5 = {"if": 12};
    var o6 = {"foo bar":null, "while":17};
    var o7 = const <String,int> { "a": 12, "b": 18 };
    var o8 = <String,int> { "a": 12, "b": 18 };
    var o9 = {x + y : z + q,
              null: () => 5,
              {1:1}: {((2)):2},
              false: true,
              (a,String b,c){if (false){var a;}} : (d,int e,f){if (false){var a;}},
              1 : 2,
             };
  }
}

/*
context                  expression            runtime type and const-ness
*/
var v1                 = {};                // LinkedHashMap<dynamic, dynamic>
var v2                 = <int, int>{};      // LinkedHashMap<int, int>
var v3                 = <int>{};           // LinkedHashSet<int>
var v4                 = {1: 1};            // LinkedHashMap<int, int>
var v5                 = {1};               // LinkedHashSet<int>

Iterable<int> v6       = {};                // LinkedHashSet<int>
Map<int, int> v7       = {};                // LinkedHashMap<int, int>
Object v8              = {};                // LinkedHashMap<dynamic, dynamic>
Iterable<num> v9       = {1};               // LinkedHashSet<num>
Iterable<num> v10      = <int>{};           // LinkedHashSet<int>
LinkedHashSet<int> v11 = {};                // LinkedHashSet<int>

const v12              = {};                // const Map<dynamic, dynamic>
const v13              = {1};               // const Set<int>
const Set v14          = {};                // const Set<dynamic>
Set v15                = const {4};         // const Set<dynamic>

// Compile-time error, overrides `==`.
// const _             = {Duration(seconds: 1)};
// const _             = {2.3};

var v16                = {1, 2, 3, 2, 1};   // LinkedHashSet<int>
var l16                = v16.toList();        // -> <int>[1, 2, 3]
// Compile-time error, contains equal elements
// const _             = {1, 2, 3, 2, 1};

var l18                = const {1, 2};      // const Set<int>

var a = {a ? b : c : d ? e : f};
var b = {a ? b : c , d ? e : f};
