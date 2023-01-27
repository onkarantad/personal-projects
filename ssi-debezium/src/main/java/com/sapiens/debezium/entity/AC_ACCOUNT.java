package com.sapiens.debezium.entity;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
public class AC_ACCOUNT {

	@Id
	private Long ID;
	private int CONTACT_ID;
	private int UPDATE_USER;
	private int UPDATE_VERSION;
	private int ACCOUNT_TYPE;
	private int CURRENCY_ID;
	private int RESERVED_VALUE_FLAG;
	private int BANK_ACCOUNT_ID;
	private int PAYMENT_PLAN_ID;
	private int PAYMENT_DAY;
	private int BRAND_COMPANY_ID;
	private int POLICY_HEADER_ID;
	private int CLAIM_ID;
	private int GL_ACCOUNT_ID;
	private int MAIN_AGENT_ACCOUNT_ID;
	private int HANDLING_CONTACT_ID;
	private int HANDLING_ACCOUNT_ID;
	private int DEFAULT_ENTRY_STATUS_ID;
	private int PAYMENT_DELAY_POINT;
	private int PAYMENT_DELAY;
	private int PAYMENT_TYPE;
	private int MASTER_POLICY_HEADER_ID;
	private int CURRENT_ACCOUNT_STATUS_ID;
	private int CURR_DUNNING_ACCOUNT_STATUS_ID;
	private int BANK_REPRESENTATIVE_ID;
	private int PAYER_ID;
	private int ACC_STATE_AFTER_CONSOLIDATE;
	private int DAY_OF_MONTH;
	private int STATEMENT_FREQ_TYPE_ID;
	private int DAY_OF_WEEK_ID;
	private int PROCESSING_STATUS_ID;
	private Timestamp UPDATE_DATE;
	private Timestamp LAST_STATEMENT_PROD_DATE;
	private Timestamp NEXT_STATEMENT_PROD_DATE;
	private double SYNCHRONIZED_ACCOUNT_FLAG;
	private String AGENT_NUMBER;
	private String EXTERNAL_NUMBER;

}
