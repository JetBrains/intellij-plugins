abstract class I {
  get foo;
  set foo(x);
}
class A implements I {

  set foo(x) {
  }

  get foo => 0;
}

main() {
  new A();
}