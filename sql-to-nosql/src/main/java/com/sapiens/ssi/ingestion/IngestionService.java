package com.sapiens.ssi.ingestion;

import com.sapiens.ssi.config.ConfigData;
import com.sapiens.ssi.exceptions.MessageInjestionFailedException;

import java.util.List;

public interface IngestionService {

    public List<Integer> fullLoad(String jsonString, String moduleName, ConfigData configData) throws MessageInjestionFailedException;
    public List<Integer> incrLoad(String jsonString, String moduleName, ConfigData configData) throws MessageInjestionFailedException;


}
