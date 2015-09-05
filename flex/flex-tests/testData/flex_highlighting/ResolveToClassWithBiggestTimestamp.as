package {

public class ResolveToClassWithBiggestTimestamp {
    public function ResolveToClassWithBiggestTimestamp() {
        LibClass.functionFromLib1();
        LibClass.<error>functionFromLib2</error>();
    }
}
}
