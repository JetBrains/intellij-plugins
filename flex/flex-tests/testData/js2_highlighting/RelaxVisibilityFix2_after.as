package foo {
public class RelaxVisibilityFix2 {
      public function foo():void {}
   }
}

package bar {
import foo.RelaxVisibilityFix2;

  class Usage {
    function foo() {
      var v : RelaxVisibilityFix2;
      v.foo();
    }
  }

}

