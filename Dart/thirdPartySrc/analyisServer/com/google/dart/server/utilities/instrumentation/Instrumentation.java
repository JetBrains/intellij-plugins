/*
 * Copyright (c) 2013, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.server.utilities.instrumentation;

/**
 * The class {@code Instrumentation} implements support for logging instrumentation information.
 * <p>
 * Instrumentation information consists of information about specific operations. Those operations
 * can range from user-facing operations, such as saving the changes to a file, to internal
 * operations, such as tokenizing source code. The information to be logged is gathered by
 * {@link InstrumentationBuilder instrumentation builder}, created by one of the static methods on
 * this class such as {@link #builder(Class)} or {@link #builder(String)}.
 * <p>
 * Note, however, that until an instrumentation logger is installed using the method
 * {@link #setLogger(InstrumentationLogger)}, all instrumentation data will be lost.
 * <p>
 * <b>Example</b>
 * <p>
 * To collect metrics about how long it took to save a file, you would write something like the
 * following:
 * 
 * <pre>
 * InstrumentationBuilder instrumentation = Instrumentation.builder(this.getClass());
 * // save the file
 * instrumentation.metric("chars", fileLength).log();
 * </pre>
 * The {@code Instrumentation.builder} method creates a new {@link InstrumentationBuilder
 * instrumentation builder} and records the time at which it was created. The
 * {@link InstrumentationBuilder#metric(String, long)} appends the information specified by the
 * arguments and records the time at which the method is called so that the time to complete the
 * save operation can be calculated. The {@code log} method tells the builder that all of the data
 * has been collected and that the resulting information should be logged.
 * 
 * @coverage dart.server.utilities
 */
public final class Instrumentation {

  /**
   * A builder that will silently ignore all data and logging requests.
   */
  private static final InstrumentationBuilder NULL_INSTRUMENTATION_BUILDER = new InstrumentationBuilder() {

    @Override
    public InstrumentationBuilder data(String name, boolean value) {
      return this;
    }

    @Override
    public InstrumentationBuilder data(String name, long value) {
      return this;
    }

    @Override
    public InstrumentationBuilder data(String name, String value) {
      return this;
    }

    @Override
    public InstrumentationBuilder data(String name, String[] value) {
      return this;
    }

    @Override
    public InstrumentationLevel getInstrumentationLevel() {
      return InstrumentationLevel.OFF;
    }

    @Override
    public void log() {
    }

    @Override
    public void log(int minTimeToLong) {
    }

    @Override
    public InstrumentationBuilder metric(String name, boolean value) {
      return this;
    }

    @Override
    public InstrumentationBuilder metric(String name, long value) {
      return this;
    }

    @Override
    public InstrumentationBuilder metric(String name, String value) {
      return this;
    }

    @Override
    public InstrumentationBuilder metric(String name, String[] value) {
      return this;
    }

    @Override
    public InstrumentationBuilder record(Throwable exception) {
      return this;
    }
  };

  /**
   * An instrumentation logger that can be used when no other instrumentation logger has been
   * configured. This logger will silently ignore all data and logging requests.
   */
  private static final InstrumentationLogger NULL_LOGGER = new InstrumentationLogger() {
    @Override
    public InstrumentationBuilder createBuilder(String name) {
      return NULL_INSTRUMENTATION_BUILDER;
    }
  };

  /**
   * The current instrumentation logger.
   */
  private static InstrumentationLogger CURRENT_LOGGER = NULL_LOGGER;

  /**
   * Create a builder that can collect the data associated with an operation.
   * 
   * @param clazz the class performing the operation (not {@code null})
   * @return the builder that was created (not {@code null})
   */
  public static InstrumentationBuilder builder(Class<?> clazz) {
    return CURRENT_LOGGER.createBuilder(clazz.getSimpleName());
  }

  /**
   * Create a builder that can collect the data associated with an operation.
   * 
   * @param name the name used to uniquely identify the operation (not {@code null})
   * @return the builder that was created (not {@code null})
   */
  public static InstrumentationBuilder builder(String name) {
    return CURRENT_LOGGER.createBuilder(name);
  }

  /**
   * Get the currently active instrumentation logger
   */
  public static InstrumentationLogger getLogger() {
    return CURRENT_LOGGER;
  }

  /**
   * Return a builder that will silently ignore all data and logging requests.
   * 
   * @return the builder (not {@code null})
   */
  public static InstrumentationBuilder getNullBuilder() {
    return NULL_INSTRUMENTATION_BUILDER;
  }

  /**
   * Is this instrumentation system currently configured to drop instrumentation data provided to
   * it?
   * 
   * @return
   */
  public static boolean isNullLogger() {
    return CURRENT_LOGGER == NULL_LOGGER;
  }

  /**
   * Set the logger that should receive instrumentation information to the given logger.
   * 
   * @param logger the logger that should receive instrumentation information
   */
  public static void setLogger(InstrumentationLogger logger) {
    CURRENT_LOGGER = logger == null ? NULL_LOGGER : logger;
  }

  /**
   * Prevent the creation of instances of this class
   */
  private Instrumentation() {
  }
}
