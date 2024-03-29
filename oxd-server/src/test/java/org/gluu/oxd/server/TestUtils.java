package org.gluu.oxd.server;

import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxd.common.ErrorResponse;
import org.gluu.oxd.common.Jackson2;
import org.gluu.oxd.common.model.AuthenticationDetails;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 21/08/2013
 */

public class TestUtils {

    private TestUtils() {
    }

    public static void notEmpty(String str) {
        assertTrue(StringUtils.isNotBlank(str));
    }

    public static void notEmpty(List<String> str) {
        assertTrue(str != null && !str.isEmpty() && StringUtils.isNotBlank(str.get(0)));
    }

    public static ErrorResponse asError(WebApplicationException e) throws IOException {
        final Object entity = e.getResponse().getEntity();
        String entityAsString = null;
        if (entity instanceof String) {
            entityAsString = (String) entity;
        } else if (entity instanceof InputStream) {
            entityAsString = IOUtils.toString((InputStream) entity, "UTF-8");
        } else {
            throw new RuntimeException("Failed to identify type of the entity");
        }
        System.out.println(entityAsString);
        return Jackson2.createJsonMapper().readValue(entityAsString, ErrorResponse.class);
    }

    public static OxdServerConfiguration parseConfiguration(String pathToYaml) throws IOException, ConfigurationException {

        File file = new File(pathToYaml);
        if (!file.exists()) {
            System.out.println("Failed to find yml configuration file. Please check " + pathToYaml);
            System.exit(1);
        }

        DefaultConfigurationFactoryFactory<OxdServerConfiguration> configurationFactoryFactory = new DefaultConfigurationFactoryFactory<>();
        ConfigurationFactory<OxdServerConfiguration> configurationFactory = configurationFactoryFactory.create(OxdServerConfiguration.class, Validators.newValidatorFactory().getValidator(), Jackson.newObjectMapper(), "dw");
        return configurationFactory.build(file);
    }

    public static AuthenticationDetails setAuthenticationDetails(String host, String opHost, String userId, String userSecret, String clientId, String redirectUrls, String nonce, String state, String userInum, String userEmail) {
        AuthenticationDetails authenticationDetails = new AuthenticationDetails();
        authenticationDetails.setHost(host);
        authenticationDetails.setNonce(nonce);
        authenticationDetails.setOpHost(opHost);
        authenticationDetails.setRedirectUrls(redirectUrls);
        authenticationDetails.setState(state);
        authenticationDetails.setUserId(userId);
        authenticationDetails.setUserInum(userInum);
        authenticationDetails.setClientId(clientId);
        authenticationDetails.setUserSecret(userSecret);
        authenticationDetails.setUserEmail(userEmail);
        return authenticationDetails;
    }
}
