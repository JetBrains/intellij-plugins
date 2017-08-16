/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.util;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.mock.MockIDEFacade;

/**
 * @author Kir
 */
public class UIUtilTest extends BaseTestCase {
  private MockIDEFacade myIdeFacade;
  private MyProgressIndicator myIndicator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myIdeFacade = new MockIDEFacade();
    myIndicator = new MyProgressIndicator();
    myIdeFacade.setIndicator(myIndicator);
  }

  public void testRunLongProcessWithProgress() throws Exception {

    final long []start = new long[1];
    Runnable runnable = () -> {
      try {
        start[0] = System.currentTimeMillis();
        Thread.sleep(500);
      } catch (InterruptedException e) {
        fail();
      }
    };

    UIUtil.run(myIdeFacade, "test text", runnable);
    assertEquals(500, System.currentTimeMillis() - start[0], 200);
    assertTrue("Indefinite progress expected ", myIndicator.isIndefinite());
    assertTrue("Should update indicator periodically",
        myIndicator.getCallCount() > 1);
  }



  public void testQuickProcess() throws Exception {
    final boolean[] wasRun = new boolean[1];
    Runnable runnable = () -> wasRun[0] = true;


    UIUtil.run(myIdeFacade, "test text", runnable);
    assertTrue("should have been run", wasRun[0]);
  }

  public void testCancelProcess() {
    Runnable runnable = () -> {
      myIndicator.setCancelled();
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    };

    boolean canceled = false;
    try {
      UIUtil.run(myIdeFacade, "test text", runnable);
    } catch (CanceledException e) {
      canceled = true;
    }

    assertTrue(canceled);
  }

  private static class MyProgressIndicator implements ProgressIndicator {
    double myLastFraction = 0;
    private int myCallCount;
    private boolean myCancelled;
    private boolean myIndefinite;

    @Override
    public void setIndefinite(boolean indefinite) {
      myIndefinite = indefinite;
    }

    @Override
    public void setText(String text) {
      assertEquals("test text", text);
    }

    @Override
    public void setFraction(double x) {
      assertTrue(x >= myLastFraction);
      myLastFraction = x;
      myCallCount ++;
    }

    @Override
    public void checkCanceled() {
      if (myCancelled) throw new RuntimeException("Canceled");
    }

    public double getLastFraction() {
      return myLastFraction;
    }

    public int getCallCount() {
      return myCallCount;
    }

    public void setCancelled() {
      myCancelled = true;
    }

    public boolean isIndefinite() {
      return myIndefinite;
    }
  }
}
