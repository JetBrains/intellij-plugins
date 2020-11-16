/*
 *****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************
*/
package org.nanocontainer;

import java.io.Serializable;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassPathElement denotes an element in a classpath allowing to grant permissions.
 *
 * @author Paul Hammant
 */
public class ClassPathElement implements Serializable {

    private final URL url;
    private PermissionCollection permissionCollection;
    private final List permissions = new ArrayList();

    public ClassPathElement(URL url) {
        this.url = url;
    }

    public Permission grantPermission(Permission permission) {
        if (permission == null) {
            throw new NullPointerException();
        }
        permissions.add(permission);
        return permission;
    }

    public URL getUrl() {
        return url;
    }

    public PermissionCollection getPermissionCollection() {
        if (permissionCollection == null) {
            permissionCollection = new Permissions();
            for (int i = 0; i < permissions.size(); i++) {
                Permission permission = (Permission) permissions.get(i);
                permissionCollection.add(permission);
            }
        }
        return permissionCollection;
    }

    public String toString() {
        return "[" + System.identityHashCode(this) + " " + url + " " + permissions.size() +"]";
    }

}
