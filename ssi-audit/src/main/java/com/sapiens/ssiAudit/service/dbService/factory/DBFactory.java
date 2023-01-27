package com.sapiens.ssiAudit.service.dbService.factory;

import com.sapiens.ssiAudit.service.dbService.DBService;
import com.sapiens.ssiAudit.service.dbService.impl.PostgreSqlDBImpl;
import com.sapiens.ssiAudit.service.dbService.impl.SQLserverDBImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DBFactory {

    public enum DBFactoryEnum {
        MSSQL, POSTGRES, ORACLE, SYBASE
    }

    // returns Db impl of specified DB
    @Bean
    public static DBService getQueries(@Value("${spring.datasource.database}") String type) {
        switch (DBFactoryEnum.valueOf(type.toUpperCase())) {
            case MSSQL:
                return new SQLserverDBImpl();
            case POSTGRES:
                return new PostgreSqlDBImpl();
            case ORACLE:
                log.warn("NO ORACLE Implementation");
                return null;
            case SYBASE:
                log.warn("NO SYBASE Implementation");
                return null;
            default:
                return null;
        }
    }
}
