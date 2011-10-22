package org.apache.archiva.jarinfo.model.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.archiva.jarinfo.JarInfoException;
import org.apache.archiva.jarinfo.model.AssignedId;
import org.apache.archiva.jarinfo.model.BytecodeDetails;
import org.apache.archiva.jarinfo.model.ClassDetail;
import org.apache.archiva.jarinfo.model.EntryDetail;
import org.apache.archiva.jarinfo.model.Generator;
import org.apache.archiva.jarinfo.model.IdValue;
import org.apache.archiva.jarinfo.model.InspectedIds;
import org.apache.archiva.jarinfo.model.JarDetails;
import org.apache.archiva.jarinfo.model.xml.AbstractJarDetailsXml;
import org.apache.archiva.jarinfo.model.xml.JarDetailsXmlSerializer;
import org.apache.archiva.jarinfo.utils.EmptyUtils;
import org.apache.archiva.jarinfo.utils.Timestamp;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * JarDetailsWriter - write the details out to XML
 * 
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class JarDetailsWriter extends AbstractJarDetailsXml {

    private void addAssignedId(SimpleXmlWriter xml, AssignedId assignedId) throws IOException {
        if (assignedId == null) {
            return;
        }
        
        if(assignedId.isEmpty()){
            return;
        }

        xml.elemOpen(ASSIGNED_ID);
        xml.elemSimple(GROUP_ID, assignedId.getGroupId());
        xml.elemSimple(ARTIFACT_ID, assignedId.getArtifactId());
        xml.elemSimple(VERSION, assignedId.getVersion());
        xml.elemSimpleOptional(NAME, assignedId.getName());
        xml.elemSimpleOptional(VENDOR, assignedId.getVendor());
        xml.elemClose();
    }

    private void addBytecode(SimpleXmlWriter xml, BytecodeDetails bytecode) throws IOException {
        if (bytecode == null) {
            return;
        }

        if (EmptyUtils.isEmpty(bytecode.getClasses())) {
            return;
        }

        // Collect packages (used for count at start of bytecode element)
        Set<String> packages = new TreeSet<String>();
        int countClasses = 0;
        int countPackages = 0;

        for (ClassDetail cdetail : bytecode.getClasses()) {
            countClasses++;
            packages.add(cdetail.getPackage());
        }

        countPackages = packages.size();

        // Write bytecode element
        xml.elemOpen(BYTECODE, new String[][] { {COUNT_CLASSES, String.valueOf(countClasses) },
            {COUNT_PACKAGES, String.valueOf(countPackages) } });

        addHashes(xml, bytecode.getHashes());
        xml.elemSimple(JDK, bytecode.getRequiredJdk());
        xml.elemSimple(DEBUG, bytecode.hasDebug());

        // Write classes
        for (ClassDetail cdetail : bytecode.getClasses()) {
            xml.elemOpen(CLASS, new String[][] { {NAME, cdetail.getName() }, {VERSION, cdetail.getClassVersion() },
                {JDK, cdetail.getTargetJdk() }, {DEBUG, Boolean.toString(cdetail.hasDebug()) } });

            addHashes(xml, cdetail.getHashes());

            // Write imports
            for (String mport : cdetail.getImports()) {
                xml.elemSimple(IMPORT, mport);
            }

            // Write methods
            for (String method : cdetail.getMethods()) {
                xml.elemSimple(METHOD, method);
            }

            xml.elemClose();
        }

        // Write packages
        for (String pcage : packages) {
            xml.elemSimple(PACKAGE, pcage);
        }

        xml.elemClose();
    }

    private void addEntries(SimpleXmlWriter xml, List<EntryDetail> entries) throws IOException {
        if (EmptyUtils.isEmpty(entries)) {
            return;
        }

        // Count the dirs / files
        int countDirs = 0;
        int countFiles = 0;

        for (EntryDetail entry : entries) {
            if (entry.isDirectory()) {
                countDirs++;
            } else {
                countFiles++;
            }
        }

        // Open the xml
        xml.elemOpen("entries", new String[][] { {COUNT_DIRS, String.valueOf(countDirs) },
            {COUNT_FILES, String.valueOf(countFiles) }, {COUNT_TOTAL, String.valueOf(countDirs + countFiles) } });

        // Dump the Directory List
        for (EntryDetail entry : entries) {
            if (entry.isDirectory()) {
                xml.elemEmpty(DIRECTORY, new String[][] { {NAME, entry.getName() },
                    {TIMESTAMP, Timestamp.convert(entry.getTimestamp()) } });
            }
        }

        // Dump the File List
        for (EntryDetail entry : entries) {
            if (!entry.isDirectory()) {
                xml.elemOpen(FILE, new String[][] { {NAME, entry.getName() }, {SIZE, String.valueOf(entry.getSize()) },
                    {TIMESTAMP, Timestamp.convert(entry.getTimestamp()) } });
                addHashes(xml, entry.getHashes());
                xml.elemClose();
            }
        }

        xml.elemClose();
    }

    private void addGenerator(SimpleXmlWriter xml, Generator generator) throws IOException {
        if (generator == null) {
            return;
        }

        if (generator.isEmpty()) {
            return;
        }

        xml.elemOpen("generator");
        xml.elemSimple(NAME, generator.getName());
        xml.elemSimple(VERSION, generator.getVersion());
        xml.elemSimple(TIMESTAMP, generator.getTimestamp());
        xml.elemClose();
    }

    private void addHashes(SimpleXmlWriter xml, Map<String, String> hashes) throws IOException {
        String hashSha1 = hashes.get("sha-1");
        if (hashSha1 != null) {
            xml.elemSimple(HASH, new String[][] {{ALGORITHM, SHA1 } }, hashSha1);
        }

        String hashMd5 = hashes.get("md5");
        if (hashMd5 != null) {
            xml.elemSimple(HASH, new String[][] {{ALGORITHM, MD5 } }, hashMd5);
        }
    }

    private void addIdValueList(SimpleXmlWriter xml, String collectionName, String entryName, List<IdValue> valueList)
            throws IOException {
        if (EmptyUtils.isEmpty(valueList)) {
            return;
        }

        xml.elemOpen(collectionName);
        for (IdValue iv : valueList) {
            xml.elemOpen(entryName);
            xml.elemSimple(VALUE, iv.getValue());
            xml.elemSimple(WEIGHT, iv.getWeight());
            for (String origin : iv.getOrigins()) {
                xml.elemSimple(ORIGIN, origin);
            }
            xml.elemClose();
        }
        xml.elemClose();
    }

    private void addInspected(SimpleXmlWriter xml, InspectedIds inspected) throws IOException {
        if (inspected == null) {
            return;
        }

        if (inspected.isEmpty()) {
            return;
        }

        xml.elemOpen(INSPECTED);

        addIdValueList(xml, GROUP_IDS, GROUP_ID, inspected.getGroupIdList());
        addIdValueList(xml, ARTIFACT_IDS, ARTIFACT_ID, inspected.getArtifactIdList());
        addIdValueList(xml, VERSIONS, VERSION, inspected.getVersionList());
        addIdValueList(xml, NAMES, NAME, inspected.getNameList());
        addIdValueList(xml, VENDORS, VENDOR, inspected.getVendorList());

        xml.elemClose();
    }

    public void write(JarDetails details, File outputFile) throws IOException, JarInfoException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(outputFile);
            write(details, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void write(JarDetails details, Writer writer) throws IOException, JarInfoException {
        SimpleXmlWriter xml = new SimpleXmlWriter(writer);
        xml.xmlPI("UTF-8");
        xml.elemOpen(DOC_ROOT, new String[][] { {"xmlns", JARINFO_NAMESPACE_ID },
            {"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" },
            {"xsi:schemaLocation", "http://archiva.apache.org/jarinfo-v1_0_0.xsd" } });

        addAssignedId(xml, details.getAssignedId());

        xml.elemSimple(FILENAME, details.getFilename());
        xml.elemSimple(TIMESTAMP, details.getTimestamp());
        xml.elemSimple(SIZE, details.getSize());
        xml.elemSimple(SIZE_UNCOMPRESSED, details.getSizeUncompressed());

        addHashes(xml, details.getHashes());

        xml.elemSimple(SEALED, details.isSealed());

        addGenerator(xml, details.getGenerator());
        addEntries(xml, details.getEntries());
        addBytecode(xml, details.getBytecode());
        addInspected(xml, details.getInspectedIds());

        xml.elemClose();

        writer.flush();
    }

    public void writeDoc(JarDetails details, Writer writer) throws IOException, JarInfoException {
        Document doc = JarDetailsXmlSerializer.serialize(details);

        // Write it out to disk.
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding("UTF-8");
        XMLWriter xmlwriter = new XMLWriter(writer, outformat);
        xmlwriter.write(doc);
        xmlwriter.flush();
    }

}
