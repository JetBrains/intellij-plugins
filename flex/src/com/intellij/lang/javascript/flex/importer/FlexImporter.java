// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.importer;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Produced from abcdump.as
 */
public final class FlexImporter {
  private static final int ABC_VER = 46 << 16 | 14;
  private static final int ABC_VER2 = 46 << 16 | 15;
  private static final int ABC_VER3 = 46 << 16 | 16;
  private static final int SWF_MAGIC = 67 | 87 << 8 | 83 << 16;
  private static final int SWF_MAGIC2 = 70 | 87 << 8 | 83 << 16;

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.out.print("FlexImporter\nusage:\nFlexImporter <filename>");
    }
    else {
      long started = System.currentTimeMillis();

      for (String file : args) {
        try {
          String result = dumpContentsFromStream(new BufferedInputStream(new FileInputStream(file)), true);

          saveStringAsFile(result, file + ".il");
        }
        finally {
          long total = System.currentTimeMillis() - started;
          System.out.println("File created... " + total + "ms");
        }
      }
    }
  }

  private static void saveStringAsFile(final String result, final String fileName) throws IOException {
    FileUtil.writeToFile(new File(fileName), result);
  }

  public static String dumpContentsFromStream(final InputStream in, boolean _dumpCode) throws IOException {
    final AbstractDumpProcessor abcDumper = new AbcDumper(_dumpCode);
    processFlexByteCode(in, abcDumper);
    return abcDumper.getResult();
  }

  @NonNls
  public static String buildInterfaceFromStream(final InputStream in) {
    try {
      final AbstractDumpProcessor abcDumper = new AS3InterfaceDumper();
      processFlexByteCode(in, abcDumper);
      final String s = abcDumper.getResult();
      //saveStringAsFile(s, File.createTempFile("fleximport", ".as").getPath());
      return s;
    }
    catch (IOException ex) {
      return "/* " + ex.getLocalizedMessage() + " */";
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      return "/* Invalid format */";
    }
  }

  @NonNls
  public static void buildStubsInterfaceFromStream(final InputStream in, final StubElement parent) throws Exception {
    processFlexByteCode(in, new AS3InterfaceStubDumper(parent));
  }

  private static void processFlexByteCode(@NotNull final InputStream in, @NotNull FlexByteCodeInformationProcessor processor) throws IOException {
    ByteBuffer data = new ByteBuffer();
    data.read(in);
    data.setLittleEndian();
    if (data.bytesSize() == 0) return;
    int version = data.readUnsignedInt();

    if (version == ABC_VER || version == ABC_VER2 || version == ABC_VER3) {
      Abc abc = new Abc(data, processor);
      abc.dump("");
    }
    else if ((version & SWF_MAGIC) == SWF_MAGIC) {
      final int delta = 8;
      data.setPosition(delta);
      ByteBuffer udata = new ByteBuffer();
      udata.setLittleEndian();
      data.readBytes(udata, data.bytesSize() - delta);
      int csize = udata.bytesSize();
      udata.uncompress();
      processor.dumpStat("decompressed swf " + csize + " -> " + udata.bytesSize() + "\n");
      udata.setPosition(0);
      new Swf(udata, processor);
    }
    else if ((version & SWF_MAGIC2) == SWF_MAGIC2) {
      data.setPosition(8); // skip header and length
      new Swf(data, processor);
    }
    else {
      processor.hasError("unknown format " + version + ", swf version: " + (version >> 24) + "\n");
    }
  }
}
