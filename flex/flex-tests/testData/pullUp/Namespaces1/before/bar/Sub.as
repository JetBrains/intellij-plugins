package bar {
import foo.Super;

use namespace MyNs;

public class Sub extends Super {
  MyNs function foo():* {
    bar = 0;
  }

  MyNs var bar;

  MyNs var bar2 = foo();
}
}