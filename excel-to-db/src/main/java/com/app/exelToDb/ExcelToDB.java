package com.app.exelToDb;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.Yaml;

public class ExcelToDB {
	
	public static void main(String[] args) {
		Yaml yaml = new Yaml();
		Reader yamlFile = null;
		try {
			yamlFile = new FileReader("config.yaml");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Map<String, Object> configMap = yaml.load(yamlFile);
		configMap.entrySet().stream().forEach(i -> System.out.println(i.getKey() + " : " + i.getValue()));

		String excelPath = (String) configMap.get("excelPath");
		String sheetName = (String) configMap.get("sheetName");
		String dbName = (String) configMap.get("dbName");
		String dbHost = (String) configMap.get("dbHost");
		String dbPort = configMap.get("dbPort").toString();
		String dbSchema = (String) configMap.get("dbSchema");
		String dbUser = (String) configMap.get("dbUser");
		String dbPassword = (String) configMap.get("dbPassword");
		String sql = (String) configMap.get("sql");
		
		int batchSize = 20;
		int itr = 1;
		Connection connection = null;

		try {
			long start = System.currentTimeMillis();
			FileInputStream fis = new FileInputStream(excelPath);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheet(sheetName);
			Iterator<Row> rowIterator = sheet.iterator();

			connection = JdbcConnection.getConnection(dbName, dbHost, dbPort, dbSchema, dbUser, dbPassword);
			//connection.setAutoCommit(false);
			PreparedStatement statement = connection.prepareStatement(sql);

			int noOfRows = sheet.getLastRowNum() + 1;
			int noOfColumns = sheet.getRow(0).getLastCellNum();
			System.out.println("noOfRows : " + noOfRows + " - 2 = " + (noOfRows - 2));
			System.out.println("noOfColumns : " + noOfColumns);
			List<String> dataTypeList = new ArrayList<>();

			int count = 0;
			rowIterator.next(); // skip the header row
			Row nextRow1 = rowIterator.next(); // get DataType row and then skip for data
			for (int i = 0; i < noOfColumns; i++) {
				Cell nextCell1 = nextRow1.getCell(i);
				if (nextCell1 == null || nextCell1.getCellType() == CellType.BLANK) {
					System.err.println("dataType -> " + (itr++) + " null");
				}
				String dataType = nextCell1.getStringCellValue().toUpperCase();
				dataTypeList.add(dataType);
			}
			System.out.println("dataTypeList (" + dataTypeList.size() + "): " + dataTypeList);

			java.util.Date date = new Date();

			while (rowIterator.hasNext()) {

				Row nextRow = rowIterator.next();
				int index = 1;
				for (int i = 0; i < noOfColumns; i++) {
					Cell nextCell = nextRow.getCell(i);
					DataType dataType = DataType.valueOf(dataTypeList.get(i));
					switch (dataType) {
					case INT:
						if (nextCell == null || nextCell.getCellType() == CellType.BLANK) {
							statement.setNull(index, Types.INTEGER);
						} else {
							int numericCell = (int) nextCell.getNumericCellValue();
							statement.setInt(index, numericCell);
						}
						break;
					case STRING:
						if (nextCell == null || nextCell.getCellType() == CellType.BLANK) {
							statement.setNull(index, Types.VARCHAR);
						} 
						else if(nextCell.getCellType() == CellType.NUMERIC) {
							int numericCell = (int) nextCell.getNumericCellValue();
							statement.setInt(index, numericCell);
						}
						else {
							String stringCell = nextCell.getStringCellValue();
							statement.setString(index, stringCell);
						}
						break;
					case DATE:
						if (nextCell == null || nextCell.getCellType() == CellType.BLANK) {
							statement.setNull(index, Types.TIMESTAMP);
						}
						else if(nextCell.getCellType() == CellType.STRING){
							if(nextCell.getStringCellValue().equalsIgnoreCase("java_date")){
								statement.setTimestamp(index, new Timestamp(date.getTime()));
							}
						}
						else {
							Date dateCell = nextCell.getDateCellValue();
//								System.out.println("dateCell -> " + dateCell);
//								System.out.println("dateCell -> " + new Timestamp(dateCell.getTime()));
							statement.setTimestamp(index, new Timestamp(dateCell.getTime()));
						}
					}
					index++;
				}
				statement.addBatch();

				if (count++ %20==0) {
					int[] result = statement.executeBatch();
					statement.clearBatch();
					//count = 0;
					System.out.println("loaded: "+count);
					try {
						//System.out.println("thread is sleeping");
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					date = new Date();
				}

//				if (count % batchSize == 0) {
//					System.out.println("batch_start");
//					statement.executeBatch();
//				}
			}

			workbook.close();
			// execute the remaining queries
			statement.executeBatch();
			//connection.commit();
			connection.close();

			long end = System.currentTimeMillis();
			long timeTaken = end - start;
			System.out.printf("Import done in " + timeTaken + " ms / " + (timeTaken / 1000.0) + " s / "
					+ (timeTaken / 60000.0) + " min");
		} catch (IOException ex1) {
			System.out.println("Error reading file");
			ex1.printStackTrace();
		} catch (SQLException ex2) {
			System.out.println("Database error");
			ex2.printStackTrace();
		}
	}

}