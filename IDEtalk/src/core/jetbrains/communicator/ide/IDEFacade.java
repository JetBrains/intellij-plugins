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

package jetbrains.communicator.ide;

import jetbrains.communicator.commands.FindUsersCommand;
import jetbrains.communicator.commands.SendMessageInvoker;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * @author Kir Maximov
 */
public interface IDEFacade {

  /** Ask user to choose contacts for the contact list.
   * @return list of users chosen to be added */
  FindUsersCommand.UsersInfo chooseUsersToBeAdded(List<User> foundNewUsers, String[] availableGroups);

  /** Ask user to enter message and send it to selected User(s) via SendMessageInvoker */
  void invokeSendMessage(User[] allUsers, User[] defaultTargetUsers, String message, SendMessageInvoker sendMessageInvoker);

  /** Show search results, returned by @{link SearchHistoryCommand} */
  void showSearchHistoryResults(List<LocalMessage> foundMessages, User user);

  /** Get ProjectsData, including open projects, files etc. See ProjectsData class for more info */
  ProjectsData getProjectsData();

  /** Get List of opened projects */
  String[] getProjects();

  /** Show user's project data in a tree-like structure */
  void showUserFiles(User user, ProjectsData data);

  boolean hasFile(VFile file);
  void open(VFile file);
  void fillFileContents(VFile vFile);
  void showDiffFor(User remoteUser, VFile vFile, String compareWith);
  Change[] getDiff(Object[] src, Object[]dest);


  //=============== Generic methods =====================
  /** Ask user to enter some text in line. Returns null on cancel */
  String getMessageLine(String labelText, String titleText);
  /** Ask user to enter some text in text area. Returns null on cancel */
  String getMessage(String labelText, String titleText, String optionalOKButtonText);

  void showMessage(String title, String message);
  boolean askQuestion(String title, String question);
  File getCacheDir();
  File getConfigDir();

  void runLongProcess(String processTitle, Process process) throws CanceledException;

  /** Create message for local delivery, based on incoming event.
   * For instance, the message for showing stacktrace in IDE.
   * These messages go to the history, and serialized as XML
   * If no message can be created, simply returns null */
  @Nullable
  LocalMessage createLocalMessageForIncomingEvent(TransportEvent event);

  /**
   * Create message for outgoung event, like sending a message or code pointer to some user.
   * These messages go to the history, and serialized as XML
   * If no message can be created, simply returns null
   */
  @Nullable
  LocalMessage createLocalMessageForOutgoingEvent(OwnMessageEvent event);

  /** Get unique ID for currently active project, if available. If Id was generated once, it is saved in
   * project file so it is not regenerated again.*/
  String getCurrentProjectId();

  interface Process {
    void run(ProgressIndicator indicator);
  }
}
