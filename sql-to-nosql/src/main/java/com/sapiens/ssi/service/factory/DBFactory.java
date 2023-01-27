package com.sapiens.ssi.service.factory;

import com.sapiens.ssi.service.DBService;
import com.sapiens.ssi.service.impl.MSsqlImpl;
import com.sapiens.ssi.service.impl.OracleImpl;
import com.sapiens.ssi.service.impl.PostgresImpl;
import com.sapiens.ssi.service.impl.SybaseImpl;

public class DBFactory {

    public static DBService getQueries(String type) {
        DBEnum dbEnum = DBEnum.valueOf(type.toUpperCase());
        switch (dbEnum) {
            case MSSQL:
                return new MSsqlImpl();

            case POSTGRES:
                return new PostgresImpl();

            case ORACLE:
                return new OracleImpl();

            case SYBASE:
                return new SybaseImpl();

            default:
                return null;

        }

    }
}
