package {
public class NoCreateMethodInLibraryClass {
  private function foo() {
    var v : MyInterface;
    v.<error>b<caret>ar</error>();
  }
}
}
