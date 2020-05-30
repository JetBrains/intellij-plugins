/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright 2003-2004 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smack.packet;

import org.jivesoftware.smack.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Base class for XMPP packets. Every packet has a unique ID (which is automatically
 * generated, but can be overriden). Optionally, the "to" and "from" fields can be set,
 * as well as an arbitrary number of properties.
 *
 * Properties provide an easy mechanism for clients to share data. Each property has a
 * String name, and a value that is a Java primitive (int, long, float, double, boolean)
 * or any Serializable object (a Java object is Serializable when it implements the
 * Serializable interface).
 *
 * @author Matt Tucker
 */
public abstract class Packet {

    /**
     * Constant used as packetID to indicate that a packet has no id. To indicate that a packet
     * has no id set this constant as the packet's id. When the packet is asked for its id the 
     * answer will be <tt>null</tt>.
     */
    public static final String ID_NOT_AVAILABLE = "ID_NOT_AVAILABLE";

    /**
     * A prefix helps to make sure that ID's are unique across mutliple instances.
     */
    private static String prefix = StringUtils.randomString(5) + "-";

    /**
     * Keeps track of the current increment, which is appended to the prefix to
     * forum a unique ID.
     */
    private static long id = 0;

    /**
     * Returns the next unique id. Each id made up of a short alphanumeric
     * prefix along with a unique numeric value.
     *
     * @return the next id.
     */
    private static synchronized String nextID() {
        return prefix + Long.toString(id++);
    }

    private String packetID = null;
    private String to = null;
    private String from = null;
    private List<PacketExtension> packetExtensions = null;
    private Map<String,Object> properties = null;
    private XMPPError error = null;

    /**
     * Returns the unique ID of the packet. The returned value could be <tt>null</tt> when
     * ID_NOT_AVAILABLE was set as the packet's id.
     *
     * @return the packet's unique ID or <tt>null</tt> if the packet's id is not available.
     */
    public String getPacketID() {
        if (ID_NOT_AVAILABLE.equals(packetID)) {
            return null;
        }
        
        if (packetID == null) {
            packetID = nextID();
        }
        return packetID;
    }

    /**
     * Sets the unique ID of the packet. To indicate that a packet has no id 
     * pass the constant ID_NOT_AVAILABLE as the packet's id value.
     *
     * @param packetID the unique ID for the packet.
     */
    public void setPacketID(String packetID) {
        this.packetID = packetID;
    }

    /**
     * Returns who the packet is being sent "to", or <tt>null</tt> if
     * the value is not set. The XMPP protocol often makes the "to"
     * attribute optional, so it does not always need to be set.
     *
     * @return who the packet is being sent to, or <tt>null</tt> if the
     *      value has not been set.
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets who the packet is being sent "to". The XMPP protocol often makes
     * the "to" attribute optional, so it does not always need to be set.
     *
     * @param to who the packet is being sent to.
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns who the packet is being sent "from" or <tt>null</tt> if
     * the value is not set. The XMPP protocol often makes the "from"
     * attribute optional, so it does not always need to be set.
     *
     * @return who the packet is being sent from, or <tt>null</tt> if the
     *      valud has not been set.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets who the packet is being sent "from". The XMPP protocol often
     * makes the "from" attribute optional, so it does not always need to
     * be set.
     *
     * @param from who the packet is being sent to.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the error associated with this packet, or <tt>null</tt> if there are
     * no errors.
     *
     * @return the error sub-packet or <tt>null</tt> if there isn't an error.
     */
    public XMPPError getError() {
        return error;
    }

    /**
     * Sets the error for this packet.
     *
     * @param error the error to associate with this packet.
     */
    public void setError(XMPPError error) {
        this.error = error;
    }

    /**
     * Returns an unmodifiable collection of the packet extensions attached to the packet.
     *
     * @return the packet extensions.
     */
    public synchronized Collection<PacketExtension> getExtensions() {
        if (packetExtensions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<PacketExtension>(packetExtensions));
    }

    /**
     * Returns the first packet extension that matches the specified element name and
     * namespace, or <tt>null</tt> if it doesn't exist. Packet extensions are
     * are arbitrary XML sub-documents in standard XMPP packets. By default, a 
     * DefaultPacketExtension instance will be returned for each extension. However, 
     * PacketExtensionProvider instances can be registered with the 
     * {@link org.jivesoftware.smack.provider.ProviderManager ProviderManager}
     * class to handle custom parsing. In that case, the type of the Object
     * will be determined by the provider.
     *
     * @param elementName the XML element name of the packet extension.
     * @param namespace the XML element namespace of the packet extension.
     * @return the extension, or <tt>null</tt> if it doesn't exist.
     */
    public synchronized PacketExtension getExtension(String elementName, String namespace) {
        if (packetExtensions == null || elementName == null || namespace == null) {
            return null;
        }
        for (PacketExtension ext : packetExtensions) {
            if (elementName.equals(ext.getElementName()) && namespace.equals(ext.getNamespace())) {
                return ext;
            }
        }
        return null;
    }

