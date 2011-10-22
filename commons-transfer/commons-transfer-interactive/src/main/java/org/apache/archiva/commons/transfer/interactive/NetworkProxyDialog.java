package org.apache.archiva.commons.transfer.interactive;

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

import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.archiva.commons.transfer.TransferNetworkProxy;
import org.apache.archiva.commons.transfer.TransferStore;
import org.apache.archiva.commons.transfer.defaults.DefaultTransferStore;
import org.apache.archiva.commons.transfer.interactive.swing.ActionMapper;
import org.apache.archiva.commons.transfer.interactive.swing.CommonStyles;
import org.apache.archiva.commons.transfer.interactive.swing.GBC;
import org.apache.archiva.commons.transfer.interactive.swing.GBCStyles;
import org.apache.archiva.commons.transfer.interactive.swing.HorizontalRule;
import org.apache.archiva.commons.transfer.interactive.swing.UIUtils;
import org.apache.archiva.commons.transfer.interactive.swing.WindowUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NetworkProxyDialog
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class NetworkProxyDialog
    extends JDialog
{
    class NoProxyListModel
        extends AbstractListModel
    {
        private static final long serialVersionUID = -268017959753502789L;

        List<String> hosts = new ArrayList<String>();

        public NoProxyListModel()
        {
            super();
        }

        public NoProxyListModel( Set<String> noProxyList )
        {
            this();
            setProxyList( noProxyList );
        }

        public void addHost( String host )
        {
            int presize = hosts.size();
            hosts.add( host );
            Collections.sort( hosts );
            fireChange( presize );
        }

        public void editHost( String selectedHost, String replacementHost )
        {
            int presize = hosts.size();
            hosts.remove( selectedHost );
            hosts.add( replacementHost );
            Collections.sort( hosts );
            fireChange( presize );
        }

        private void fireChange( int presize )
        {
            int nowsize = hosts.size();
            if ( nowsize > presize )
            {
                fireIntervalAdded( this, 0, nowsize - 1 );
            }
            else if ( nowsize < presize )
            {
                fireIntervalRemoved( this, 0, nowsize - 1 );
            }
            else
            {
                fireContentsChanged( this, 0, nowsize - 1 );
            }
        }

        public Object getElementAt( int index )
        {
            if ( index >= hosts.size() )
            {
                return null;
            }

            return hosts.get( index );
        }

        public List<String> getHosts()
        {
            return Collections.unmodifiableList( hosts );
        }

        public int getSize()
        {
            return hosts.size();
        }

        public void removeHost( String host )
        {
            int presize = hosts.size();
            hosts.remove( host );
            fireChange( presize );
        }

        public void setProxyList( Set<String> noProxyList )
        {
            hosts.clear();
            hosts.addAll( noProxyList );
            Collections.sort( hosts );
            fireChange( 0 );
        }
    }

    private static final long serialVersionUID = 816496870511533703L;

    private static final Log log = LogFactory.getLog( NetworkProxyDialog.class );

    // Action commands - Typically buttons
    public static final String OK = "ok";

    public static final String CANCEL = "cancel";

    public static final String ADD_NOPROXY_HOST = "addNoProxy";

    public static final String EDIT_NOPROXY_HOST = "editNoProxy";

    public static final String REMOVE_NOPROXY_HOST = "removeNoProxy";

    public static final String USE_BROWSER_SETTINGS = "useBrowserSettings";

    public static final String USE_DIRECT_CONNECTION = "useDirectConnection";

    public static final String USE_PROXIED_CONNECTION = "useProxiedConnection";

    public static final String PROXY_AUTH_STATE = "proxyAuthState";

    private static final int DIRECT = 0;

    private static final int PROXIED = 1;

    public static void collectAndSave()
    {
        UIUtils.setJavaLookAndFeel();

        NetworkProxyDialog dlg = new NetworkProxyDialog();
        TransferNetworkProxy proxy = DefaultTransferStore.getDefault().getNetworkProxy();

        if ( proxy == null )
        {
            proxy = new TransferNetworkProxy();
            proxy.setEnabled( false );
            proxy.setHost( "example.hostname.com" );
            proxy.setPort( 8080 );
            proxy.setUsername( "username" );
            proxy.setPassword( null );
            proxy.setAuthEnabled( false );
            proxy.addNoProxyHost( "localhost" );
            proxy.addNoProxyHost( "127.0.0.1" );
        }

        proxy = dlg.show( proxy );

        if ( proxy == null )
        {
            System.out.println( "User canceled." );
        }
        else
        {
            System.out.println( "User clicked OK." );
            System.out.println( proxy.toDump() );
            DefaultTransferStore.getDefault().setNetworkProxy( proxy );
            try
            {
                DefaultTransferStore.getDefault().save();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    /* .\ Dialog Widgets \.___________________________________ */

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                NetworkProxyDialog.collectAndSave();
                System.exit( -1 );
            }
        } );
    }

    private TransferStore transferStore;

    private List<JComponent> sectionProxied;

    private List<JComponent> sectionProxiedAuth;

    /** Direct Connections Option */
    private JRadioButton radioDirectConnection;

    /** Proxied Connections Options */
    private JRadioButton radioProxiedConnection;

    /** Proxy Hostname */
    private JTextField txtProxyHost;

    /** Proxy Port */
    private JTextField txtProxyPort;

    /** No Proxy List */
    private JList lstNoProxy;

    private NoProxyListModel noProxyListModel;

    /** No Proxy - Add Button */
    private JButton btnAddNoProxyHost;

    /** No Proxy - Edit Button */
    private JButton btnEditNoProxyHost;

    /** No Proxy - Remove Button */
    private JButton btnRemoveNoProxyHost;

    /** Option to enable Proxy Auth */
    private JCheckBox boxEnableProxyAuth;

    /** Proxy Auth - User Name */
    private JTextField txtProxyUsername;

    /** Proxy Auth - Password */
    private JPasswordField txtProxyPassword;

    /** Button - Accept settings */
    private JButton btnOk;

    /** Button - Cancel settings */
    private JButton btnCancel;

    // Event Handlers
    private ActionMapper actionMapper;

    private TransferNetworkProxy model;

    private boolean bCanceled = false;

    private JButton createJButton( String text, String actionCommand )
    {
        JButton button = new JButton();
        button.setAction( getActionMapper() );
        button.setActionCommand( actionCommand );
        button.setText( text );
        return button;
    }

    @Override
    protected void dialogInit()
    {
        super.dialogInit();
        setModal( true );

        /* .\ Init \._________________________________________ */

        sectionProxied = new ArrayList<JComponent>();
        sectionProxiedAuth = new ArrayList<JComponent>();

        radioDirectConnection = new JRadioButton();
        radioDirectConnection.setAction( getActionMapper() );
        radioDirectConnection.setActionCommand( USE_DIRECT_CONNECTION );
        radioDirectConnection.setText( "Use a Direct Connection" );

        // Init Section 1
        radioProxiedConnection = new JRadioButton();
        radioProxiedConnection.setAction( getActionMapper() );
        radioProxiedConnection.setActionCommand( USE_PROXIED_CONNECTION );
        radioProxiedConnection.setText( "Use a Proxied Connection" );

        ButtonGroup connectionTypeGroup = new ButtonGroup();
        connectionTypeGroup.add( radioDirectConnection );
        connectionTypeGroup.add( radioProxiedConnection );

        radioDirectConnection.setSelected( true );

        txtProxyHost = new JTextField( "[host]" );
        txtProxyPort = new JTextField( "[port]" );
        txtProxyPort.setColumns( 6 );

        // Init Section 2
        noProxyListModel = new NoProxyListModel();
        lstNoProxy = new JList( noProxyListModel );
        btnAddNoProxyHost = createJButton( "Add ...", ADD_NOPROXY_HOST );
        btnEditNoProxyHost = createJButton( "Edit ...", EDIT_NOPROXY_HOST );
        btnRemoveNoProxyHost = createJButton( "Remove", REMOVE_NOPROXY_HOST );

        // Init Section 3
        boxEnableProxyAuth = new JCheckBox();
        boxEnableProxyAuth.setAction( getActionMapper() );
        boxEnableProxyAuth.setActionCommand( PROXY_AUTH_STATE );
        boxEnableProxyAuth.setText( "Enable Proxy Auth" );
        txtProxyUsername = new JTextField( "[username]" );
        txtProxyPassword = new JPasswordField( "[password]" );

        // Init Button Bar
        btnOk = createJButton( "OK", OK );
        btnCancel = createJButton( "Cancel", CANCEL );

        /* .\ Layout \._______________________________________ */

        Container pane = getContentPane();
        pane.setLayout( new GridBagLayout() );

        GBCStyles styles = CommonStyles.baseline();
        styles.define( "left_spacer" ).marginLeft( 20 ).fillTall();
        styles.define( "radio_button" ).margin( 5, 5, 0, 5 ).spanCol( 2 ).fillWide();
        styles.define( "checkbox" ).margin( 5, 5, 0, 5 ).spanCol( 2 ).fillWide();
        styles.define( "hr" ).margin( 5, 5, 0, 5 ).spanCol( 2 ).fillWide().endRow();
        styles.define( "noproxy_button" ).margin( 0, 5, 5, 5 ).x( 1 ).stretch( GBC.WIDE ).endRow();

        // Layout top of dialog.
        pane.add( radioDirectConnection, styles.use( "radio_button" ).endRow() );
        pane.add( radioProxiedConnection, styles.use( "radio_button" ).endRow() );
        pane.add( new JPanel(), styles.base().marginLeft( 20 ) );
        pane.add( getProxiedPanel( styles ), styles.base().margin( 5, 5, 5, 5 ).fillBoth().endRow() );
        pane.add( layoutButtonBar( styles ), styles.use( "button_bar" ).fillWide().endRow() );

        /* .\ Properties \.___________________________________ */

        setTitle( "Network Proxy Setup" );
        setResizable( false );
        pack();

        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    }

    public void doAddNoProxy( ActionEvent evt )
    {
        String host = JOptionPane.showInputDialog( this, "Enter Hostname to Add.\n"
            + "The hostname can use globs such as \"*.hostname.com\"", "No Proxy List Add",
                                                   JOptionPane.QUESTION_MESSAGE );

        host = StringUtils.trimToNull( host );

        if ( host != null )
        {
            noProxyListModel.addHost( host );
        }
    }

    public void doCancel( ActionEvent evt )
    {
        bCanceled = true;
        setVisible( false );
    }

    public void doEditNoProxy( ActionEvent evt )
    {
        if ( lstNoProxy.isSelectionEmpty() )
        {
            JOptionPane.showMessageDialog( this, "Select a hostname to edit.", "No Proxy List Edit Warning",
                                           JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        String selectedHost = (String) lstNoProxy.getSelectedValue();
        String host = (String) JOptionPane.showInputDialog( this, "Edit Hostname.\n"
            + "The hostname can use globs such as \"*.hostname.com\"", "No Proxy List Edit",
                                                            JOptionPane.QUESTION_MESSAGE, null, null, selectedHost );

        host = StringUtils.trimToNull( host );

        if ( host != null )
        {
            noProxyListModel.editHost( selectedHost, host );
        }
    }

    public void doOk( ActionEvent evt )
    {
        boolean directSelected = radioDirectConnection.isSelected();
        boolean proxiedSelected = radioProxiedConnection.isSelected();

        if ( !directSelected && !proxiedSelected )
        {
            JOptionPane.showMessageDialog( this, "Select a mode: Direct or Proxied.", "Validation Error",
                                           JOptionPane.WARNING_MESSAGE );
            return;
        }

        this.model.setEnabled( proxiedSelected );

        // Validate Host
        String host = StringUtils.trimToEmpty( txtProxyHost.getText() );
        if ( proxiedSelected && StringUtils.isBlank( host ) )
        {
            JOptionPane.showMessageDialog( this, "The proxy host name is required.", "Validation Error",
                                           JOptionPane.WARNING_MESSAGE );
            return;
        }
        if ( !StringUtils.containsNone( host, " \\/\t:" ) )
        {
            JOptionPane.showMessageDialog( this, "The proxy host name entered contains invalid characters.",
                                           "Validation Error", JOptionPane.WARNING_MESSAGE );
            return;
        }
        this.model.setHost( host );

        // Validate Port
        String portStr = StringUtils.trimToEmpty( txtProxyPort.getText() );
        if ( proxiedSelected && StringUtils.isBlank( portStr ) )
        {
            JOptionPane.showMessageDialog( this, "The proxy port is required.", "Validation Error",
                                           JOptionPane.WARNING_MESSAGE );
            return;
        }
        if ( !StringUtils.isNumeric( portStr ) )
        {
            JOptionPane.showMessageDialog( this, "The proxy port entered is invalid.", "Validation Error",
                                           JOptionPane.WARNING_MESSAGE );
            return;
        }
        int port = NumberUtils.toInt( portStr, 0 );
        IntRange validTcpPorts = new IntRange( 1, 65535 );
        if ( !validTcpPorts.containsInteger( port ) )
        {
            JOptionPane.showMessageDialog( this, "The proxy port must be a valid tcp port number, within the range of "
                + validTcpPorts.getMinimumInteger() + " and " + validTcpPorts.getMaximumInteger() + ".",
                                           "Validation Error", JOptionPane.WARNING_MESSAGE );
            return;
        }
        this.model.setPort( port );

        // Validate No Proxy List
        this.model.getNoProxyHosts().clear();
        for ( String nohost : noProxyListModel.getHosts() )
        {
            this.model.addNoProxyHost( nohost );
        }

        // Validate Auth
        boolean authEnabled = boxEnableProxyAuth.isSelected();
        this.model.setAuthEnabled( authEnabled );

        // Validate Username
        String username = StringUtils.trimToEmpty( txtProxyUsername.getText() );
        if ( authEnabled && StringUtils.isBlank( username ) )
        {
            JOptionPane.showMessageDialog( this, "The proxy username is required.", "Validation Error",
                                           JOptionPane.WARNING_MESSAGE );
            return;
        }
        this.model.setUsername( username );

        // Validate Password
        String password = new String( txtProxyPassword.getPassword() );
        if ( authEnabled && StringUtils.isBlank( password ) )
        {
            JOptionPane.showMessageDialog( this, "The proxy username is required.", "Validation Error",
                                           JOptionPane.WARNING_MESSAGE );
            return;
        }
        this.model.setPassword( password );

        // Persist network proxy settings.
        try
        {
            getTransferStore().setNetworkProxy( this.model );
            getTransferStore().save();
        }
        catch ( IOException e )
        {
            log.warn( e.getMessage(), e );
        }

        bCanceled = false;
        setVisible( false );
    }

    public void doProxyAuthState( ActionEvent evt )
    {
        enableSection( sectionProxiedAuth, boxEnableProxyAuth.isSelected() );
    }

    public void doRemoveNoProxy( ActionEvent evt )
    {
        if ( lstNoProxy.isSelectionEmpty() )
        {
            JOptionPane.showMessageDialog( this, "Select a hostname to remove.", "No Proxy List Remove Warning",
                                           JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        String selectedHost = (String) lstNoProxy.getSelectedValue();
        noProxyListModel.removeHost( selectedHost );
    }

    public void doUseDirectConnection( ActionEvent evt )
    {
        setMode( DIRECT );
    }

    public void doUseProxiedConnection( ActionEvent evt )
    {
        setMode( PROXIED );
    }

    private void enableSection( List<JComponent> components, boolean enabled )
    {
        for ( JComponent comp : components )
        {
            comp.setEnabled( enabled );
        }
    }

    private ActionMapper getActionMapper()
    {
        if ( actionMapper == null )
        {
            actionMapper = new ActionMapper( this );
        }

        return actionMapper;
    }

    private JPanel getProxiedPanel( GBCStyles styles )
    {
        JPanel pnlProxied = new JPanel();
        pnlProxied.setLayout( new GridBagLayout() );

        /* .\ Http Proxy Section \. ________________________________ */

        JLabel lblHost = new JLabel( "Host:" );
        JLabel lblPort = new JLabel( "Port:" );

        pnlProxied.add( lblHost, styles.use( "label" ) );
        pnlProxied.add( txtProxyHost, styles.use( "value" ).endRow() );
        pnlProxied.add( lblPort, styles.use( "label" ) );
        pnlProxied.add( txtProxyPort, styles.use( "value" ).left().stretch( 0 ).endRow() );

        sectionProxied.add( lblHost );
        sectionProxied.add( txtProxyHost );
        sectionProxied.add( lblPort );
        sectionProxied.add( txtProxyPort );

        pnlProxied.add( new HorizontalRule(), styles.use( "hr" ) );

        /* .\ No Proxy Section \. ________________________________ */
        JScrollPane noproxyScroller = new JScrollPane( lstNoProxy );
        noproxyScroller.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        noproxyScroller.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

        JPanel pnlNoProxy = new JPanel();
        pnlNoProxy.setLayout( new GridBagLayout() );

        JLabel lblProxyFor = new JLabel( "No Proxy For:" );
        pnlNoProxy.add( lblProxyFor, styles.use( "label" ).spanCol( 2 ).left().endRow() );
        pnlNoProxy.add( noproxyScroller, styles.base().margin( 0, 5, 5, 5 ).spanRow( 4 ).fillBoth() );
        pnlNoProxy.add( btnAddNoProxyHost, styles.use( "noproxy_button" ) );
        pnlNoProxy.add( btnEditNoProxyHost, styles.use( "noproxy_button" ) );
        pnlNoProxy.add( btnRemoveNoProxyHost, styles.use( "noproxy_button" ) );
        pnlNoProxy.add( new JPanel(), styles.base().x( 3 ).fillTall().endBoth() );

        pnlProxied.add( pnlNoProxy, styles.base().fillWide().endRow() );

        sectionProxied.add( lblProxyFor );
        sectionProxied.add( noproxyScroller );
        sectionProxied.add( lstNoProxy );
        sectionProxied.add( btnAddNoProxyHost );
        sectionProxied.add( btnEditNoProxyHost );
        sectionProxied.add( btnRemoveNoProxyHost );

        pnlProxied.add( new HorizontalRule(), styles.use( "hr" ) );

        /* .\ Proxy Auth Section \. ________________________________ */
        JLabel lblUsername = new JLabel( "User Name:" );
        JLabel lblPassword = new JLabel( "Password:" );

        pnlProxied.add( boxEnableProxyAuth, styles.use( "checkbox" ).endRow() );
        pnlProxied.add( lblUsername, styles.use( "label" ) );
        pnlProxied.add( txtProxyUsername, styles.use( "value" ).endRow() );
        pnlProxied.add( lblPassword, styles.use( "label" ) );
        pnlProxied.add( txtProxyPassword, styles.use( "value" ).endRow() );

        sectionProxiedAuth.add( lblUsername );
        sectionProxiedAuth.add( txtProxyUsername );
        sectionProxiedAuth.add( lblPassword );
        sectionProxiedAuth.add( txtProxyPassword );

        sectionProxied.addAll( sectionProxiedAuth );
        sectionProxied.add( boxEnableProxyAuth );

        pnlProxied.add( new HorizontalRule(), styles.use( "hr" ) );

        return pnlProxied;
    }

    public TransferStore getTransferStore()
    {
        if ( transferStore == null )
        {
            transferStore = DefaultTransferStore.getDefault();
        }
        return transferStore;
    }

    private JPanel layoutButtonBar( GBCStyles styles )
    {
        JPanel pnlButtonBar = new JPanel();
        pnlButtonBar.setLayout( new GridBagLayout() );
        pnlButtonBar.add( btnOk, styles.use( "button" ).weightWide( 1.0 ).right().marginLeft( 15 ) );
        pnlButtonBar.add( btnCancel, styles.use( "button" ).right().endBoth() );

        return pnlButtonBar;
    }

    private void populate( TransferNetworkProxy proxy )
    {
        this.model = proxy; // TODO: make a clone.

        if ( model != null )
        {
            txtProxyHost.setText( model.getHost() );
            txtProxyPort.setText( String.valueOf( model.getPort() ) );
            noProxyListModel.setProxyList( model.getNoProxyHosts() );
            boxEnableProxyAuth.setSelected( model.isAuthEnabled() );
            txtProxyUsername.setText( model.getUsername() );
            txtProxyPassword.setText( model.getPassword() );

            if ( model.isEnabled() )
            {
                radioProxiedConnection.setSelected( true );
                setMode( PROXIED );
            }
            else
            {
                radioDirectConnection.setSelected( true );
                setMode( DIRECT );
            }

            // this.pack();
        }
        else
        {
            // Try persisted proxy.
            model = getTransferStore().getNetworkProxy();
            if ( model != null )
            {
                populate( model );
                return;
            }

            // Use empty proxy.
            model = new TransferNetworkProxy();
            model.setEnabled( false );
            populate( model );
            return;
        }

        WindowUtils.centerWindowOnScreen( this );
    }

    public void setMode( int mode )
    {
        switch ( mode )
        {
            case DIRECT:
                enableSection( sectionProxied, false );
                break;
            case PROXIED:
                enableSection( sectionProxied, true );
                enableSection( sectionProxiedAuth, boxEnableProxyAuth.isSelected() );
                break;
            default:
                throw new IllegalArgumentException( "mode of " + mode + " not recognized." );
        }
    }

    public void setTransferStore( TransferStore transferStore )
    {
        this.transferStore = transferStore;
    }

    public TransferNetworkProxy show( TransferNetworkProxy proxy )
    {
        populate( proxy );
        setVisible( true );

        if ( bCanceled )
        {
            return null;
        }
        else
        {
            return model;
        }
    }
}
