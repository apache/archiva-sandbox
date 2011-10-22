package org.apache.archiva.commons.transfer.defaults;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.archiva.commons.transfer.TransferNetworkProxy;
import org.apache.archiva.commons.transfer.TransferStore;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default TransferStore.
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class DefaultTransferStore implements TransferStore {
    private static Log log = LogFactory.getLog(DefaultTransferStore.class);
    private static TransferStore _INSTANCE = new DefaultTransferStore();
    private static final String NETPROXY = "net.proxy.";
    private File file;
    private Map<String, String> config = new TreeMap<String, String>();

    public static TransferStore getDefault() {
        return _INSTANCE;
    }

    public DefaultTransferStore() {
        file = new File(SystemUtils.getUserHome(), ".apache/commons-transfer.properties");
        try {
            load();
        } catch (IOException e) {
            log.warn("Unable to load existing properties.", e);
        }
    }

    public boolean isInteractive() {
        return getBoolean("interactive", true);
    }

    public void setInteractive(boolean interactive) {
        setBoolean("interactive", interactive);
    }

    public Set<String> getPrefixedKeys(String prefix) {
        Set<String> keys = new TreeSet<String>();

        for (String key : config.keySet()) {
            if (key.startsWith(prefix)) {
                keys.add(key);
            }
        }

        return keys;
    }

    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }

        config.clear();

        FileReader reader = null;
        try {
            reader = new FileReader(file);
            BufferedReader buf = new BufferedReader(reader);
            String line;
            while ((line = buf.readLine()) != null) {
                if (StringUtils.isBlank(line)) {
                    // Skip blank lines.
                    continue;
                }
                line = line.trim();
                if (line.startsWith("#")) {
                    // Skip comments.
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx <= 0) {
                    // Bad line, skip.
                    continue;
                }
                String key = line.substring(0, idx);
                String value = line.substring(idx + 1);
                config.put(key, value);
            }
        } catch (IOException e) {
            // Unable to load the properties.
            log.error("Unable to load the existing properties.", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public void save() throws IOException {
        FileWriter writer = null;
        try {
            File outdir = file.getParentFile();
            if((outdir != null) && (outdir.exists() == false)) {
                if(outdir.mkdirs() == false) {
                    System.err.println("Unable to create destination directory: " + outdir.getAbsolutePath());
                }
            }

            writer = new FileWriter(file);
            PrintWriter p = new PrintWriter(writer);
            p.println("# Created by " + DefaultTransferStore.class.getName());
            p.println("# WARNING: Editing this file by hand is detrimental to your free time.");
            p.println("# Written: " + new SimpleDateFormat().format(Calendar.getInstance().getTime()));
            p.println();
            for (Map.Entry<String, String> entry : config.entrySet()) {
                p.println(entry.getKey() + "=" + entry.getValue());
            }
            p.println();
        } catch (IOException e) {
            // Unable to store properties.
            log.error("Unable to store the existing properties.", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public TransferNetworkProxy getNetworkProxy() {
        TransferNetworkProxy proxy = new TransferNetworkProxy();

        proxy.setEnabled(this.getBoolean(NETPROXY + "enabled", false));
        proxy.setHost(this.getString(NETPROXY + "host", null));
        proxy.setPort(this.getInt(NETPROXY + "port", -1));

        proxy.setAuthEnabled(this.getBoolean(NETPROXY + "auth-enabled", false));
        proxy.setUsername(this.getString(NETPROXY + "username", null));

        proxy.setPassword(this.getPassword(NETPROXY + "password"));

        String hostList = this.getString(NETPROXY + "no-proxy-hosts", null);
        if (StringUtils.isNotBlank(hostList)) {
            String hosts[] = StringUtils.split(hostList, '|');
            for (String host : hosts) {
                proxy.addNoProxyHost(host);
            }
        }

        return proxy;
    }

    public void setNetworkProxy(TransferNetworkProxy proxy) {
        this.setBoolean(NETPROXY + "enabled", proxy.isEnabled());
        this.setString(NETPROXY + "host", StringUtils.defaultString(proxy.getHost()));
        this.setInt(NETPROXY + "port", proxy.getPort());
        this.setBoolean(NETPROXY + "auth-enabled", proxy.isAuthEnabled());
        this.setString(NETPROXY + "username", StringUtils.defaultString(proxy.getUsername()));
        this.setPassword(NETPROXY + "password", proxy.getPassword());

        if (proxy.getNoProxyHosts().isEmpty()) {
            this.setString(NETPROXY + "no-proxy-hosts", "");
        } else {
            String hostList = StringUtils.join(proxy.getNoProxyHosts().iterator(), '|');
            this.setString(NETPROXY + "no-proxy-hosts", StringUtils.defaultString(hostList));
        }
    }

    public String getString(String key, String defaultvalue) {
        String val = config.get(key);
        if (StringUtils.isBlank(val)) {
            return defaultvalue;
        }
        return val;
    }

    public boolean getBoolean(String key, boolean defaultvalue) {
        String val = this.getString(key, String.valueOf(defaultvalue));
        Boolean bool = BooleanUtils.toBooleanObject(val);
        return BooleanUtils.toBooleanDefaultIfNull(bool, defaultvalue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultvalue) {
        String val = config.get(key);
        return NumberUtils.toInt(val, defaultvalue);
    }

    public String getPassword(String key) {
        String encrypted = config.get(key);
        if (StringUtils.isNotBlank(encrypted)) {
            try {
                Password pw = new Password();
                char decrypted[] = pw.decrypt(encrypted);
                return new String(decrypted);
            } catch (IOException e) {
                log.warn("Unable to decrypt password: " + e.getMessage(), e);
            } catch (GeneralSecurityException e) {
                log.warn("Unable to decrypt password: " + e.getMessage(), e);
            }
        }
        return null;
    }

    public void setString(String key, String val) {
        config.put(key, val);
    }

    public void setBoolean(String key, boolean val) {
        config.put(key, String.valueOf(val));
    }

    public void setInt(String key, int val) {
        config.put(key, String.valueOf(val));
    }

    public void setPassword(String key, String decrypted) {
        if (StringUtils.isBlank(decrypted)) {
            config.put(key, "");
        } else {
            try {
                Password pw = new Password();
                String encrypted = pw.encrypt(decrypted.toCharArray());
                config.put(key, encrypted);
            } catch (IOException e) {
                log.warn("Unable to encrypt proxy password: " + e.getMessage(), e);
            } catch (GeneralSecurityException e) {
                log.warn("Unable to encrypt proxy password: " + e.getMessage(), e);
            }
        }
    }
}
