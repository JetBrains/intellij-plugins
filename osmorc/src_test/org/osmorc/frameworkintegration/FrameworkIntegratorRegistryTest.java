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
package org.osmorc.frameworkintegration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
@RunWith(SwingRunner.class)
public class FrameworkIntegratorRegistryTest {
  public FrameworkIntegratorRegistryTest() {
    TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
      IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder();
    fixture = fixtureBuilder.getFixture();
  }

  @Before
  public void setUp() throws Exception {
    fixture.setUp();
  }

  @After
  public void tearDown() throws Exception {
    fixture.tearDown();
  }

  @Test
  public void testRegistry() {
    FrameworkIntegratorRegistry registry = ServiceManager.getService(FrameworkIntegratorRegistry.class);

    FrameworkIntegrator[] integrators = registry.getFrameworkIntegrators();

    for (int i = 0, integratorsLength = integrators.length; i < integratorsLength; i++) {
      FrameworkIntegrator integrator = integrators[i];
      assertThat(registry.findIntegratorByName(integrator.getDisplayName()), sameInstance(integrator));
    }

    FrameworkInstanceDefinition instanceDefinition = new FrameworkInstanceDefinition();
    instanceDefinition.setFrameworkIntegratorName(integrators[1].getDisplayName());
    assertThat(registry.findIntegratorByInstanceDefinition(instanceDefinition), sameInstance(integrators[1]));
  }

  private final IdeaProjectTestFixture fixture;
}
