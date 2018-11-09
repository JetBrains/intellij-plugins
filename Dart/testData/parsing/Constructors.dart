import 'Reference7Helper.dart' as helper;

class ConstructorSyntax {
  ConstructorSyntax(void this.a(), int this.b());
  ConstructorSyntax(x, y) : super(), this.x = x, this.y = y {}
  ConstructorSyntax.a(x, y) : x = x, super(), y = x {}
  ConstructorSyntax.b(x, y) : assert(false), this.x = y, assert(1<2, ""), this.y = x, super() {}
}

class CompileTimeConstructorSyntax {
  const CompileTimeConstructorSyntax();
  const CompileTimeConstructorSyntax() : super(1, 2, 3);
}

class NastyConstructor {

  // NOTE: These examples aren't pretty but they illustrate what's legal.
  A() : x = (() => 42);
  A() : x = (() => 42) {}
  A() : x = foo() {}
  A() : x = ((){}) {}

  A() : x = ((foo()) { }) { }
  A() : x = ((foo()) => 42) { }
  A() : x = ((foo()) { }) => 87;
  A() : x = ((foo()) => 42) => 87;

  A() : x = bar((foo()) { }) { }
  A() : x = bar((foo()) => 42) { }
  A() : x = bar((foo()) { }) => 87;
  A() : x = bar((foo()) => 42) => 87;

  A() : x = [(foo()) { }] { }
  A() : x = [(foo()) => 42] { }
  A() : x = [(foo()) { }] => 87;
  A() : x = [(foo()) => 42] => 87;

  A() : x = {'x':(foo()) { }} { }
  A() : x = {'x':(foo()) => 42} { }
  A() : x = {'x':(foo()) { }} => 87;
  A() : x = {'x':(foo()) => 42} => 87;

  // it's a function
  helper.Bar getBar(){
    return new helper.Bar();
  }
}

class MappedIterable<S, T> {
  MappedIterable._(this._iterable, T this._f(S element));
}

main() {
  const a;
  const a<b> c;
  const a();
  const a.b();
  const a<b>();
  const a<b>.c();
}

class SimpleObject {
  final String aString;
  final int anInt;
  final double aDouble;
  final List<String> aListOfStrings;
  final List<int> aListOfInts;
  final List<double> aListOfDoubles;

  SimpleObject.fromJson(Map<String, dynamic> json)
      : aString = json['aString'],
        anInt = json['anInt'] ?? 0,
        aDouble = json['aDouble'] ?? 0.0,
        aListOfStrings = List<String>.from(json['aListOfStrings']),
        aListOfInts = List<int>.from(json['aListOfInts']),
        aListOfDoubles = List<double>.from(json['aListOfDoubles']);
}
