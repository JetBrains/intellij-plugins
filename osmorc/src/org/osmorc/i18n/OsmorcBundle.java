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
package org.osmorc.i18n;

import com.intellij.DynamicBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.NlsContexts.NotificationContent;
import com.intellij.openapi.util.NlsContexts.NotificationTitle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

/**
 * Internationalization bundle for Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public final class OsmorcBundle {
  private static final String BUNDLE = "messages.OsmorcBundle";
  private static final DynamicBundle INSTANCE = new DynamicBundle(OsmorcBundle.class, BUNDLE);

  private OsmorcBundle() {
  }

  public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  public static Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }

  public static Notification notification(@NotNull @NotificationTitle String title,
                                          @NotNull @NotificationContent String message,
                                          @NotNull NotificationType type) {
    return NotificationGroupManager.getInstance().getNotificationGroup("OSGi").createNotification(title, message, type);
  }

  public static Notification important(@NotNull @NotificationTitle String title,
                                       @NotNull @NotificationContent String message,
                                       @NotNull NotificationType type) {
    return NotificationGroupManager.getInstance().getNotificationGroup("OSGi-important").createNotification(title, message, type);
  }

  public static Notification bnd(@NotNull @NotificationTitle String title,
                                 @NotNull @NotificationContent String message,
                                 @NotNull NotificationType type) {
    return NotificationGroupManager.getInstance().getNotificationGroup("OSGi-bnd").createNotification(title, message, type);
  }
}
