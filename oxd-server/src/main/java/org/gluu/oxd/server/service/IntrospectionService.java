package org.gluu.oxd.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.UriBuilder;

import org.gluu.oxauth.model.common.IntrospectionResponse;
import org.gluu.oxauth.model.uma.UmaMetadata;
import org.gluu.oxd.common.introspection.CorrectRptIntrospectionResponse;
import org.gluu.oxd.common.introspection.CorrectUmaPermission;
import org.gluu.oxd.server.introspection.BackCompatibleIntrospectionResponse;
import org.gluu.oxd.server.introspection.BackCompatibleIntrospectionService;
import org.gluu.oxd.server.introspection.BadRptIntrospectionResponse;
import org.gluu.oxd.server.introspection.BadRptIntrospectionService;
import org.gluu.oxd.server.introspection.BadUmaPermission;
import org.gluu.oxd.server.introspection.ClientFactory;
import org.gluu.oxd.server.introspection.CorrectRptIntrospectionService;
import org.gluu.oxd.server.op.OpClientFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.ReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * @author yuriyz
 */
public class IntrospectionService {

    private static final Logger LOG = LoggerFactory.getLogger(IntrospectionService.class);

    private HttpService httpService;
    private UmaTokenService umaTokenService;
    private DiscoveryService discoveryService;
    private OpClientFactory opClientFactory;

    @Inject
    public IntrospectionService(HttpService httpService, UmaTokenService umaTokenService, DiscoveryService discoveryService, OpClientFactory opClientFactory) {
        this.httpService = httpService;
        this.umaTokenService = umaTokenService;
        this.discoveryService = discoveryService;
        this.opClientFactory = opClientFactory;
    }

    public IntrospectionResponse introspectToken(String oxdId, String accessToken) {
        return introspectToken(oxdId, accessToken, true);
    }

    private IntrospectionResponse introspectToken(String oxdId, String accessToken, boolean retry) {
        final String introspectionEndpoint = discoveryService.getConnectDiscoveryResponseByOxdId(oxdId).getIntrospectionEndpoint();

        final ResteasyClient client = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).httpEngine(httpService.getClientEngine()).build();
        final ResteasyWebTarget target = client.target(UriBuilder.fromPath(introspectionEndpoint));
        final org.gluu.oxauth.client.service.IntrospectionService introspectionService = target.proxy(org.gluu.oxauth.client.service.IntrospectionService.class);

        try {
            IntrospectionResponse response = introspectionService.introspectToken("Bearer " + umaTokenService.getOAuthToken(oxdId).getToken(), accessToken);
            return response; // we need local variable to force convertion here
        } catch (ClientErrorException e) {
            int status = e.getResponse().getStatus();
            LOG.debug("Failed to introspect token. Entity: " + e.getResponse().readEntity(String.class) + ", status: " + status, e);
            if (retry && (status == 400 || status == 401)) {
                LOG.debug("Try maybe OAuthToken is lost on AS, force refresh OAuthToken and re-try ...");
                umaTokenService.obtainOauthToken(oxdId); // force to refresh OAuthToken
                return introspectToken(oxdId, accessToken, false);
            } else {
                throw e;
            }
        } catch (Throwable e) {
            LOG.trace("Exception during access token introspection.", e);
            if (e instanceof ReaderException) { // dummy construction but checked JsonParseException is thrown inside jackson provider, so we don't have choice
                // trying to handle compatiblity issue.
                LOG.trace("Trying to handle compatibility issue ...");
                BackCompatibleIntrospectionService backCompatibleIntrospectionService = ClientFactory.instance().createBackCompatibleIntrospectionService(introspectionEndpoint, httpService.getClientEngine());
                BackCompatibleIntrospectionResponse backResponse = backCompatibleIntrospectionService.introspectToken("Bearer " + umaTokenService.getOAuthToken(oxdId).getToken(), accessToken);
                LOG.trace("Handled compatibility issue. Response: " + backResponse);

                IntrospectionResponse response = new IntrospectionResponse();
                response.setSub(backResponse.getSubject());
                response.setAudience(backResponse.getAudience());
                response.setTokenType(backResponse.getTokenType());
                response.setActive(backResponse.isActive());
                response.setScope(backResponse.getScope());
                if (!backResponse.getScope().isEmpty()) {
                    response.setScope(backResponse.getScope());
                }
                response.setIssuer(backResponse.getIssuer());
                response.setUsername(backResponse.getUsername());
                response.setClientId(backResponse.getClientId());
                response.setJti(backResponse.getJti());
                response.setAcrValues(backResponse.getAcrValues());
                response.setExpiresAt(dateToSeconds(backResponse.getExpiresAt()));
                response.setIssuedAt(dateToSeconds(backResponse.getIssuedAt()));

                return response;
            }
            throw e;
        }
    }

    public CorrectRptIntrospectionResponse introspectRpt(String oxdId, String rpt) {
        return introspectRpt(oxdId, rpt, true);
    }

    private CorrectRptIntrospectionResponse introspectRpt(String oxdId, String rpt, boolean retry) {
        final UmaMetadata metadata = discoveryService.getUmaDiscoveryByOxdId(oxdId);

        try {
            final CorrectRptIntrospectionService introspectionService = opClientFactory.createClientFactory().createCorrectRptStatusService(metadata, httpService.getClientEngine());
            return introspectionService.requestRptStatus("Bearer " + umaTokenService.getPat(oxdId).getToken(), rpt, "");
        } catch (ClientErrorException e) {
            int httpStatus = e.getResponse().getStatus();
            if (retry && (httpStatus == 401 || httpStatus == 400 || httpStatus == 403)) {
                umaTokenService.obtainPat(oxdId).getToken();
                return introspectRpt(oxdId, rpt, false);
            } else {
                throw e;
            }
        } catch (Throwable e) {
            LOG.trace("Exception during rpt introspection, message: " + e.getMessage());
            if (e instanceof ReaderException) { // dummy construction but checked JsonParseException is thrown inside jackson provider, so we don't have choice
                // trying to handle compatiblity issue.
                LOG.trace("Trying to handle compatibility issue ...");
                BadRptIntrospectionService badService = ClientFactory.instance().createBadRptStatusService(metadata, httpService.getClientEngine());
                BadRptIntrospectionResponse badResponse = badService.requestRptStatus("Bearer " + umaTokenService.getPat(oxdId).getToken(), rpt, "");

                LOG.trace("Handled compatibility issue. Response: " + badResponse);

                final List<CorrectUmaPermission> permissions = new ArrayList<>();

                CorrectRptIntrospectionResponse response = new CorrectRptIntrospectionResponse();
                response.setActive(badResponse.getActive());
                response.setClientId(badResponse.getClientId());
                response.setJti(badResponse.getJti());
                response.setExpiresAt(dateToSeconds(badResponse.getExpiresAt()));
                response.setIssuedAt(dateToSeconds(badResponse.getIssuedAt()));
                response.setNbf(dateToSeconds(badResponse.getNbf()));
                response.setPermissions(permissions);

                if (badResponse.getPermissions() != null) {
                    for (BadUmaPermission badPermission : badResponse.getPermissions()) {
                        CorrectUmaPermission p = new CorrectUmaPermission();
                        p.setExpiresAt(dateToSeconds(badPermission.getExpiresAt()));
                        p.setResourceId(badPermission.getResourceId());
                        p.setScopes(badPermission.getScopes());

                        permissions.add(p);
                    }
                }

                return response;
            }
            throw e;
        }
    }

    public static Integer dateToSeconds(Date date) {
        return date != null ? (int) (date.getTime() / 1000) : null;
    }
}
