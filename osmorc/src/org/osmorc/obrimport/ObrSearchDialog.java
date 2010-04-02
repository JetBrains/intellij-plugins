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

package org.osmorc.obrimport;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.obrimport.springsource.ObrMavenResult;

import javax.swing.JComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A dialog for searching inside the known Obrs.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class ObrSearchDialog extends DialogWrapper
{
    /**
     * Opens the search dialog and allows the user to query and return a maven artifact.
     *
     * @param project     the current project
     * @param queryString a pre-set query string. Can be null.
     * @return a result. If the user canceled the dialog or nothing was found, returns null.
     */
    public static
    @Nullable
    ObrMavenResult queryForMavenArtifact(@NotNull Project project, @Nullable String queryString)
    {
        ObrSearchDialog dialog = new ObrSearchDialog(project, QueryType.Maven);
        dialog.setQueryString(queryString);
        dialog.show();
        if (!dialog.isOK())
        {
            return null;
        }
        else
        {
            return (ObrMavenResult) dialog.getResult();
        }
    }

    protected void setQueryString(String queryString)
    {
        _searchPanel.setQueryString(queryString);
    }

    protected Object getResult()
    {
        return _searchPanel.getResult();
    }

    protected ObrSearchDialog(Project project, QueryType queryType)
    {
        super(project, true);
        setOKActionEnabled(false);
        _searchPanel = new ObrSearchPanel(queryType);
        _searchPanel.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                setOKActionEnabled(_searchPanel.isHasResult());
            }
        });
        setTitle("OBR search");
        init();
    getRootPane().setDefaultButton(_searchPanel.getSearchButton());
    }

    protected JComponent createCenterPanel()
    {
        return _searchPanel.getRootPanel();
    }

    private final ObrSearchPanel _searchPanel;

}
