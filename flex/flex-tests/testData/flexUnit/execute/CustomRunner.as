package {
import org.flexunit.runners.BlockFlexUnit4ClassRunner;
import mx.utils.ObjectUtil;
import mx.controls.Alert;

public class CustomRunner extends BlockFlexUnit4ClassRunner {
    public function CustomRunner(klass:Class) {
        trace("Custom runner created for " + ObjectUtil.toString(klass));
        super(klass);
    }

    override protected function computeTestMethods():Array {
        var methods : Array = testClass.getMetaDataMethods( "Foo" );
        trace("Test methods for class " + ObjectUtil.toString(testClass) + ": " + ObjectUtil.toString(methods));
        return methods;
    }
}
}