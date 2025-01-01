package org.kolade.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class DatabaseDetailsService {

    private DatabaseDetails activeDatabaseDetails;

    public boolean hasActiveConnection() {
        return activeDatabaseDetails != null;
    }

    public void clearActiveDatabaseDetails() {
        setActiveDatabaseDetails(null);
    }
}
