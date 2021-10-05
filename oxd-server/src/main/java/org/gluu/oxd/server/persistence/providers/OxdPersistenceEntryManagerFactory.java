package org.gluu.oxd.server.persistence.providers;

import org.gluu.orm.couchbase.impl.CouchbaseEntryManagerFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.PersistenceEntryManagerFactory;
import org.gluu.persist.exception.operation.ConfigurationException;
import org.gluu.persist.service.StandalonePersistanceFactoryService;

import java.util.Properties;


public class OxdPersistenceEntryManagerFactory {

    private StandalonePersistanceFactoryService standalonePersistanceFactoryService;

    public OxdPersistenceEntryManagerFactory(){
        this.standalonePersistanceFactoryService = new StandalonePersistanceFactoryService();
    }


    public final PersistenceEntryManager createPersistenceEntryManager(Properties properties, String persistenceType) {

        try {
            PersistenceEntryManagerFactory persistenceEntryManagerFactory = this.standalonePersistanceFactoryService.getPersistenceEntryManagerFactory(persistenceType);
            if (persistenceEntryManagerFactory.getPersistenceType().equalsIgnoreCase("couchbase")) {
                ((CouchbaseEntryManagerFactory) persistenceEntryManagerFactory).create();
            }
            Properties connProps = createConnectionProperties(properties, persistenceEntryManagerFactory.getPersistenceType());
            PersistenceEntryManager ret = persistenceEntryManagerFactory.createEntryManager(connProps);
            if (ret == null)
                throw new RuntimeException("Could not create persistence entry manager");
            return ret;
        } catch (ConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static final Properties createConnectionProperties(Properties properties, String connPrefix) {

        Properties connProps = new Properties();
        for (String propname : properties.stringPropertyNames()) {
            connProps.setProperty(connPrefix + "#" + propname, properties.getProperty(propname));
        }
        return connProps;
    }

}
