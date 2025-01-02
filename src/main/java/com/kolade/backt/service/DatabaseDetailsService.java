package com.kolade.backt.service;

import com.kolade.backt.common.DatabaseConnection;
import com.kolade.backt.common.DatabaseDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class DatabaseDetailsService {

    private DatabaseDetails activeDatabaseDetails;
    private DatabaseConnection activeDatabaseConnection;

    public boolean hasActiveConnection() {
        return activeDatabaseConnection != null;
    }
    public void clearActiveDatabase() {
        setActiveDatabaseDetails(null);
    }
}
