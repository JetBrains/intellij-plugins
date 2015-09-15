class A {
  main() {
    int a = 1;
    int b = 2;
    int c = 3;
    print(test(a, b));
    print(test(b, c));
    print(b * c);
  }

  int test(int a, int b) => a + b;
}