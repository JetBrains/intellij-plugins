package {
public class RelaxVisibilityFix {
      internal static var v;
   }

    class Z {
      var z = RelaxVisibilityFix.v;
    }
}

