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

import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.TimeUtil;

import java.util.*;

/**
 * @author Kir
*/
class DayHistory {
  private Map<User, List<LocalMessage>> myData = new HashMap<User, List<LocalMessage>>();
  private transient Date myOldestDate;
  private transient boolean myHasFullHistory;

  private List<LocalMessage> getMessages(User user) {
    List<LocalMessage> list = myData.get(user);
    if (list == null) {
      list = new ArrayList<LocalMessage>();
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
      Collections.sort(list, new Comparator<LocalMessage>() {
        public int compare(LocalMessage o1, LocalMessage o2) {
          return o1.getWhen().compareTo(o2.getWhen());
        }
      });
    }
  }


  public String toString() {
    return myData.toString();
  }
}
