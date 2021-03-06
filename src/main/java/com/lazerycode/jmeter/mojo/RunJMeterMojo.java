package com.lazerycode.jmeter.mojo;

import com.lazerycode.jmeter.json.TestConfig;
import com.lazerycode.jmeter.testrunner.TestManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

/**
 * Goal that runs jmeter based on configuration defined in your pom.<br/>
 * This goal runs within Lifecycle phase {@link LifecyclePhase#INTEGRATION_TEST}.
 */
@Mojo(name = "jmeter", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunJMeterMojo extends AbstractJMeterMojo {

    /**
     * Run all the JMeter tests.
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    @Override
    public void doExecute() throws MojoExecutionException {
        getLog().info(" ");
        getLog().info(LINE_SEPARATOR);
        getLog().info(" P E R F O R M A N C E    T E S T S");
        getLog().info(LINE_SEPARATOR);
        getLog().info(" ");

        if (!testFilesDirectory.exists()) {
            getLog().info("<testFilesDirectory>" + testFilesDirectory.getAbsolutePath() + "</testFilesDirectory> does not exist...");
            getLog().info("Performance tests skipped!");
            getLog().info(" ");
            return;
        }

        TestConfig testConfig = new TestConfig(new File(testConfigFile));
        JMeterConfigurationHolder configuration = JMeterConfigurationHolder.getInstance();
        remoteConfig.setPropertiesMap(configuration.getPropertiesMap());
        jMeterProcessJVMSettings.setHeadlessDefaultIfRequired();
        copyFilesInTestDirectory(testFilesDirectory, testFilesBuildDirectory);
        TestManager jMeterTestManager = new TestManager()
                .setBaseTestArgs(computeJMeterArgumentsArray(true, testConfig.getResultsOutputIsCSVFormat()))
                .setTestFilesDirectory(testFilesBuildDirectory)
                .setTestFilesIncluded(testFilesIncluded)
                .setTestFilesExcluded(testFilesExcluded)
                .setRemoteServerConfiguration(remoteConfig)
                .setSuppressJMeterOutput(suppressJMeterOutput)
                .setBinDir(configuration.getWorkingDirectory())
                .setJMeterProcessJVMSettings(jMeterProcessJVMSettings)
                .setRuntimeJarName(configuration.getRuntimeJarName())
                .setReportDirectory(reportDirectory)
                .setGenerateReports(generateReports)
                .setPostTestPauseInSeconds(postTestPauseInSeconds);
        if (proxyConfig != null) {
            getLog().info(this.proxyConfig.toString());
        }

        testConfig.setResultsFileLocations(jMeterTestManager.executeTests());
        testConfig.writeResultFilesConfigTo(testConfigFile);
    }
}