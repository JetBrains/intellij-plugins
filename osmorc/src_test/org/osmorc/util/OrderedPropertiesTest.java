package org.osmorc.util;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of {@link OrderedProperties}
 */
public class OrderedPropertiesTest {

  @Test
  public void testRead() throws IOException {
    String propsAsString = "Foo: Bar,\\\nnarf\nBar: narf\nBaz=narf\n";
    OrderedProperties props = new OrderedProperties();
    props.load(new StringReader(propsAsString));

    Enumeration<Object> keys = props.keys();
    String key1  = (String)keys.nextElement();
    String key2 = (String)keys.nextElement();
    String key3 = (String)keys.nextElement();

    assertThat(key1, equalTo("Foo"));
    assertThat(key2, equalTo("Bar"));
    assertThat(key3, equalTo("Baz"));


    Enumeration<?> propertyNames = props.propertyNames();
    key1 = (String)propertyNames.nextElement();
    key2 = (String)propertyNames.nextElement();
    key3 = (String)propertyNames.nextElement();

    assertThat(key1, equalTo("Foo"));
    assertThat(key2, equalTo("Bar"));
    assertThat(key3, equalTo("Baz"));

    Set<String> stringPropertyNames = props.stringPropertyNames();
    Iterator<String> iterator = stringPropertyNames.iterator();
    key1 = iterator.next();
    key2 = iterator.next();
    key3 = iterator.next();

    assertThat(key1, equalTo("Foo"));
    assertThat(key2, equalTo("Bar"));
    assertThat(key3, equalTo("Baz"));

  }
}
