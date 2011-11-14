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

package org.osmorc.settings;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * This is actually built into DialogWrapper, but it does not resize properly on long strings, so i had
 * to duplicate it. Relayout of the dialog should fix OSMORC-111
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class MyErrorText extends JPanel {

  public void setStatus(@Nullable String text) {
    if (text == null) {
      myLabel.setText("");
      myLabel.setIcon(null);
      setBorder(null);
      myPrefSize = null;
    }
    else {
      myLabel.setText(text);
      myLabel.setBorder(new EmptyBorder(2, 2, 0, 0));
      myPrefSize = myLabel.getPreferredSize();
    }
    revalidate();
  }

  public void setError(@Nullable String text) {
    if (text == null) {
      myLabel.setText("");
      myLabel.setIcon(null);
      setBorder(null);
      myPrefSize = null;
    }
    else {
      myLabel.setText((new StringBuilder()).append("<html><body><font color=red><left>").append(text)
                        .append("</left></b></font></body></html>").toString());
      myLabel.setIcon(IconLoader.getIcon("/actions/lightning.png"));
      myLabel.setBorder(new EmptyBorder(2, 2, 0, 0));
      myPrefSize = myLabel.getPreferredSize();
    }
    revalidate();
  }

  public Dimension getPreferredSize() {
    return myPrefSize != null ? myPrefSize : super.getPreferredSize();
  }

  private final JLabel myLabel = new JLabel();
  private Dimension myPrefSize;

  public MyErrorText() {
    myLabel.setVerticalAlignment(SwingConstants.TOP);
    setLayout(new BorderLayout());
    setBorder(null);
    UIUtil.removeQuaquaVisualMarginsIn(this);
    add(myLabel, "Center");
  }
}
