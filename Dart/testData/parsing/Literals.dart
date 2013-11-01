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
        #<<, #>>,
        #>=, #>, #<=, #<,
        #&, #^, #|,
        #name, #q.n.a.m.e
    );
    x = #~;
    x = #[]; x = #[]=;
    x = #*; x = #/; x = #%; x = #~/;
    x = #+; x = #-;
    x = #<<; x = #>>;
    x = #>=; x = #>; x = #<=; x = #<;
    x = #&; x = #^; x = #|;
    x = #name; x = #q.n.a.m.e;
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
  }
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
  }
}
