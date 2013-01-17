package {

import com.intellij.flexunit.launcher.TestLauncherAs;
import com.intellij.flexunit.runner.*;

/*imports*/

public class ____FlexUnitLauncher extends com.intellij.flexunit.launcher.TestLauncherAs {

    override protected function createTestRunner(port:int, socketPolicyPort:int, moduleName:String, beforeRunTests:Function):TestRunnerBase {
        return new /*test_runner*/(port, socketPolicyPort, moduleName, beforeRunTests);
    }

    override protected function get dataPort():int {
        return 0/*port*/;
    }

    override protected function get socketPolicyPort():int {
        return 0/*socketPolicyPort*/;
    }

    override protected function get moduleName():String {
        return "/*module*/";
    }

    override protected function addTests(__testRunner:com.intellij.flexunit.runner.TestRunnerBase):void {
        /*code*/
    }

}
}
