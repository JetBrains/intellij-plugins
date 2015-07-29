package foo {
  import foo.MovePackage;

  class MovePackage {
      [ArrayElementType("foo.MovePackage")]
      var x;

      /**
       * @see foo.MovePackage
       */
  }
}