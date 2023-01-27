package com.sapiens.ssi.ingestion.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.sapiens.ssi.commonUtils.CustomUtility;
import com.sapiens.ssi.config.ConfigData;
import com.sapiens.ssi.connection.nosql.MongoConnectionPool;
import com.sapiens.ssi.constants.SSIConstant;
import com.sapiens.ssi.exceptions.MessageInjestionFailedException;
import com.sapiens.ssi.ingestion.IngestionService;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;

import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class MongoIngest implements IngestionService {

    public static MongoIngest MongoIngest = null;

    private static String uriTarget = null;
    private static String dbTarget = null;

    private static MongoClient mongoClient = null;
    private static MongoDatabase targetDatabase = null;
    private static String targetCollection = null;
    private static JSONObject json = null;
    private static JSONArray array = null;
    private static int count = 1;
    AtomicInteger src_count = new AtomicInteger(0);
    AtomicInteger trgt_count = new AtomicInteger(0);


    // making singletone
    private MongoIngest() {
        log.info("MongoIngest instance gets created");

    }

    public static MongoIngest getMongoIngest() {

        if (MongoIngest == null)
            MongoIngest = new MongoIngest();

        return MongoIngest;

    }


    @Override
    public List<Integer> fullLoad(String jsonString, String moduleName, ConfigData configData) throws MessageInjestionFailedException {
        List<Integer> srcAndTrgtCountList = new ArrayList<>();
        List<Document> documentList = new ArrayList<>();
        try {
            // initialize mongoClient
            if (mongoClient == null) {

                uriTarget = configData.getTarget().getTarget_database_uri();

                mongoClient = MongoConnectionPool.getConnection(uriTarget);

            }
            // initialize targetDatabase
            if (targetDatabase == null) {
                dbTarget = configData.getTarget().getTarget_database_name();
                targetDatabase = mongoClient.getDatabase(dbTarget);
            }

            targetCollection = moduleName.toLowerCase();  // "contact";
            MongoCollection<Document> collection = targetDatabase.getCollection(targetCollection);


            long convertJsonStart = System.currentTimeMillis();
            log.info("convertJsonStart : " + CustomUtility.convertTime(convertJsonStart));

            //Convert JSON string to JSONArray
            JSONParser parser = new JSONParser();
            try {
                Object object = (Object) parser.parse(jsonString);
                array = (JSONArray) object;

            } catch (ParseException e) {
                e.printStackTrace();
            }
            long convertJsonEnd = System.currentTimeMillis();
            log.info("convertJsonEnd : " + CustomUtility.convertTime(convertJsonEnd));

            // Iterate over array and Parse it to document and add into List of document of batch
            src_count.addAndGet(array.size());
            log.info("SRC_COUNT " + src_count);
            if (configData.getTarget().getBatch_enable_ind() == 1) {
                for (int i = 0; i < array.size(); i++) {
                    documentList.add(Document.parse(array.get(i).toString()));

                }
                //Insert into collection
                collection.insertMany(documentList);
                trgt_count.addAndGet(documentList.size());
                log.info("Ingested " + configData.getTarget().getBatch_size() + " records :" + (count++));

            } else {
                //Make a data into chunks/batches and Ingest data into mongodb
                for (int i = 0; i < array.size(); i++) {
                    documentList.add(Document.parse(array.get(i).toString()));

                    if (documentList.size() == configData.getTarget().getBatch_size()) {

                        //when data is equal to batch_size
                        //Insert into collection
                        collection.insertMany(documentList);
                        trgt_count.addAndGet(documentList.size());
                        log.info("Loaded a batch of " + configData.getTarget().getBatch_size() + ":" + (count++));
                        documentList.clear();
                    }
                }

                if (documentList.size() > 0) {
                    //when data is less than batch_size
                    //Insert into collection
                    collection.insertMany(documentList);
                    log.info("Loaded a batch of " + documentList.size());
                }
                trgt_count.addAndGet(documentList.size());
                System.out.println("count: " + count);
                System.out.println("documentList.size(): " + documentList.size());

            }
            srcAndTrgtCountList.add(src_count.get());
            srcAndTrgtCountList.add(trgt_count.get());
            src_count.set(0);
            trgt_count.set(0);

        } catch (Exception e) {

            log.error("<" + MongoIngest.class.getSimpleName() + "> Ingestion process failed for "
                    + targetCollection + "- " + e);
            throw new MessageInjestionFailedException("<" + MongoIngest.class.getSimpleName() + "> Ingestion process failed for "
                    + targetCollection + "- " + e);
        }
        return srcAndTrgtCountList;
    }

    @Override
    public List<Integer> incrLoad(String jsonString, String moduleName, ConfigData configData) throws MessageInjestionFailedException {
        List<Integer> srcAndTrgtCountList = new ArrayList<>();
        Document targetDocument = null;
        Bson filter = null;//{"_id":"123"}
        //Make option upset - true
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        try {
            // initialize mongoClient
            if (mongoClient == null) {

                uriTarget = configData.getTarget().getTarget_database_uri();

                mongoClient = MongoConnectionPool.getConnection(uriTarget);

            }
            // initialize targetDatabase
            if (targetDatabase == null) {
                dbTarget = configData.getTarget().getTarget_database_name();
                targetDatabase = mongoClient.getDatabase(dbTarget);
            }

            String targetCollection = moduleName.toLowerCase();  // "contact";
            MongoCollection<Document> collection = targetDatabase.getCollection(targetCollection);


            long convertJsonStart = System.currentTimeMillis();
            log.info("convertJsonStart : " + CustomUtility.convertTime(convertJsonStart));

            //Convert JSON string to JSONArray
            JSONParser parser = new JSONParser();
            try {
                Object object = (Object) parser.parse(jsonString);
                array = (JSONArray) object;

            } catch (ParseException e) {
                e.printStackTrace();
            }
            long convertJsonEnd = System.currentTimeMillis();
            log.info("convertJsonEnd : " + CustomUtility.convertTime(convertJsonEnd));

            src_count.addAndGet(array.size());
            log.info("SRC_COUNT " + src_count);

            // Iterate over array and Parse it to document and ingest(insert/update) into database
            for (int i = 0; i < array.size(); i++) {
                targetDocument = Document.parse(array.get(i).toString());

                if (targetDocument.get(SSIConstant._id) != null) {
                    filter = Filters.eq(SSIConstant._id, targetDocument.get(SSIConstant._id));

                } else {

                    log.error("<" + MongoIngest.class.getSimpleName() + "> identifier _id not exist in collection "
                            + targetDocument);
                    throw new MessageInjestionFailedException("<" + MongoIngest.class.getSimpleName() + "> identifier _id not exist in collection "
                            + targetDocument);
                }

                // loading transformed json to target collection
                collection.replaceOne(filter, targetDocument, options);

                ++count;
            }
            trgt_count.addAndGet(count-1);
            srcAndTrgtCountList.add(src_count.get());
            srcAndTrgtCountList.add(trgt_count.get());
            src_count.set(0);
            trgt_count.set(0);

            log.info("Successfully upserted records..! - " + (count-1));
        } catch (Exception e) {

            log.error("<" + MongoIngest.class.getSimpleName() + "> Ingestion process failed for "
                    + targetCollection + "- " + e);
            throw new MessageInjestionFailedException("<" + MongoIngest.class.getSimpleName() + "> Ingestion process failed for "
                    + targetCollection + "- " + e);
        }
        return srcAndTrgtCountList;
    }
}
