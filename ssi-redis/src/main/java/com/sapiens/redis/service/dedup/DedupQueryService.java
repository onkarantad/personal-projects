package com.sapiens.redis.service.dedup;

import java.sql.SQLException;

public interface DedupQueryService {
    String dedupPreLoadQueryGetter(String query) throws SQLException;

    String dedupMBQueryGetter(String mbQuery, int loadInd);
}
