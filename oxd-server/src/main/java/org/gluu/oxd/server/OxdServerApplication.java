package org.gluu.oxd.server;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OxdServerApplication extends Application<OxdServerConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(OxdServerApplication.class);

    public static void main(String[] args) {
        try {
            if (args.length > 0 && "stop".equalsIgnoreCase(args[0])) {
                ServerLauncher.shutdown(true);
                return;
            } else {
                List<String> supportedProtocols = Arrays.asList("SSLv3", "TLSv1");

                /*HttpsConnectorFactory factory = new HttpsConnectorFactory();
                factory.setKeyStorePassword("example"); // necessary to avoid a prompt for a password
                factory.setSupportedProtocols(supportedProtocols);
                factory.setExcludedProtocols(Collections.emptyList());*/
                KeyStore keyStore = KeyStore.getInstance("PKCS11");
                try {
                    keyStore.load(null, "example".toCharArray());
                } catch (Exception e) {
                    LOG.error("Error in generating PKCS11 keystore.", e);
                }

                SslContextFactory sslContextFactory = new SslContextFactory.Server();
                sslContextFactory.setKeyStore(keyStore);
                SSLContext sslContext = sslContextFactory.getSslContext();
                sslContext.init(getKeyManagersWithPkcs11(keyStore), null, null);

                sslContextFactory.start();
                new OxdServerApplication().run(args);
            }
        } catch (Throwable e) {
            if (args.length > 0 && "stop".equalsIgnoreCase(args[0])) {
                LOG.error("Failed to stop oxd-server.", e);
            } else {
                LOG.error("Failed to start oxd-server.", e);
            }
            System.exit(1);
        }
    }

    public static KeyManager[] getKeyManagersWithPkcs11(KeyStore keyStore) throws Exception {
        try {
            String keyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyManagerAlgorithm);
            keyManagerFactory.init(keyStore, "example".toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            LOG.error("Error in generating PKCS11 keyManager.", e);
            throw e;
        }
    }

    @Override
    public void initialize(Bootstrap<OxdServerConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(OxdServerConfiguration configuration, Environment environment) {
        ServerLauncher.configureServices(configuration);
        TracingUtil.configureGlobalTracer(configuration, "oxd-server");
        environment.healthChecks().register("dummy", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.healthy();
            }
        });
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(RestResource.class);
    }
}
