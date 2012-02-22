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
package org.osmorc.run.managingbundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * OSGi Bundle which is used by the plugin to list information about the container and control the deployment of
 * Bundles.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id$
 */
public class ManagingBundleActivator implements BundleActivator {

  public void start(BundleContext context) throws Exception {
    // install a security manager in case none is installed
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }
    try {
      String name = ManagingBundle.class.getName();
      // instanciate the managing bundle
      ManagingBundle implementation = new ManagingBundleImpl(context);
      // create a stub
      ManagingBundle stub = (ManagingBundle)UnicastRemoteObject.exportObject(implementation, 0);
      // export it to the remote registry
      Registry registry = LocateRegistry.getRegistry();
      registry.rebind(name, stub);
    }
    catch (Exception e) {
      // XXX: using sout is crappy style, however I do not want to introduce any dependencies to logging frameworks here
      // so unless there are any ideas how to provide logging, it's sout.
      System.err.println("Problem when trying to register the ManagingBundle:");
      e.printStackTrace();
    }
  }

  public void stop(BundleContext context) throws Exception {
    try {
      // unbind the name from the remote registry, making the service unavailable.
      Registry registry = LocateRegistry.getRegistry();
      registry.unbind(ManagingBundle.class.getName());
    }
    catch (Exception e) {
      // XXX: another sout... 
      System.err.println("Problem when trying to unregister the ManagingBundle:");
      e.printStackTrace();
    }
  }
}
