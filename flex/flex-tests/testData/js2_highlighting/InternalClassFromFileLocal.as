package <error>foo</error> {
internal class InternalClassFromFileLocal {

}
}

package <error>bar</error> {
  public class <error>Zz</error> {
    internal function z() {}
  }
}

import foo.<error>InternalClassFromFileLocal</error>;
import bar.Zz;

function ref() {
  var v : <error>InternalClassFromFileLocal</error>;

  var v2 : Zz;
  v2.<error><caret>z</error>();
}