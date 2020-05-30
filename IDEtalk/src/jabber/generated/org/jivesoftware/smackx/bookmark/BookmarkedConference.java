/**
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-2005 Jive Software. All rights reserved.
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package org.jivesoftware.smackx.bookmark;

/**
 * Respresents a Conference Room bookmarked on the server using JEP-0048 Bookmark Storage JEP.
 *
 * @author Derek DeMoro
 */
public class BookmarkedConference implements SharedBookmark {

    private String name;
    private boolean autoJoin;
    private final String jid;

    private String nickname;
    private String password;
    private boolean isShared;

    protected BookmarkedConference(String jid) {
        this.jid = jid;
    }

    protected BookmarkedConference(String name, String jid, boolean autoJoin, String nickname,
            String password)
    {
        this.name = name;
        this.jid = jid;
        this.autoJoin = autoJoin;
        this.nickname = nickname;
        this.password = password;
    }


    /**
     * Returns the display label representing the Conference room.
     *
     * @return the name of the conference room.
     */
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns true if this conference room should be auto-joined on startup.
     *
     * @return true if room should be joined on startup, otherwise false.
     */
    public boolean isAutoJoin() {
        return autoJoin;
    }

    protected void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    /**
     * Returns the full JID of this conference room. (ex.dev@conference.jivesoftware.com)
     *
     * @return the full JID of  this conference room.
     */
    public String getJid() {
        return jid;
    }

    /**
     * Returns the nickname to use when joining this conference room. This is an optional
     * value and may return null.
     *
     * @return the nickname to use when joining, null may be returned.
     */
    public String getNickname() {
        return nickname;
    }

    protected void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the password to use when joining this conference room. This is an optional
     * value and may return null.
     *
     * @return the password to use when joining this conference room, null may be returned.
     */
    public String getPassword() {
        return password;
    }

    protected void setPassword(String password) {
        this.password = password;
    }

    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof BookmarkedConference)) {
            return false;
        }
        BookmarkedConference conference = (BookmarkedConference)obj;
        return conference.getJid().equalsIgnoreCase(jid);
    }

    protected void setShared(boolean isShared) {
        this.isShared = isShared;
    }

    public boolean isShared() {
        return isShared;
    }
}
