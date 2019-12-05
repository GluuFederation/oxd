package org.gluu.oxd.server.persistence;

import org.gluu.oxd.server.service.Rp;

import java.util.Map;
import java.util.Set;

/**
 * @author yuriyz
 */
public interface PersistenceService {

    void create();

    boolean create(Rp rp);

    boolean createState(String state);

    boolean createNonce(String nonce);

    boolean update(Rp rp);

    Rp getRp(String oxdId);

    boolean isStatePresent(String state);

    boolean isNoncePresent(String nonce);

    boolean removeAllRps();

    Set<Rp> getRps();

    Map<String, String> getAllStates();

    Map<String, String> getAllNonce();

    void destroy();

    boolean remove(String oxdId);

    boolean removeState(String state);

    boolean removeNonce(String nonce);
}
