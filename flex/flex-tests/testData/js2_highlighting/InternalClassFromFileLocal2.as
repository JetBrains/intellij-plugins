package <error>foo</error> {
internal class InternalClassFromFileLocal2 {

}
}

import foo.<error>InternalClassFromFileLocal2</error>;

function ref() {
  var v : <error>Inter<caret>nalClassFromFileLocal2</error>;
}