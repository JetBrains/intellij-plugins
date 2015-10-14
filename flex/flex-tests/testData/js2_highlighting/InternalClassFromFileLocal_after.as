package foo {
internal class InternalClassFromFileLocal {

}
}

package bar {
  public class Zz {
    public function z() {}
  }
}

import foo.InternalClassFromFileLocal;
import bar.Zz;

function ref() {
  var v : InternalClassFromFileLocal;

  var v2 : Zz;
  v2.z();
}