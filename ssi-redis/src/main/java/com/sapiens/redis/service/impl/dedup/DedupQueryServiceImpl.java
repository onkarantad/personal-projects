package com.sapiens.redis.service.impl.dedup;

import com.sapiens.redis.commons.SSIRedisAppConstants;
import com.sapiens.redis.service.dedup.DedupQueryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Service
@Log4j2
public class DedupQueryServiceImpl implements DedupQueryService {

	@Autowired
	private NamedParameterJdbcTemplate dbSession;

	// Dedup Column list
	public static List<String> dedupColumnList = new ArrayList<>();
	private static List dList;

	@Override
	public String dedupPreLoadQueryGetter(String queryCMS) throws SQLException {

		return dbSession.query(queryCMS,new ResultSetExtractor<String>(){

			@Override
			public String extractData(ResultSet rs) throws SQLException, DataAccessException {
				StringBuilder dedupQuery = new StringBuilder();
				Set<Map<String, String>> dedupQueryJoinConditionClauseSet = new HashSet<>();
				Map<String, String> dedupQueryJoinConditionClause = new HashMap<>();

				StringBuilder selectClause = new StringBuilder();
				StringBuilder joinCondition = new StringBuilder();
				StringBuilder primaryTable = new StringBuilder();

				LinkedList<String> selectClauseList = new LinkedList<>();

				while (rs.next()) {

					if(selectClause.toString().isEmpty()){
						selectClause.append(" select LOWER(concat(");
					}

					if (rs.getString(SSIRedisAppConstants.Target_Table) != null && rs.getString(SSIRedisAppConstants.Target_Column) != null ){
						selectClauseList.add(rs.getString(SSIRedisAppConstants.Target_Column));
					}

					if (rs.getString(SSIRedisAppConstants.Condition) != null) {

						dedupQueryJoinConditionClause.put(rs.getString(SSIRedisAppConstants.Target_Table),
								rs.getString(SSIRedisAppConstants.Condition).trim().toUpperCase());

					} else {
						primaryTable = (primaryTable.length() > 0)
								? primaryTable.indexOf(rs.getString(SSIRedisAppConstants.Target_Table)) != -1 ? primaryTable
								: primaryTable.append(",").append(rs.getString(SSIRedisAppConstants.Target_Table))
								: primaryTable.append(" ").append(rs.getString(SSIRedisAppConstants.Target_Table));
					}
				}
				selectClause.append(String.join(" ,'|', ", selectClauseList));
				selectClause.append(")) ").append(SSIRedisAppConstants.dedupAttr).append(",").append(primaryTable).append(".").append(SSIRedisAppConstants.ceId);

				dedupQueryJoinConditionClauseSet.add(dedupQueryJoinConditionClause);
				dedupQueryJoinConditionClauseSet.forEach(
						i -> i.forEach((k, v) -> joinCondition.append(SSIRedisAppConstants.leftjoin).append(k).append(SSIRedisAppConstants.on).append(v)));

				dedupQuery.append(selectClause).append(" from ").append(primaryTable).append(" ").append(joinCondition)
						.append(" where ").append(SSIRedisAppConstants.ceId).append(" IS NOT NULL order by ").append(SSIRedisAppConstants.ceId);

				return dedupQuery.toString();
			}

		});
	}

	@Override
	public String dedupMBQueryGetter(String mbQuery, int loadInd) {

		Set<Map<String, String>> MapSet = new HashSet<>();
		Map<String, String> dupJC = new HashMap<>();

		return dbSession.query(mbQuery, new ResultSetExtractor<String>() {
			String query = null;

			@Override
			public String extractData(ResultSet rs) throws SQLException, DataAccessException {
				dList = new ArrayList<>();
				ResultSetMetaData rsmt = rs.getMetaData();
				for (int i = 1; i < rsmt.getColumnCount(); i++) {
					log.debug("rsmt : " + rsmt.getColumnName(i) + ":");
				}

				if (query == null || query.length() == 0) {
					StringBuilder selectClause = new StringBuilder();
					StringBuilder selectDAttrClause = new StringBuilder();
					StringBuilder joinCondition = new StringBuilder();
					StringBuilder primaryTable = new StringBuilder();
					StringBuilder whereClause = new StringBuilder();

					while (rs.next()) {

						if (rs.getInt(SSIRedisAppConstants.dedupAttrInd) != 2) {
							if (rs.getString(SSIRedisAppConstants.Target_Column).endsWith(SSIRedisAppConstants.ceId)) {
								if (loadInd == 0) {
									selectClause = ((selectClause.length() > 0) && (rs.getInt(SSIRedisAppConstants.dedupAttrInd) == 0))
											? selectClause.append(" , ").append(rs.getString(SSIRedisAppConstants.Target_Column))
											: (rs.getInt(SSIRedisAppConstants.dedupAttrInd) == 0)
											? selectClause.append(rs.getString(SSIRedisAppConstants.Target_Column))
											: selectClause.append("");
								}
							} else {
								selectClause = ((selectClause.length() > 0) && (rs.getInt(SSIRedisAppConstants.dedupAttrInd) == 0))
										? selectClause.append(" , ").append(rs.getString(SSIRedisAppConstants.Target_Column))
										: (rs.getInt(SSIRedisAppConstants.dedupAttrInd) == 0)
										? selectClause.append(rs.getString(SSIRedisAppConstants.Target_Column))
										: selectClause.append("");
							}
							selectDAttrClause = ((selectDAttrClause.length() > 0) && (rs.getInt(SSIRedisAppConstants.dedupAttrInd) == 1))
									? selectDAttrClause.append(" +'|'+ ").append(rs.getString(SSIRedisAppConstants.Target_Column))
									: (rs.getInt(SSIRedisAppConstants.dedupAttrInd) == 1)
									? selectDAttrClause.append(rs.getString(SSIRedisAppConstants.Target_Column))
									: selectDAttrClause.append("");
						}
						if (rs.getString(SSIRedisAppConstants.Condition) != null && rs.getInt(SSIRedisAppConstants.dedupAttrInd) == 2) {
							whereClause.append(rs.getString(SSIRedisAppConstants.Condition));
						} else if (rs.getString(SSIRedisAppConstants.Condition) != null) {
							dupJC.put(rs.getString(SSIRedisAppConstants.Target_Table), rs.getString(SSIRedisAppConstants.Condition).toUpperCase());

						} else {
							primaryTable = (primaryTable.length() > 0)
									? primaryTable.indexOf(rs.getString(SSIRedisAppConstants.Target_Table)) != -1 ? primaryTable
									: primaryTable.append(",").append(rs.getString(SSIRedisAppConstants.Target_Table))
									: primaryTable.append(" ").append(rs.getString(SSIRedisAppConstants.Target_Table));
						}
						// System.out.println(rs.getString(Constants.Target_Column));
						log.debug(rs.getString(SSIRedisAppConstants.Target_Column));
					}

					MapSet.add(dupJC);
					MapSet.forEach(i -> i.forEach((k, v) -> {
						if (v.contains(" JOIN "))
							joinCondition.append(" left join ").append(v);
						else
							joinCondition.append(" left join ").append(k).append(" on ").append(v);

					}));
					query = "select " + selectClause + " ," + "LOWER(" + selectDAttrClause + ") "+SSIRedisAppConstants.dedupAttr+" from "
							+ primaryTable + " " + joinCondition + " where " + whereClause;

				}
				return query;
			}
		});
	}
}
