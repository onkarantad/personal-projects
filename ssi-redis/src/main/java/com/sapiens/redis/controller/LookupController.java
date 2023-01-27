package com.sapiens.redis.controller;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sapiens.redis.RedisPreRunner;
import com.sapiens.redis.service.common.MethodGetterService;
import com.sapiens.redis.service.lookup.DaoLookupService;

import lombok.extern.log4j.Log4j2;

@RestController
@CrossOrigin
@Component
@Log4j2
@RequestMapping(path = "/lookup")
public class LookupController {

    @Autowired
    RedissonClient redisson;

    @Autowired
    RedisPreRunner redisPreRunner;

    @Autowired
    DaoLookupService daoLookupService;

    @Autowired
    MethodGetterService methodGetterService;


    @SuppressWarnings("static-access")
    @PostMapping("/nrt")
    public String processLookUp(@RequestBody ObjectNode entity) {

        //  lookup RequestBody
/*
    {
        "MAP_NAME":"",
        "LOOKUP_VALUE":"",
        "LOOKUP_KEY":{
                "key1": [],
                "key2": []
            }
    }
*/
        return daoLookupService.getLookupValue(entity);
    }

}
