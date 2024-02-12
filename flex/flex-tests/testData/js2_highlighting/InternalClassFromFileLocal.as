package <error descr="Package name 'foo' does not correspond to file path ''">foo</error> {
internal class InternalClassFromFileLocal {

}
}

<error descr="Package should be first statement in file">package</error> <error descr="Package name 'bar' does not correspond to file path ''">bar</error> {
  public class <error descr="Class 'Zz' should be defined in file 'Zz.as'">Zz</error> {
    internal function z() {}
  }
}

import foo.<error descr="Element is not accessible">InternalClassFromFileLocal</error>;
import bar.Zz;

function ref() {
  var v : <error descr="Element is not accessible">InternalClassFromFileLocal</error>;

  var v2 : Zz;
  v2.<error descr="Element is not accessible">z</error>();
}