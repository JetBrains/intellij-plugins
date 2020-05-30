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

package org.jivesoftware.smackx.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.xmlpull.v1.XmlPullParser;

/**
* The DiscoverInfoProvider parses Service Discovery items packets.
*
* @author Gaston Dombiak
*/
public class DiscoverItemsProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        DiscoverItems discoverItems = new DiscoverItems();
        boolean done = false;
        DiscoverItems.Item item = null;
        String jid = "";
        String name = "";
        String action = "";
        String node = "";
        discoverItems.setNode(parser.getAttributeValue("", "node"));
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("item")) {
                    // Initialize the variables from the parsed XML
                    jid = parser.getAttributeValue("", "jid");
                    name = parser.getAttributeValue("", "name");
                    node = parser.getAttributeValue("", "node");
                    action = parser.getAttributeValue("", "action");
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("item")) {
                    // Create a new Item and add it to DiscoverItems.
                    item = new DiscoverItems.Item(jid);
                    item.setName(name);
                    item.setNode(node);
                    item.setAction(action);
                    discoverItems.addItem(item);
                }
                if (parser.getName().equals("query")) {
                    done = true;
                }
            }
        }

        return discoverItems;
    }
}