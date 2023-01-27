package com.sapiens.ssi.service;

import com.sapiens.ssi.auditing.AuditPayload;
import com.sapiens.ssi.auditing.RestAPI;
import com.sapiens.ssi.commonUtils.CustomUtility;
import com.sapiens.ssi.config.ConfigData;
import com.sapiens.ssi.config.GetConfig;
import com.sapiens.ssi.config.Target;
import com.sapiens.ssi.connection.sql.SourceConnectionPool;
import com.sapiens.ssi.constants.SSIConstant;
import com.sapiens.ssi.constants.StaticObjects;
import com.sapiens.ssi.exceptions.MessageInjestionFailedException;
import com.sapiens.ssi.exceptions.NullPointerException;
import com.sapiens.ssi.ingestion.IngestionService;
import com.sapiens.ssi.ingestion.impl.IngestionFactory;
import com.sapiens.ssi.ingestion.impl.MongoIngest;
import com.sapiens.ssi.queryGeneration.QueryGenService;
import com.sapiens.ssi.queryGeneration.impl.QueryGenFactory;
import com.sapiens.ssi.service.factory.DBFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class ServiceImpl implements ServiceInterface {
    static ConfigData configData = GetConfig.getConfigData();

    static DBService dbService = DBFactory.getQueries(configData.getSource().getSource_database());
    static QueryGenService queryGenService = QueryGenFactory.getQueries(configData.getSource().getSource_database());
    static IngestionService ingestionService = IngestionFactory.getQueries(configData.getTarget().getTarget_database());
    static int INCR_LOAD_IND = 0;

    List<Integer> srcAndTrgtCountList = new ArrayList<>();

    public void serviceMethod() {
        long start = System.currentTimeMillis();
        String current_batch_start_date = CustomUtility.convertTime(start);
        log.info("start : " + current_batch_start_date);
        String current_batch_end_date = null;
        long end = 0l;
        int BATCH_ID = 0;
        int SUB_BATCH_ID = 0;


        try (//take source connection instance from connection pool
             BasicDataSource dataSource = SourceConnectionPool.getDataSource(configData.getSource().getSource_jdbc_url(), configData.getSource().getSource_user_id(), configData.getSource().getSource_password());
             Connection sourceConnection = dataSource.getConnection();) {


            //query MST_JOB table and fetch active job and keep in maplist
            // **call REST API development needs to be done here**
            String mstJobInputQuery = dbService.mstJobInputQuery(configData.getSource().getAudit_schema_nm());
            log.trace("mstJobInputQuery: " + mstJobInputQuery);
            StaticObjects.MST_JOB_MAP_LIST = CustomUtility.DBUtilsFindAll(mstJobInputQuery, sourceConnection);
            log.trace("MST_JOB_MAP_LIST: " + StaticObjects.MST_JOB_MAP_LIST);


            for (Map<String, Object> mstJobMap : StaticObjects.MST_JOB_MAP_LIST) {

                List<Map<String, Object>> preScriptRestObj = RestAPI.getResObj(configData.getAuditingRestURL().getPre_script_url(), AuditPayload.getPreScriptPayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date));
                log.info("preScriptRestObj: " + preScriptRestObj);
                BATCH_ID = (int) preScriptRestObj.get(0).get(SSIConstant.BATCH_ID);
                SUB_BATCH_ID = (int) preScriptRestObj.get(0).get(SSIConstant.SUB_BATCH_ID);
                log.debug("BATCH_ID: " + BATCH_ID + " | " + "SUB_BATCH_ID: " + SUB_BATCH_ID);

                INCR_LOAD_IND = (int) mstJobMap.get(SSIConstant.INCR_LOAD_IND);

                //query MST_WORKFLOW table and fetch active module info. and keep in maplist
                // **call REST API development needs to be done here**
                String mstWFInputQuery = dbService.mstWFInputQuery(configData.getSource().getAudit_schema_nm(), (Integer) mstJobMap.get(SSIConstant.JOB_NUM));
                log.info("mstWFInputQuery: " + mstWFInputQuery);
                StaticObjects.MST_WF_MAP_LIST = CustomUtility.DBUtilsFindAll(mstWFInputQuery, sourceConnection);
                log.info("MST_WF_MAP_LIST: " + StaticObjects.MST_WF_MAP_LIST);

                // query CJMS mapping table and keep in maplist
                String cjmsInputQuery = dbService.cjmsInputQuery(configData.getSource().getAudit_schema_nm());
                StaticObjects.CONFIG_JOINER_MAP_LIST = CustomUtility.DBUtilsFindAll(cjmsInputQuery, sourceConnection);
                log.trace("CONFIG_JOINER_MAP_LIST: " + StaticObjects.CONFIG_JOINER_MAP_LIST);

                for (Map<String, Object> mstWFMap : StaticObjects.MST_WF_MAP_LIST) {
                    Optional<Object> query;
                    if (mstWFMap.get(SSIConstant.WF_NAME) == null) {

                        RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "MST_WF - iteration", "WF_NAME is null"));

                        log.error("<" + ServiceImpl.class.getSimpleName() + "> WF_NAME is null");
                        throw new NullPointerException("<" + ServiceImpl.class.getSimpleName() + "> WF_NAME is null");
                    }
                    String moduleName = mstWFMap.get(SSIConstant.WF_NAME).toString();

                    //one by one workflow-wise fetch CMS mapping data and keep in maplist
                    String cmsInputQuery = dbService.cmsInputQuery(configData.getSource().getAudit_schema_nm(), moduleName);
                    StaticObjects.DB_CONFIG_MAP_LIST = CustomUtility.DBUtilsFindAll(cmsInputQuery, sourceConnection);
                    log.trace("DB_CONFIG_MAP_LIST: " + StaticObjects.DB_CONFIG_MAP_LIST);

                    if (mstWFMap.get(SSIConstant.MAPPING_TYPE).toString().equalsIgnoreCase(SSIConstant.cms_cjms)) {
                        //cms_cjms method call
                    } else {
                        //stored query pickup method call
                        if (INCR_LOAD_IND == 1) {
                            //Incremental load

                            while (true) {
                                System.out.println("loop");

                                //get business start date of all dependent tables
                                String businessStartDateQuery = CustomUtility.getbusinessStartDateQuery(moduleName,configData.getSource().getSource_schema_nm(),configData.getSource().getAudit_schema_nm());
                                log.trace("businessStartDateQuery: "+businessStartDateQuery);

                                Map<String,Object> businessStartDatesMap = CustomUtility.getMapFromQuery(sourceConnection,businessStartDateQuery);
                                log.info("businessStartDatesMap: "+businessStartDatesMap);

                                //get the incremental load stored query from cms table
                                query = queryGenService.getIncrLoadStoredQuery(StaticObjects.DB_CONFIG_MAP_LIST);
                                log.trace("query: "+query.get());

                                query = CustomUtility.replaceBusinessStartDate(moduleName,query.get().toString(),businessStartDatesMap);
                                log.trace("replacedQuery: "+query.get());



                                if (query.isPresent()) {
                                    // by executing stored_query get resultSet
                                    ResultSet resultSet = null;
                                    try {
                                         resultSet = CustomUtility.executeQuery(query.get().toString(), sourceConnection,
                                                moduleName, 0, 0, 0);
                                    } catch (SQLException e) {
                                        RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "CustomUtility.executeQuery", e.getLocalizedMessage().replace("\"", "")));

                                        throw new SQLException(e.getLocalizedMessage());
                                    }

                                    if (resultSet == null || !resultSet.isBeforeFirst()) {
                                        RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "INCR_LOAD - IF", "resultSet is null.!"));
                                        throw new NullPointerException("resultSet is null.!");
                                    }

                                    //concat the JSON string which recieved from resultset
                                    String jsonString = CustomUtility.getJSONData(resultSet);
                                    log.trace("jsonString: " + jsonString);

                                    //Convert JSON string into JSON Object/Array and
                                    // Ingest incremental data into MongoDB

                                    try {
                                        srcAndTrgtCountList = ingestionService.incrLoad(jsonString, moduleName, configData);
                                        log.info("srcAndTrgtCountList: "+srcAndTrgtCountList);
                                    } catch (MessageInjestionFailedException e) {
                                        RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "MongoIngest.incrLoad", e.getLocalizedMessage().replace("\"", "")));

                                        throw new MessageInjestionFailedException(e.getLocalizedMessage());
                                    }


                                } else {

                                    RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "INCR_LOAD - ELSE", " Stored_query is null"));

                                    log.error("<" + ServiceImpl.class.getSimpleName() + "> Stored_query is null");
                                    throw new NullPointerException("<" + ServiceImpl.class.getSimpleName() + "> Stored_query is null");
                                }
                                if (configData.getTarget().getIncr_loop_ind() == 0) {
                                    break;
                                }
                            }
                        } else {
                            //Full Load

                            //get the full load stored query from cms table
                            query = queryGenService.getFullLoadStoredQuery(StaticObjects.DB_CONFIG_MAP_LIST);
                            if (query.isPresent()) {
                                log.debug("Stored_query: " + query.get().toString());

                                int offset = 0;
                                Target configDataTarget = configData.getTarget();
                                int batchSize = configDataTarget != null ? configDataTarget.getBatch_size() : 10000;
                                int batchEnableInd = configDataTarget != null ? configDataTarget.getBatch_enable_ind() : 0;
                                if (batchEnableInd == 1) {
                                    while (true) {

                                        // by executing stored_query get resultSet
                                        ResultSet resultSet = null;
                                        try {
                                             resultSet = CustomUtility.executeQuery(query.get().toString(),
                                                    sourceConnection, moduleName, batchEnableInd, offset, batchSize);
                                        } catch (SQLException e) {
                                            RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "CustomUtility.executeQuery", e.getLocalizedMessage().replace("\"", "").replaceAll("[\\t\\n\\r]+"," ")));

                                            throw new SQLException(e.getLocalizedMessage());
                                        }


                                        if (resultSet == null || !resultSet.isBeforeFirst())
                                            break;
                                        //concat the JSON string which recieved from resultset
                                        String jsonString = CustomUtility.getJSONData(resultSet);
                                        log.trace("jsonString: " + jsonString);

                                        //Convert JSON string into JSON Object/Array and
                                        // Ingest data into MongoDB
                                        try {
                                            srcAndTrgtCountList = ingestionService.fullLoad(jsonString, moduleName, configData);
                                            log.info("srcAndTrgtCountList: "+srcAndTrgtCountList);
                                        } catch (MessageInjestionFailedException e) {
                                            RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "MongoIngest.fullLoad", e.getLocalizedMessage().replace("\"", "")));

                                            throw new MessageInjestionFailedException(e.getLocalizedMessage());

                                        }

                                        offset += batchSize;
                                    }

                                } else {
                                    // by executing stored_query get resultSet
                                    ResultSet resultSet = null;
                                    try {
                                        resultSet = CustomUtility.executeQuery(query.get().toString(),
                                                sourceConnection, moduleName, batchEnableInd, offset, batchSize);
                                    } catch (SQLException e) {
                                        RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "CustomUtility.executeQuery", e.getLocalizedMessage().replace("\"", "")));

                                        throw new SQLException(e.getLocalizedMessage());
                                    }


                                    if (resultSet == null || !resultSet.isBeforeFirst()) {

                                        RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "FULL_LOAD - ELSE", "resultSet is null.!"));
                                        throw new NullPointerException("resultSet is null.!");

                                    }
                                    //concat the JSON string which recieved from resultset
                                    String jsonString = CustomUtility.getJSONData(resultSet);
                                    log.trace("jsonString: " + jsonString);

                                    //Convert JSON string into JSON Object/Array and
                                    // Ingest data into MongoDB
                                    try {
                                        srcAndTrgtCountList = ingestionService.fullLoad(jsonString, moduleName, configData);
                                        log.info("srcAndTrgtCountList: "+srcAndTrgtCountList);
                                    } catch (MessageInjestionFailedException e) {
                                        RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "MongoIngest.fullLoad", e.getLocalizedMessage().replace("\"", "")));

                                        throw new MessageInjestionFailedException(e.getLocalizedMessage());

                                    }


                                }

                            } else {

                                RestAPI.postRequest(configData.getAuditingRestURL().getAudit_log_exce_url(), AuditPayload.getAuditLogExcePayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, CustomUtility.convertTime(System.currentTimeMillis()), BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), "FULL_LOAD - ELSE", "Stored_query is null"));

                                log.error("<" + ServiceImpl.class.getSimpleName() + "> Stored_query is null");
                                throw new NullPointerException("<" + ServiceImpl.class.getSimpleName() + "> Stored_query is null");
                            }
                        }
                    }

                    end = System.currentTimeMillis();
                    current_batch_end_date = CustomUtility.convertTime(end);
                    log.info("IngestionEnd : " + current_batch_end_date);

                    RestAPI.postRequest(configData.getAuditingRestURL().getAugit_log_url(), AuditPayload.getAuditLogPayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_start_date, current_batch_end_date, BATCH_ID, SUB_BATCH_ID, (String) mstWFMap.get(SSIConstant.WF_NAME), (int) mstWFMap.get(SSIConstant.WF_NUM), (String) mstWFMap.get(SSIConstant.MAPPING_TYPE), srcAndTrgtCountList.get(0), srcAndTrgtCountList.get(1)));
                }
                RestAPI.postRequest(configData.getAuditingRestURL().getPost_script_url(), AuditPayload.getPostScriptPayload(configData.getSource().getAudit_schema_nm(), (int) mstJobMap.get(SSIConstant.JOB_NUM), configData.getSource().getSource_zone_id(), current_batch_end_date, BATCH_ID, SUB_BATCH_ID));
            }


            long timeTaken = end - start;
            log.info("Time Taken " + timeTaken + " ms / " + (timeTaken / 1000.0) + " s / "
                    + (timeTaken / 60000.0) + " min");
        } catch (Exception e) {
            log.error("<" + ServiceImpl.class.getSimpleName() + "> Failed to load");
            e.printStackTrace();
        }
    }


}
