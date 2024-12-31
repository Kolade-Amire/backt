package com.kolade.backt.mongodb;

import com.kolade.backt.common.DatabaseConnection;
import com.kolade.backt.common.DatabaseDetails;
import com.kolade.backt.exception.CustomBacktException;
import com.kolade.backt.exception.DatabaseConnectionException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class MongoDBConnection implements DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);

    private MongoClient mongoClient;

    @Override
    public void connect(DatabaseDetails databaseDetails) {
        try {
            mongoClient = MongoClients.create(databaseDetails.getConnectionUrl());
        } catch (Exception e) {
            throw new DatabaseConnectionException("Unable to connect to the MongoDB database: ", e, databaseDetails.getConnectionUrl());
        }
    }

    @Override
    public boolean testConnection() {
        try {
            mongoClient.listDatabaseNames().first();
            return true;
        } catch (Exception e) {
            logger.error("Error testing connection: ", e);
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception e) {
                throw new CustomBacktException("Unable to close connection: ", e);
            }
        }
    }


    @Override
    public String getType() {
        return "MongoDB";
    }

    @Override
    public String getDatabaseName() {
        try {
            return mongoClient.listDatabaseNames().first();
        } catch (Exception e) {
            throw new CustomBacktException("Unable to get database name: ", e);
        }
    }

    @Override
    public String getDatabaseVersion() {
        try {
            Document buildInfo = mongoClient.getDatabase("admin").runCommand(new Document("buildInfo", 1));
            return buildInfo.getString("version");
        } catch (Exception e) {
            throw new CustomBacktException("Unable to get database version: ", e);
        }
    }
}
