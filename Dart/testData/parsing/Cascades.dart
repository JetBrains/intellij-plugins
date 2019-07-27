web_8831() {
  var person = new Person()
    ..name = 'Bob Smith'
    ..address = (new Address()
      ..city = 'Springfield'
      ..zip = '99999');

  var map = new Map()
    ..[1] = 1
    ..[2] = 2;
  aaa.bcc() as CCC ..ddd();
}

class A {
  int x;
  int y;

  A(this.x, this.y);

  A setX(int x) { this.x = x; return this; }

  void setY(int y) { this.y = y; }

  Function swap() {
    a..b = n(c..d);
    int tmp = x;
    x = y;
    y = tmp;
    return this.swap;
  }

  void check(int x, int y) {
    Expect.equals(x, this.x);
    Expect.equals(y, this.y);
  }

  operator[](var i) {
    if (i == 0) return x;
    if (i == 1) return y;
    if (i == "swap") return this.swap;
    return null;
  }

  int operator[]=(int i, int value) {
    if (i == 0) {
      x = value;
    } else if (i == 1) {
      y = value;
    }
  }

  /**
   * A pseudo-keyword.
   */
  import() {
    x++;
  }
}

main() {
  juggler.transition()
    ..onComplete = () {
      juggler.transition()
        ..onComplete = () {
          print('complete');
      };
    };

  A a = new A(1, 2);
  a?..check(1, 2)
   ..swap()..check(2, 1)
   ..x = 4..y = 9..check(4, 9)
   ..setX(10)..check(10, 9)
   ..y = 5..check(10, 5)
   ..swap()()()?..check(5, 10)
   ..check(2, 10)
   ..setX(10).setY(3)..check(10, 3)
   ..setX(7)["swap"]()..check(3, 7)
   ..import()..check(4, 7)
   ?..check(7, 4);
}

var BANG = new A()..x = ["foo"..padLeft(1)];
var BANG2 = new B()..x..y = {"bar"..padLeft(1) : baz..q..w()};