package org.apache.archiva.commons.transfer.interactive.swing;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * WindowHandler
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class WindowHandler
    extends WindowAdapter
{

    private Preferences appPrefs;

    public WindowHandler()
    {
        appPrefs = Preferences.userNodeForPackage( WindowHandler.class );
    }

    public void restore( Window win )
    {
        String prefix = win.getName();
        if ( prefix == null )
        {
            throw new IllegalStateException( "Unable to restore window preferences from nameless window." );
        }

        WindowUtils.centerWindowOnScreen( win );

        int x = appPrefs.getInt( prefix + ".x", win.getX() );
        int y = appPrefs.getInt( prefix + ".y", win.getY() );

        if ( WindowUtils.isResizable( win ) )
        {
            int width = appPrefs.getInt( prefix + ".width", win.getWidth() );
            int height = appPrefs.getInt( prefix + ".height", win.getHeight() );
            win.setBounds( x, y, width, height );
        }
    }

    public void save( Window win )
    {
        String prefix = win.getName();
        if ( prefix == null )
        {
            throw new IllegalStateException( "Unable to save window preferences from nameless window." );
        }

        Point location = win.getLocation();
        appPrefs.putInt( prefix + ".x", location.x );
        appPrefs.putInt( prefix + ".y", location.y );

        if ( WindowUtils.isResizable( win ) )
        {
            Dimension dim = win.getSize();
            appPrefs.putInt( prefix + ".width", dim.width );
            appPrefs.putInt( prefix + ".height", dim.height );
        }

        try
        {
            appPrefs.flush();
            appPrefs.sync();
        }
        catch ( BackingStoreException ignore )
        {
            /* ignore */
        }
    }

    @Override
    public void windowClosing( WindowEvent e )
    {
        super.windowClosing( e );
        save( e.getWindow() );
    }
}
