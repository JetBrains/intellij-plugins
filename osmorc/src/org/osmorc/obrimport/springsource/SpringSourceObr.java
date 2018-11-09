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
package org.osmorc.obrimport.springsource;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;
import org.jetbrains.annotations.NotNull;
import org.osmorc.obrimport.MavenRepository;
import org.osmorc.obrimport.Obr;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link Obr} for the springsource bundle repository.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class SpringSourceObr implements Obr {

  @Override
  public String getDisplayName() {
    return "Springsource Enterprise Bundle Repository";
  }

  @Override
  public boolean supportsMaven() {
    return true;
  }

  @Override
  @NotNull
  public ObrMavenResult[] queryForMavenArtifact(@NotNull String queryString, @NotNull ProgressIndicator indicator) throws IOException {
    try {
      // TODO: make this more robust against URL changes.
      List<ObrMavenResult> result = new ArrayList<>();

      indicator.setText("Connecting to " + getDisplayName() + "...");
      // http://www.springsource.com/repository/app/search?query=log4j
      // http://www.springsource.com/repository/app/bundle/version/detail?name=com.springsource.org.apache.log4j&version=1.2.15&searchType=bundlesByName&searchQuery=log4j
      String url = "http://www.springsource.com/repository/app/search?query=" + URLEncoder.encode(queryString, "utf-8");
      String contents = HttpRequests.request(url).readString(indicator);

      indicator.setText("Search completed. Getting results.");
      indicator.checkCanceled();

      // now it's happy regexp-parsing
      // first, get the results fragment, it's a single div
      // <div id="results_fragment"> ... content ... </div> and no divs inbetween.
      // we can do this with cheap indexof
      int start = contents.indexOf("<div id=\"results-fragment\">");
      int end = contents.indexOf("</div>", start);
      contents = contents.substring(start, end);

      // next up, we extract all links in there which leads us to the search results
      // <li>*whitespace*<a href="url">Package Name</a>Package Version*whitespace*</li>
      // on multiple lines. We in fact only care for the URLs.

      Matcher m = RESULT_PARSING_PATTERN.matcher(contents);
      while (m.find()) {
        String detailUrl = m.group(1);
        // cut out the session id.
        detailUrl = detailUrl.replaceAll(";jsessionid.*?\\?", "?");
        // replace &amp; with &
        detailUrl = detailUrl.replace("&amp;", "&");

        String packageName = m.group(2);
        indicator.setText("Loading details for result " + packageName + "...");
        // read the detail page.
        // the detail url always starts with a / so we can just concatenate it
        String detail = HttpRequests.request("http://www.springsource.com" + detailUrl).readString(indicator);
        indicator.checkCanceled();

        indicator.setText("Details retrieved. Getting detail information...");
        // we have the maven dependency in some nice html gibberish in there
        // &lt;groupId&gt;org.apache.log4j&lt;/groupId&gt;
        // &lt;artifactId&gt;com.springsource.org.apache.log4j&lt;/artifactId&gt;
        // &lt;version&gt;1.2.15&lt;/version&gt;

        String groupId;
        String artifactId;
        String version;
        String classifier = null;
        Matcher groupMatcher = GROUP_ID_PATTERN.matcher(detail);
        if (groupMatcher.find()) {
          groupId = groupMatcher.group(1);
        }
        else {
          continue;
        }
        Matcher artifactMatcher = ARTIFACT_ID_PATTERN.matcher(detail);
        if (artifactMatcher.find()) {
          artifactId = artifactMatcher.group(1);
        }
        else {
          continue;
        }
        Matcher versionMatcher = VERSION_PATTERN.matcher(detail);
        if (versionMatcher.find()) {
          version = versionMatcher.group(1);
        }
        else {
          continue;
        }
        Matcher classifierMatcher = CLASSIFIER_PATTERN.matcher(detail);
        if (classifierMatcher.find()) {
          classifier = classifierMatcher.group(1);
        }
        // no else here ,since the classifier is optional

        // finally add the result to the list
        result.add(new ObrMavenResult(groupId, artifactId, version, classifier, this));
      }
      indicator.setText("Done. " + result.size() + " artifacts found.");
      return result.toArray(new ObrMavenResult[0]);
    }
    catch (ProcessCanceledException ignored) {
      indicator.setText("Canceled.");
      return new ObrMavenResult[0];
    }
    finally {
      indicator.setIndeterminate(false);
    }
  }

  @Override
  @NotNull
  public MavenRepository[] getMavenRepositories() {
    return SPRINGSOURCE_REPOS;
  }

  private static final Pattern RESULT_PARSING_PATTERN = Pattern.compile("<a\\s+href=\"([^\"]+)\"[^>]*>([^<]+)");
  private static final Pattern GROUP_ID_PATTERN = Pattern.compile("&lt;groupId&gt;(.*?)&lt;/groupId&gt;");
  private static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("&lt;artifactId&gt;(.*?)&lt;/artifactId&gt;");
  private static final Pattern VERSION_PATTERN = Pattern.compile("&lt;version&gt;(.*?)&lt;/version&gt;");
  private static final Pattern CLASSIFIER_PATTERN = Pattern.compile("&lt;classifier&gt;(.*?)&lt;/classifier&gt;");
  private static final MavenRepository[] SPRINGSOURCE_REPOS = new MavenRepository[]{
    new MavenRepository("repository.springsource.com.release", "SpringSource OBR - Release",
                        "http://repository.springsource.com/maven/bundles/release"),
    new MavenRepository("repository.springsource.com.external", "SpringSource OBR - External",
                        "http://repository.springsource.com/maven/bundles/external")
  };
}
