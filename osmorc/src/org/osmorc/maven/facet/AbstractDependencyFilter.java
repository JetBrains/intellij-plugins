/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Modified for usage within IntelliJ IDEA.
 */
package org.osmorc.maven.facet;

import aQute.bnd.header.Attrs;
import aQute.bnd.header.OSGiHeader;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Instruction;
import org.jetbrains.idea.maven.model.MavenArtifact;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Apply clause-based filter over given dependencies.
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public abstract class AbstractDependencyFilter {
  private static final Pattern MISSING_KEY_PATTERN = Pattern.compile("(^|,)\\p{Blank}*(!)?\\p{Blank}*([a-zA-Z]+=)");

  /**
   * Dependency artifacts.
   */
  private final Collection<? extends MavenArtifact> m_dependencyArtifacts;


  public AbstractDependencyFilter(Collection<? extends MavenArtifact> dependencyArtifacts) {
    m_dependencyArtifacts = dependencyArtifacts;
  }

  private abstract static class DependencyFilter {
    private final Instruction m_instruction;
    private final String m_defaultValue;


    DependencyFilter(String expression) {
      this(expression, "");
    }


    DependencyFilter(String expression, String defaultValue) {
      m_instruction = new Instruction(expression);
      m_defaultValue = defaultValue;
    }


    public void filter(Collection<MavenArtifact> dependencies) {
      for (Iterator<MavenArtifact> i = dependencies.iterator(); i.hasNext(); ) {
        if (!matches(i.next())) {
          i.remove();
        }
      }
    }


    abstract boolean matches(MavenArtifact dependency);


    boolean matches(String text) {
      boolean result;

      if (null == text) {
        result = m_instruction.matches(m_defaultValue);
      }
      else {
        result = m_instruction.matches(text);
      }

      return m_instruction.isNegated() ? !result : result;
    }
  }


  protected final void processInstructions(String header) throws DependencyEmbedderException {
    Parameters instructions = OSGiHeader.parseHeader(MISSING_KEY_PATTERN.matcher(header).replaceAll("$1$2*;$3"));

    Collection<MavenArtifact> availableDependencies = new LinkedHashSet<>(m_dependencyArtifacts);

    DependencyFilter filter;
    for (Iterator<Map.Entry<String, Attrs>> clauseIterator = instructions.entrySet().iterator(); clauseIterator.hasNext(); ) {
      String inline = "false";

      // always start with a fresh *modifiable* collection for each unique clause
      Collection<MavenArtifact> filteredDependencies = new LinkedHashSet<>(availableDependencies);

      // CLAUSE: REGEXP --> { ATTRIBUTE MAP }
      Map.Entry<String, Attrs> clause = clauseIterator.next();
      String primaryKey = (clause.getKey()).replaceFirst("~+$", "");
      boolean isNegative = primaryKey.startsWith("!");
      if (isNegative) {
        primaryKey = primaryKey.substring(1);
      }

      if (!"*".equals(primaryKey)) {
        filter = new DependencyFilter(primaryKey) {
          @Override
          boolean matches(MavenArtifact dependency) {
            return super.matches(dependency.getArtifactId());
          }
        };
        // FILTER ON MAIN CLAUSE
        filter.filter(filteredDependencies);
      }

      for (Map.Entry<String, String> attr : (clause.getValue()).entrySet()) {
        // ATTRIBUTE: KEY --> REGEXP
        if ("groupId".equals(attr.getKey())) {
          filter = new DependencyFilter(attr.getValue()) {
            @Override
            boolean matches(MavenArtifact dependency) {
              return super.matches(dependency.getGroupId());
            }
          };
        }
        else if ("artifactId".equals(attr.getKey())) {
          filter = new DependencyFilter(attr.getValue()) {
            @Override
            boolean matches(MavenArtifact dependency) {
              return super.matches(dependency.getArtifactId());
            }
          };
        }
        else if ("version".equals(attr.getKey())) {
          filter = new DependencyFilter(attr.getValue()) {
            @Override
            boolean matches(MavenArtifact dependency) {
              try {
                // use the symbolic version if available (ie. 1.0.0-SNAPSHOT)
                return super.matches(dependency.getVersion());
              }
              catch (Exception e) {
                return super.matches(dependency.getVersion());
              }
            }
          };
        }
        else if ("scope".equals(attr.getKey())) {
          filter = new DependencyFilter(attr.getValue(), "compile") {
            @Override
            boolean matches(MavenArtifact dependency) {
              return super.matches(dependency.getScope());
            }
          };
        }
        else if ("type".equals(attr.getKey())) {
          filter = new DependencyFilter(attr.getValue(), "jar") {
            @Override
            boolean matches(MavenArtifact dependency) {
              return super.matches(dependency.getType());
            }
          };
        }
        else if ("classifier".equals(attr.getKey())) {
          filter = new DependencyFilter(attr.getValue()) {
            @Override
            boolean matches(MavenArtifact dependency) {
              return super.matches(dependency.getClassifier());
            }
          };
        }
        else if ("optional".equals(attr.getKey())) {
          filter = new DependencyFilter(attr.getValue(), "false") {
            @Override
            boolean matches(MavenArtifact dependency) {
              return super.matches(String.valueOf(dependency.isOptional()));
            }
          };
        }
        else if ("inline".equals(attr.getKey())) {
          inline = attr.getValue();
          continue;
        }
        else {
          throw new DependencyEmbedderException("Unexpected attribute " + attr.getKey());
        }

        // FILTER ON EACH ATTRIBUTE
        filter.filter(filteredDependencies);
      }

      if (isNegative) {
        // negative clauses reduce the set of available artifacts
        availableDependencies.removeAll(filteredDependencies);
        if (!clauseIterator.hasNext()) {
          // assume there's an implicit * missing at the end
          processDependencies(availableDependencies, inline);
        }
      }
      else {
        // positive clause; doesn't alter the available artifacts
        processDependencies(filteredDependencies, inline);
      }
    }
  }


  protected abstract void processDependencies(Collection<MavenArtifact> dependencies, String inline);
}
