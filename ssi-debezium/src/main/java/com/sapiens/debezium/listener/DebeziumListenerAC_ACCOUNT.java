package com.sapiens.debezium.listener;

import static io.debezium.data.Envelope.FieldName.AFTER;
import static io.debezium.data.Envelope.FieldName.BEFORE;
import static io.debezium.data.Envelope.FieldName.OPERATION;
import static java.util.stream.Collectors.toMap;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Component;

import com.sapiens.debezium.service.AC_ACCOUNT_Service;

import io.debezium.data.Envelope.Operation;
//import io.debezium.data.Envelope.Operation;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
//import io.debezium.embedded.EmbeddedEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DebeziumListenerAC_ACCOUNT {
//	private  EMPLOYEE_Service employeeService;
	private AC_ACCOUNT_Service ac_ACCOUNT_Service;
	private Executor executor = Executors.newSingleThreadExecutor();
//	private final EmbeddedEngine engine;

	private final DebeziumEngine<RecordChangeEvent<SourceRecord>> engine;

	private DebeziumListenerAC_ACCOUNT(AC_ACCOUNT_Service sourceRecordChangeValue) throws IOException {
		// this.engine =
		// EmbeddedEngine.create().using(employeeConnector).notifying(this::handleEvent).build();

		FileReader reader = new FileReader("debezium.properties");
		Properties properties = new Properties();
		properties.load(reader);

		this.engine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class)).using(properties)
				.notifying(this::handleChangeEvent).build();

		this.ac_ACCOUNT_Service = sourceRecordChangeValue;
	}

//	private void handleEvent(SourceRecord sourceRecord) {
//		try {
//			log.info("Got record :" + sourceRecord.toString());
//		} catch (Exception ex) {
//			log.info("exception in handle event:" + ex);
//		}
//
//	}

	private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {

		SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
		Struct sourceRecordChangeValue = (Struct) sourceRecord.value();

//		log.info("sourceRecord ::-> " + sourceRecord);
//		log.info("Key = '" + sourceRecord.key() + "' value = '" + sourceRecord.value() + "'");
//		log.info("sourceRecordChangeValue ::-> " + sourceRecordChangeValue);

		if (sourceRecordChangeValue != null) {

			Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));

			if (operation != Operation.READ) {

				String record = operation == Operation.DELETE ? BEFORE : AFTER; // Handling Update & Insert operations.
//				log.info("record ::-> " + record);

				Struct struct = (Struct) sourceRecordChangeValue.get(record);

				Map<String, Object> payload = struct.schema().fields().stream().map(Field::name)
						.filter(fieldName -> struct.get(fieldName) != null)
						.map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
						.collect(toMap(Pair::getKey, Pair::getValue));
//				log.info("payload ::-> " + payload);

				this.ac_ACCOUNT_Service.replicateData(payload, operation);
				log.info("Updated Data: " + payload + " with Operation: " + operation.name());
			}
		}
	}

	@PostConstruct
	private void start() {
		this.executor.execute(engine);
	}

	@PreDestroy
	private void stop() throws IOException {
		if (this.engine != null) {
//			this.engine.stop(); //embaded
			this.engine.close();
		}
	}

}
