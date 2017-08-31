// Test generic types.
class Box<T> {
  T t;
  getT() { return t; }
  setT(T t) { this.t = t; }
}

class UseBox {
  Box<Box<Box<prefix.Fisk>>> boxIt(Box<Box<prefix.Fisk>> box) {
    foo(a<b, c>(d)); // generic params
    foo(a<b, 2>(d)); // comparison operators

    // func expr call
    <K extends Object, V>(){}();

    int local1<T, E extends Object>(){};
    local2<T>(){};

    return new Box<Box<Box<prefix.Fisk>>>(box);
  }

  foo<@deprecated T1 extends Map<int, List<bool>>>(){}
  void bar(param<T>()){}
}

int global1<T, E extends Object>(){}
global2<T>(){}

foo<void> bar(a<void> b) {}