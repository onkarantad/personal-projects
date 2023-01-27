package com.sapiens.ssi.ingestion.impl;

import com.sapiens.ssi.ingestion.IngestionService;


public class IngestionFactory {

    public static IngestionService getQueries(String type) {
        TargetDBEnum targetDbEnum = TargetDBEnum.valueOf(type.toUpperCase());
        switch (targetDbEnum) {
            case MONGODB:
                return MongoIngest.getMongoIngest();

            default:
                return null;

        }

    }
}
