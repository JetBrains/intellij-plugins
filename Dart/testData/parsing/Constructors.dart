import 'Reference7Helper.dart' as helper;

class ConstructorSyntax {
  ConstructorSyntax(x, y) : super(), this.x = x, this.y = y {}
  ConstructorSyntax.a(x, y) : x = x, super(), y = x {}
  ConstructorSyntax.b(x, y) : this.x = y, this.y = x, super() {}
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
