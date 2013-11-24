/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.make;

import com.intellij.openapi.compiler.ValidityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileAttributes;
import com.intellij.openapi.util.io.FileSystemUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.newvfs.FileSystemInterface;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.intellij.util.io.IOUtil;
import gnu.trove.TObjectLongHashMap;
import gnu.trove.TObjectLongProcedure;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.frameworkintegration.LibraryBundlificationRule;
import org.osmorc.settings.ApplicationSettings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The validity state of a bundle. This tells IntelliJ if files have been changed lately.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class BundleValidityState implements ValidityState {
  private final boolean myAlwaysRebuildBundleJAR;
  private final String myModuleName;
  private final String[] myFilePaths;
  private final long[] myFileTimestamps;
  private final String myJarPath;
  private final long myJarLastModificationTime;
  private final long myRulesModifiedTimeStamp;

  public BundleValidityState(@NotNull Module module) {
    OsmorcFacet osmorcFacet = OsmorcFacet.getInstance(module);
    OsmorcFacetConfiguration configuration = osmorcFacet != null ? osmorcFacet.getConfiguration() : null;
    myAlwaysRebuildBundleJAR = configuration != null && configuration.isAlwaysRebuildBundleJAR();

    myModuleName = module.getName();
    myJarPath = ObjectUtils.notNull(BundleCompiler.getJarFileName(module), "");

    if (myAlwaysRebuildBundleJAR) {
      myFilePaths = ArrayUtil.EMPTY_STRING_ARRAY;
      myFileTimestamps = ArrayUtil.EMPTY_LONG_ARRAY;
      myJarLastModificationTime = 0;
    }
    else {
      TObjectLongHashMap<String> paths2Timestamps = new TObjectLongHashMap<String>();

      // note down the modification times of all files that will be copied by the Jar builder
      File moduleOutputDir = BundleCompiler.getModuleOutputDir(module);
      if (moduleOutputDir != null) {
        registerTimestamps(moduleOutputDir, paths2Timestamps);
      }

      // add the manifest from the facet settings (it might not be in the source roots)
      // so the build is also triggered when only the manifest has changed
      File manifestFile = BundleCompiler.getManifestFile(module);
      if (manifestFile != null) {
        registerTimestamps(manifestFile, paths2Timestamps);
      }

      if (osmorcFacet != null) {
        List<Pair<String, String>> jarContents = configuration.getAdditionalJARContents();
        for (Pair<String, String> jarContent : jarContents) {
          registerTimestamps(new File(jarContent.getFirst()), paths2Timestamps);
        }

        // OSMORC-130 - include BND files into change calculation
        if (configuration.isUseBndFile()) {
          String bndFileLocation = configuration.getBndFileLocation();
          File bndFile = BundleCompiler.findFileInModuleContentRoots(bndFileLocation, module);
          if (bndFile != null) {
            registerTimestamps(bndFile, paths2Timestamps);
            registerDependencies(bndFile, paths2Timestamps);
          }
        }
      }

      // we put the paths and timestamps into two arrays for easy serialization
      myFilePaths = new String[paths2Timestamps.size()];
      myFileTimestamps = new long[paths2Timestamps.size()];

      paths2Timestamps.forEachEntry(new TObjectLongProcedure<String>() {
        private int i = 0;

        @Override
        public boolean execute(String s, long l) {
          myFilePaths[i] = s;
          myFileTimestamps[i] = l;
          i++;
          return true;
        }
      });

      FileAttributes attributes = FileSystemUtil.getAttributes(myJarPath);
      myJarLastModificationTime = attributes != null ? attributes.lastModified : FileSystemInterface.DEFAULT_TIMESTAMP;
    }

    long lastModified = 0;
    ApplicationSettings settings = ServiceManager.getService(ApplicationSettings.class);
    for (LibraryBundlificationRule bundlificationRule : settings.getLibraryBundlificationRules()) {
      lastModified = Math.max(lastModified, bundlificationRule.getLastModified());
    }
    myRulesModifiedTimeStamp = lastModified;
  }

  private static void registerTimestamps(File root, final TObjectLongHashMap<String> paths2Timestamps) {
    FileUtil.visitFiles(root, new Processor<File>() {
      @Override
      public boolean process(File file) {
        FileAttributes attributes = FileSystemUtil.getAttributes(file);
        if (attributes != null && !attributes.isDirectory()) {
          paths2Timestamps.put(file.getAbsolutePath(), attributes.lastModified);
        }
        return true;
      }
    });
  }

  private static void registerDependencies(File bndFile, TObjectLongHashMap<String> paths2Timestamps) {
    try {
      String contents = FileUtil.loadFile(bndFile);
      Matcher m = Pattern.compile("-include[:=\\s](.+)").matcher(contents);
      while (m.find()) {
        // get the file list
        String dependentFileLocation = m.group(1);

        String[] listMembers = dependentFileLocation.split(",");
        for (String listMember : listMembers) {
          // trim it, and remove any leading tilde or minus chars, which do not belong to the path name
          listMember = listMember.trim().replaceFirst("^[~-]", "");

          // according to bnd specs all file locations are relative to the including file
          // TODO: we currently do not support replacing bnd's properties or macros in the file locations
          File dependentFile = new File(bndFile, listMember);
          if (dependentFile.exists()) {
            if (paths2Timestamps.containsKey(dependentFile.getAbsolutePath())) {
              // welcome to the world of circular dependencies
              return;
            }
            else {
              registerTimestamps(dependentFile, paths2Timestamps);
              // recursively call for includes inside the included file
              registerDependencies(dependentFile, paths2Timestamps);
            }
          }
        }
      }
    }
    catch (IOException e) {
      // bummer...
    }
  }

  public BundleValidityState(@NotNull DataInput in) throws IOException {
    myAlwaysRebuildBundleJAR = in.readBoolean();
    myModuleName = IOUtil.readString(in);

    int i = in.readInt();
    myFilePaths = new String[i];
    myFileTimestamps = new long[i];
    for (int j = 0; j < i; j++) {
      String s = IOUtil.readString(in);
      long l = in.readLong();
      myFilePaths[j] = s;
      myFileTimestamps[j] = l;
    }

    myJarPath = IOUtil.readString(in);
    myJarLastModificationTime = in.readLong();
    myRulesModifiedTimeStamp = in.readLong();
  }

  @Override
  public void save(@NotNull DataOutput out) throws IOException {
    out.writeBoolean(myAlwaysRebuildBundleJAR);
    IOUtil.writeString(myModuleName, out);
    int i = myFilePaths.length;
    out.writeInt(i);
    for (int j = 0; j < i; j++) {
      String s = myFilePaths[j];
      long l = myFileTimestamps[j];
      IOUtil.writeString(s, out);
      out.writeLong(l);
    }

    IOUtil.writeString(myJarPath, out);
    out.writeLong(myJarLastModificationTime);
    out.writeLong(myRulesModifiedTimeStamp);
  }

  @Override
  public boolean equalsTo(ValidityState validityState) {
    if (myAlwaysRebuildBundleJAR) {
      return false;
    }
    if (!(validityState instanceof BundleValidityState)) {
      return false;
    }

    BundleValidityState other = (BundleValidityState)validityState;
    if (myRulesModifiedTimeStamp != other.myRulesModifiedTimeStamp) {
      return false;
    }
    if (!myModuleName.equals(other.myModuleName)) {
      return false;
    }
    if (myFilePaths.length != other.myFilePaths.length) {
      return false;
    }
    for (int i = 0; i < myFilePaths.length; i++) {
      if (!Comparing.strEqual(myFilePaths[i], other.myFilePaths[i]) ||
          myFileTimestamps[i] != other.myFileTimestamps[i]) {
        return false;
      }
    }
    if (!Comparing.strEqual(myJarPath, other.myJarPath)) {
      return false;
    }
    if (myJarLastModificationTime != other.myJarLastModificationTime) {
      return false;
    }

    return true;
  }
}
