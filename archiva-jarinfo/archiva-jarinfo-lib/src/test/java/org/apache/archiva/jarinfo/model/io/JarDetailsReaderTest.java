/*
 * -------------------------------------------------------------------------
 *
 * (C) Copyright / American Express, Inc. All rights reserved.
 * The contents of this file represent American Express trade secrets and
 * are confidential. Use outside of American Express is prohibited and in
 * violation of copyright law.
 *
 * -------------------------------------------------------------------------
 */

package org.apache.archiva.jarinfo.model.io;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.archiva.jarinfo.model.AssignedId;
import org.apache.archiva.jarinfo.model.BytecodeDetails;
import org.apache.archiva.jarinfo.model.ClassDetail;
import org.apache.archiva.jarinfo.model.EntryDetail;
import org.apache.archiva.jarinfo.model.InspectedIds;
import org.apache.archiva.jarinfo.model.JarDetails;

/**
 * JarDetailsReaderTest
 * 
 * @version $Id$
 */
public class JarDetailsReaderTest extends TestCase {
    private File getFile(String name) {
        return new File("src/test/resources/", name);
    }

    public void testReadDummy() throws IOException {
        File file = getFile("dummy-test.jardetails.xml");
        JarDetailsReader reader = new JarDetailsReader();
        JarDetails details = reader.read(file);
        assertNotNull("details should not be null", details);

        assertEquals("Filename", "dummy-test.jar", details.getFilename());
        assertTimestamp("Timestamp", "Aug 22, 2007 at 1:44 PM", details.getTimestamp());
        assertEquals("Size", 54321, details.getSize());
        assertEquals("Size Uncompressed", 0, details.getSizeUncompressed());
        assertEquals("Sealed", false, details.isSealed());
    }

    public void testReadMavenSharedJar() throws IOException {
        File file = getFile("maven-shared.jar-details.xml");
        JarDetailsReader reader = new JarDetailsReader();
        JarDetails details = reader.read(file);
        assertNotNull("details should not be null", details);

        assertEquals("Filename", "maven-shared-jar-1.0.jar", details.getFilename());
        assertTimestamp("Timestamp", "Dec 8, 2007 at 3:28 PM", details.getTimestamp());
        assertEquals("Size", 37030, details.getSize());
        assertEquals("Size Uncompressed", 74761, details.getSizeUncompressed());
        assertEquals("Sealed", false, details.isSealed());

        AssignedId aid = details.getAssignedId();
        assertNotNull("assigned id should not be null", aid);
        assertEquals("GroupId", "org.apache.maven.shared", aid.getGroupId());
        assertEquals("ArtifactId", "maven-shared-jar", aid.getArtifactId());
        assertEquals("Version", "1.0", aid.getVersion());
        assertNull("Vendor", aid.getVendor());
        assertNull("Name", aid.getName());

        Map<String, String> hashes = details.getHashes();
        assertNotNull("Hashes should not be null", hashes);
        assertEquals("Hashes.size", 2, hashes.size());
        assertEquals("Hash[sha1]", "6ba382560083d0c0f34ece30c868f05d5afe793b", hashes.get("sha-1"));
        assertEquals("Hash[md5]", "2be0f752bea6a42194c401d5f713ba6e", hashes.get("md5"));

        // Entries

        List<EntryDetail> entries = details.getEntries();
        assertNotNull("Entries should not be null", entries);
        assertEquals("Entries.size", 43, entries.size());

        int countDirs = 0;
        int countFiles = 0;
        for (EntryDetail entry : entries) {
            assertNotNull("entry.name should not be null", entry.getName());
            assertNotNull("entry.timestamp should not be null", entry.getTimestamp());
            if (entry.isDirectory()) {
                countDirs++;
                assertEquals("entry[dir].size should be 0", 0, entry.getSize());
            } else {
                countFiles++;
                assertNotNull("entry[file].hashes should not be null", entry.getHashes());
            }
        }
        assertEquals("Entries dir.count", 15, countDirs);
        assertEquals("Entries file.count", 28, countFiles);

        // Bytecode

        BytecodeDetails bytecode = details.getBytecode();
        assertNotNull("Bytecode should not be null", bytecode);
        assertEquals("Bytecode.classes.count", 22, bytecode.getClasses().size());

        for (ClassDetail cdetail : bytecode.getClasses()) {
            assertNotNull("bytecode.classes.detail should not be null", cdetail);
            assertNotNull("classdetail.name should not be null", cdetail.getName());
            assertNotNull("classdetail.version should not be null", cdetail.getClassVersion());
            assertNotNull("classdetail.jdk should not be null", cdetail.getTargetJdk());
            assertNotNull("classdetail.hashes should not be null", cdetail.getHashes());
            assertEquals("classdetail.hashes.size", 1, cdetail.getHashes().size());
        }

        // Inspected

        InspectedIds inspected = details.getInspectedIds();
        assertNotNull("Inspected should not be null", inspected);
        assertEquals("Inspected.groupId.count", 7, inspected.getGroupIdList().size());
        assertEquals("Inspected.artifactId.count", 1, inspected.getArtifactIdList().size());
        assertEquals("Inspected.version.count", 2, inspected.getVersionList().size());
        assertEquals("Inspected.name.count", 1, inspected.getNameList().size());
        assertEquals("Inspected.vendor.count", 0, inspected.getVendorList().size());
    }

    private void assertTimestamp(String msg, String expected, Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String actual = format.format(cal.getTime());
        assertEquals(msg, expected, actual);
    }
}
