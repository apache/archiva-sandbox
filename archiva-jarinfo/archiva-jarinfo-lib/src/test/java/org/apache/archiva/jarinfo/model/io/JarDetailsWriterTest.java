package org.apache.archiva.jarinfo.model.io;

import java.io.File;
import java.io.StringWriter;
import java.util.Calendar;

import junit.framework.TestCase;

import org.apache.archiva.jarinfo.analysis.JarAnalysis;
import org.apache.archiva.jarinfo.model.JarDetails;
import org.apache.archiva.jarinfo.utils.Timestamp;
import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.xml.sax.SAXException;

/**
 * JarDetailsWriterTest
 * 
 * @version $Id$
 */
public class JarDetailsWriterTest extends TestCase {
    public void testToXml() throws Exception {
        JarDetails details = new JarDetails();
        details.setFilename("dummy-test.jar");
        details.setSize(54321);

        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.AUGUST, 22, 13, 44, 55);
        details.setTimestamp(cal);

        assertDetails("dummy-test.jardetails.xml", details);
    }

    public void testMysteryInspectionSerialize() throws Exception {
        JarAnalysis analysis = new JarAnalysis(true);
        File mysteryFile = new File("src/test/jars", "mystery.jar");
        JarDetails details = analysis.analyze(mysteryFile);
        // Workaround/Hack for timestamp change on svn checkout.
        details.setTimestamp(Timestamp.convert("2007-12-08 15:28:21 UTC"));
        details.getGenerator().setTimestamp(Timestamp.convert("2007-12-11 16:31:43 UTC"));

        assertDetails("mystery.jar-details.xml", details);
    }

    public void testMavenSharedInspectionSerialize() throws Exception {
        JarAnalysis analysis = new JarAnalysis(true);
        File mavenSharedFile = new File("src/test/jars", "maven-shared-jar-1.0.jar");
        JarDetails details = analysis.analyze(mavenSharedFile);
        // Workaround/Hack for timestamp change on svn checkout.
        details.setTimestamp(Timestamp.convert("2007-12-08 15:28:21 UTC"));
        details.getGenerator().setTimestamp(Timestamp.convert("2007-12-11 16:31:43 UTC"));

        assertDetails("maven-shared.jar-details.xml", details);
    }

    public void testJxrNoInspectionSerialize() throws Exception {
        // Set it up as no inspection.
        JarAnalysis analysis = new JarAnalysis(false);
        File jxrFile = new File("src/test/jars", "jxr.jar");
        JarDetails details = analysis.analyze(jxrFile);
        // Workaround/Hack for timestamp change on svn checkout.
        details.setTimestamp(Timestamp.convert("2007-12-08 15:28:21 UTC"));
        details.getGenerator().setTimestamp(Timestamp.convert("2007-12-11 16:31:43 UTC"));

        assertDetails("jxr.jar-details.xml", details);
    }

    private void assertDetails(String expectedContentsFile, JarDetails details) throws Exception {

        StringWriter str = new StringWriter();
        JarDetailsWriter writer = new JarDetailsWriter();
        writer.write(details, str);

        String actualContents = str.toString();
        String expectedContents = FileUtils.readFileToString(new File("src/test/resources/", expectedContentsFile));

        try {
            DetailedDiff diff = new DetailedDiff(new Diff(expectedContents, actualContents));
            if (!diff.similar()) {
                assertEquals(expectedContents, actualContents);
            }
        } catch (SAXException e) {
            System.out.println("[[Actual]]\n\n" + actualContents);
            throw e;
        }
    }

}