    /**
     * Adds a packet extension to the packet.
     *
     * @param extension a packet extension.
     */
    public synchronized void addExtension(PacketExtension extension) {
        if (packetExtensions == null) {
            packetExtensions = new ArrayList<PacketExtension>();
        }
        packetExtensions.add(extension);
    }

    /**
     * Removes a packet extension from the packet.
     *
     * @param extension the packet extension to remove.
     */
    public synchronized void removeExtension(PacketExtension extension)  {
        if (packetExtensions != null) {
            packetExtensions.remove(extension);
        }
    }

    /**
     * Returns the packet property with the specified name or <tt>null</tt> if the
     * property doesn't exist. Property values that were orginally primitives will
     * be returned as their object equivalent. For example, an int property will be
     * returned as an Integer, a double as a Double, etc.
     *
     * @param name the name of the property.
     * @return the property, or <tt>null</tt> if the property doesn't exist.
     */
    public synchronized Object getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return properties.get(name);
    }

    /**
     * Sets a property with an Object as the value. The value must be Serializable
     * or an IllegalArgumentException will be thrown.
     *
     * @param name the name of the property.
     * @param value the value of the property.
     */
    public synchronized void setProperty(String name, Object value) {
        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException("Value must be serialiazble");
        }
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(name, value);
    }

    /**
     * Deletes a property.
     *
     * @param name the name of the property to delete.
     */
    public synchronized void deleteProperty(String name) {
        if (properties == null) {
            return;
        }
        properties.remove(name);
    }

    /**
     * Returns an unmodifiable collection of all the property names that are set.
     *
     * @return all property names.
     */
    public synchronized Collection<String> getPropertyNames() {
        if (properties == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<String>(properties.keySet()));
    }

    /**
     * Returns the packet as XML. Every concrete extension of Packet must implement
     * this method. In addition to writing out packet-specific data, every sub-class
     * should also write out the error and the extensions data if they are defined.
     *
     * @return the XML format of the packet as a String.
     */
    public abstract String toXML();

    /**
     * Returns the extension sub-packets (including properties data) as an XML
     * String, or the Empty String if there are no packet extensions.
     *
     * @return the extension sub-packets as XML or the Empty String if there
     * are no packet extensions.
     */
    protected synchronized String getExtensionsXML() {
        StringBuilder buf = new StringBuilder();
        // Add in all standard extension sub-packets.
        for (PacketExtension extension : getExtensions()) {
            buf.append(extension.toXML());
        }
        // Add in packet properties.
        if (properties != null && !properties.isEmpty()) {
            buf.append("<properties xmlns=\"http://www.jivesoftware.com/xmlns/xmpp/properties\">");
            // Loop through all properties and write them out.
            for (String name : getPropertyNames()) {
                Object value = getProperty(name);
                buf.append("<property>");
                buf.append("<name>").append(StringUtils.escapeForXML(name)).append("</name>");
                buf.append("<value type=\"");
                if (value instanceof Integer) {
                    buf.append("integer\">").append(value).append("</value>");
                }
                else if (value instanceof Long) {
                    buf.append("long\">").append(value).append("</value>");
                }
                else if (value instanceof Float) {
                    buf.append("float\">").append(value).append("</value>");
                }
                else if (value instanceof Double) {
                    buf.append("double\">").append(value).append("</value>");
                }
                else if (value instanceof Boolean) {
                    buf.append("boolean\">").append(value).append("</value>");
                }
                else if (value instanceof String) {
                    buf.append("string\">");
                    buf.append(StringUtils.escapeForXML((String)value));
                    buf.append("</value>");
                }
                // Otherwise, it's a generic Serializable object. Serialized objects are in
                // a binary format, which won't work well inside of XML. Therefore, we base-64
                // encode the binary data before adding it.
                else {
                    ByteArrayOutputStream byteStream = null;
                    ObjectOutputStream out = null;
                    try {
                        byteStream = new ByteArrayOutputStream();
                        out = new ObjectOutputStream(byteStream);
                        out.writeObject(value);
                        buf.append("java-object\">");
                        String encodedVal = StringUtils.encodeBase64(byteStream.toByteArray());
                        buf.append(encodedVal).append("</value>");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (out != null) {
                            try {
                                out.close();
                            }
                            catch (Exception e) {
                                // Ignore.
                            }
                        }
                        if (byteStream != null) {
                            try {
                                byteStream.close();
                            }
                            catch (Exception e) {
                                // Ignore.
                            }
                        }
                    }
                }
                buf.append("</property>");
            }
            buf.append("</properties>");
        }
        return buf.toString();
    }
}