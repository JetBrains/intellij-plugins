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

package org.osmorc.obrimport;

import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;
import org.osmorc.obrimport.springsource.ObrMavenResult;

import java.io.IOException;

/**
 * A query interface for an Open Bundle Repository.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public interface Obr {
  /**
   * @return the name of the Obr, which is displayed to the user.
   */
  public String getDisplayName();

  /**
   * @return true, if the repository supports maven, false otherwise.
   */
  public boolean supportsMaven();

  /**
   * Queries the remote repository and returns information about possibly matching maven dependencies.
   *
   * @param queryString       the query string. This is usually the name of the bundle that should be found.
   * @param progressIndicator a progress indicator, to show progress on the querying action.
   * @return a list of results. If nothing is found an empty array is returned.
   * @throws IOException if the connection to the bundle repository failed.
   */
  public
  @NotNull
  ObrMavenResult[] queryForMavenArtifact(@NotNull String queryString,
                                         @NotNull ProgressIndicator progressIndicator) throws
                                                                                       IOException;

  /**
   * Returns a list of maven repositories where artifacts which are returned by this OBR can be retrieved.
   *
   * @return a list of repositories or an empty array if this obr does not support maven.
   */
  public
  @NotNull
  MavenRepository[] getMavenRepositories();
}
