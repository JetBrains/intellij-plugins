package com.intellij.tapestry.core.ioc;

import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class ServiceBindingTest {

    @Test
    public void getters_setters() {
        JavaClassTypeMock classTypeMock = new JavaClassTypeMock("MyClass");

        ServiceBinding serviceBinding = new ServiceBinding();
        serviceBinding.setServiceClass(classTypeMock);
        serviceBinding.setEagerLoad(true);
        serviceBinding.setScope("myscope");
        serviceBinding.setId("myid");

        assert serviceBinding.getServiceClass().getName().equals("MyClass");
        assert serviceBinding.getId().equals("myid");
        assert serviceBinding.getScope().equals("myscope");
        assert serviceBinding.isEagerLoad();
    }
}
