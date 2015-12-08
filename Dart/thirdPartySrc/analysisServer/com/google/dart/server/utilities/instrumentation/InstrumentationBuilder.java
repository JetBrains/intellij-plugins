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
 * The interface {@code InstrumentationBuilder} defines the behavior of objects used to collect data
 * about an operation that has occurred and record that data through an instrumentation logger.
 * <p>
 * For an example of using objects that implement this interface, see {@link Instrumentation}.
 * 
 * @coverage dart.server.utilities
 */
public interface InstrumentationBuilder {

  /**
   * Append the given data to the data being collected by this builder. The information is declared
   * to potentially contain data that is either user identifiable or contains user intellectual
   * property (but is not guaranteed to contain either).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder data(String name, boolean value);

  /**
   * Append the given data to the data being collected by this builder. The information is declared
   * to potentially contain data that is either user identifiable or contains user intellectual
   * property (but is not guaranteed to contain either).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder data(String name, long value);

  /**
   * Append the given data to the data being collected by this builder. The information is declared
   * to potentially contain data that is either user identifiable or contains user intellectual
   * property (but is not guaranteed to contain either).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder data(String name, String value);

  /**
   * Append the given data to the data being collected by this builder. The information is declared
   * to potentially contain data that is either user identifiable or contains user intellectual
   * property (but is not guaranteed to contain either).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder data(String name, String[] value);

  /**
   * Answer the {@link InstrumentationLevel} of this {@code InstrumentationBuilder}.
   * 
   * @return one of {@link InstrumentationLevel#EVERYTHING}, {@link InstrumentationLevel#METRICS},
   *         {@link InstrumentationLevel#OFF}
   */
  public InstrumentationLevel getInstrumentationLevel();

  /**
   * Log the data that has been collected. The instrumentation builder should not be used after this
   * method is invoked. The behavior of any method defined on this interface that is used after this
   * method is invoked is undefined.
   */
  public void log();

  /**
   * Log the data that has been collected. The instrumentation builder should not be used after this
   * method is invoked. The behavior of any method defined on this interface that is used after this
   * method is invoked is undefined.
   * 
   * @param minTimeToLog if the total elapsed time is less than this, do not record
   */
  public void log(int minTimeToLog);

  /**
   * Append the given metric to the data being collected by this builder. The information is
   * declared to contain only metrics data (data that is not user identifiable and does not contain
   * user intellectual property).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder metric(String name, boolean value);

  /**
   * Append the given metric to the data being collected by this builder. The information is
   * declared to contain only metrics data (data that is not user identifiable and does not contain
   * user intellectual property).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder metric(String name, long value);

  /**
   * Append the given metric to the data being collected by this builder. The information is
   * declared to contain only metrics data (data that is not user identifiable and does not contain
   * user intellectual property).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder metric(String name, String value);

  /**
   * Append the given metric to the data being collected by this builder. The information is
   * declared to contain only metrics data (data that is not user identifiable and does not contain
   * user intellectual property).
   * 
   * @param name the name used to identify the data
   * @param value the value of the data to be collected
   * @return this builder
   */
  public InstrumentationBuilder metric(String name, String[] value);

  /**
   * Append the given exception to the information being collected by this builder. The exception's
   * class name is captured using {@link #metric(String, String)}. Other aspects of the exception
   * may contain either user identifiable or contains user intellectual property (but is not
   * guaranteed to contain either) and thus are captured using the various data methods such as
   * {@link #data(String, String)}.
   * 
   * @param exception the exception (may be {@code null})
   */
  public InstrumentationBuilder record(Throwable exception);
}
