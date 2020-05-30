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

/**
 * Represents XMPP presence packets. Every presence packet has a type, which is one of
 * the following values:
 * <ul>
 *      <li>{@link Presence.Type#available available} -- (Default) indicates the user is available to
 *          receive messages.
 *      <li>{@link Presence.Type#unavailable unavailable} -- the user is unavailable to receive messages.
 *      <li>{@link Presence.Type#subscribe subscribe} -- request subscription to recipient's presence.
 *      <li>{@link Presence.Type#subscribed subscribed} -- grant subscription to sender's presence.
 *      <li>{@link Presence.Type#unsubscribe unsubscribe} -- request removal of subscription to
 *          sender's presence.
 *      <li>{@link Presence.Type#unsubscribed unsubscribed} -- grant removal of subscription to
 *          sender's presence.
 *      <li>{@link Presence.Type#error error} -- the presence packet contains an error message.
 * </ul><p>
 *
 * A number of attributes are optional:
 * <ul>
 *      <li>Status -- free-form text describing a user's presence (i.e., gone to lunch).
 *      <li>Priority -- non-negative numerical priority of a sender's resource. The
 *          highest resource priority is the default recipient of packets not addressed
 *          to a particular resource.
 *      <li>Mode -- one of five presence modes: {@link Mode#available available} (the default),
 *          {@link Mode#chat chat}, {@link Mode#away away}, {@link Mode#xa xa} (extended away), and
 *          {@link Mode#dnd dnd} (do not disturb).
 * </ul><p>
 *
 * Presence packets are used for two purposes. First, to notify the server of our
 * the clients current presence status. Second, they are used to subscribe and
 * unsubscribe users from the roster.
 *
 * @see RosterPacket
 * @author Matt Tucker
 */
public class Presence extends Packet {

    private Type type;
    private String status = null;
    private int priority = -1;
    private Mode mode = Mode.available;

    /**
     * Creates a new presence update. Status, priority, and mode are left un-set.
     *
     * @param type the type.
     */
    public Presence(Type type) {
        this.type = type;
    }

    /**
     * Creates a new presence update with a specified status, priority, and mode.
     *
     * @param type the type.
     * @param status a text message describing the presence update.
     * @param priority the priority of this presence update.
     * @param mode the mode type for this presence update.
     */
    public Presence(Type type, String status, int priority, Mode mode) {
        this.type = type;
        this.status = status;
        this.priority = priority;
        this.mode = mode;
    }

    /**
     * Returns the type of this presence packet.
     *
     * @return the type of the presence packet.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the presence packet.
     *
     * @param type the type of the presence packet.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns the status message of the presence update, or <tt>null</tt> if there
     * is not a status. The status is free-form text describing a user's presence
     * (i.e., "gone to lunch").
     *
     * @return the status message.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status message of the presence update. The status is free-form text
     * describing a user's presence (i.e., "gone to lunch").
     *
     * @param status the status message.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the priority of the presence, or -1 if no priority has been set.
     *
     * @return the priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the presence. The valid range is -128 through 128.
     *
     * @param priority the priority of the presence.
     * @throws IllegalArgumentException if the priority is outside the valid range.
     */
    public void setPriority(int priority) {
        if (priority < -128 || priority > 128) {
            throw new IllegalArgumentException("Priority value " + priority +
                    " is not valid. Valid range is -128 through 128.");
        }
        this.priority = priority;
    }

    /**
     * Returns the mode of the presence update.
     *
     * @return the mode.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the mode of the presence update. For the standard "available" state, set
     * the mode to <tt>null</tt>.
     *
     * @param mode the mode.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<presence");
        if (getPacketID() != null) {
            buf.append(" id=\"").append(getPacketID()).append("\"");
        }
        if (getTo() != null) {
            buf.append(" to=\"").append(StringUtils.escapeForXML(getTo())).append("\"");
        }
        if (getFrom() != null) {
            buf.append(" from=\"").append(StringUtils.escapeForXML(getFrom())).append("\"");
        }
        if (type != Type.available) {
            buf.append(" type=\"").append(type).append("\"");
        }
        buf.append(">");
        if (status != null) {
            buf.append("<status>").append(StringUtils.escapeForXML(status)).append("</status>");
        }
        if (priority != -1) {
            buf.append("<priority>").append(priority).append("</priority>");
        }
        if (mode != null && mode != Mode.available) {
            buf.append("<show>").append(mode).append("</show>");
        }

        buf.append(this.getExtensionsXML());

        // Add the error sub-packet, if there is one.
        XMPPError error = getError();
        if (error != null) {
            buf.append(error.toXML());
        }

        buf.append("</presence>");

        return buf.toString();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(type);
        if (mode != null) {
            buf.append(": ").append(mode);
        }
        if (status != null) {
            buf.append(" (").append(status).append(")");
        }
        return buf.toString();
    }

    /**
     * A enum to represent the presecence type. Not that presence type is often confused
     * with presence mode. Generally, if a user is signed into a server, they have a presence
     * type of {@link #available available}, even if the mode is {@link Mode#away away},
     * {@link Mode#dnd dnd}, etc. The presence type is only {@link #unavailable unavailable} when
     * the user is signing out of the server.
     */
    public enum Type {

       /**
        * The user is available to receive messages (default).
        */
        available,

        /**
         * The user is unavailable to receive messages.
         */
        unavailable,

        /**
         * Request subscription to recipient's presence.
         */
        subscribe,

        /**
         * Grant subscription to sender's presence.
         */
        subscribed,

        /**
         * Request removal of subscription to sender's presence.
         */
        unsubscribe,

        /**
         * Grant removal of subscription to sender's presence.
         */
        unsubscribed,

        /**
         * The presence packet contains an error message.
         */
        error
    }

    /**
     * An enum to represent the presence mode.
     */
    public enum Mode {

        /**
         * Available (the default).
         */
        available,

        /**
         * Free to chat.
         */
        chat,

        /**
         * Away.
         */
        away,

        /**
         * Away for an extended period of time.
         */
        xa,

        /**
         * Do not disturb.
         */
        dnd
    }
}