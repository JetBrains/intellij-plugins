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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.headerparser.impl.SimpleHeaderParser;
import org.osmorc.manifest.lang.psi.ManifestHeader;
import org.osmorc.manifest.lang.psi.ManifestHeaderValue;

import java.util.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class HeaderParserRepository
{
  public HeaderParserRepository(SimpleHeaderParser simpleHeaderParser)
  {
    _simpleHeaderParser = simpleHeaderParser;
    _headerParserProviderRepositories = Extensions
        .getExtensions(new ExtensionPointName<HeaderParserProviderRepository>("Osmorc.headerParserProviderRepository"));
  }

  public HeaderParser getHeaderParser(@NotNull ManifestHeaderValue manifestHeaderValue)
  {
    ManifestHeader manifestHeader = findHeader(manifestHeaderValue);
    String headerName = manifestHeader != null ? manifestHeader.getName() : null;
    return getHeaderParser(headerName);
  }

  public HeaderParser getHeaderParser(@NotNull ManifestHeader manifestHeader)
  {
    return getHeaderParser(manifestHeader.getName());
  }

  public HeaderParser getHeaderParser(String headerName)
  {
    if (headerName != null)
    {
      for (HeaderParserProviderRepository headerParserProviderRepository : _headerParserProviderRepositories)
      {
        for (HeaderParserProvider headerParserProvider : headerParserProviderRepository.getHeaderParserProviders())
        {
          if (headerName.equalsIgnoreCase(headerParserProvider.getHeaderName()))
          {
            return headerParserProvider.getHeaderParser();
          }
        }
      }
    }
    return _simpleHeaderParser;
  }

  public Collection<HeaderNameMatch> getMatches(@NotNull String headerName)
  {
    SortedSet<HeaderNameMatch> result = new TreeSet<HeaderNameMatch>();

    for (HeaderParserProviderRepository headerParserProviderRepository : _headerParserProviderRepositories)
    {
      for (HeaderParserProvider headerParserProvider : headerParserProviderRepository.getHeaderParserProviders())
      {
        if (headerName.equals(headerParserProvider.getHeaderName()))
        {
          return new ArrayList<HeaderNameMatch>();
        }
        else
        {
          result.add(
              new HeaderNameMatch(StringUtils.getLevenshteinDistance(headerName, headerParserProvider.getHeaderName()),
                  headerParserProvider));
        }
      }
    }

    return result;
  }

  public Set<String> getAllHeaderNames()
  {
    Set<String> result = new HashSet<String>();

    for (HeaderParserProviderRepository headerParserProviderRepository : _headerParserProviderRepositories)
    {
      Collection<HeaderParserProvider> headerParserProviders =
          headerParserProviderRepository.getHeaderParserProviders();
      for (HeaderParserProvider headerParserProvider : headerParserProviders)
      {
        result.add(headerParserProvider.getHeaderName());
      }
    }

    return result;
  }

  private ManifestHeader findHeader(PsiElement element)
  {
    if (element == null)
    {
      return null;
    }
    else if (element instanceof ManifestHeader)
    {
      return (ManifestHeader) element;
    }
    return findHeader(element.getParent());
  }


  private HeaderParserProviderRepository[] _headerParserProviderRepositories;
  private SimpleHeaderParser _simpleHeaderParser;
}
