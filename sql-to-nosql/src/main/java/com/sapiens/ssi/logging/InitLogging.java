package com.sapiens.ssi.logging;

import com.sapiens.ssi.config.ConfigData;
import com.sapiens.ssi.config.GetConfig;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class InitLogging {
    static ConfigData configData = GetConfig.getConfigData();

    //Set the log properties given from config.yaml file
    public static void setLogProperties()  {
        SSILogger.setLog(log);
        SSILogConfig.logConfig(configData.getLogging().getLog_folderpath(),
                configData.getLogging().getLog_filename(), configData.getLogging().getLog_filesize(),
                configData.getLogging().getLog_rolloversize());

        SSILogConfig.ChangeLevel(configData.getLogging().getLog_mode());




    }
}

