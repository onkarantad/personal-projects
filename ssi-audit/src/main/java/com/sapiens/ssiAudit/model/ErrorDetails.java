package com.sapiens.ssiAudit.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ErrorDetails {
    private Date timestamp;
    private String message;
    private String details;
}
