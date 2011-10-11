/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.jstestdriver.ActionRunner;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.JsTestDriverModule;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.guice.DebugModule;
import com.google.jstestdriver.html.HtmlDocModule;

/**
 * A builder for IDE's to use. Minimizes the surface area of the API which needs
 * to be maintained on the IDE plugin side. TODO(jeremiele) We should rename
 * this for other API uses. Refactor the crap out of this.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class IDEPluginActionBuilder {

  private final Configuration myResolvedConfiguration;
  private final FlagsImpl myFlags;

  private final LinkedList<Module> modules = new LinkedList<Module>();

  public IDEPluginActionBuilder(Configuration resolvedConfiguration, FlagsImpl flags) {
    myResolvedConfiguration = resolvedConfiguration;
    myFlags = flags;
  }

  public IDEPluginActionBuilder addAllTests() {
    return addTests(Collections.singletonList("all"));
  }

  public IDEPluginActionBuilder addTests(List<String> testCases) {
    myFlags.setTests(concatLists(myFlags.getTests(), testCases));
    return this;
  }

  public IDEPluginActionBuilder dryRunFor(List<String> dryRunFor) {
    myFlags.setDryRunFor(concatLists(myFlags.getDryRunFor(), dryRunFor));
    return this;
  }

  public IDEPluginActionBuilder resetBrowsers() {
    myFlags.setReset(true);
    return this;
  }

  public IDEPluginActionBuilder install(Module module) {
    modules.add(module);
    return this;
  }

  public ActionRunner build() {
    install(new HtmlDocModule());
    install(new DebugModule(true));
    String serverAddress = myResolvedConfiguration.getServer(
        myFlags.getServer(),
        myFlags.getPort(),
        myFlags.getServerHandlerPrefix()
    );
    String captureAddress = myResolvedConfiguration.getCaptureAddress(
        serverAddress,
        myFlags.getCaptureAddress(),
        myFlags.getServerHandlerPrefix()
    );
    Injector injector = Guice.createInjector(
            new CompositeModule(modules),
            new JsTestDriverModule(
              myFlags,
                    myResolvedConfiguration.getFilesList(),
                    serverAddress,
                    captureAddress,
                    System.out,
                    myResolvedConfiguration.getBasePath(),
                    myResolvedConfiguration.getTestSuiteTimeout(),
                    myResolvedConfiguration.getTests(),
                    Collections.<FileInfo>emptyList(),
                    myResolvedConfiguration.getGatewayConfiguration()
            )
    );

    return injector.getInstance(ActionRunner.class);
  }

  private static <E> List<E> concatLists(List<E> list1, List<E> list2) {
    List<E> res = Lists.newArrayList(list1);
    res.addAll(list2);
    return res;
  }

  private static class CompositeModule implements Module {

    private final List<Module> myModules;

    public CompositeModule(List<Module> modules) {
      myModules = modules;
    }

    @Override
    public void configure(Binder binder) {
      for (Module module : myModules) {
        binder.install(module);
      }
    }
  }

}
