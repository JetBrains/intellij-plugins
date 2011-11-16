package com.intellij.flex.uiDesigner.libraries;

import com.google.common.base.Charsets;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.abc.AbcTranscoder;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.xml.NanoXmlUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.TestOnly;

import java.io.*;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class LibraryUtil {
  public static Pair<CharArrayReader, ByteArrayInputStream> openSwc(final File in) throws IOException {
    return openSwc(new BufferedInputStream(new FileInputStream(in)));
  }

  public static Pair<CharArrayReader, ByteArrayInputStream> openSwc(final InputStream in) throws IOException {
    final MyZipInputStream zipIn = new MyZipInputStream(in);
    MyCharArrayReader catalogReader = null;
    ByteArrayInputStream swfIn = null;
    try {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        if (entry.getName().equals("catalog.xml")) {
          final InputStreamReader reader = new InputStreamReader(zipIn, Charsets.UTF_8);
          try {
            catalogReader = new MyCharArrayReader(FileUtil.adaptiveLoadText(reader));
          }
          finally {
            reader.close();
          }
        }
        else if (entry.getName().equals("library.swf")) {
          swfIn = new ByteArrayInputStream(FileUtil.adaptiveLoadBytes(zipIn));
        }
      }
    }
    finally {
      zipIn.ignoreClose = false;
      zipIn.close();
    }

    assert catalogReader != null;
    assert swfIn != null;
    return new Pair<CharArrayReader, ByteArrayInputStream>(catalogReader, swfIn);
  }

  @TestOnly
  // we cannot use LocalFileSystem, because our test can run outside the idea
  static VirtualFile getTestGlobalLibrary(boolean isPlayer) {
    String name = (isPlayer ? "player" : "air") + "-catalog.xml";
    File file = new File(DebugPathManager.getTestDataPath() + "/lib/playerglobal", name);
    assert file.exists();
    try {
      return new LightVirtualFile(name, XmlFileType.INSTANCE, IOUtil.getCharSequence(file), Charsets.UTF_8, 0);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Set<CharSequence> getDefinitions(VirtualFile file) throws IOException {
    return getDefinitions(IOUtil.getCharArrayReader(file.getInputStream(), (int)file.getLength()));
  }

  public static Set<CharSequence> getDefinitions(Reader reader) throws IOException {
    final THashSet<CharSequence> set = new THashSet<CharSequence>(512, AbcTranscoder.HASHING_STRATEGY);
    NanoXmlUtil.parse(reader, new NanoXmlUtil.IXMLBuilderAdapter() {
      private boolean processingDef;

      @Override
      public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
        if (name.equals("def")) {
          processingDef = true;
        }
      }

      @Override
      public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
        if (name.equals("def")) {
          processingDef = false;
        }
      }

      @Override
      public void addAttribute(String name, String nsPrefix, String nsURI, String value, String type) throws Exception {
        if (processingDef && name.equals("id")) {
          set.add(value);
        }
      }
    });

    return set;
  }

  private static class MyZipInputStream extends ZipInputStream {
    private boolean ignoreClose = true;

    public MyZipInputStream(InputStream in) {
      super(in);
    }

    @Override
    public void close() throws IOException {
      if (!ignoreClose) {
        super.close();
      }
    }
  }

  private static class MyCharArrayReader extends CharArrayReader {
    public MyCharArrayReader(char[] buf) {
      super(buf);
    }

    @Override
    public void close() {
      pos = 0;
    }
  }
}
