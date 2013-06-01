class A {
  get hole => ;

  int foo() {
    return hole; // no such field
  }
}