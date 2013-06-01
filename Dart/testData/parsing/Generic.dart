// Test generic types.
class Box<T> {
  T t;
  getT() { return t; }
  setT(T t) { this.t = t; }
}

class UseBox {
  Box<Box<Box<prefix.Fisk>>> boxIt(Box<Box<prefix.Fisk>> box) {
    return new Box<Box<Box<prefix.Fisk>>>(box);
  }
}