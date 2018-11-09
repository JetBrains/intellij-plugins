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
package jetbrains.communicator.core;

import com.intellij.openapi.util.io.FileUtil;
import jetbrains.communicator.core.commands.CommandManager;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.CommandManagerImpl;
import jetbrains.communicator.core.impl.EventBroadcasterImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.mock.MockUser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kir
 */
public class TestFactory {
  private static final Set<File> ourFiles = new HashSet<>();

  private TestFactory() {
  }

  public static UserModel createUserListWithUsers(BaseTestCase test) {

    UserModelImpl result = new UserModelImpl(test.getBroadcaster());
    test.disposeOnTearDown(result);
    result.addUser(new MockUser("aaa", "group1"));
    result.addUser(new MockUser("ccc", "group1"));

    result.addUser(new MockUser("zzz", "group2", true));
    result.addUser(new MockUser("aaaa", "group2"));
    result.addUser(new MockUser("bbb", "group2"));

    Pico.getInstance().registerComponentInstance(UserModel.class, result);

    return result;
  }

  public static void init() {
    Pico.getInstance().registerComponentImplementation(EventBroadcaster.class, EventBroadcasterImpl.class);
    Pico.getInstance().registerComponentImplementation(CommandManager.class, CommandManagerImpl.class);
    Pico.getInstance().registerComponentImplementation(IDEtalkOptions.class, MockOptions.class);
    assert Pico.getEventBroadcaster() != null;
  }

  public static File createDir(Class<?> aClass) throws IOException {
    return createDir(aClass.getSimpleName());
  }

  public static File createDir(String name) throws IOException {
    File dir = FileUtil.createTempFile(name, "");
    dir.delete();
    dir.mkdir();
    addFileToDelete(dir);
    return dir;
  }

  public static void addFileToDelete(File dir) {
    ourFiles.add(dir);
  }

  public static void deleteFiles() {
    for (final File fileToDelete : ourFiles) {
      boolean deleted = FileUtil.delete(fileToDelete);
      assert deleted : "Can't delete "+fileToDelete;
    }
  }
}
