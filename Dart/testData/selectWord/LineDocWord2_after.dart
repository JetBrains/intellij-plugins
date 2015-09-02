library foo;

// some comment

/// this is a comment
main() {
  Iterable b = new Bar().doSomething(2+2==4, 1+1, 2*2.0, "", [1], {1:2}, #*, null);
}

/// another one
biff() {
  /// <selection>Documentation</selection> goes here.
  int e;
  // stuff
  int k; // more line comment
  /* comment
  wrong, really
   */
  g() {
    /// Need to check it
    f() => 0;
    /** And this */
    int x = 3;
  }
}

/**
 * More comment
 */
bill() {
  /* random comment
   * stuff
   */
}
