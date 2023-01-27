package com.sapiens.ssi.commonUtils;

import com.sapiens.ssi.exceptions.NullPointerException;
import com.sapiens.ssi.service.DBService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class CustomUtility {


    public static List<Map<String, Object>> DBUtilsFindAll(String query, Connection conn) {
        // QueryRunner execute the query and load the resultSet into List<Map<String,
        // Object>> collection internally
        List<Map<String, Object>> mapList = null;
        if (query == null || query.length() < 1)
            throw new NullPointerException("<" + CustomUtility.class.getSimpleName() + "> query is null");
        if (conn == null)
            throw new NullPointerException("<" + CustomUtility.class.getSimpleName() + "> Connection is not established");
        try {
            QueryRunner queryRunner = new QueryRunner();
            mapList = queryRunner.query(conn, query, new MapListHandler());
        } catch (SQLException se) {
            log.error("<" + CustomUtility.class.getSimpleName() + "> - Failed to execute query - "
                    + se);
        }
        return mapList;
    }

    public static Map<String, Object> getMapFromQuery(Connection sqlConnection, String businessStartDateQuery) {
        Map<String, Object> map = new HashMap<>();
        try {
            PreparedStatement preparedStatement = sqlConnection.prepareStatement(businessStartDateQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getString(1), resultSet.getObject(2));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    public static String getbusinessStartDateQuery(String moduleName, String source_schema_nm, String audit_schema_nm) {

        if (moduleName.equalsIgnoreCase("CONTACT")) {
            return "select " +
                    "'ENTITY_DETAILS_TIMESTAMP' TABLE_TIMESTAMP, " +
                    "cast(coalesce(max((business_end_date)),'2022-05-05 00:00:00.0000') as datetime2) as BUSINESS_END_DATE " +
                    "from  " + audit_schema_nm + ".AUDIT_LOG where TABLE_NAME = '" + source_schema_nm + ".ENTITY_DETAILS' " +
                    "UNION ALL " +
                    "select " +
                    "'ENTITY_HISTORY_DETAILS_TIMESTAMP' TABLE_TIMESTAMP, " +
                    "cast(coalesce(max((business_end_date)),'2022-05-05 00:00:00.0000') as datetime2) as BUSINESS_END_DATE " +
                    "from  " + audit_schema_nm + ".AUDIT_LOG where TABLE_NAME = '" + source_schema_nm + ".ENTITY_HISTORY_DETAILS' " +
                    "UNION ALL " +
                    "select " +
                    "'INDIVIDUAL_TIMESTAMP' TABLE_TIMESTAMP, " +
                    "cast(coalesce(max((business_end_date)),'2022-05-05 00:00:00.0000') as datetime2) as BUSINESS_END_DATE " +
                    "from  " + audit_schema_nm + ".AUDIT_LOG where TABLE_NAME = '" + source_schema_nm + ".INDIVIDUAL' " +
                    "UNION ALL " +
                    "select " +
                    "'ADDRESS_DETAILS_TIMESTAMP' TABLE_TIMESTAMP, " +
                    "cast(coalesce(max((business_end_date)),'2022-05-05 00:00:00.0000') as datetime2) as BUSINESS_END_DATE " +
                    "from  " + audit_schema_nm + ".AUDIT_LOG where TABLE_NAME = '" + source_schema_nm + ".ADDRESS_DETAILS' " +
                    "UNION ALL " +
                    "select " +
                    "'ENTITY_CONTACT_TIMESTAMP' TABLE_TIMESTAMP, " +
                    "cast(coalesce(max((business_end_date)),'2022-05-05 00:00:00.0000') as datetime2) as BUSINESS_END_DATE " +
                    "from  " + audit_schema_nm + ".AUDIT_LOG where TABLE_NAME = '" + source_schema_nm + ".ENTITY_CONTACT' " +
                    "UNION ALL " +
                    "select " +
                    "'ENTITY_EMAIL_TIMESTAMP' TABLE_TIMESTAMP, " +
                    "cast(coalesce(max((business_end_date)),'2022-05-05 00:00:00.0000') as datetime2) as BUSINESS_END_DATE " +
                    "from  " + audit_schema_nm + ".AUDIT_LOG where TABLE_NAME = '" + source_schema_nm + ".ENTITY_EMAIL' " +
                    "UNION ALL " +
                    "select " +
                    "'ENTITY_PAYMENT_PLAN_TIMESTAMP' TABLE_TIMESTAMP, " +
                    "cast(coalesce(max((business_end_date)),'2022-05-05 00:00:00.0000') as datetime2) as BUSINESS_END_DATE " +
                    "from  " + audit_schema_nm + ".AUDIT_LOG where TABLE_NAME = '" + source_schema_nm + ".ENTITY_PAYMENT_PLAN'";
        } else if (moduleName.equalsIgnoreCase("POLICY")) {


        } else if (moduleName.equalsIgnoreCase("CLAIM")) {

        }

        return null;
    }

    public static Optional<Object>  replaceBusinessStartDate(String moduleName, String query, Map<String, Object> businessStartDatesMap) {

        if (moduleName.equalsIgnoreCase("CONTACT")) {
            return Optional.ofNullable(query.replace("'?ENTITY_DETAILS_TIMESTAMP'", "'"+businessStartDatesMap.get("ENTITY_DETAILS_TIMESTAMP")+"'")
                    .replace("'?ENTITY_HISTORY_DETAILS_TIMESTAMP'", "'"+businessStartDatesMap.get("ENTITY_HISTORY_DETAILS_TIMESTAMP")+"'")
                    .replace("'?INDIVIDUAL_TIMESTAMP'", "'"+businessStartDatesMap.get("INDIVIDUAL_TIMESTAMP")+"'")
                    .replace("'?ADDRESS_DETAILS_TIMESTAMP'", "'"+businessStartDatesMap.get("ADDRESS_DETAILS_TIMESTAMP")+"'")
                    .replace("'?ENTITY_CONTACT_TIMESTAMP'", "'"+businessStartDatesMap.get("ENTITY_CONTACT_TIMESTAMP")+"'")
                    .replace("'?ENTITY_EMAIL_TIMESTAMP'", "'"+businessStartDatesMap.get("ENTITY_EMAIL_TIMESTAMP")+"'")
                    .replace("'?ENTITY_PAYMENT_PLAN_TIMESTAMP'", "'"+businessStartDatesMap.get("ENTITY_PAYMENT_PLAN_TIMESTAMP")+"'"));
        } else if (moduleName.equalsIgnoreCase("POLICY")) {


        } else if (moduleName.equalsIgnoreCase("CLAIM")) {

        }

        return null;
    }


    public static String getReplaceOption(String moduleName) {

        if (moduleName.equalsIgnoreCase("CONTACT")) {
            return "order by ENTITY_DETAILS.ENTITY_ID \n" +
                    "OFFSET '?offset' ROWS\n" +
                    "FETCH NEXT '?batchSize' ROWS ONLY";
        } else if (moduleName.equalsIgnoreCase("POLICY")) {
            return "order by CONTRACT.CONTRACT_ID \n" +
                    "OFFSET '?offset' ROWS\n" +
                    "FETCH NEXT '?batchSize' ROWS ONLY";

        } else if (moduleName.equalsIgnoreCase("CLAIM")) {
            return "order by GENINS_CLAIM_DETAILS.CLAIM_ID \n" +
                    "OFFSET '?offset' ROWS\n" +
                    "FETCH NEXT '?batchSize' ROWS ONLY";
        }

        return null;
    }

    public static ResultSet executeQuery(String query, Connection sqlConnection, String moduleName, int batchEnableInd, int offset, int batchSize) throws SQLException {
        if (query == null || query.length() < 1)
            throw new NullPointerException("<" + CustomUtility.class.getSimpleName() + "> query is null");
        if (sqlConnection == null)
            throw new NullPointerException("<" + CustomUtility.class.getSimpleName() + "> Connection is not established");
        ResultSet resultSet = null;

        String replaceOption = CustomUtility.getReplaceOption(moduleName);
        if (batchEnableInd == 1) {
            query = query.replace("'?replaceOption'", replaceOption);
            query = query.replace("'?offset'", Integer.toString(offset))
                    .replace("'?batchSize'", Integer.toString(batchSize));
        } else {
            query = query.replace("'?replaceOption'", " ");
        }


        try {

            PreparedStatement preparedStatement = sqlConnection.prepareStatement(query);
            long queryExecutionStart = System.currentTimeMillis();
            log.info("queryExecutionStart : " + CustomUtility.convertTime(queryExecutionStart));
            //Execute the query on SQL database
            resultSet = preparedStatement.executeQuery();
            long queryExecutionEnd = System.currentTimeMillis();
            log.info("queryExecutionEnd : " + CustomUtility.convertTime(queryExecutionEnd));

        } catch (SQLException e) {
            log.error("<" + CustomUtility.class.getSimpleName() + "> - Failed to execute query - "
                    + e + "SQLQUERY:- " + query);
            throw new SQLException("<" + CustomUtility.class.getSimpleName() + "> - Failed to execute query - "
                    + e + "SQLQUERY:- " + query);
        }

        return resultSet;
    }

    public static String getJSONData(ResultSet resultSet) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();

        if (!resultSet.isBeforeFirst())
            throw new NullPointerException("<" + CustomUtility.class.getSimpleName() + "> ResultSet is null");

        long concatStart = System.currentTimeMillis();
        log.info("concatStart : " + CustomUtility.convertTime(concatStart));
        //Concat all JSON strings in one
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getString(1));
        }
        long concatEnd = System.currentTimeMillis();
        log.info("concatEnd : " + CustomUtility.convertTime(concatEnd));

        return stringBuilder.toString();
    }

    public static String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(date);
    }


}
