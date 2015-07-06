import 'superLib';

class Foo {
  // comment
  Array<Array<Int>> tmp;

  function foo(int x, z) {
    switch (1) {
      case KeyCode.LEFT:
        action = Action.LEFT;
    }
    new Foo(x, 2);
    int absSum(int a, int b) {
      int value = a + b;
      return value > 0 ? value : -value;
    }
    var arr = ["zero", "one"];
    var y = (x ^ 0x123) << 2;
    for (i in tmp) {
      y = (y ^ 0x123) << 2;
    }
    var k = x % 2 == 1 ? 0 : 1;
    do {
      try {
        if (0 < x && x < 10) {
          while (x != y) {
            x = absSum(x * 3, 5);
          }
          z += 2;
        } else if (x > 20) {
          z = x << 1;
        } else if (false) {
          z = x | 2;
        } else {
          1 + 1;
        }
        switch (k) {
          case 0:
            var s1 = 'zero';
            var s2 = '0';
          case 2:
            var s1 = 'two';
          default:
            var s1 = 'other';
        }
      } catch (e) {
        var message = arr[0];
      }
    } while (x < 0);
  }

  Foo(int n, int m) {
    tmp = new Array<Array<Int>>();
    for (int i; i < 10; ++i)
      tmp.push(new Array<Int>());
  }
}

void main() {
  query("#text")
    ..text = "Click me!"
    ..onClick.listen(reverseText)
    ..onMouseOver.listen(colorText);

  query("#text")
    ..text = "Click me!"
    ..onClick.listen(reverseText)
    ..onMouseOver.listen(colorText);

  if (true)
    print("1");
  else if (true)
    print("2");
  else if (true)
    print("3");
  else if (true)
    print("4");
  else if (true)
    print("5");
  else
    print("4");
}

@deprecated
enum
/**/
Enum {
  /**/
  a,
  b, /**/
  c,
  d
  ,
  e
  ,
  f,
}
