package org.kolade.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.core.DatabaseDetails;
import org.springframework.stereotype.Component;

@Component
public class MongoDBConnection implements DatabaseConnection {

    private MongoClient mongoClient;

    @Override
    public void connect(DatabaseDetails databaseDetails) {
        try {
            mongoClient = MongoClients.create(databaseDetails.getConnectionUrl());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean testConnection() {
        try{
            mongoClient.listDatabaseNames().first();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getType() {
        return "MongoDB";
    }
}
