package com.sapiens.ssi.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.sapiens.ssi.constants.SSIConstant;

public class GetConfig {

    private static GetConfig instance = null;
    private static ConfigData config;

    private GetConfig(){}

    private static GetConfig getInstance(){
        if (instance==null){
            synchronized (GetConfig.class){
                instance = new GetConfig();
                initializeConfig();
            }
        }
        return instance;
    }

    private static void initializeConfig(){

        Yaml yaml = new Yaml(new Constructor(ConfigData.class));
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(SSIConstant.configFileLocation);
        } catch (FileNotFoundException fe) {
        	System.err.println("Config File Not Found!");
        }
        config = yaml.load(inputStream);
    }



    public static ConfigData getConfigData(){
        if (instance==null){
            instance = GetConfig.getInstance();
        }
        return config;
    }


}
