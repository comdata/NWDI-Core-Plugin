/**
 *
 */
package org.arachna.netweaver.dctool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.arachna.netweaver.dc.types.BuildVariant;
import org.arachna.netweaver.dc.types.DevelopmentConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit tests for {@link DCToolCommandExecutor}.
 * 
 * @author Dirk Weigenand
 */
public class DCToolCommandExecutorTest {
    /**
     * default encoding.
     */
    private static final String UTF_8 = "UTF-8";

    /**
     * use base 8 for creating file system permissions.
     */
    private static final int OCTAL = 8;

    /**
     * The folder used for testing.
     */
    private static File TEST_DIRECTORY;

    /**
     * Folder where the NWDI tool libs are found.
     */
    private String nwdiToolLibDir;

    /**
     * the {@link DCToolCommandExecutor} instance under test.
     */
    private DCToolCommandExecutor executor;

    /**
     * Create a temporary directory for the test and set up the objects used for
     * this test.
     * 
     * @throws IOException
     *             when the temporary directory for the test could not be
     *             created.
     * @throws InterruptedException
     */
    @BeforeClass
    public static void setUpBeforeClass() throws IOException, InterruptedException {
        TEST_DIRECTORY = Util.createTempDir();

        if (TEST_DIRECTORY == null || !TEST_DIRECTORY.exists()) {
            fail("Could not create " + TEST_DIRECTORY.getAbsolutePath());
        }

        final FilePath testFolder = new FilePath(TEST_DIRECTORY);
        final FilePath dctoolSh = testFolder.child("dc/dctool.sh");
        dctoolSh.write("#!/bin/sh\necho TEST > dtool.out", UTF_8);
        dctoolSh.chmod(Integer.parseInt("0700", OCTAL));

        final FilePath dctoolBat = testFolder.child("dc/dctool.bat");
        dctoolBat.write("@echo on\n\recho TEST > dtool.out\n\r", UTF_8);
    }

    /**
     * set up test fixture (executor).
     * 
     * @throws IOException
     */
    @Before
    public void setUp() throws IOException {
        this.nwdiToolLibDir = String.format("%s%clib", TEST_DIRECTORY.getAbsolutePath(), File.separatorChar);
        this.executor = this.createDCToolCommandExecutor();
    }

    /**
     * Create an executor object for dctool.
     * 
     * @param nwdiToolLibPath
     *            path to the NWDI tool library folder.
     * @return the newly created {@link DCToolCommandExecutor} object.
     * @throws IOException
     */
    private DCToolCommandExecutor createDCToolCommandExecutor() throws IOException {
        final BuildVariant buildVariant = new BuildVariant("default");
        buildVariant
            .addBuildOption(DCToolCommandExecutor.COM_SAP_JDK_HOME_PATH_KEY, JdkHomeAlias.Jdk131Home.toString());

        final JdkHomePaths paths = new JdkHomePaths();
        paths.add(JdkHomeAlias.Jdk131Home, TEST_DIRECTORY.getAbsolutePath());
        paths.add(JdkHomeAlias.Jdk142Home, TEST_DIRECTORY.getAbsolutePath());

        final Writer messages = new OutputStreamWriter(System.out);
        final Launcher launcher = new Launcher.LocalLauncher(new StreamTaskListener(messages));
        final DCToolDescriptor dcToolDescriptor =
            new DCToolDescriptor("developer", "secret", this.nwdiToolLibDir, "dtr", paths);
        final DevelopmentConfiguration config = new DevelopmentConfiguration("Test");
        config.setBuildVariant(buildVariant);

        return new DCToolCommandExecutor(launcher, new FilePath(TEST_DIRECTORY), dcToolDescriptor, config);
    }

    /**
     * @throws IOException
     *             when removing the test directory failed.
     */
    @AfterClass
    public static void tearDownAfterClass() throws IOException {
        // Util.deleteRecursive(TEST_DIRECTORY);
        TEST_DIRECTORY = null;
    }

    /**
     * Clean up fixture.
     */
    @After
    public void tearDown() {
        this.executor = null;
    }

    /**
     * Test method for
     * {@link org.arachna.netweaver.dctool.DCToolCommandExecutor#execute(hudson.Launcher, hudson.FilePath, org.arachna.netweaver.dctool.DCToolCommandBuilder)}
     * .
     */
    @Test
    public final void testExecute() {
        try {
            this.executor = this.createDCToolCommandExecutor();
            this.executor.execute(new DummyDCToolCommandBuilder());
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
            // fail(e.getMessage());
        }
        catch (final InterruptedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.arachna.netweaver.dctool.DCToolCommandExecutor#getJdkPropertyName()}
     * .
     */
    @Test
    public final void testGetJdkPropertyName() {
        assertThat(this.executor.getJdkPropertyName(), is(equalTo(JdkHomeAlias.Jdk131Home)));
    }

    private static final class DummyDCToolCommandBuilder implements DCToolCommandBuilder {
        public List<String> execute() {
            final List<String> commands = new ArrayList<String>();
            commands.add("xxx;");

            return commands;
        }
    }
}