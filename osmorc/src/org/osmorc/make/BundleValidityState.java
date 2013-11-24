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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.ValidityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.ArrayUtil;
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
  private final String myModuleName;
  private final String[] myFileUrls;
  private final long[] myFileTimestamps;
  private final String myJarUrl;
  private final long myJarLastModificationTime;
  private final boolean myAlwaysRebuildBundleJAR;
  private final long myRulesModifiedTimeStamp;

  /**
   * Ctor. Used by the bundle compiler to create a validity state for a given module.
   *
   * @param module the mode to create the validity state for.
   */
  public BundleValidityState(final Module module) {
    myModuleName = module.getName();
    myJarUrl = BundleCompiler.getJarFileName(module);

    OsmorcFacet osmorcFacet = OsmorcFacet.getInstance(module);
    OsmorcFacetConfiguration configuration = osmorcFacet != null ? osmorcFacet.getConfiguration() : null;
    myAlwaysRebuildBundleJAR = configuration != null && configuration.isAlwaysRebuildBundleJAR();

    if (myAlwaysRebuildBundleJAR) {
      myJarLastModificationTime = 0;
      myFileTimestamps = new long[0];
      myFileUrls = ArrayUtil.EMPTY_STRING_ARRAY;
    }
    else {
      myJarLastModificationTime = (new File(VfsUtilCore.urlToPath(myJarUrl))).lastModified();
      final TObjectLongHashMap<String> url2Timestamps = new TObjectLongHashMap<String>();

      // note down the modification times of all files that will be copied by the Jar builder
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          VirtualFile moduleOutputDir = BundleCompiler.getModuleOutputUrl(module);
          if (moduleOutputDir != null) {
            registerTimestamps(moduleOutputDir, url2Timestamps);
          }
        }
      });

      // add the manifest from the facet settings (it might not be in the source roots)
      // so the build is also triggered when only the manifest has changed
      VirtualFile manifestFile = BundleCompiler.getManifestFile(module);
      if (manifestFile != null) {
        registerTimestamps(manifestFile, url2Timestamps);
      }

      if (osmorcFacet != null) {
        List<Pair<String, String>> jarContents = configuration.getAdditionalJARContents();
        for (Pair<String, String> jarContent : jarContents) {
          VirtualFile file = LocalFileSystem.getInstance().findFileByPath(jarContent.getFirst());
          if (file != null) {
            registerTimestamps(file, url2Timestamps);
          }
        }

        // OSMORC-130 - include BND files into change calculation
        if (configuration.isUseBndFile()) {
          String bndFileLocation = configuration.getBndFileLocation();
          File bndFileIo = BundleCompiler.findFileInModuleContentRoots(bndFileLocation, module);
          if (bndFileIo != null) {
            VirtualFile bndFile = LocalFileSystem.getInstance().findFileByIoFile(bndFileIo);
            if (bndFile != null && bndFile.exists()) {
              registerTimestamps(bndFile, url2Timestamps);
              registerDependencies(bndFile, url2Timestamps);
            }
          }
        }
      }

      // we put the urls and timestamps into two arrays for easy serialization
      myFileUrls = new String[url2Timestamps.size()];
      myFileTimestamps = new long[url2Timestamps.size()];

      url2Timestamps.forEachEntry(new TObjectLongProcedure<String>() {
        public boolean execute(String s, long l) {
          myFileUrls[i] = s;
          myFileTimestamps[i] = l;
          i++;
          return true;
        }

        int i;
      });
    }

    long lastModified = 0;
    ApplicationSettings settings = ServiceManager.getService(ApplicationSettings.class);
    for (LibraryBundlificationRule bundlificationRule : settings.getLibraryBundlificationRules()) {
      lastModified = Math.max(lastModified, bundlificationRule.getLastModified());
    }
    myRulesModifiedTimeStamp = lastModified;
  }

  /**
   * Finds all included files of the given bnd file and registers them as dependencies as well
   *
   * @param bndFile        the bnd file.
   * @param url2Timestamps the map containing the known timestamps
   */
  private static void registerDependencies(VirtualFile bndFile, TObjectLongHashMap<String> url2Timestamps) {
    try {
      String contents = VfsUtilCore.loadText(bndFile);
      Pattern p = Pattern.compile("-include[:=\\s](.+)");
      Matcher m = p.matcher(contents);
      while (m.find()) {
        // get the file list
        String dependentFileLocation = m.group(1);

        String[] listMembers = dependentFileLocation.split(",");
        for (String listMember : listMembers) {
          // trim it, and remove any leading tilde or minus chars, which do not belong to the path name
          listMember = listMember.trim().replaceFirst("^[~-]", "");

          // according to bnd specs all file locations are relative to the including file
          // TODO: we currently do not support replacing bnd's properties or macros in the file locations
          VirtualFile dependentFile = VfsUtilCore.findRelativeFile(listMember, bndFile);
          if (dependentFile != null && dependentFile.exists()) {
            if (url2Timestamps.containsKey(dependentFile.getUrl())) {
              // welcome to the world of circular dependencies
              return;
            }
            else {
              registerTimestamps(dependentFile, url2Timestamps);
              // recursively call for includes inside the included file
              registerDependencies(dependentFile, url2Timestamps);
            }
          }
        }
      }
    }
    catch (IOException e) {
      // bummer...
    }
  }

  public BundleValidityState(DataInput in) throws IOException {
    myAlwaysRebuildBundleJAR = in.readBoolean();
    myModuleName = IOUtil.readString(in);
    int i = in.readInt();
    myFileUrls = new String[i];
    myFileTimestamps = new long[i];
    for (int j = 0; j < i; j++) {
      String s = IOUtil.readString(in);
      long l = in.readLong();
      myFileUrls[j] = s;
      myFileTimestamps[j] = l;
    }

    myJarUrl = IOUtil.readString(in);
    myJarLastModificationTime = in.readLong();
    myRulesModifiedTimeStamp = in.readLong();
  }

  @Override
  public void save(DataOutput out) throws IOException {
    out.writeBoolean(myAlwaysRebuildBundleJAR);
    IOUtil.writeString(myModuleName, out);
    int i = myFileUrls.length;
    out.writeInt(i);
    for (int j = 0; j < i; j++) {
      String s = myFileUrls[j];
      long l = myFileTimestamps[j];
      IOUtil.writeString(s, out);
      out.writeLong(l);
    }

    IOUtil.writeString(myJarUrl, out);
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
    if (myFileUrls.length != other.myFileUrls.length) {
      return false;
    }
    for (int i = 0; i < myFileUrls.length; i++) {
      String s = myFileUrls[i];
      long l = myFileTimestamps[i];
      if (!s.equals(other.myFileUrls[i]) || l != other.myFileTimestamps[i]) {
        return false;
      }
    }
    return Comparing.strEqual(myJarUrl, other.myJarUrl) &&
           myJarLastModificationTime == other.myJarLastModificationTime;
  }

  private static void registerTimestamps(VirtualFile root, final TObjectLongHashMap<String> url2Timestamps) {
    VfsUtilCore.visitChildrenRecursively(root, new VirtualFileVisitor() {
      @Override
      public boolean visitFile(@NotNull VirtualFile file) {
        if (!file.isDirectory()) {
          url2Timestamps.put(file.getUrl(), file.getTimeStamp());
        }
        return super.visitFile(file);
      }
    });
  }
}
