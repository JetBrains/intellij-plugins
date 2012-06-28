package {

import com.intellij.flexunit.launcher.TestLauncherAs;
import com.intellij.flexunit.runner.TestRunnerBase;

/*imports*/

public class ____FlexUnitLauncher extends com.intellij.flexunit.launcher.TestLauncherAs {

    override protected virtual function get dataPort():int {
        return 0/*port*/;
    }

    override protected virtual function get socketPolicyPort():int {
        return 0/*socketPolicyPort*/;
    }

    override protected virtual function get moduleName():String {
        return "/*module*/";
    }

    override protected virtual function addTests(__testRunner:com.intellij.flexunit.runner.TestRunnerBase):void {
        /*code*/
    }

}
}
