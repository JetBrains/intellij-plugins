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

package jetbrains.communicator.commands;

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.transport.GetProjectsDataXmlMessage;
import jetbrains.communicator.core.transport.GetVFileContentsXmlMessage;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Helper {
  private static final Logger LOG = Logger.getLogger(Helper.class);

  private Helper() {
  }

  public static ProjectsData doGetProjectsData(final Transport transport, final User user, IDEFacade ideFacade) {
    final ProjectsData[] result = new ProjectsData[]{ProjectsData.NULL};
    if (user.isOnline()) {
      try {
        UIUtil.run(ideFacade, StringUtil.getMsg("ViewFilesCommand.title", user.getDisplayName()),
            new Runnable() {
              public void run() {
                final Semaphore semaphore = new Semaphore(1);
                try {
                  semaphore.acquire();

                  transport.sendXmlMessage(user, new GetProjectsDataXmlMessage(result) {
                    public void processResponse(Element responseElement) {
                      super.processResponse(responseElement);
                      semaphore.release();
                    }
                  });

                  semaphore.tryAcquire(getWaitTimeout(), TimeUnit.MILLISECONDS);

                } catch (InterruptedException e) { }
              }
            });
      } catch (CanceledException e) {
        //
      }
    }
    return result[0];
  }

  public static void fillVFileContent(final Transport transport, final User user, final VFile vFile, IDEFacade ideFacade) {
    if (user.isOnline()) {
      try {
        UIUtil.run(ideFacade, StringUtil.getMsg("GetVFileContents.title"),
            new Runnable() {
              public void run() {

                final Semaphore semaphore = new Semaphore(1);
                try {
                  semaphore.acquire();

                  transport.sendXmlMessage(user, new GetVFileContentsXmlMessage(vFile) {
                    public void processResponse(Element responseElement) {
                      super.processResponse(responseElement);
                      semaphore.release();
                    }
                  });

                  semaphore.tryAcquire(getWaitTimeout(), TimeUnit.MILLISECONDS);

                } catch (InterruptedException e) {
                  // noop
                }
              }
            });
      } catch (CanceledException e) {
        LOG.info(e.getMessage(), e);
      }
      if (vFile.getContents() == null) {
        String secondParamForFailMessage = user.getDisplayName();
        String address = transport.getAddressString(user);
        if (StringUtil.isNotEmpty(address)) {
          secondParamForFailMessage += " from " + address;
        }

        ideFacade.showMessage(StringUtil.FAILED_TITLE,
            StringUtil.getMsg("GetVFileContents.fail", vFile.getDisplayName(),
                secondParamForFailMessage));
      }
    }
  }

  private static int getWaitTimeout() {
    return Pico.isUnitTest() ? 2000 : 120 * 1000;
  }
}
