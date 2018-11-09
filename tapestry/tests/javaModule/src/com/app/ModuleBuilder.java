package com.app;

import org.apache.tapestry5.ioc.ServiceBinder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class ModuleBuilder {

    public static void empty_bind(ServiceBinder binder) {

    }

    public static void simple_bind(ServiceBinder binder) {
        binder.bind(String.class);

        binder.bind(Collection.class, ArrayList.class);
    }

    public static void bind_with_configuration(ServiceBinder binder) {
        binder.bind(String.class).withId("id").eagerLoad().scope("scope");

        binder.bind(Collection.class, ArrayList.class).withId("id").eagerLoad().scope("scope");
    }
}
