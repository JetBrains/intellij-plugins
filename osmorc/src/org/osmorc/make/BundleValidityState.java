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
import com.intellij.openapi.compiler.make.BuildInstructionVisitor;
import com.intellij.openapi.compiler.make.BuildRecipe;
import com.intellij.openapi.compiler.make.FileCopyInstruction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.IOUtil;
import gnu.trove.TObjectLongHashMap;
import gnu.trove.TObjectLongProcedure;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The validitystate of a bundle. This tells IntellIJ if files have been changed lately.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class BundleValidityState implements ValidityState
{

  /**
   * Ctor. Used by the bundle compiler to create a validity state for a given module.
   *
   * @param module the mode to create the validity state for.
   */
  public BundleValidityState(final Module module)
  {
    moduleName = module.getName();
    jarUrl = BundleCompiler.getJarFileName(module);

    final OsmorcFacet osmorcFacet = OsmorcFacet.getInstance(module);
    alwaysRebuildBundleJAR = OsmorcFacet.hasOsmorcFacet(module) &&
        osmorcFacet.getConfiguration().isAlwaysRebuildBundleJAR();

    if (alwaysRebuildBundleJAR)
    {
      jarLastModificationTime = 0;
      fileTimestamps = new long[0];
      fileUrls = new String[0];
    }
    else
    {
      jarLastModificationTime = (new File(VfsUtil.urlToPath(jarUrl))).lastModified();
      final TObjectLongHashMap<String> url2Timestamps = new TObjectLongHashMap<String>();

      // note down the modification times of all files that will be copied by the Jar builder
      ApplicationManager.getApplication().runReadAction(new Runnable()
      {
        public void run()
        {
          // get the compilers build recipe
          BuildRecipe buildrecipe = BundleCompiler.getBuildRecipe(module);
          buildrecipe.visitInstructions(new BuildInstructionVisitor()
          {
            // the recipe contains file copy instructions which point to the files that will be copied
            // note down the timestamps of these files
            public boolean visitFileCopyInstruction(FileCopyInstruction filecopyinstruction) throws Exception
            {
              File file = filecopyinstruction.getFile();
              VirtualFile virtualfile =
                  LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(file.getPath()));
              if (virtualfile != null)
              {
                BundleValidityState.registerTimestamps(virtualfile, url2Timestamps);
              }
              return true;
            }
          }, false);
        }
      }
      );

      // add the manifest from the facet settings (it might not be in the source roots)
      // so the build is also triggered when only the manifest has changed
      VirtualFile manifestFile = BundleCompiler.getManifestFile(module);
      if (manifestFile != null)
      {
        registerTimestamps(manifestFile, url2Timestamps);
      }

      if (osmorcFacet != null)
      {
        OsmorcFacetConfiguration configuration = osmorcFacet.getConfiguration();
        List<Pair<String, String>> jarContents = configuration.getAdditionalJARContents();
        for (Pair<String, String> jarContent : jarContents)
        {
          VirtualFile file = LocalFileSystem.getInstance().findFileByPath(jarContent.getFirst());
          if (file != null)
          {
            registerTimestamps(file, url2Timestamps);
          }
        }
        // OSMORC-130 - include BND files into change calculation
        if (configuration.isUseBndFile())
        {
          String bndFileLocation = configuration.getBndFileLocation();
          VirtualFile bndFile = LocalFileSystem.getInstance()
              .findFileByIoFile(BundleCompiler.findFileInModuleContentRoots(bndFileLocation, module));
          if (bndFile != null && bndFile.exists())
          {
            registerTimestamps(bndFile, url2Timestamps);
            registerDependencies(bndFile, url2Timestamps);
          }
        }
      }


      // we put the urls and timestamps into two arrays for easy serialization
      fileUrls = new String[url2Timestamps.size()];
      fileTimestamps = new long[url2Timestamps.size()];

      // functor for copying from map to arrays.
      TObjectLongProcedure<String> tobjectlongprocedure = new TObjectLongProcedure<String>()
      {
        public boolean execute(String s, long l)
        {
          fileUrls[i] = s;
          fileTimestamps[i] = l;
          i++;
          return true;
        }

        int i;

      };
      // and copy
      url2Timestamps.forEachEntry(tobjectlongprocedure);
    }
  }

  /**
   * Finds all included files of the given bnd file and registers them as dependencies as well
   *
   * @param bndFile        the bnd file.
   * @param url2Timestamps the map containing the known timestamps
   */
  private void registerDependencies(VirtualFile bndFile, TObjectLongHashMap<String> url2Timestamps)
  {
    try
    {
      String contents = VfsUtil.loadText(bndFile);
      Pattern p = Pattern.compile("-include (.*)");
      Matcher m = p.matcher(contents);
      while (m.find())
      {
        // get the filename
        String dependentFileLocation = m.group(1);
        // according to bnd specs all file locations are relative to the including file
        VirtualFile dependentFile = VfsUtil.findRelativeFile(dependentFileLocation, bndFile);
        if (dependentFile != null && dependentFile.exists())
        {
          if (url2Timestamps.containsKey(dependentFile.getUrl()))
          {
            // welcome to the world of circular dependencies
            return;
          }
          else
          {
            registerTimestamps(dependentFile, url2Timestamps);
            // recursively call for includes inside the included file
            registerDependencies(dependentFile, url2Timestamps);
          }
        }
      }
    }
    catch (IOException e)
    {
      // bummer...
    }

  }

  /**
   * Deserialization ctor.
   *
   * @param in the input stream which contains the serialzed data
   * @throws IOException in case there is a problem during deserialization
   */
  public BundleValidityState(DataInput in)
      throws IOException
  {
    alwaysRebuildBundleJAR = in.readBoolean();
    moduleName = IOUtil.readString(in);
    int i = in.readInt();
    fileUrls = new String[i];
    fileTimestamps = new long[i];
    for (int j = 0; j < i; j++)
    {
      String s = IOUtil.readString(in);
      long l = in.readLong();
      fileUrls[j] = s;
      fileTimestamps[j] = l;
    }

    jarUrl = IOUtil.readString(in);
    jarLastModificationTime = in.readLong();
  }

  /**
   * Serialization method
   *
   * @param out output stream to serialize to
   * @throws IOException in case a problem occurs during serialization
   */
  public void save(DataOutput out)
      throws IOException
  {
    out.writeBoolean(alwaysRebuildBundleJAR);
    IOUtil.writeString(moduleName, out);
    int i = fileUrls.length;
    out.writeInt(i);
    for (int j = 0; j < i; j++)
    {
      String s = fileUrls[j];
      long l = fileTimestamps[j];
      IOUtil.writeString(s, out);
      out.writeLong(l);
    }

    IOUtil.writeString(jarUrl, out);
    out.writeLong(jarLastModificationTime);
  }


  /**
   * @return the URL where the jar for this bundle will be created at
   */
  public String getOutputJarUrl()
  {
    return jarUrl;
  }

  public boolean equalsTo(ValidityState validitystate)
  {
    if (alwaysRebuildBundleJAR)
    {
      return false;
    }
    if (!(validitystate instanceof BundleValidityState))
    {
      return false;
    }
    BundleValidityState myvalstate = (BundleValidityState) validitystate;
    if (!moduleName.equals(myvalstate.moduleName))
    {
      return false;
    }
    if (fileUrls.length != myvalstate.fileUrls.length)
    {
      return false;
    }
    for (int i = 0; i < fileUrls.length; i++)
    {
      String s = fileUrls[i];
      long l = fileTimestamps[i];
      if (!s.equals(myvalstate.fileUrls[i]) || l != myvalstate.fileTimestamps[i])
      {
        return false;
      }
    }

    return Comparing.strEqual(jarUrl, myvalstate.jarUrl) &&
        jarLastModificationTime == myvalstate.jarLastModificationTime;
  }

  private final String moduleName;
  private final String fileUrls[];
  private final long fileTimestamps[];
  private final String jarUrl;
  private final long jarLastModificationTime;
  private final boolean alwaysRebuildBundleJAR;


  private static void registerTimestamps(VirtualFile virtualfile, TObjectLongHashMap<String> url2Timestamps)
  {
    if (virtualfile.isDirectory())
    {
      VirtualFile avirtualfile1[] = virtualfile.getChildren();
      for (VirtualFile virtualfile1 : avirtualfile1)
      {
        registerTimestamps(virtualfile1, url2Timestamps);
      }
    }
    else
    {
      url2Timestamps.put(virtualfile.getUrl(), virtualfile.getTimeStamp());
    }
  }

}

