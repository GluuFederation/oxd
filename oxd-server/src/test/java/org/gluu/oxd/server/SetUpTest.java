package org.gluu.oxd.server;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxd.common.response.RegisterSiteResponse;
import org.gluu.oxd.server.persistence.service.PersistenceService;
import org.gluu.oxd.server.service.RpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

/**
 * Main class to set up and tear down suite.
 *
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 21/08/2013
 */
public class SetUpTest {

    private static final Logger LOG = LoggerFactory.getLogger(SetUpTest.class);

    public static DropwizardTestSupport<OxdServerConfiguration> SUPPORT = null;


    @Parameters({"host", "opHost", "redirectUrls"})
    @BeforeSuite
    public static void beforeSuite(String host, String opHost, String redirectUrls) {
        try {
            LOG.info("Running beforeSuite ...");
            ServerLauncher.setSetUpSuite(true);

            SUPPORT = new DropwizardTestSupport<OxdServerConfiguration>(OxdServerApplication.class,
                    ResourceHelpers.resourceFilePath("oxd-server-jenkins.yml"),
                    ConfigOverride.config("server.applicationConnectors[0].port", "0") // Optional, if not using a separate testing-specific configuration file, use a randomly selected port
            );
            SUPPORT.before();
            LOG.info("HTTP server started.");

            removeExistingRps();
            LOG.info("Existing RPs are removed.");

            RegisterSiteResponse setupClient = SetupClientTest.setupClient(Tester.newClient(host), opHost, redirectUrls);
            Tester.setSetupClient(setupClient, host, opHost);
            LOG.info("SETUP_CLIENT is set in Tester.");

            Preconditions.checkNotNull(Tester.getAuthorization());
            LOG.info("Tester's authorization is set.");

            setupSwaggerSuite(Tester.getTargetHost(host), opHost, redirectUrls);
            LOG.info("Finished beforeSuite!");
        } catch (Exception e) {
            LOG.error("Failed to start suite.", e);
            throw new AssertionError("Failed to start suite.");
        }
    }

    private static void setupSwaggerSuite(String host, String opHost, String redirectUrls) {
        try {
            if (StringUtils.countMatches(host, ":") < 2 && "http://localhost".equalsIgnoreCase(host) || "http://127.0.0.1".equalsIgnoreCase(host) ) {
                host = host + ":" + SetUpTest.SUPPORT.getLocalPort();
            }
            io.swagger.client.api.SetUpTest.beforeSuite(host, opHost, redirectUrls); // manual swagger tests setup
            io.swagger.client.api.SetUpTest.setTokenProtectionEnabled(SUPPORT.getConfiguration().getProtectCommandsWithAccessToken());
        } catch (Throwable e) {
            LOG.error("Failed to setup swagger suite.");
        }
    }

    private static void removeExistingRps() {
        try {
            ServerLauncher.getInjector().getInstance(PersistenceService.class).create();
            ServerLauncher.getInjector().getInstance(RpService.class).removeAllRps();
            ServerLauncher.getInjector().getInstance(RpService.class).load();
            LOG.debug("Finished removeExistingRps successfullly.");
        } catch (Exception e) {
            LOG.error("Failed to removed existing RPs.", e);
        }
    }

    @AfterSuite
    public static void afterSuite() {
        try {
            LOG.debug("Running afterSuite ...");
            SUPPORT.after();
            ServerLauncher.shutdown(false);
            LOG.debug("HTTP server is successfully stopped.");
        } catch (Exception e) {
            LOG.error("Failed to stop HTTP server.", e);
        }
    }

}
