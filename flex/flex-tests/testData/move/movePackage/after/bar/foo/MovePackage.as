package bar.foo {
  import bar.foo.MovePackage;

  class MovePackage {
      [ArrayElementType("bar.foo.MovePackage")]
      var x;

      /**
       * @see bar.foo.MovePackage
       */
  }
}