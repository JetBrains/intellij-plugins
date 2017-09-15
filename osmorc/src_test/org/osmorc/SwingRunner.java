package org.osmorc;

import com.intellij.testFramework.EdtTestUtil;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;

public class SwingRunner extends BlockJUnit4ClassRunner {
  public SwingRunner(Class<?> klass) throws org.junit.runners.model.InitializationError {
    super(klass);
  }

  @Override
  public void run(final RunNotifier arg0) {
    EdtTestUtil.runInEdtAndWait(() -> super.run(arg0));
  }
}
