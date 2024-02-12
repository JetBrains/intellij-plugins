package <error descr="Package name 'foo' does not correspond to file path ''">foo</error> {
public class RelaxVisibilityFix2 {
      internal function foo():void {}
   }
}

<error descr="Package should be first statement in file">package</error> <error descr="Package name 'bar' does not correspond to file path ''">bar</error> {
import foo.RelaxVisibilityFix2;

  class <error descr="Class 'Usage' should be defined in file 'Usage.as'">Usage</error> {
    function foo() {
      var v : RelaxVisibilityFix2;
      v.<error descr="Element is not accessible">foo</error>();
    }
  }

}

