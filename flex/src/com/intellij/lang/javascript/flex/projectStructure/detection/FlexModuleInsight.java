package com.intellij.lang.javascript.flex.projectStructure.detection;

import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ModuleInsight;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.xml.NanoXmlUtil;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collection;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FlexModuleInsight extends ModuleInsight {


  public FlexModuleInsight(@Nullable final ProgressIndicator progress,
                           Set<String> existingModuleNames,
                           Set<String> existingProjectLibraryNames) {
    super(progress, existingModuleNames, existingProjectLibraryNames);
  }

  protected ModuleDescriptor createModuleDescriptor(final File moduleContentRoot, final Collection<DetectedProjectRoot> sourceRoots) {
    return new ModuleDescriptor(moduleContentRoot, FlexModuleType.getInstance(), sourceRoots);
  }

  protected boolean isSourceFile(final File file) {
    // TODO we need to scan import statements in MXML files as well
    return FlexProjectStructureDetector.isActionScriptFile(file);
  }

  protected void scanSourceFileForImportedPackages(final CharSequence chars, final Consumer<String> result) {
    Lexer lexer = LanguageParserDefinitions.INSTANCE.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).createLexer(null);
    lexer.start(chars);

    if (FlexProjectStructureDetector.readPackageName(chars, lexer) == null) {
      return;
    }

    while (true) {
      while (lexer.getTokenType() != null && lexer.getTokenType() != JSTokenTypes.IMPORT_KEYWORD) {
        lexer.advance();
      }
      if (lexer.getTokenType() == null) {
        break;
      }
      lexer.advance();
      FlexProjectStructureDetector.skipWhiteSpaceAndComments(lexer);
      String packageName = FlexProjectStructureDetector.readQualifiedName(chars, lexer, true);
      if (packageName != null) {
        String s = packageName.endsWith(".*") ? StringUtil.trimEnd(packageName, ".*") : StringUtil.getPackageName(packageName);
        if (!s.isEmpty()) {
          result.consume(s);
        }
      }
    }
  }

  protected boolean isLibraryFile(final String fileName) {
    return fileName.toLowerCase().endsWith(".swc");
  }

  protected void scanLibraryForDeclaredPackages(final File file, final Consumer<String> result) throws IOException {
    FileInputStream in = null;
    try {
      in = new FileInputStream(file);
      ZipInputStream zip = new ZipInputStream(in);
      ZipEntry e;
      while ((e = zip.getNextEntry()) != null) {
        if (!e.isDirectory() && "catalog.xml".equals(e.getName())) {
          InputStreamReader reader = new InputStreamReader(zip, "UTF-8");
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
                String fqn = value.replace(':', '.');
                String packageName = StringUtil.getPackageName(fqn);
                if (!packageName.isEmpty()) {
                  result.consume(packageName);
                }
              }
            }
          });
        }
      }
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException ignored) {
        }
      }
    }
  }

  public boolean isApplicableRoot(final DetectedProjectRoot root) {
    return root instanceof FlexModuleSourceRoot;
  }
}
