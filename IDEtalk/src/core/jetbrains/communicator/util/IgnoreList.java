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

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.communicator.ide.IDEFacade;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class IgnoreList {
  @NonNls
  private static final Logger LOG = Logger.getLogger(IgnoreList.class);
  @NonNls
  private static final String JABBER_IGNORE_TXT = "jabber.ignore.txt";
  private final IDEFacade myIdeFacade;

  private final List<String> myIgnored = new ArrayList<>();
  private long myWhenIgnoredListUpdated;

  public IgnoreList(IDEFacade ideFacade) {
    myIdeFacade = ideFacade;
  }

  public boolean isIgnored(String name) {
    return isInIgnoreList(name);
  }

  private boolean isInIgnoreList(String from) {
    File ignoreList = new File(myIdeFacade.getConfigDir(), JABBER_IGNORE_TXT);
    if (ignoreList.isFile()) {
      long changed = ignoreList.lastModified();
      if (changed != myWhenIgnoredListUpdated) {
        myWhenIgnoredListUpdated = changed;
        fillIgnoreList(ignoreList);
      }
      for (String ignorePattern : myIgnored) {
        if (StringUtil.toLowerCase(from).contains(ignorePattern)) {
          return true;
        }
      }
    }
    return false;
  }

  private void fillIgnoreList(File ignoreList) {
    try {
      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(ignoreList))) {
        myIgnored.clear();
        String line = bufferedReader.readLine();
        while (line != null) {
          if (!StringUtil.isEmptyOrSpaces(line)) {
            myIgnored.add(StringUtil.toLowerCase(line));
          }
          line = bufferedReader.readLine();
        }
      }
    } catch (IOException e) {
      LOG.warn(e.getMessage());
      LOG.info(e.getMessage(), e);
    }
  }


}
