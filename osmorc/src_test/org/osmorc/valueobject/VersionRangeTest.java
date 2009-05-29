/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.valueobject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.osmorc.valueobject.VersionRange.Boundary.EXCLUSIVE;
import static org.osmorc.valueobject.VersionRange.Boundary.INCLUSIVE;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class VersionRangeTest {
    @Test
    public void testContains() {
        Version version100 = new Version(1, 0, 0, null);
        Version version101 = new Version(1, 0, 1, null);
        Version version200 = new Version(2, 0, 0, null);
        Version version300 = new Version(3, 0, 0, null);
        Version version301 = new Version(3, 0, 1, null);

        assertTrue(new VersionRange(version100).contains(version100));
        assertTrue(new VersionRange(version100).contains(version301));

        assertTrue(new VersionRange(INCLUSIVE, version100, version301, INCLUSIVE).contains(version100));
        assertTrue(new VersionRange(EXCLUSIVE, version100, version301, INCLUSIVE).contains(version101));
        assertFalse(new VersionRange(EXCLUSIVE, version100, version301, INCLUSIVE).contains(version100));

        assertTrue(new VersionRange(INCLUSIVE, version100, version301, INCLUSIVE).contains(version200));
        assertTrue(new VersionRange(EXCLUSIVE, version100, version301, EXCLUSIVE).contains(version200));

        assertTrue(new VersionRange(INCLUSIVE, version100, version301, INCLUSIVE).contains(version301));
        assertTrue(new VersionRange(INCLUSIVE, version100, version301, EXCLUSIVE).contains(version300));
        assertFalse(new VersionRange(INCLUSIVE, version100, version301, EXCLUSIVE).contains(version301));
    }
}
