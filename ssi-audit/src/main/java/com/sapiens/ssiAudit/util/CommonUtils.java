package com.sapiens.ssiAudit.util;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
public class CommonUtils {
    @Autowired
    DataSource dataSource;

    // get timestamp by zone ID
    public Object getTimestampByZoneID(String Zone_Id) {
        ZoneId zid;
        if (Zone_Id.isEmpty()) {
            zid = ZoneId.systemDefault();
        } else {
            zid = ZoneId.of(Zone_Id);
        }
        return Timestamp.valueOf(ZonedDateTime.now(zid).toLocalDateTime());
    }


     //returns resultSet of query
    public List<Map<String, Object>> dbUtilFindAll(String query) {
        Connection con = null;
        List<Map<String, Object>> listOfMaps = null;
        try {
            con = dataSource.getConnection();
            QueryRunner queryRunner = new QueryRunner();
            listOfMaps = queryRunner.query(con, query, new MapListHandler());
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't query the database.", e);
        } finally {
            DbUtils.closeQuietly(con);
        }
        return listOfMaps;
    }

}
