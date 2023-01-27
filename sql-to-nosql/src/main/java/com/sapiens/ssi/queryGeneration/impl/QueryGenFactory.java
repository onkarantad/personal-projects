package com.sapiens.ssi.queryGeneration.impl;

import com.sapiens.ssi.queryGeneration.QueryGenService;
import com.sapiens.ssi.service.factory.DBEnum;


public class QueryGenFactory {

    public static QueryGenService getQueries(String type) {
        DBEnum dbEnum = DBEnum.valueOf(type.toUpperCase());
        switch (dbEnum) {
            case MSSQL:
                return new MSsqlQueryGen();

            case POSTGRES:
                return new PostgresQueryGen();

            case ORACLE:
                return new OracleQueryGen();

            case SYBASE:
                return new SybaseQueryGen();

            default:
                return null;

        }

    }
}
