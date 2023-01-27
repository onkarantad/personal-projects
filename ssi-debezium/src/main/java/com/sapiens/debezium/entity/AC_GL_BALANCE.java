package com.sapiens.debezium.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class AC_GL_BALANCE {

	@Id
	private Long ID;
	private Long UPDATE_USER;
	private Long YEAR;
	private Long MONTH;
	private Long GL_ACCOUNT_ID;
	private Long ARCHIVE_BATCH_ID;
	private Long ARCHIVE_STATUS_ID;
	@Setter(AccessLevel.NONE)
	private Timestamp UPDATE_DATE;
	@Setter(AccessLevel.NONE)
	private Timestamp ARCHIVE_DATE;
	private int UPDATE_VERSION;
	private BigDecimal OPENING_BALANCE;
	private BigDecimal MONTHLY_MOVEMENTS;
	private BigDecimal CLOSING_BALANCE;
	private String EXT_DATA;
	
	public void setUPDATE_DATE(Timestamp uPDATE_DATE) {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> :::: "+uPDATE_DATE);      
		UPDATE_DATE = uPDATE_DATE;
	}
	public void setARCHIVE_DATE(Timestamp aRCHIVE_DATE) {
		ARCHIVE_DATE = aRCHIVE_DATE;
	}
	
	

}
