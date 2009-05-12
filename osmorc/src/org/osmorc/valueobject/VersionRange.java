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

import org.jetbrains.annotations.NotNull;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class VersionRange
{
  public enum Boundary
  {
    INCLUSIVE, EXCLUSIVE
  }

  public VersionRange(@NotNull Boundary floorBoundary, @NotNull Version floor, @NotNull Version ceiling,
                      @NotNull Boundary ceilingBoundary)
  {
    _floorBoundary = floorBoundary;
    _floor = floor;
    _ceiling = ceiling;
    _ceilingBoundary = ceilingBoundary;
  }

  public VersionRange(@NotNull Version atleast)
  {
    _floor = atleast;
    _floorBoundary = Boundary.INCLUSIVE;
    _ceiling = null;
    _ceilingBoundary = null;
  }

  public boolean contains(@NotNull Version version)
  {
    return checkFloor(version) && checkCeiling(version);
  }

  private boolean checkFloor(@NotNull Version version)
  {
    int comparison = version.compareTo(_floor);
    return _floorBoundary == Boundary.INCLUSIVE ? comparison >= 0 : comparison > 0;
  }

  private boolean checkCeiling(@NotNull Version version)
  {
    if (_ceiling != null)
    {
      int comparison = version.compareTo(_ceiling);
      return _ceilingBoundary == Boundary.INCLUSIVE ? comparison <= 0 : comparison < 0;
    }

    return true;
  }

  private final Boundary _floorBoundary;
  private final Version _floor;
  private final Version _ceiling;
  private final Boundary _ceilingBoundary;
}