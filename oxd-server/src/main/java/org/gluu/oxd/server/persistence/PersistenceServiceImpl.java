package org.gluu.oxd.server.persistence;

import com.google.inject.Inject;
import org.gluu.oxd.server.service.ConfigurationService;
import org.gluu.oxd.server.service.Rp;

import java.util.Map;
import java.util.Set;
import java.util.Timer;

/**
 * @author Yuriy Zabrovarnyy
 */

public class PersistenceServiceImpl implements PersistenceService {

    private ConfigurationService configurationService;
    private SqlPersistenceProvider sqlProvider;
    private PersistenceService persistenceService;

    @Inject
    public PersistenceServiceImpl(SqlPersistenceProvider sqlProvider, ConfigurationService configurationService) {
        this.sqlProvider = sqlProvider;
        this.configurationService = configurationService;
    }

    public void create() {
        persistenceService = createServiceInstance();
        persistenceService.create();
    }

    private PersistenceService createServiceInstance() {
        String storage = configurationService.getConfiguration().getStorage();
        if ("h2".equalsIgnoreCase(storage)) {
            setTimerForDBCleanUpTask();

            return new SqlPersistenceServiceImpl(sqlProvider, configurationService);
        } else if ("redis".equalsIgnoreCase(storage)) {
            return new RedisPersistenceService(configurationService.getConfiguration());
        }
        throw new RuntimeException("Failed to create persistence provider. Unrecognized storage specified: " + storage + ", full configuration: " + configurationService.get());
    }

    public void setTimerForDBCleanUpTask() {
        Timer timer = new Timer();
        H2DBCleanerService h2DBCleanerService = new H2DBCleanerService(configurationService, new SqlPersistenceServiceImpl(sqlProvider, configurationService));
        timer.schedule(h2DBCleanerService, 1000 * 60 * 5, 1000 * 60 * 5);
    }

    public boolean create(Rp rp) {
        return persistenceService.create(rp);
    }

    public boolean createState(String state) {
        return persistenceService.createState(state);
    }

    public boolean createNonce(String nonce) {
        return persistenceService.createNonce(nonce);
    }

    public boolean isStatePresent(String state) {
        return persistenceService.isStatePresent(state);
    }

    public boolean isNoncePresent(String nonce) {
        return persistenceService.isNoncePresent(nonce);
    }

    public boolean update(Rp rp) {
        return persistenceService.update(rp);
    }

    public Rp getRp(String oxdId) {
        return persistenceService.getRp(oxdId);
    }

    public boolean removeAllRps() {
        return persistenceService.removeAllRps();
    }

    public Set<Rp> getRps() {
        return persistenceService.getRps();
    }

    public Map<String, String> getAllStates() {
        return persistenceService.getAllStates();
    }

    public Map<String, String> getAllNonce() {
        return persistenceService.getAllNonce();
    }

    public boolean removeState(String state) {
        return persistenceService.removeState(state);
    }

    public boolean removeNonce(String nonce) {
        return persistenceService.removeNonce(nonce);
    }


    public void destroy() {
        persistenceService.destroy();
    }

    @Override
    public boolean remove(String oxdId) {
        return persistenceService.remove(oxdId);
    }
}
