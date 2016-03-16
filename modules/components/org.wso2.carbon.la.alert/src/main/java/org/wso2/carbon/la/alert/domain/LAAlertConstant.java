package org.wso2.carbon.la.alert.domain;

import org.wso2.carbon.registry.core.RegistryConstants;

/**
 * Created by nalaka on 2/24/16.
 */
public class LAAlertConstant {
    public static final String TABLE_NAME = "tableName";
    public static final String QUERY = "query";
    public static final String USER_NAME = "userName";
    public static final String TIME_FROM = "timeFrom";
    public static final String TIME_TO = "timeTo";
    public static final String START = "start";
    public static final String LENGTH = "length";
    public static final String ALERT_NAME = "alertName";
    public static final String ALERT_CONFIGURATION_LOCATION = "repository" + RegistryConstants.PATH_SEPARATOR
            + "components" + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR
            + "org.wso2.carbon.la.alert";
    public static final String CONFIGURATION_EXTENSION_SEPARATOR = ".";
    public static final String CONFIGURATION_EXTENSION = "json";
    public static final String CONFIGURATION_MEDIA_TYPE = "application/json";

    public static final String SCHEDULE_ALERT_TASK_TYPE = "LAScheduleAlertTask";


}
