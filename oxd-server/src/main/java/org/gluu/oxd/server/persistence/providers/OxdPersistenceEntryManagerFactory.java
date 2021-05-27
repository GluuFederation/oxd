package org.gluu.oxd.server.persistence.providers;

import org.gluu.orm.couchbase.impl.CouchbaseEntryManagerFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.PersistenceEntryManagerFactory;
import org.gluu.persist.exception.PropertyNotFoundException;
import org.gluu.persist.exception.operation.ConfigurationException;
import org.gluu.persist.reflect.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class OxdPersistenceEntryManagerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OxdPersistenceEntryManagerFactory.class);

    private HashMap<String, PersistenceEntryManagerFactory> persistenceEntryManagerFactoryNames;

    public final PersistenceEntryManager createPersistenceEntryManager(Properties properties, String persistenceType) {

        try {
            if (this.persistenceEntryManagerFactoryNames == null) {
                initPersistenceManagerMaps();
            }

            PersistenceEntryManagerFactory persistenceEntryManagerFactory = this.persistenceEntryManagerFactoryNames.get(persistenceType);
            if (persistenceType.equalsIgnoreCase("couchbase")) {
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
    @SuppressWarnings("all")
    private void initPersistenceManagerMaps() {
        this.persistenceEntryManagerFactoryNames = new HashMap<String, PersistenceEntryManagerFactory>();

        org.reflections.Reflections reflections = new org.reflections.Reflections(new org.reflections.util.ConfigurationBuilder()
                .setUrls(org.reflections.util.ClasspathHelper.forPackage("org.gluu.persist"))
                .addUrls(org.reflections.util.ClasspathHelper.forPackage("org.gluu.orm"))
                .setScanners(new org.reflections.scanners.SubTypesScanner()));
        Set<Class<? extends PersistenceEntryManagerFactory>> classes = reflections.getSubTypesOf(PersistenceEntryManagerFactory.class);

        LOG.info("Found '{}' PersistenceEntryManagerFactory", classes.size());

        List<Class<? extends PersistenceEntryManagerFactory>> classesList = new ArrayList<>(classes);
        for (Class<? extends PersistenceEntryManagerFactory> clazz : classesList) {
            LOG.info("Found PersistenceEntryManagerFactory '{}'", clazz);
            PersistenceEntryManagerFactory persistenceEntryManagerFactory = createPersistenceEntryManagerFactoryImpl(clazz);
            persistenceEntryManagerFactoryNames.put(persistenceEntryManagerFactory.getPersistenceType(), persistenceEntryManagerFactory);
        }
    }

    private PersistenceEntryManagerFactory createPersistenceEntryManagerFactoryImpl(Class<? extends PersistenceEntryManagerFactory> persistenceEntryManagerFactoryClass) {
        PersistenceEntryManagerFactory persistenceEntryManagerFactory;
        try {
            persistenceEntryManagerFactory = ReflectHelper.createObjectByDefaultConstructor(persistenceEntryManagerFactoryClass);
            //persistenceEntryManagerFactory.initStandalone(this);
        } catch (PropertyNotFoundException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new ConfigurationException(
                    String.format("Failed to create PersistenceEntryManagerFactory by type '%s'!", persistenceEntryManagerFactoryClass));
        }

        return persistenceEntryManagerFactory;
    }

}
