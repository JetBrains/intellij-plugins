package {
public class RelaxVisibilityFix {
      private static var v;
   }

    class <error>Z</error> {
      var z = RelaxVisibilityFix.<error>v</error>;
    }
}

