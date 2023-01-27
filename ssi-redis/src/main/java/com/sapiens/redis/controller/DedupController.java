package com.sapiens.redis.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sapiens.redis.commons.SSIRedisAppConstants;
import com.sapiens.redis.service.dedup.DaoDedupService;

import lombok.extern.log4j.Log4j2;

import org.json.JSONException;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Log4j2
@RestController
@RequestMapping(path = "/dedup")
public class DedupController {

    @Autowired
    RedissonClient redisson;

    private HashOperations hashOperations;
    private RedisTemplate redisTemplate;

    public DedupController(RedisTemplate redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    private DaoDedupService daoDedupService;

    //nrt dedupe
    @PostMapping("/nrt")
    public Integer getCeIdNRT(@RequestBody ObjectNode entity) {

        return daoDedupService.getNrtCeId(entity);
    }


    //micro batch dedupe
    @PostMapping(path = "/microJob")
    public List<Object> getCeIdMicroJob(@RequestBody final Map<String, Object> payload) throws ClassNotFoundException, SQLException, JSONException{

        // dedup RequestBody
/*		{
			"key1": "",
			"key2": "",
			"key3": ""
		}
*/
        return daoDedupService.getMicroJobCeId(payload);

    }

}
