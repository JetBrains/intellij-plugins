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
package org.osmorc.manifest.lang.headerparser;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.text.LevenshteinDistance;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.lang.headerparser.impl.AbstractHeaderParser;
import org.osmorc.manifest.lang.psi.Header;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

import java.util.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class HeaderParserRepository {
  public static HeaderParserRepository getInstance() {
    return ServiceManager.getService(HeaderParserRepository.class);
  }

  public HeaderParser getHeaderParser(@NotNull HeaderValuePart manifestHeaderValue) {
    Header manifestHeader = findHeader(manifestHeaderValue);
    String headerName = manifestHeader != null ? manifestHeader.getName() : null;
    return getHeaderParser(headerName);
  }

  public HeaderParser getHeaderParser(String headerName) {
    if (headerName != null) {
      for (HeaderParserProviderRepository headerParserProviderRepository : getRepositories()) {
        for (HeaderParserProvider headerParserProvider : headerParserProviderRepository.getHeaderParserProviders()) {
          if (headerName.equalsIgnoreCase(headerParserProvider.getHeaderName())) {
            return headerParserProvider.getHeaderParser();
          }
        }
      }
    }

    return AbstractHeaderParser.SIMPLE;
  }

  public Collection<HeaderNameMatch> getMatches(@NotNull String headerName) {
    Set<HeaderNameMatch> result = new TreeSet<HeaderNameMatch>();

    for (HeaderParserProviderRepository headerParserProviderRepository : getRepositories()) {
      for (HeaderParserProvider headerParserProvider : headerParserProviderRepository.getHeaderParserProviders()) {
        if (headerName.equals(headerParserProvider.getHeaderName())) {
          return ContainerUtil.emptyList();
        }
        else {
          int dist = new LevenshteinDistance().calculateMetrics(headerName, headerParserProvider.getHeaderName());
          result.add(new HeaderNameMatch(dist, headerParserProvider));
        }
      }
    }

    return result;
  }

  public Set<String> getAllHeaderNames() {
    Set<String> result = new HashSet<String>();

    for (HeaderParserProviderRepository headerParserProviderRepository : getRepositories()) {
      for (HeaderParserProvider headerParserProvider : headerParserProviderRepository.getHeaderParserProviders()) {
        result.add(headerParserProvider.getHeaderName());
      }
    }

    return result;
  }

  private static HeaderParserProviderRepository[] getRepositories() {
    return Extensions.getExtensions(HeaderParserProviderRepository.EP_NAME);
  }

  @Nullable
  private static Header findHeader(PsiElement element) {
    while (element != null && !(element instanceof Header)) {
      element = element.getParent();
    }
    return (Header)element;
  }
}
