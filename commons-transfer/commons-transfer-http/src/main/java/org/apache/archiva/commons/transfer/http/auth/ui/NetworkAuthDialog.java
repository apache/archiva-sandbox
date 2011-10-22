package org.apache.archiva.commons.transfer.http.auth.ui;

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

import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.archiva.commons.transfer.http.auth.HttpSimpleAuth;
import org.apache.archiva.commons.transfer.interactive.swing.ActionMapper;
import org.apache.archiva.commons.transfer.interactive.swing.CommonStyles;
import org.apache.archiva.commons.transfer.interactive.swing.GBCStyles;
import org.apache.archiva.commons.transfer.interactive.swing.HorizontalRule;
import org.apache.archiva.commons.transfer.interactive.swing.KeyAction;
import org.apache.archiva.commons.transfer.interactive.swing.UIUtils;
import org.apache.archiva.commons.transfer.interactive.swing.WindowUtils;
import org.apache.commons.lang.StringUtils;

/**
 * NetworkAuthDialog
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class NetworkAuthDialog
    extends JDialog
{
    private static final long serialVersionUID = 2535900226076861162L;

    private static final String OK = "ok";

    private static final String CANCEL = "cancel";

    public static void main( String[] args )
    {
        // UIUtils.setDefaultLookAndFeel();
        UIUtils.setJavaLookAndFeel();

        NetworkAuthDialog dlg = new NetworkAuthDialog();

        HttpSimpleAuth httpauth = new HttpSimpleAuth();
        httpauth.getKey().setRealm( "Example Realm" );
        httpauth.getKey().setHost( "example.hostname.com" );
        httpauth.getKey().setPort( 8080 );
        httpauth.getCredentials().setUsername( "joakim" );
        HttpSimpleAuth outauth = dlg.getAuth( httpauth );
        System.out.println( "Done with getAuth()" );
        if ( outauth == null )
        {
            System.out.println( "   Canceled Auth." );
        }
        else
        {
            System.out.println( "   Entered Auth: " + outauth.getCredentials().getUsername() + ":"
                + outauth.getCredentials().getPassword() );
            System.out.println( "   Persist? : " + outauth.isPersisted() );
        }
        System.exit( -1 );
    }

    private JLabel lblRealm;

    private JLabel lblHost;

    private JLabel lblUsername;

    private JLabel lblPassword;

    private JLabel txtRealm;

    private JLabel txtHost;

    private JTextField txtUsername;

    private JPasswordField txtPassword;

    private JCheckBox boxRemember;

    private JButton btnOk;

    private JButton btnCancel;

    private ActionMapper actionMapper;

    private boolean bCanceled = false;

    private HttpSimpleAuth auth;

    public NetworkAuthDialog()
    {
        super();
    }

    public NetworkAuthDialog( Frame owner )
    {
        super( owner );
    }

    @Override
    protected void dialogInit()
    {
        super.dialogInit();

        this.setModal( true );
        this.getContentPane().setLayout( new GridBagLayout() );

        // Init components.
        lblRealm = new JLabel( "Realm:" );
        lblHost = new JLabel( "Host:" );
        lblUsername = new JLabel( "Username:" );
        lblPassword = new JLabel( "Password:" );

        txtRealm = new JLabel( "[example realm]" );
        txtHost = new JLabel( "[example host]" );

        txtUsername = new JTextField( "[example username]", 20 );
        txtPassword = new JPasswordField( "[password]" );
        boxRemember = new JCheckBox( "Remember Credentials" );

        btnOk = new JButton();
        btnOk.setAction( getActionMapper() );
        btnOk.setActionCommand( OK );
        btnOk.setText( "OK" );
        btnOk.setDefaultCapable( true );

        btnCancel = new JButton();
        btnCancel.setAction( getActionMapper() );
        btnCancel.setActionCommand( CANCEL );
        btnCancel.setText( "Cancel" );

        GBCStyles styles = CommonStyles.baseline();

        this.getContentPane().add( lblRealm, styles.use( "label" ) );
        this.getContentPane().add( txtRealm, styles.use( "value" ).endRow() );
        this.getContentPane().add( lblHost, styles.use( "label" ) );
        this.getContentPane().add( txtHost, styles.use( "value" ).endRow() );
        this.getContentPane().add( lblUsername, styles.use( "label" ) );
        this.getContentPane().add( txtUsername, styles.use( "value" ).endRow() );
        this.getContentPane().add( lblPassword, styles.use( "label" ) );
        this.getContentPane().add( txtPassword, styles.use( "value" ).endRow() );
        this.getContentPane().add( boxRemember, styles.base().spanRow( 2 ).center().margin( 5, 5, 0, 5 ).endRow() );
        this.getContentPane().add( new HorizontalRule(),
                                   styles.base().spanRow( 2 ).fillWide().margin( 5, 5, 0, 5 ).endRow() );

        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout( new GridBagLayout() );
        pnlButtons.add( btnOk, styles.use( "button" ).right() );
        pnlButtons.add( btnCancel, styles.use( "button" ).left().endRow() );

        this.getContentPane().add( pnlButtons, styles.use( "button_bar" ).spanRow( 2 ).center().fillWide().endRow() );

        this.setTitle( "Network Authentication Required." );
        this.setResizable( false );
        this.pack();

        setEnterSubmitsOk( txtUsername );
        setEnterSubmitsOk( txtPassword );

        txtUsername.requestFocus();

        this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    }

    public void doCancel( ActionEvent evt )
    {
        bCanceled = true;
        this.setVisible( false );
    }

    public void doOk( ActionEvent evt )
    {
        bCanceled = false;
        if ( StringUtils.isBlank( txtUsername.getText() ) )
        {
            JOptionPane.showMessageDialog( this, "You must provide a username.", "Username Missing",
                                           JOptionPane.ERROR_MESSAGE );
            return;
        }

        if ( ( txtPassword.getPassword() == null ) || ( txtPassword.getPassword().length <= 0 ) )
        {
            JOptionPane.showMessageDialog( this, "You must provide a password.", "Password Missing",
                                           JOptionPane.ERROR_MESSAGE );
            return;
        }

        this.auth.getCredentials().setUsername( txtUsername.getText() );
        this.auth.getCredentials().setPassword( new String( txtPassword.getPassword() ) );
        this.auth.setPersisted( boxRemember.isSelected() );
        this.setVisible( false );
    }

    private ActionMapper getActionMapper()
    {
        if ( actionMapper == null )
        {
            actionMapper = new ActionMapper( this );
        }

        return actionMapper;
    }

    public HttpSimpleAuth getAuth( HttpSimpleAuth httpauth )
    {
        this.auth = httpauth;
        txtRealm.setText( httpauth.getKey().getRealm() );
        txtHost.setText( httpauth.getKey().getHost() + ":" + httpauth.getKey().getPort() );
        txtUsername.setText( httpauth.getCredentials().getUsername() );
        txtPassword.setText( httpauth.getCredentials().getPassword() );
        boxRemember.setSelected( httpauth.isPersisted() );

        txtUsername.setCaretPosition( txtUsername.getText().length() );
        this.pack();
        WindowUtils.centerWindowOnScreen( this );

        this.setVisible( true );

        if ( bCanceled )
        {
            return null;
        }

        return this.auth;
    }

    private void setEnterSubmitsOk( JComponent comp )
    {
        InputMap inputs = comp.getInputMap();
        inputs.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), OK );
        comp.getActionMap().put( OK, new KeyAction( getActionMapper(), OK ) );
    }
}
