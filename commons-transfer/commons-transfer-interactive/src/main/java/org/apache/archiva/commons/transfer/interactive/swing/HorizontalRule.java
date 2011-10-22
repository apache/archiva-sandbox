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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * HorizontalRule
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class HorizontalRule
    extends JComponent
{
    private static final long serialVersionUID = 360658256715376753L;

    private Color lineColor;

    public HorizontalRule()
    {
        setPreferredSize( new Dimension( 10, 5 ) );
    }

    public Color getLineColor()
    {
        return lineColor;
    }

    public Color getLineColor( JComponent c )
    {
        if ( lineColor != null )
        {
            return lineColor;
        }

        return c.getBackground().darker().darker();
    }

    @Override
    protected void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        g.setColor( getLineColor( this ) );
        g.drawLine( 0, 0, getSize().width, 0 );
    }

    public void setLineColor( Color color )
    {
        this.lineColor = color;
    }
}
