// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.dispatcher;

import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.TimeUtil;

import java.util.*;

/**
 * @author Kir
*/
class DayHistory {
  private final Map<User, List<LocalMessage>> myData = new HashMap<>();
  private transient Date myOldestDate;
  private transient boolean myHasFullHistory;

  private List<LocalMessage> getMessages(User user) {
    List<LocalMessage> list = myData.get(user);
    if (list == null) {
      list = new ArrayList<>();
      myData.put(user, list);
    }
    return list;
  }

  public List<LocalMessage> readMessages(User user) {
    return Collections.unmodifiableList(getMessages(user));
  }

  public void addMessage(User user, LocalMessage message) {
    if (myOldestDate == null || myOldestDate.after(message.getWhen())) {
      myOldestDate = TimeUtil.getDay(message.getWhen());
    }
    getMessages(user).add(message);
  }

  public void clear() {
    myOldestDate = null;
    myHasFullHistory = false;
    myData.clear();
  }

  public Iterable<? extends User> keySet() {
    return myData.keySet();
  }

  public List<LocalMessage> get(User user) {
    return myData.get(user);
  }

  public boolean hasHistorySince(Date since) {
    if (myHasFullHistory) return true;
    return myOldestDate != null && since != null && !since.before(myOldestDate) ;
  }

  public void copyTo(DayHistory history) {
    for (User user : keySet()) {
      for (LocalMessage message : readMessages(user)) {
        history.addMessage(user, message);
      }
    }
  }

  public void setHasFullHistory() {
    myHasFullHistory = true;
  }

  public void resort() {
    for (User user : myData.keySet()) {
      List<LocalMessage> list = myData.get(user);
      list.sort(Comparator.comparing(LocalMessage::getWhen));
    }
  }


  public String toString() {
    return myData.toString();
  }
}
