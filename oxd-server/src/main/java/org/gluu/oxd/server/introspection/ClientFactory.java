package org.gluu.oxd.server.introspection;

import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.core.UriBuilder;

import org.gluu.oxauth.model.uma.UmaMetadata;

/**
 * @author yuriyz
 */
public class ClientFactory {
    private final static ClientFactory INSTANCE = new ClientFactory();

    private ClientFactory() {
    }

    public static ClientFactory instance() {
        return INSTANCE;
    }

    public BackCompatibleIntrospectionService createBackCompatibleIntrospectionService(String url) {
        return ProxyFactory.create(BackCompatibleIntrospectionService.class, url);
    }

    public BackCompatibleIntrospectionService createBackCompatibleIntrospectionService(String url, ClientHttpEngine clientEngine) {
        final ResteasyClient client = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).httpEngine(clientEngine).build();
        final ResteasyWebTarget target = client.target(UriBuilder.fromPath(url));

        return target.proxy(BackCompatibleIntrospectionService.class);
    }

    public BadRptIntrospectionService createBadRptStatusService(UmaMetadata metadata) {
        return ProxyFactory.create(BadRptIntrospectionService.class, metadata.getIntrospectionEndpoint());
    }

    public BadRptIntrospectionService createBadRptStatusService(UmaMetadata metadata, ClientHttpEngine clientEngine) {
        final ResteasyClient client = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).httpEngine(clientEngine).build();
        final ResteasyWebTarget target = client.target(UriBuilder.fromPath(metadata.getIntrospectionEndpoint()));

        return target.proxy(BadRptIntrospectionService.class);
    }

    public CorrectRptIntrospectionService createCorrectRptStatusService(UmaMetadata metadata) {
        return ProxyFactory.create(CorrectRptIntrospectionService.class, metadata.getIntrospectionEndpoint());
    }

    public CorrectRptIntrospectionService createCorrectRptStatusService(UmaMetadata metadata, ClientHttpEngine clientEngine) {
        final ResteasyClient client = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).httpEngine(clientEngine).build();
        final ResteasyWebTarget target = client.target(UriBuilder.fromPath(metadata.getIntrospectionEndpoint()));

        return target.proxy(CorrectRptIntrospectionService.class);
    }
}
