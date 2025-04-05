package com.taskmanager.infrastructure.persistence.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

/**
 * MongoDB Testcontainers extension for integration tests. OOTB option with test-resources has issues during aot build.
 * <p>
 * This class starts a MongoDB container on port 27018 and reuses it across test runs.
 * </p>
 */
@Singleton
@SuppressWarnings({"rawtypes", "resource", "deprecation"})
public class MongoDbExtension implements BeforeAllCallback, TestExecutionListener {

    private static final GenericContainer MONGO_DB = new FixedHostPortGenericContainer("mongodb/mongodb-community-server:7.0.11-ubi8")
            .withFixedExposedPort(27018, 27017)
            .withReuse(true)
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Waiting for connections.*"));

    private MongoClient mongoClient;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        MONGO_DB.start();
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        mongoClient = applicationContext.getBean(MongoClient.class);
    }

    @Override
    public void beforeTestExecution(TestContext testContext) throws Exception {
        MongoDatabase database = mongoClient.getDatabase("task-manager");
        database.listCollectionNames()
                .forEach(name -> database.getCollection(name).deleteMany(Filters.empty()));
    }
}
