package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.css.CssWriter;
import com.intellij.flex.uiDesigner.io.ByteArrayOutputStreamEx;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.io.StringRegistry.StringWriter;
import com.intellij.javascript.flex.css.FlexStyleIndex;
import com.intellij.javascript.flex.css.FlexStyleIndexInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class LibraryStyleInfoCollector implements Consumer<Library> {
  private final Module module;

  private final PrimitiveAmfOutputStream bytes = new PrimitiveAmfOutputStream(new ByteArrayOutputStreamEx(128));
  private final CssWriter cssWriter;
  private final StringWriter stringWriter;

  public LibraryStyleInfoCollector(CssWriter cssWriter, Module module, StringWriter stringWriter) {
    this.module = module;
    this.cssWriter = cssWriter;
    this.stringWriter = stringWriter;
  }

  private byte[] collectInherited(final VirtualFile jarFile) {
    bytes.allocateShort();

    final VirtualFile libraryFile = Library.getSwfFile(jarFile);
    final FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
    final GlobalSearchScope searchScope = GlobalSearchScope.fileScope(module.getProject(), libraryFile);

    final List<String> dataKeys = new ArrayList<String>(32);
    fileBasedIndex.processAllKeys(FlexStyleIndex.INDEX_ID, new Processor<String>() {
      @Override
      public boolean process(String dataKey) {
        dataKeys.add(dataKey);
        return true;
      }
    }, module.getProject());

    final THashSet<String> uniqueGuard = new THashSet<String>();
    final FileBasedIndex.ValueProcessor<Set<FlexStyleIndexInfo>> processor = new FileBasedIndex.ValueProcessor<Set<FlexStyleIndexInfo>>() {
      @Override
      public boolean process(VirtualFile file, Set<FlexStyleIndexInfo> value) {
        final FlexStyleIndexInfo firstInfo = value.iterator().next();
        if (firstInfo.getInherit().charAt(0) == 'y' && uniqueGuard.add(firstInfo.getAttributeName())) {
          bytes.writeUInt29(stringWriter.getReference(firstInfo.getAttributeName()) - 1);
        }

        // If the property is defined in the library — we it consider that unique for all library — we make an assumption that
        // may not be in a class stylePName be inherited, and another class of the same library not inherited
        return false;
      }
    };

    for (String dataKey : dataKeys) {
      fileBasedIndex.processValues(FlexStyleIndex.INDEX_ID, dataKey, libraryFile, processor, searchScope);
    }

    if (uniqueGuard.size() == 0) {
      return null;
    }
    else {
      bytes.putShort(uniqueGuard.size(), 0);
      return bytes.getByteArrayOut().toByteArray();
    }
  }

  @Override
  public void consume(Library library) {
    try {
      library.inheritingStyles = collectInherited(library.getFile());
    }
    finally {
      bytes.reset();
    }

    VirtualFile defaultsCssVirtualFile = library.getDefaultsCssFile();
    if (defaultsCssVirtualFile != null) {
      library.defaultsStyle = cssWriter.write(defaultsCssVirtualFile, module);
    }
  }
}