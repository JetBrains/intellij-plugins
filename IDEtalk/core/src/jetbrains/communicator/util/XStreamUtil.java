// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.Pico;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Kir Maximov
 */
public final class XStreamUtil {
  @NonNls
  private static final Logger LOG = Logger.getLogger(XStreamUtil.class);

  private XStreamUtil() {
  }

  public static XStream createXStream() {
    final XStream xStream = new XStream();
    xStream.setClassLoader(XStreamUtil.class.getClassLoader());
    return xStream;
  }

  public static Object fromXml(XStream xStream, File dir, String fileName, boolean reportFailureAsError) {
    return fromXml(xStream, new File(dir, fileName).getAbsolutePath(), reportFailureAsError);
  }

  public static Object fromXml(XStream xStream, String fullFileName, boolean reportFailureAsError) {
    Reader fileReader = null;
    try {
      BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fullFileName));
      fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      return xStream.fromXML(fileReader);
    }
    catch (FileNotFoundException ignored) {
      return null;
    }
    catch (Throwable e) {
      if (reportFailureAsError) {
        LOG.error("Error reading " + fullFileName, e);
      }
      else {
        LOG.info("Error reading " + fullFileName, e);
      }
      return null;
    }
    finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        }
        catch (IOException e) {
          processError(e);
        }
      }
    }
  }

  public static void toXml(XStream xStream, File dir, String fileName, Object object) {
    toXml(xStream, new File(dir, fileName).getAbsolutePath(), object);
  }

  public static void toXml(XStream xStream, String fullFileName, Object object) {
    Writer writer = null;
    try {
      BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fullFileName));
      writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
      xStream.toXML(object, writer);
    } catch (Exception e) {
      processError(e);
    }
    finally{
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          processError(e);
        }
      }
    }
  }

  private static void processError(Exception e) {
    LOG.error(e.getMessage());
    LOG.info(e.getMessage(), e);
    if (Pico.isUnitTest()) {
      assert false: "LOG.error()";
    }
  }
}