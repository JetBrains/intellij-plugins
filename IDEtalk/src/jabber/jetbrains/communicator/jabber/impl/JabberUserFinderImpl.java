// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber.impl;

import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.jabber.JabberUserFinder;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.XMLUtil;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kir
 */
public class JabberUserFinderImpl implements JabberUserFinder {
  private static final Logger LOG = Logger.getLogger(JabberUserFinderImpl.class);

  private static final String REAL_URL = "http://idetalk.jetbrains.com/registry.php";
  static final String TEST_URL = "http://localhost/ideTalk/registry.php";

  private final IDEFacade myIdeFacade;
  private final String myRegistryUrl;
  private final UserModel myUserModel;

  public JabberUserFinderImpl(IDEFacade ideFacade, UserModel userModel) {
    myIdeFacade = ideFacade;
    myUserModel = userModel;
    myRegistryUrl = Pico.isUnitTest() ? TEST_URL : REAL_URL;
  }

  @Override
  public User[] findUsers(ProgressIndicator progressIndicator) {
    final String currentProjectId = myIdeFacade.getCurrentProjectId();
    List<User> users = new ArrayList<>();
    if (currentProjectId != null) {
      try {
        progressIndicator.setText(StringUtil.getMsg("jabber.findUsers.text"));
        URL url = new URL(myRegistryUrl + "?id=" + currentProjectId);
        InputStream inputStream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String csv = reader.readLine();
        inputStream.close();

        if (csv != null) {
          String[] strings = csv.split(",");
          for (String userId : strings) {
            if (!com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(userId)) {
              users.add(myUserModel.createUser(userId, JabberTransport.CODE));
            }
          }
        }
      } catch (MalformedURLException e) {
        LOG.debug(e.getMessage(), e);
      } catch (IOException e) {
        LOG.debug(e.getMessage(), e);
      }
    }
    return users.toArray(new User[0]);
  }

  @Override
  public void registerForProject(final String jabberUserId) {
    final String currentProjectId = myIdeFacade.getCurrentProjectId();
    if (currentProjectId != null) {
      if (neverAsked(jabberUserId, currentProjectId) &&
          myIdeFacade.askQuestion(StringUtil.getMsg("register.in.public.registry"),
          StringUtil.getMsg("register.in.public.registry.question", jabberUserId)
      )) {
        doRegister(jabberUserId, currentProjectId);
      }
    }
  }

  private boolean neverAsked(String jabberUserId, String currentProjectId) {
    XStream xStream = new XStream();
    Set<String> keys = (Set<String>) XMLUtil.fromXml(xStream, myIdeFacade.getCacheDir(), "registryQuestions.xml", false);
    if (keys == null) {
      keys = new HashSet<>();
    }
    String key = jabberUserId + "_" + currentProjectId;
    boolean result = !keys.contains(key);
    keys.add(key);
    XMLUtil.toXml(xStream, myIdeFacade.getCacheDir(), "registryQuestions.xml", keys);
    return result;
  }

  protected void doRegister(final String jabberUserId, final String currentProjectId) {
    myIdeFacade.runOnPooledThread(() -> {
      try {
        URL url = new URL(myRegistryUrl + "?user=" + jabberUserId + "&id=" + currentProjectId);
        url.getContent();
      } catch (Exception e) {
        LOG.debug(e.getMessage(), e);
      }
    });
  }
}
