package org.gluu.oxd.server.persistence;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.gluu.oxd.common.Jackson2;
import org.gluu.oxd.server.OxdServerConfiguration;
import org.gluu.oxd.server.service.ConfigurationService;
import org.gluu.oxd.server.service.MigrationService;
import org.gluu.oxd.server.service.Rp;
import org.h2.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * @author yuriyz
 */
public class SqlPersistenceServiceImpl implements PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(SqlPersistenceServiceImpl.class);

    private SqlPersistenceProvider provider;

    private OxdServerConfiguration conf;

    @Inject
    public SqlPersistenceServiceImpl(SqlPersistenceProvider provider, ConfigurationService configurationService) {
        this.provider = provider;
        this.conf = configurationService.get();
    }

    public void create() {
        provider.onCreate();

        createSchema();
    }

    private void createSchema() {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();

            stmt.addBatch("create table if not exists rp(id varchar(36) primary key, data varchar(65534))");
            stmt.addBatch("create table if not exists state(state varchar(36), created_at varchar(50))");
            stmt.addBatch("create table if not exists nonce(nonce varchar(36), created_at varchar(50))");

            stmt.executeBatch();

            stmt.close();
            conn.commit();

            LOG.debug("Schema created successfully.");
        } catch (Exception e) {
            LOG.error("Failed to create schema. Error: " + e.getMessage(), e);
            rollbackSilently(conn);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean createState(String state) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement query = conn.prepareStatement("insert into state(state, created_at) values(?, ?)");
            query.setString(1, state);
            query.setString(2, String.valueOf(System.currentTimeMillis()));
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("State created successfully. State : " + state);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to create state: " + state, e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean createNonce(String nonce) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement query = conn.prepareStatement("insert into nonce(nonce, created_at) values(?, ?)");
            query.setString(1, nonce);
            query.setString(2, String.valueOf(System.currentTimeMillis()));
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("Nonce created successfully. nonce : " + nonce);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to create nonce: " + nonce, e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean create(Rp rp) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement query = conn.prepareStatement("insert into rp(id, data) values(?, ?)");
            query.setString(1, rp.getOxdId());
            query.setString(2, Jackson2.serializeWithoutNulls(rp));
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("RP created successfully. RP : " + rp);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to create RP: " + rp, e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean update(Rp rp) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement query = conn.prepareStatement("update rp set data = ? where id = ?");
            query.setString(1, Jackson2.serializeWithoutNulls(rp));
            query.setString(2, rp.getOxdId());
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("RP updated successfully. RP : " + rp);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to update RP: " + rp, e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public Rp getRp(String oxdId) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("select id, data from rp where id = ?");
            query.setString(1, oxdId);
            ResultSet rs = query.executeQuery();

            rs.next();
            String data = rs.getString("data");
            query.close();
            conn.commit();

            Rp rp = MigrationService.parseRp(data);
            if (rp != null) {
                LOG.debug("Found RP id: " + oxdId + ", RP : " + rp);
                return rp;
            } else {
                LOG.error("Failed to fetch RP by id: " + oxdId);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Failed to find RP by id: " + oxdId + ". Error: " + e.getMessage(), e);
            rollbackSilently(conn);
            return null;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean isStatePresent(String state) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("select state, created_at from state where state = ?");
            query.setString(1, state);
            ResultSet rs = query.executeQuery();

            rs.next();
            String stateValue = rs.getString("state");
            String createdOn = rs.getString("created_at");
            query.close();
            conn.commit();

            if (!Strings.isNullOrEmpty(stateValue) && !Strings.isNullOrEmpty(createdOn)) {
                if (H2DBCleanerService.hasStateOrNoanceExpired(createdOn, conf.getStateExpirationInMinutes())) {
                    LOG.debug("State has expired: " + state);
                    removeState(state);
                    return false;
                }
                LOG.debug("Found state: " + stateValue);
                return true;
            } else {
                LOG.error("Failed to fetch state: " + state);
                return false;
            }
        } catch (Exception e) {
            LOG.error("Failed to find state: " + state + ". Error: " + e.getMessage(), e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean isNoncePresent(String nonce) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("select nonce, created_at from nonce where nonce = ?");
            query.setString(1, nonce);
            ResultSet rs = query.executeQuery();

            rs.next();
            String nonceValue = rs.getString("nonce");
            String createdAt = rs.getString("created_at");
            query.close();
            conn.commit();

            if (!Strings.isNullOrEmpty(nonceValue) && !Strings.isNullOrEmpty(createdAt)) {
                if (H2DBCleanerService.hasStateOrNoanceExpired(createdAt, conf.getNonceExpirationInMinutes())) {
                    LOG.debug("nonce has expired: " + nonce);
                    removeNonce(nonce);
                    return false;
                }
                LOG.debug("Found nonce: " + nonceValue);
                return true;
            } else {
                LOG.error("Failed to fetch nonce: " + nonce);
                return false;
            }
        } catch (Exception e) {
            LOG.error("Failed to find nonce: " + nonce + ". Error: " + e.getMessage(), e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public Map<String, String> getAllStates() {
        Connection conn = null;
        Map<String, String> stateMap = new HashMap<>();
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("select state, created_at from state");
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                String stateValue = rs.getString("state");
                String createdAt = rs.getString("created_at");
                stateMap.put(stateValue, createdAt);
            }
            query.close();
            conn.commit();

            return stateMap;
        } catch (Exception e) {
            LOG.error("Failed to fetch any state. Error: " + e.getMessage(), e);
            rollbackSilently(conn);
            return stateMap;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public Map<String, String> getAllNonce() {
        Connection conn = null;
        Map<String, String> nonceMap = new HashMap<>();
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("select nonce, created_at from nonce");
            ResultSet rs = query.executeQuery();

            while (rs.next()) {
                String nonceValue = rs.getString("nonce");
                String createdAt = rs.getString("created_at");
                nonceMap.put(nonceValue, createdAt);
            }
            query.close();
            conn.commit();

            return nonceMap;
        } catch (Exception e) {
            LOG.error("Failed to fetch any nonce. Error: " + e.getMessage(), e);
            rollbackSilently(conn);
            return nonceMap;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean removeAllRps() {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement query = conn.prepareStatement("delete from rp");
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("All RPs are removed successfully.");
            return true;
        } catch (Exception e) {
            LOG.error("Failed to drop all RPs", e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public Set<Rp> getRps() {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("select id, data from rp");
            ResultSet rs = query.executeQuery();

            Set<Rp> result = new HashSet<>();
            while (rs.next()) {
                String id = rs.getString("id");
                String data = rs.getString("data");

                Rp rp = MigrationService.parseRp(data);
                if (rp != null) {
                    result.add(rp);
                } else {
                    LOG.error("Failed to parse rp, id: " + id);
                }
            }

            query.close();
            conn.commit();
            LOG.info("Loaded " + result.size() + " RPs.");
            return result;
        } catch (Exception e) {
            LOG.error("Failed to fetch rps. Error: " + e.getMessage(), e);
            rollbackSilently(conn);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public static void rollbackSilently(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            LOG.error("Failed to rollback transaction, error: " + e.getMessage(), e);
        }
    }

    public void destroy() {
        provider.onDestroy();
    }

    @Override
    public boolean remove(String oxdId) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("delete from rp where id = ?");
            query.setString(1, oxdId);
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("Removed rp successfully. oxdId: " + oxdId);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to remove rp with oxdId: " + oxdId, e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean removeState(String state) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("delete from state where state = ?");
            query.setString(1, state);
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("Removed state successfully: " + state);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to remove state: " + state, e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }

    public boolean removeNonce(String nonce) {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement query = conn.prepareStatement("delete from nonce where nonce = ?");
            query.setString(1, nonce);
            query.executeUpdate();
            query.close();

            conn.commit();
            LOG.debug("Removed nonce successfully: " + nonce);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to remove nonce: " + nonce, e);
            rollbackSilently(conn);
            return false;
        } finally {
            IOUtils.closeSilently(conn);
        }
    }
}
