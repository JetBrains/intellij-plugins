package com.intellij.tapestry.core.exceptions;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class CoreExceptionTest {

    @Test
    public void constructor() {
        new CoreException(new Throwable());
    }
}
