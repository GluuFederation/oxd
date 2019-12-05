package org.gluu.oxd.server.persistence;

import com.google.inject.Inject;
import org.gluu.oxd.server.OxdServerConfiguration;
import org.gluu.oxd.server.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TimerTask;

public class H2DBCleanerService extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(H2DBCleanerService.class);

    private OxdServerConfiguration conf;

    private PersistenceService persistenceService;

    @Inject
    public H2DBCleanerService(ConfigurationService configurationService, PersistenceService persistenceService) {
        this.conf = configurationService.get();
        this.persistenceService = persistenceService;
    }

    public void run() {
        LOG.debug("Starting state clean-up task.");
        Map<String, String> stateMap = persistenceService.getAllStates();

        stateMap.forEach(
                (state, createdAt) -> {
                    if (hasStateOrNoanceExpired(createdAt, conf.getStateExpirationInMinutes())) {
                        LOG.debug("State has expired. Removing from storage. State: " + state);
                        persistenceService.removeState(state);
                    }
                });
        LOG.debug("Starting nonce clean-up task.");
        Map<String, String> nonceMap = persistenceService.getAllNonce();
        nonceMap.forEach(
                (nonce, createdAt) -> {
                    if (hasStateOrNoanceExpired(createdAt, conf.getNonceExpirationInMinutes())) {
                        LOG.debug("Nonce has expired. Removing from storage. Nonce: " + nonce);
                        persistenceService.removeNonce(nonce);
                    }
                });
        LOG.debug("State/Nonce clean-up process ends.");
    }

    public static boolean hasStateOrNoanceExpired(String createdAtTimeStamp, int expirationInMinutes) {
        Timestamp createTs = new Timestamp(Long.valueOf(createdAtTimeStamp).longValue());

        Timestamp currentTs = new Timestamp(System.currentTimeMillis());
        long millisecTimeDiff = currentTs.getTime() - createTs.getTime();
        int secTimeDiff = (int) millisecTimeDiff / 1000;
        int minTimeDiff = (secTimeDiff % 3600) / 60;

        return (minTimeDiff >= expirationInMinutes);
    }
}
