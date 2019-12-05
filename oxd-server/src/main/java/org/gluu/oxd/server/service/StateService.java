package org.gluu.oxd.server.service;

import com.google.inject.Inject;
import org.gluu.oxd.server.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author Yuriy Zabrovarnyy
 */

public class StateService {

    private static final Logger LOG = LoggerFactory.getLogger(StateService.class);

    private PersistenceService persistenceService;

    private final SecureRandom random = new SecureRandom();

    @Inject
    public StateService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public String generateState() {
        return putState(generateSecureString());
    }

    public String generateNonce() {
        return putNonce(generateSecureString());
    }

    public String generateSecureString() {
        return new BigInteger(130, random).toString(32);
    }

    public boolean isStateValid(String state) {
        return persistenceService.isStatePresent(state);
    }

    public boolean isNonceValid(String nonce) {
        return persistenceService.isNoncePresent(nonce);
    }

    public void invalidateState(String state) {
        persistenceService.removeState(state);
    }

    public void invalidateNonce(String nonce) {
        persistenceService.removeNonce(nonce);
    }

    public String putState(String state) {
        persistenceService.createState(state);
        return state;
    }

    public String putNonce(String nonce) {
        persistenceService.createNonce(nonce);
        return nonce;
    }
}
