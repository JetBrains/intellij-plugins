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
package jetbrains.communicator;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.jmock.MockObjectTestCase;
import org.picocontainer.Disposable;

import java.util.Stack;

/**
 * @author Kir
 */
public abstract class LightTestCase extends MockObjectTestCase {
  protected Stack<Disposable> myDisposables = new Stack<>();
  protected static boolean ourShouldFail;

  public LightTestCase(String string) {
    super(string);
  }

  public LightTestCase() {
  }

  public void disposeOnTearDown(Disposable disposable) {
    myDisposables.push(disposable);
  }


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ourShouldFail = false;

    Logger.getRootLogger().addAppender(new AppenderSkeleton() {
      @Override
      protected void append(LoggingEvent loggingEvent) {
        if (loggingEvent.level.isGreaterOrEqual(Priority.ERROR) ) {
          ourShouldFail = true;
        }
      }
      @Override
      public boolean requiresLayout() {
        return false;
      }
      @Override
      public void close() {
      }
    });

  }

  @Override
  protected void tearDown() throws Exception {
    while (!myDisposables.isEmpty()) {
      Disposable disposable = myDisposables.pop();
      //WatchDog watchDog = new WatchDog(disposable.toString());
      disposable.dispose();
      //watchDog.stop();
    }

    super.tearDown();
    if (ourShouldFail) {
      fail("Test marked as failed. See console for exceptions");
    }
  }
}
