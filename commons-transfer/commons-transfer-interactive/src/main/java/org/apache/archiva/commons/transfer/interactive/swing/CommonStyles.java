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

/**
 * CommonStyles
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class CommonStyles
{
    public static GBCStyles baseline()
    {
        GBCStyles styles = new GBCStyles();

        styles.define( "label" ).margin( 5, 5, 0, 0 ).left();
        styles.define( "value" ).margin( 5, 5, 0, 5 ).fillWide();
        styles.define( "button" ).margin( 0, 2, 0, 2 );
        styles.define( "button_bar" ).margin( 5, 5, 5, 5 );

        return styles;
    }
}
