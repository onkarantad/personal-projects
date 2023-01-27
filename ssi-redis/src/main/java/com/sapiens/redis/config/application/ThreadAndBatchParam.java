package com.sapiens.redis.config.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ToString
public class ThreadAndBatchParam {
    int thread;
    int batch;
}