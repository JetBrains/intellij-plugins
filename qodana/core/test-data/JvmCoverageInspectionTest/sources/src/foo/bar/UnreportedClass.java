package foo.bar;

public class UnreportedClass {
  public void unreportedMethod1(int x) {
    if (x % 2 == 0) throw new IllegalStateException("Just writing something")
    if (x + 14 == 533) return;
    for (int i=0; i<42; i++) {
      x += x * i % 13;
    }
  }
}