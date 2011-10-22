package org.apache.archiva.jarinfo.model.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Stack;

import org.apache.archiva.jarinfo.utils.Timestamp;

/**
 * SimpleXmlWriter
 * 
 * @version $Id$
 */
public class SimpleXmlWriter {
    private Writer writer;
    private Stack<String> elemStack = new Stack<String>();

    public SimpleXmlWriter(Writer writer) {
        this.writer = writer;
    }

    public void attribute(String key, String value) throws IOException {
        this.writer.append(" ").append(key).append("=\"").append(value).append("\"");
    }

    public void elemClose() throws IOException {
        // New Line
        this.writer.append("\n");
        String text = elemStack.pop();
        indent();
        // Create </close>
        this.writer.append("</").append(text).append(">");
    }

    /**
     * Creates an empty <code>&lt;elem attr="value" /&gt;</code> entry
     * 
     * @param elem
     * @param attribs
     * @throws IOException
     */
    public void elemEmpty(String elem, String[][] attribs) throws IOException {
        // New Line
        this.writer.append("\n");
        // Add Indent
        indent();
        // Create <elem>text</elem>
        this.writer.append("<").append(elem);
        for (int i = 0; i < attribs.length; i++) {
            attribute(attribs[i][0], attribs[i][1]);
        }
        this.writer.append("/>");
    }

    public void elemOpen(String elem) throws IOException {
        // New Line
        this.writer.append("\n");
        // Add Indent
        indent();
        // Create <elem
        this.writer.append("<").append(elem).append(">");
        elemStack.push(elem);
    }

    public void elemOpen(String elem, String[][] attributes) throws IOException {
        // New Line
        this.writer.append("\n");
        // Add Indent
        indent();
        // Create <elem
        this.writer.append("<").append(elem);
        // Add attributes
        for (int i = 0; i < attributes.length; i++) {
            attribute(attributes[i][0], attributes[i][1]);
        }
        this.writer.append(">");
        elemStack.push(elem);
    }

    /**
     * Creates a simple <code>&lt;elem&gt;text&lt;/elem&gt;</code> entry
     * 
     * @param elem
     * @param text
     * @throws IOException
     */
    public void elemSimple(String elem, Object obj) throws IOException {
        // New Line
        this.writer.append("\n");
        // Add Indent
        indent();
        // Create <elem>text</elem>
        this.writer.append("<").append(elem).append(">");
        text(obj);
        this.writer.append("</").append(elem).append(">");
    }

    /**
     * Creates a simple <code>&lt;elem attr="value"&gt;text&lt;/elem&gt;</code>
     * entry
     * 
     * @param elem
     * @param attribs
     * @param text
     * @throws IOException
     */
    public void elemSimple(String elem, String[][] attribs, Object obj) throws IOException {
        // New Line
        this.writer.append("\n");
        // Add Indent
        indent();
        // Create <elem>text</elem>
        this.writer.append("<").append(elem);
        for (int i = 0; i < attribs.length; i++) {
            attribute(attribs[i][0], attribs[i][1]);
        }
        this.writer.append(">");
        text(obj);
        this.writer.append("</").append(elem).append(">");
    }

    public void elemSimpleOptional(String elem, Object obj) throws IOException {
        if (obj == null) {
            return;
        }
        elemSimple(elem, obj);
    }

    private void indent() throws IOException {
        int len = elemStack.size();
        for (int i = 0; i < len; i++) {
            this.writer.append("  ");
        }
    }

    private void text(Object obj) throws IOException {
        if (obj instanceof Calendar) {
            text(Timestamp.convert((Calendar) obj));
        } else if (obj instanceof Number) {
            text(String.valueOf(obj));
        } else if (obj instanceof Boolean) {
            text(Boolean.toString((Boolean) obj));
        } else {
            text(String.valueOf(obj));
        }
    }

    public void text(String text) throws IOException {
        int len = text.length();
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            switch (c) {
                case '&':
                    this.writer.append("&amp;");
                    break;
                case '>':
                    this.writer.append("&gt;");
                    break;
                case '<':
                    this.writer.append("&lt;");
                    break;
                default:
                    this.writer.append(c);
            }
        }
    }

    public void xmlPI() throws IOException {
        xmlPI("UTF-8");
    }

    public void xmlPI(String encoding) throws IOException {
        this.writer.append("<?xml");
        attribute("version", "1.0");
        attribute("encoding", encoding);
        this.writer.append("?>\n");
    }
}
