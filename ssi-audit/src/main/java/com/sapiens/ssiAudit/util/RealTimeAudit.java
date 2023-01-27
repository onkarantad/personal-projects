package com.sapiens.ssiAudit.util;

public class RealTimeAudit {

    public  enum RealTimeAuditEnum {
        STARTED,ERROR_ENCOUNTERED,COMPLETED,ACTIVE,INACTIVE
    }

    // returns StatusDesc by status Num
    public static String getStatusDesc(Object status) {
        switch (RealTimeAuditEnum.values()[Integer.parseInt(status.toString())-1]) {
            case STARTED:
                return "'STARTED'";
            case ERROR_ENCOUNTERED:
                return "'ERROR ENCOUNTERED'";
            case COMPLETED:
                return "'COMPLETED'";
            case ACTIVE:
                return "'ACTIVE'";
            case INACTIVE:
                return "'INACTIVE'";
        }
        return null;
    }


}