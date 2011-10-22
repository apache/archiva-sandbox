package org.apache.archiva.jarinfo.model.io;

import org.apache.archiva.jarinfo.model.JarDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * JarDetailsParser
 * 
 * @version $Id$
 */
public class JarDetailsParser extends DefaultHandler {
    private Logger log = LoggerFactory.getLogger(JarDetailsParser.class);
    private JarDetails details;
    private transient StringBuilder activeNode;

    public JarDetails getDetails() {
        return details;
    }

    @Override
    public void startDocument() throws SAXException {
        log.info("Start Document");
        details = new JarDetails();
        activeNode = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException {
        activeNode.append("/").append(qName);
        log.info("(start) " + activeNode);
        
        int len = attribs.getLength();
        for (int i = 0; i < len; i++) {
            String key = attribs.getQName(i);
            String value = attribs.getValue(i);
            activeNode.append("[").append(key).append("=").append(value)
                .append("]");
        }
        
        if("version".equals(qName)) {
            
        } else {
            log.warn("Unhandled element: " + activeNode + " (start element)");
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        log.info("(end) " + activeNode);
        
        int slashIdx = activeNode.lastIndexOf("/");
        if(slashIdx >= 0) {
            activeNode.delete(slashIdx, activeNode.length());
        }
    }
}