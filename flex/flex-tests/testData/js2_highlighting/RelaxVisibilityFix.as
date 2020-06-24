package {
public class <error descr="More than one externally visible symbol defined in file">RelaxVisibilityFix</error> {
      private static var v;
   }

    class <error>Z</error> {
      var z = RelaxVisibilityFix.<error>v</error>;
    }
}

