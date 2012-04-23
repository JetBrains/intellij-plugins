package org.angularjs;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.ResourceRegistrarImpl;
import com.intellij.javaee.StandardResourceProvider;

/**
 * Created with IntelliJ IDEA.
 * User: johnlindquist
 * Date: 4/23/12
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class AngularJSResourceProvider implements StandardResourceProvider {
    @Override
    public void registerResources(ResourceRegistrar registrar) {
        ResourceRegistrarImpl impl = (ResourceRegistrarImpl) registrar;
        impl.addStdResource("http://angularjs.org/angularjs.dtd", "/angular.dtd", getClass());
    }
}
