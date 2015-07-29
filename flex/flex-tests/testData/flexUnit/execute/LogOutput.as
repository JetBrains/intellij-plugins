package {

import mx.logging.Log;
import flexunit.framework.TestCase;

public class LogOutput extends TestCase {

    public function test1() {
        Log.getLogger("f").fatal("Fatal_message");
        Log.getLogger("e").error("Error_message");
        Log.getLogger("w").warn("Warning_message");
        Log.getLogger("i").info("Info_message");
        Log.getLogger("d").debug("Debug_message");
    }
}
}

<testResults status="Completed">
  <suite name="LogOutput" status="Completed">
      <test name="test1" status="Completed"/>
  </suite>
</testResults>