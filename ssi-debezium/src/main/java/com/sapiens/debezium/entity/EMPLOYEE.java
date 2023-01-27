package com.sapiens.debezium.entity;

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
@ToString
@Entity
public class EMPLOYEE {
	@Id
	private Long ID;
	private String FULL_NAME;
	private String EMAIL;

}
