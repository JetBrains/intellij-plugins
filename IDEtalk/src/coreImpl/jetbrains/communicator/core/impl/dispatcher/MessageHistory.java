// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.dispatcher;

import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.TimeUtil;
import jetbrains.communicator.util.XStreamUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author Kir
 */
class MessageHistory {
  @NonNls
  private static final Logger LOG = Logger.getLogger(MessageHistory.class);

  public static final long SAVE_TIMEOUT = 300;
  @NonNls
  private static final String HISTORY = "history";

  private final DayHistory myHistory = new DayHistory();
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

    myXStream = XStreamUtil.createXStream();
    setupXStream();

    loadHistorySince(new Date());
  }

  public synchronized void dispose() {
    if (myPendingSave != null) {
      myPendingSave.cancel(true);
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
    return list.toArray(new LocalMessage[0]);
  }

  private List<LocalMessage> filterHistoryByDate(User user, Date since) {
    List<LocalMessage> list = myHistory.readMessages(user);
    if (since != null) {
      List<LocalMessage> result = new ArrayList<>(list.size());
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

    String[] historyFiles = historyDir.list((dir, name) -> name.endsWith(".xml"));

    Arrays.sort(historyFiles);
    for (int i = historyFiles.length - 1; i >= 0; i--) {
      String historyFile = historyFiles[i];
      try {
        Date date = DATE_FORMAT.parse(historyFile);
        if (!date.before(since) && !myHistory.hasHistorySince(date)) {
          DayHistory dayHistory = (DayHistory)XStreamUtil.fromXml(myXStream, myFacade.getCacheDir(), getFileNameForDate(date), false);
          if (dayHistory != null) {
            dayHistory.copyTo(myHistory);
          }
        }
      }
      catch (ParseException e) {
        // ignore file of wrong format
      }
      catch (NumberFormatException e) {
        // ignore file of wrong format
      }
    }

    myHistory.resort();
  }

  private void triggerSave() {
    if (myPendingSave == null) {
      myPendingSave = myFacade.runOnPooledThread(() -> {
        try {
          Thread.sleep(SAVE_TIMEOUT);
        } catch (InterruptedException e) {
          // Ignore here.
        }
        finally {
          saveHistory();
          myPendingSave = null;
        }
      });
    }
  }

  private synchronized void saveHistory() {
    LOG.debug("Start history save");
    Map<Date, DayHistory> map = getHistory();
    for (Date date : map.keySet()) {
      DayHistory dayHistory = map.get(date);
      try {
        XStreamUtil.toXml(myXStream, myFacade.getCacheDir(), getFileNameForDate(date), dayHistory);
      } catch (RuntimeException e) {
        LOG.error("Unable to save dayHistory for " + date + ": " + dayHistory, e);
      }
    }

    LOG.debug("Done history save");
  }

  private synchronized Map<Date, DayHistory> getHistory() {
    Map<Date, DayHistory> result = new HashMap<>();

    for (User user : myHistory.keySet()) {
      List<LocalMessage> messages = myHistory.get(user);
      for (LocalMessage message : messages) {
        DayHistory dayHistory = getDayHistoryFor(message, result);

        dayHistory.addMessage(user, message);
      }
    }

    return result;
  }

  private static DayHistory getDayHistoryFor(LocalMessage message, Map<Date, DayHistory> result) {
    Date day = TimeUtil.getDay(message.getWhen());
    DayHistory dayHistory = result.get(day);
    if (dayHistory == null) {
      dayHistory = new DayHistory();
      result.put(day, dayHistory);
    }
    return dayHistory;
  }

  @NonNls
  private static String getFileNameForDate(Date date) {
    return HISTORY + '/' + DATE_FORMAT.format(date) + ".xml";
  }

  public boolean isEmpty() {
    File historyDir = getHistoryDir();
    return !(historyDir.isDirectory() && historyDir.list().length > 0);
  }
}
