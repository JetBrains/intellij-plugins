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

package jetbrains.communicator.core.impl.dispatcher;



import com.thoughtworks.xstream.XStream;

import jetbrains.communicator.core.dispatcher.LocalMessage;

import jetbrains.communicator.core.impl.users.UserImpl;

import jetbrains.communicator.core.users.User;

import jetbrains.communicator.core.users.UserModel;

import jetbrains.communicator.ide.IDEFacade;

import jetbrains.communicator.util.TimeUtil;

import jetbrains.communicator.util.UIUtil;

import jetbrains.communicator.util.XMLUtil;

import org.apache.log4j.Logger;

import org.jetbrains.annotations.NonNls;

import org.jetbrains.annotations.Nullable;



import java.io.File;

import java.io.FilenameFilter;

import java.text.ParseException;

import java.text.SimpleDateFormat;

import java.util.*;

import java.util.concurrent.Future;

import java.util.concurrent.TimeUnit;



/**

 * @author Kir

 */

class MessageHistory {

  @NonNls

  private static final Logger LOG = Logger.getLogger(MessageHistory.class);



  public static final long SAVE_TIMEOUT = 300;

  @NonNls

  private static final String HISTORY = "history";



  private DayHistory myHistory = new DayHistory();

  @NonNls

  private final XStream myXStream;

  private final UserModel myUserModel;

  private final IDEFacade myFacade;



  private Future<?> myPendingSave;



  @NonNls

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");



  MessageHistory(IDEFacade facade, UserModel userModel) {

    myFacade = facade;

    myUserModel = userModel;



    getHistoryDir().mkdir();



    myXStream = XMLUtil.createXStream();

    setupXStream();



    loadHistorySince(new Date());

  }



  public synchronized void dispose() {

    if (myPendingSave != null) {

      myPendingSave.cancel(false);

      myPendingSave = null;

    }

    myHistory.clear();

  }



  private void setupXStream() {

    myXStream.alias("user", UserImpl.class);

    myXStream.alias("dayHistory", DayHistory.class);

    myXStream.aliasField("historyEntries", DayHistory.class, "myData");



    myXStream.registerConverter(new UserConverter(myUserModel));

  }



  public synchronized void addMessage(User user, LocalMessage message) {

    myHistory.addMessage(user, message);

    triggerSave();

  }



  public synchronized void clear() {

    myHistory.clear();



    deleteAllHistoryFiles();

  }



  private void deleteAllHistoryFiles() {

    File historyDir = getHistoryDir();

    Thread thread = Thread.currentThread();



    for (File file : historyDir.listFiles()) {

      if (thread.isInterrupted()) return;

      file.delete();

    }

  }



  private File getHistoryDir() {

    return new File(myFacade.getCacheDir(), HISTORY);

  }



  public synchronized LocalMessage[] getHistory(User user, @Nullable Date since) {

    loadHistorySince(since);



    List<LocalMessage> list = filterHistoryByDate(user, since);

    return list.toArray(new LocalMessage[list.size()]);

  }



  private List<LocalMessage> filterHistoryByDate(User user, Date since) {

    List<LocalMessage> list = myHistory.readMessages(user);

    if (since != null) {

      List<LocalMessage> result = new ArrayList<LocalMessage>(list.size());

      for (LocalMessage message : list) {

        if (message.getWhen().after(since)) {

          result.add(message);

        }

      }

      list = result;

    }

    return list;

  }



  private void loadHistorySince(@Nullable Date since) {

    if (since != null) {

      since = TimeUtil.getDay(since);

    }



    if (!myHistory.hasHistorySince(since)) {

      if (since == null) {

        doLoadHistorySince(new Date(0));

        myHistory.setHasFullHistory();

      }

      else {

        doLoadHistorySince(since);

      }

    }

  }



  private void doLoadHistorySince(Date since) {

    File historyDir = getHistoryDir();



    String[] historyFiles = historyDir.list(new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return name.endsWith(".xml");

      }

    });



    Arrays.sort(historyFiles);

    for (int i = historyFiles.length - 1; i >= 0; i--) {

      String historyFile = historyFiles[i];

      try {

        Date date = DATE_FORMAT.parse(historyFile);

        if (!date.before(since) && !myHistory.hasHistorySince(date)) {

          DayHistory dayHistory = (DayHistory) XMLUtil.fromXml(myXStream, myFacade.getCacheDir(), getFileNameForDate(date), false);

          if (dayHistory != null) {

            dayHistory.copyTo(myHistory);

          }

        }

      } catch (ParseException e) {

        // ignore file of wrong format

      }

    }



    myHistory.resort();

  }



  private void triggerSave() {

    if (myPendingSave == null) {

      myPendingSave = UIUtil.scheduleDelayed(new Runnable() {

        public void run() {

          saveHistory();

        }

      }, SAVE_TIMEOUT, TimeUnit.MILLISECONDS);

    }

  }



  private synchronized void saveHistory() {

    LOG.debug("Start history save");

    Map<Date, DayHistory> map = getHistory();

    for (Date date : map.keySet()) {

      DayHistory dayHistory = map.get(date);

      try {

        XMLUtil.toXml(myXStream, myFacade.getCacheDir(), getFileNameForDate(date), dayHistory);

      } catch (RuntimeException e) {

        LOG.error("Unable to save dayHistory for " + date + ": " + dayHistory, e);

      }

    }

    LOG.debug("Done history save");

  }



  private synchronized Map<Date, DayHistory> getHistory() {

    Map<Date, DayHistory> result = new HashMap<Date, DayHistory>();



    for (User user : myHistory.keySet()) {

      List<LocalMessage> messages = myHistory.get(user);

      for (LocalMessage message : messages) {

        DayHistory dayHistory = getDayHistoryFor(message, result);



        dayHistory.addMessage(user, message);

      }

    }



    return result;

  }



  private DayHistory getDayHistoryFor(LocalMessage message, Map<Date, DayHistory> result) {

    Date day = TimeUtil.getDay(message.getWhen());

    DayHistory dayHistory = result.get(day);

    if (dayHistory == null) {

      dayHistory = new DayHistory();

      result.put(day, dayHistory);

    }

    return dayHistory;

  }



  @NonNls

  private String getFileNameForDate(Date date) {

    //noinspection HardCodedStringLiteral

    return HISTORY + '/' + DATE_FORMAT.format(date) + ".xml";

  }



  public boolean isEmpty() {

    File historyDir = getHistoryDir();

    return !(historyDir.isDirectory() && historyDir.list().length > 0);

  }

}

