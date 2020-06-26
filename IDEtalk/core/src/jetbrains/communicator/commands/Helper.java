// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.UIUtil;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class Helper {
  private static final Logger LOG = Logger.getLogger(Helper.class);

  private Helper() {
  }

  public static ProjectsData doGetProjectsData(final Transport transport, final User user, IDEFacade ideFacade) {
    final ProjectsData[] result = new ProjectsData[]{ProjectsData.NULL};
    if (user.isOnline()) {
      try {
        UIUtil.run(ideFacade, CommunicatorStrings.getMsg("ViewFilesCommand.title", user.getDisplayName()),
                   new Runnable() {
              @Override
              public void run() {
                final Semaphore semaphore = new Semaphore(1);
                try {
                  semaphore.acquire();

                  transport.sendXmlMessage(user, new GetProjectsDataXmlMessage(result) {
                    @Override
                    public void processResponse(Element responseElement) {
                      super.processResponse(responseElement);
                      semaphore.release();
                    }
                  });

                  semaphore.tryAcquire(getWaitTimeout(), TimeUnit.MILLISECONDS);

                } catch (InterruptedException ignored) { }
              }
            });
      } catch (CanceledException ignored) {
        //
      }
    }
    return result[0];
  }

  public static void fillVFileContent(final Transport transport, final User user, final VFile vFile, IDEFacade ideFacade) {
    if (user.isOnline()) {
      try {
        UIUtil.run(ideFacade, CommunicatorStrings.getMsg("GetVFileContents.title"),
                   new Runnable() {
              @Override
              public void run() {

                final Semaphore semaphore = new Semaphore(1);
                try {
                  semaphore.acquire();

                  transport.sendXmlMessage(user, new GetVFileContentsXmlMessage(vFile) {
                    @Override
                    public void processResponse(Element responseElement) {
                      super.processResponse(responseElement);
                      semaphore.release();
                    }
                  });

                  semaphore.tryAcquire(getWaitTimeout(), TimeUnit.MILLISECONDS);

                } catch (InterruptedException ignored) {
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
        if (!com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(address)) {
          secondParamForFailMessage += " from " + address;
        }

        ideFacade.showMessage(CommunicatorStrings.FAILED_TITLE,
                              CommunicatorStrings.getMsg("GetVFileContents.fail", vFile.getDisplayName(),
                                                         secondParamForFailMessage));
      }
    }
  }

  private static int getWaitTimeout() {
    return Pico.isUnitTest() ? 2000 : 120 * 1000;
  }
}
