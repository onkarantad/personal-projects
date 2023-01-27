package com.sapiens.redis.service.impl.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.sapiens.redis.commons.SSIRedisAppConstants;
import com.sapiens.redis.config.application.Application;
import com.sapiens.redis.service.common.MethodGetterService;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MethodGetterImpl implements MethodGetterService {


	@Autowired
	private DataSource dataSource;

//	@Value("${dateFormat.inputDateFormat}")
//	private String inputDateFormate;
//
//	@Value("${dateFormat.outputDateFormat}")
//	private String outputDateFormate;

	@Autowired
	private Application application;

	public LinkedHashMap<String, String> findAll(JdbcTemplate jdbcTemplate, String query) {
		LinkedHashMap<String, String> mapList = jdbcTemplate.query(query, (ResultSet rs) -> {
			LinkedHashMap<String, String> results = new LinkedHashMap<>();
			while (rs.next()) {
				results.put(rs.getString(1), rs.getString(2));
			}
			return results;
		});
		return mapList;
	}

	@Override
	public List<Map<String, Object>> DBUtilFindAll(String query) {
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

	public String getValueByFilter(ObjectNode entity, RMapCache<String, String> redisMapLookup,
								   List<String> lookupValueList) {
		Map<String, List<String>> lookupKeyArrayList = new LinkedHashMap<>();
		entity.get(SSIRedisAppConstants.lookupKey).fields()
				.forEachRemaining(i -> lookupKeyArrayList.put(i.getKey().toUpperCase(), Arrays.asList(i.getValue()
						.toString().toUpperCase().replace("\"", "").replace("[", "").replace("]", "").split(","))));

		log.trace("lookupKeyArrayList: "+lookupKeyArrayList);

		if (redisMapLookup.isExists()) {
			Map<String, String> result = redisMapLookup;

			log.trace("result: "+result.keySet());

			for (String keySet : lookupKeyArrayList.keySet()) {

				log.trace("keySet: "+keySet);

				List<String> keyList = new ArrayList<>(lookupKeyArrayList.keySet());

				log.trace("keyList: "+keyList);

				int index = keyList.indexOf(keySet);

				log.trace("index: "+index);

				result = result.entrySet().stream().filter(cache -> {
					for (String arr : lookupKeyArrayList.get(keySet)) {

						log.trace("arr: "+arr);
						log.trace("cache.getKey(): "+cache.getKey());
						log.trace("splitChar: "+application.getKeyConcatChar());
						log.trace("cache.getKey().split: "+cache.getKey().split(application.getKeyConcatChar())[index]);
						log.trace("cache.getKey().equals: "+cache.getKey().split(application.getKeyConcatChar())[index].equals(arr));

						if (cache.getKey().split(application.getKeyConcatChar())[index].equalsIgnoreCase(arr)) {
							return true;
						}
					}
					return false;
				}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

				log.trace("result: "+result);
			}

			StringJoiner sj = new StringJoiner(",");

			if (entity.has(SSIRedisAppConstants.lookupValue)) {
				String lookupValue = entity.get(SSIRedisAppConstants.lookupValue).asText().toUpperCase();
				if (lookupValueList.contains(lookupValue)) {
					int i = lookupValueList.indexOf(lookupValue);
					result.forEach((k, v) -> sj.add(v.split(application.getKeyConcatChar())[i]));
					if (sj.length() == 0) {
						log.error("Lookup cache not available for provided input");
						return null;
					}
					log.info("Response: " + sj);
					return sj.toString();
				} else {
					log.error("LOOKUP_VALUE not available");
					return null;
				}
			}

			result.forEach((k, v) -> sj.add(v));
			log.info("Response: " + sj.length());
			return sj.toString();
		}
		return null;
	}
	public  String dateToString(String key) {
		List<String> ll = Arrays.asList(key.split("\\|"));

		DateFormat inDateFormate = new SimpleDateFormat(application.getDateFormat().getInputDateFormat());
		DateFormat outDateFormat = new SimpleDateFormat(application.getDateFormat().getOutputDateFormat());
		ll.stream().forEach(i -> {
			if (isValidDate(i)) {
				Date date;
				try {
					date = inDateFormate.parse(i);
					String strDate = outDateFormat.format(date);
					ll.set(ll.indexOf(i), strDate);
				} catch (ParseException e) {
					log.fatal("Date Format incorrect- "+e);
				}
			}
		});
		String outKey = String.join("|", ll);

		return outKey;
	}

	public  boolean isValidDate(String key) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(application.getDateFormat().getInputDateFormat());
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(key.trim());
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}

	public  boolean isDatePresent(String key) {
		List<String> ll = Arrays.asList(key.split("\\|"));
		boolean flag = ll.stream().anyMatch(i -> isValidDate(i));
		log.warn("date_flag: "+flag);
		return flag;
	}

}
