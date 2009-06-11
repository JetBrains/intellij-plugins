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

import jetbrains.communicator.core.commands.CommandManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.nanocontainer.script.xml.XMLContainerBuilder;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;

/**
 * @author Kir Maximov
 */
public class Pico extends DefaultPicoContainer {
  private static final Logger LOG = Logger.getLogger(Pico.class);

  private static Pico ourInstance;
  private static final String PLUGINFILE = "ideTalk-plugin{0}.xml";

  private Pico() {
  }

  private static void initLogger() {
    if (isUnitTest() || !LogManager.getCurrentLoggers().hasMoreElements()) {
      DOMConfigurator.configure(Pico.class.getClassLoader().getResource("log4j.xml"));
    }
  }

  public static Pico getInstance() {
    if (ourInstance == null) {
      if (isUnitTest()) {
        assert false: "Pico container not initialized";
      }
      synchronized (PLUGINFILE) {
        if (ourInstance != null) {
          return ourInstance;
        }

        initLogger();
        ourInstance = new Pico();
        readPicoXml();
      }
    }
    return ourInstance;
  }

  public static void initInTests() {
    setUnitTest(true);
    initLogger();
    ourInstance = new Pico();
  }

  public static void disposeInstance() {
    if (ourInstance != null) {
      try {
        ourInstance.dispose();
      }
      finally{
        ourInstance = null;
      }
    }
  }

  public void dispose() {
    try {
      super.dispose();
    }
    finally {
      LOG.info("Disposed");
    }
  }

  private static void readPicoXml() {
    ClassLoader classLoader = Pico.class.getClassLoader();

    try {
      Enumeration<URL> resources = classLoader.getResources(MessageFormat.format(PLUGINFILE, ""));
      loadResources(resources, classLoader);
      for (int i = 0; i < 20; i++) {
        String filename = MessageFormat.format(PLUGINFILE, new Integer(i));
        loadResources(classLoader.getResources(filename), classLoader);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void loadResources(Enumeration<URL> resources, ClassLoader classLoader) throws IOException {
    while (resources != null && resources.hasMoreElements()) {
      URL url = resources.nextElement();
      InputStream inputStream = url.openStream();
      loadFromStream(inputStream, classLoader);
      inputStream.close();
    }
  }

  private static void loadFromStream(InputStream stream, ClassLoader classLoader) {
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(stream);
      XMLContainerBuilder builder = new XMLContainerBuilder(reader, classLoader);
      builder.populateContainer(ourInstance);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  public static EventBroadcaster getEventBroadcaster() {
    return (EventBroadcaster) getInstance().getComponentInstanceOfType(EventBroadcaster.class);
  }

  public static CommandManager getCommandManager() {
    return (CommandManager) getInstance().getComponentInstanceOfType(CommandManager.class);
  }

  public static boolean isUnitTest() {
    return "yes".equals(System.getProperty(IDEtalkProperties.IS_MAXKIR_TEST));
  }

  public static void setUnitTest(boolean enableUnitTestMode) {
    System.setProperty(IDEtalkProperties.IS_MAXKIR_TEST, enableUnitTestMode ? "yes" : "no");
  }

  public static boolean isLocalTesting() {
    return System.getProperty(IDEtalkProperties.IDEA_IDE_TALK_TESTING) != null;
  }

  public static IDEtalkOptions getOptions() {
    return (IDEtalkOptions) getInstance().getComponentInstanceOfType(IDEtalkOptions.class);
  }
}
