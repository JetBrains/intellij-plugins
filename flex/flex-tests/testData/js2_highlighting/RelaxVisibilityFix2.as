package <error>foo</error> {
public class RelaxVisibilityFix2 {
      internal function foo():void {}
   }
}

package <error>bar</error> {
import foo.RelaxVisibilityFix2;

  class <error>Usage</error> {
    function foo() {
      var v : RelaxVisibilityFix2;
      v.<error>foo</error>();
    }
  }

}

