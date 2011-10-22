package org.apache.archiva.jarinfo.model.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Calendar;

import org.apache.archiva.jarinfo.model.AssignedId;
import org.apache.archiva.jarinfo.model.BytecodeDetails;
import org.apache.archiva.jarinfo.model.ClassDetail;
import org.apache.archiva.jarinfo.model.EntryDetail;
import org.apache.archiva.jarinfo.model.IdValue;
import org.apache.archiva.jarinfo.model.InspectedIds;
import org.apache.archiva.jarinfo.model.JarDetails;
import org.apache.archiva.jarinfo.model.xml.AbstractJarDetailsXml;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

public class JarDetailsReader extends AbstractJarDetailsXml {
    public JarDetails read(File file) throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            return read(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Read and parse the jarinfo xml.
     * 
     * @param reader
     *                the reader to read from.
     * @throws MojoExecutionException
     *                 if there was a problem parsing.
     */
    public JarDetails read(Reader reader) throws IOException {
        Digester digester = new Digester();
        digester.setNamespaceAware(false);
        ConvertUtils.register(new TimestampConverter(), Calendar.class);

        String path;
        digester.addObjectCreate(DOC_ROOT, JarDetails.class);

        // Base variables
        path = DOC_ROOT;
        addNestedElem(digester, path, FILENAME, "setFilename", String.class);
        addNestedElem(digester, path, TIMESTAMP, "setTimestamp", Calendar.class);
        addNestedElem(digester, path, SIZE, "setSize", Long.class);
        addNestedElem(digester, path, SIZE_UNCOMPRESSED, "setSizeUncompressed", Long.class);
        addNestedElem(digester, path, SEALED, "setSealed", Boolean.class);
        addHashes(digester, path);

        // Assigned Id
        path = DOC_ROOT + "/" + ASSIGNED_ID;
        digester.addObjectCreate(path, AssignedId.class);
        digester.addSetNestedProperties(path);
        digester.addSetNext(path, "setAssignedId", AssignedId.class.getName());

        // Entries
        path = DOC_ROOT + "/" + ENTRIES + "/" + DIRECTORY;
        digester.addObjectCreate(path, EntryDetail.class);
        digester.addSetProperties(path);
        digester.addSetNext(path, "addDirectory", EntryDetail.class.getName());

        path = DOC_ROOT + "/" + ENTRIES + "/" + FILE;
        digester.addObjectCreate(path, EntryDetail.class);
        digester.addSetProperties(path);
        addHashes(digester, path);
        digester.addSetNext(path, "addFile", EntryDetail.class.getName());

        // Bytecode
        path = DOC_ROOT + "/" + BYTECODE;
        digester.addObjectCreate(path, BytecodeDetails.class);
        addHashes(digester, path);
        addNestedElem(digester, path, "jdk", "setRequiredJdk", String.class);
        addNestedElem(digester, path, "debug", "setDebug", Boolean.class);
        digester.addSetNext(path, "setBytecode", BytecodeDetails.class.getName());

        // Bytecode / Class
        path = DOC_ROOT + "/" + BYTECODE + "/" + CLASS;
        digester.addObjectCreate(path, ClassDetail.class);
        digester.addSetProperties(path);
        digester.addSetProperties(path, "jdk", "targetJdk");
        digester.addSetProperties(path, "version", "classVersion");
        addHashes(digester, path);
        digester.addCallMethod(path + "/" + IMPORT, "addImport", 1, new Class[] {String.class });
        digester.addCallParam(path + "/" + IMPORT, 0);
        digester.addCallMethod(path + "/" + METHOD, "addMethod", 1, new Class[] {String.class });
        digester.addCallParam(path + "/" + METHOD, 0);
        digester.addSetNext(path, "addClass", ClassDetail.class.getName());

        // Inspected
        path = DOC_ROOT + "/" + INSPECTED;
        digester.addObjectCreate(path, InspectedIds.class);
        addIdValueList(digester, path, "addGroupId", GROUP_IDS, GROUP_ID);
        addIdValueList(digester, path, "addArtifactId", ARTIFACT_IDS, ARTIFACT_ID);
        addIdValueList(digester, path, "addVersion", VERSIONS, VERSION);
        addIdValueList(digester, path, "addName", NAMES, NAME);
        addIdValueList(digester, path, "addVendor", VENDORS, VENDOR);
        digester.addSetNext(path, "setInspectedIds", InspectedIds.class.getName());

        // Parse it
        try {
            return (JarDetails) digester.parse(reader);
        } catch (SAXException e) {
            throw new IOException("Unable to parse jardetails: " + e.getMessage(), e);
        }
    }

    private void addIdValueList(Digester digester, String path, String method, String collectionId, String entryId) {
        String entryPath = path + "/" + collectionId + "/" + entryId;
        digester.addObjectCreate(entryPath, IdValue.class);
        addNestedElem(digester, entryPath, VALUE, "setValue", String.class);
        addNestedElem(digester, entryPath, WEIGHT, "setWeight", Long.class);
        addNestedElem(digester, entryPath, ORIGIN, "addOrigin", String.class);

        digester.addSetNext(entryPath, method, IdValue.class.getName());
    }

    private void addNestedElem(Digester digester, String path, String element, String property, Class clazz) {
        digester.addCallMethod(path + "/" + element, property, 1, new Class[] {clazz });
        digester.addCallParam(path + "/" + element, 0);
    }

    private void addHashes(Digester digester, String path) {
        digester.addCallMethod(path + "/hash", "setHash", 2, new Class[] {String.class, String.class });
        digester.addCallParam(path + "/hash", 0, "algorithm");
        digester.addCallParam(path + "/hash", 1);
    }
}
