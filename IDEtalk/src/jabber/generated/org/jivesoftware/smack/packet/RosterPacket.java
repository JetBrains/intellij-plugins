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

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents XMPP roster packets.
 *
 * @author Matt Tucker
 */
public class RosterPacket extends IQ {

    private final List<Item> rosterItems = new ArrayList<>();

    /**
     * Adds a roster item to the packet.
     *
     * @param item a roster item.
     */
    public void addRosterItem(Item item) {
        synchronized (rosterItems) {
            rosterItems.add(item);
        }
    }

    /**
     * Returns the number of roster items in this roster packet.
     *
     * @return the number of roster items.
     */
    public int getRosterItemCount() {
        synchronized (rosterItems) {
            return rosterItems.size();
        }
    }

    /**
     * Returns an unmodifiable collection for the roster items in the packet.
     *
     * @return an unmodifiable collection for the roster items in the packet.
     */
    public Collection<Item> getRosterItems() {
        synchronized (rosterItems) {
            return Collections.unmodifiableList(new ArrayList<Item>(rosterItems));
        }
    }

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<query xmlns=\"jabber:iq:roster\">");
        synchronized (rosterItems) {
            for (Item entry : rosterItems) {
                buf.append(entry.toXML());
            }
        }
        buf.append("</query>");
        return buf.toString();
    }

    /**
     * A roster item, which consists of a JID, their name, the type of subscription, and
     * the groups the roster item belongs to.
     */
    public static class Item {

        private String user;
        private String name;
        private ItemType itemType;
        private ItemStatus itemStatus;
        private final Set<String> groupNames;

        /**
         * Creates a new roster item.
         *
         * @param user the user.
         * @param name the user's name.
         */
        public Item(String user, String name) {
            this.user = user.toLowerCase();
            this.name = name;
            itemType = null;
            itemStatus = null;
            groupNames = new CopyOnWriteArraySet<String>();
        }

        /**
         * Returns the user.
         *
         * @return the user.
         */
        public String getUser() {
            return user;
        }

        /**
         * Returns the user's name.
         *
         * @return the user's name.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the user's name.
         *
         * @param name the user's name.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the roster item type.
         *
         * @return the roster item type.
         */
        public ItemType getItemType() {
            return itemType;
        }

        /**
         * Sets the roster item type.
         *
         * @param itemType the roster item type.
         */
        public void setItemType(ItemType itemType) {
            this.itemType = itemType;
        }

        /**
         * Returns the roster item status.
         *
         * @return the roster item status.
         */
        public ItemStatus getItemStatus() {
            return itemStatus;
        }

        /**
         * Sets the roster item status.
         *
         * @param itemStatus the roster item status.
         */
        public void setItemStatus(ItemStatus itemStatus) {
            this.itemStatus = itemStatus;
        }

        /**
         * Returns an unmodifiable set of the group names that the roster item
         * belongs to.
         *
         * @return an unmodifiable set of the group names.
         */
        public Set<String> getGroupNames() {
            return Collections.unmodifiableSet(groupNames);
        }

        /**
         * Adds a group name.
         *
         * @param groupName the group name.
         */
        public void addGroupName(String groupName) {
            groupNames.add(groupName);
        }

        /**
         * Removes a group name.
         *
         * @param groupName the group name.
         */
        public void removeGroupName(String groupName) {
            groupNames.remove(groupName);
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<item jid=\"").append(user).append("\"");
            if (name != null) {
                buf.append(" name=\"").append(name).append("\"");
            }
            if (itemType != null) {
                buf.append(" subscription=\"").append(itemType).append("\"");
            }
            if (itemStatus != null) {
                buf.append(" ask=\"").append(itemStatus).append("\"");
            }
            buf.append(">");
            for (String groupName : groupNames) {
                buf.append("<group>").append(StringUtils.escapeForXML(groupName)).append("</group>");
            }
            buf.append("</item>");
            return buf.toString();
        }
    }

    /**
     * The subscription status of a roster item. An optional element that indicates
     * the subscription status if a change request is pending.
     */
    public static class ItemStatus {

        /**
         * Request to subcribe.
         */
        public static final ItemStatus SUBSCRIPTION_PENDING = new ItemStatus("subscribe");

        /**
         * Request to unsubscribe.
         */
        public static final ItemStatus UNSUBCRIPTION_PENDING = new ItemStatus("unsubscribe");

        public static ItemStatus fromString(String value) {
            if (value == null) {
                return null;
            }
            value = value.toLowerCase();
            if ("unsubscribe".equals(value)) {
                return SUBSCRIPTION_PENDING;
            }
            else if ("subscribe".equals(value)) {
                return SUBSCRIPTION_PENDING;
            }
            else {
                return null;
            }
        }

        private String value;

        /**
         * Returns the item status associated with the specified string.
         *
         * @param value the item status.
         */
        private ItemStatus(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    /**
     * The subscription type of a roster item.
     */
    public static class ItemType {

        /**
         * The user and subscriber have no interest in each other's presence.
         */
        public static final ItemType NONE = new ItemType("none");

        /**
         * The user is interested in receiving presence updates from the subscriber.
         */
        public static final ItemType TO = new ItemType("to");

        /**
         * The subscriber is interested in receiving presence updates from the user.
         */
        public static final ItemType FROM = new ItemType("from");

        /**
         * The user and subscriber have a mutual interest in each other's presence.
         */
        public static final ItemType BOTH = new ItemType("both");

        /**
         * The user wishes to stop receiving presence updates from the subscriber.
         */
        public static final ItemType REMOVE = new ItemType("remove");

        public static ItemType fromString(String value) {
            if (value == null) {
                return null;
            }
            value = value.toLowerCase();
            if ("none".equals(value)) {
                return NONE;
            }
            else if ("to".equals(value)) {
                return TO;
            }
            else if ("from".equals(value)) {
                return FROM;
            }
            else if ("both".equals(value)) {
                return BOTH;
            }
            else if ("remove".equals(value)) {
                return REMOVE;
            }
            else {
                return null;
            }
        }

        private String value;

        /**
         * Returns the item type associated with the specified string.
         *
         * @param value the item type.
         */
        public ItemType(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }
}