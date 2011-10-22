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

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ActionMapper
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class ActionMapper
    extends AbstractAction
{
    private static final long serialVersionUID = 7299545918029341401L;

    private static final Log log = LogFactory.getLog( ActionMapper.class );

    private Map<String, Method> commandMap;

    private Object commandObject;

    /**
     * Create an Application Commands processor.
     */
    public ActionMapper( Object commandObj )
    {
        log.debug( "Finding action methods for " + commandObj.getClass().getName() );
        commandMap = new HashMap<String, Method>();
        commandObject = commandObj;

        Pattern namePat = Pattern.compile( "do[A-Z][a-z].*" );
        Method methods[] = commandObject.getClass().getMethods();
        for ( Method method : methods )
        {
            int modifiers = method.getModifiers();
            String name = method.getName();
            Object params[] = method.getParameterTypes();

            Matcher mat = namePat.matcher( name );
            if ( !mat.matches() )
            {
                // log.debug( "Method " + name + " not an application command."
                // );
                continue;
            }

            if ( !Modifier.isPublic( modifiers ) || Modifier.isAbstract( modifiers ) || Modifier.isNative( modifiers )
                || Modifier.isStatic( modifiers ) )
            {
                log.debug( "Method " + name + " is has wrong modifiers." );
                continue;
            }

            if ( ( params == null ) || ( params.length < 1 ) || ( params.length > 1 ) )
            {
                log.debug( "Method " + name + " has wrong number of parameters." );
                continue;
            }

            Object param = params[0];

            if ( param instanceof ActionEvent )
            {
                String commandName = name.substring( 2 ).toLowerCase();
                commandMap.put( commandName, method );
            }
            else if ( param instanceof Class )
            {
                Class<?> pclass = (Class<?>) param;
                if ( pclass.isAssignableFrom( ActionEvent.class ) )
                {
                    String commandName = name.substring( 2 ).toLowerCase();
                    commandMap.put( commandName, method );
                }
                else
                {
                    log.warn( "Method " + name + " has wrong parameter type class.  Expected "
                        + ActionEvent.class.getName() + " but found " + pclass.getName() + " instead." );
                }
            }
            else
            {
                log.warn( "Method " + name + " has wrong parameter type.  Expected " + ActionEvent.class.getName()
                    + " but found " + param.getClass().getName() + " instead." );
            }
        }

        log.debug( "Done with ActionMapper()" );
    }

    public void actionPerformed( ActionEvent evt )
    {
        String actionCommand = evt.getActionCommand();

        if ( evt.getSource() instanceof JButton )
        {
            JButton btn = (JButton) evt.getSource();
            actionCommand = btn.getActionCommand();
        }

        if ( actionCommand == null )
        {
            log.debug( "Encountered null action command. " + evt );
            return;
        }

        Method method = commandMap.get( actionCommand.toLowerCase() );
        if ( method == null )
        {
            log.debug( "Action command " + actionCommand + " does not have a corresponding command method." );
            return;
        }

        try
        {
            method.invoke( commandObject, new Object[] { evt } );
        }
        catch ( IllegalArgumentException e )
        {
            log.error( "Unable to call method " + method + ".", e );
        }
        catch ( IllegalAccessException e )
        {
            log.error( "Unable to call method " + method + ".", e );
        }
        catch ( InvocationTargetException e )
        {
            log.error( "Unable to call method " + method + ".", e );
        }
    }
}
