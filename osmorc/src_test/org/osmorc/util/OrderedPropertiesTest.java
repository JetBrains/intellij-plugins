package org.osmorc.util;

import org.jetbrains.osgi.jps.util.OrderedProperties;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of {@link OrderedProperties}
 */
public class OrderedPropertiesTest {
  @Test
  public void testRead() throws IOException {
    OrderedProperties props = new OrderedProperties();
    props.load(new StringReader("Foo: Bar,\\\nnaf\nBar: naf\nBaz=naf\n"));

    Enumeration<Object> keys = props.keys();
    assertThat(keys.nextElement(), equalTo("Foo"));
    assertThat(keys.nextElement(), equalTo("Bar"));
    assertThat(keys.nextElement(), equalTo("Baz"));

    Enumeration<?> propertyNames = props.propertyNames();
    assertThat(propertyNames.nextElement(), equalTo("Foo"));
    assertThat(propertyNames.nextElement(), equalTo("Bar"));
    assertThat(propertyNames.nextElement(), equalTo("Baz"));

    Iterator<String> iterator = props.stringPropertyNames().iterator();
    assertThat(iterator.next(), equalTo("Foo"));
    assertThat(iterator.next(), equalTo("Bar"));
    assertThat(iterator.next(), equalTo("Baz"));
  }

  @Test
  public void testPopulate() {
    OrderedProperties props = new OrderedProperties();
    props.setProperty("key1", "value1");
    props.setProperty("key3", "value3");
    props.setProperty("key2", "value2");

    Iterator<String> iterator = props.stringPropertyNames().iterator();
    assertThat(iterator.next(), equalTo("key1"));
    assertThat(iterator.next(), equalTo("key3"));
    assertThat(iterator.next(), equalTo("key2"));
  }
}
