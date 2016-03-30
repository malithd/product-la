package org.wso2.carbon.la.alert.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.la.alert.domain.LAAlertConstant;
import org.wso2.carbon.la.alert.domain.SATaskInfo;
import org.wso2.carbon.la.commons.constants.LAConstants;
import org.wso2.carbon.la.database.exceptions.LAConfigurationParserException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nalaka on 3/23/16.
 */
public class ScheduleAlertControllerImplTestCase {
    public static final Log log= LogFactory.getLog(ScheduleAlertControllerImplTestCase.class);

    @Test
    public void testCreateScheduleAlert () {
        log.info("testRegisterScheduleAlertTask ");
        SATaskInfo saTaskInfo=new SATaskInfo();
        saTaskInfo.setAlertName("AlertTest");
        saTaskInfo.setDescription("This is for test");
        saTaskInfo.setTableName(LAConstants.LOG_ANALYZER_STREAM_NAME);
        saTaskInfo.setQuery("_level:WARN");
        saTaskInfo.setStart(0);
        saTaskInfo.setTimeFrom(0);
        saTaskInfo.setTimeTo(8640000000000000l);
        saTaskInfo.setLength(100);
        saTaskInfo.setCronExpression("0 0/1 * * * ?");
        saTaskInfo.setCondition("gt");
        saTaskInfo.setConditionValue(3);
        saTaskInfo.setAlertActionType("logger");
        Map<String,String> properties=new HashMap<String,String>();
        properties.put("uniqueId","WARNAlert");
        properties.put("message","Count is {{count}}");
        saTaskInfo.setAlertActionProperties(properties);

        String userName="admin";
        int tenantId=-1234;

        ScheduleAlertControllerImpl scheduleAlertController=new ScheduleAlertControllerImpl();
        scheduleAlertController.createScheduleAlert(saTaskInfo,userName,tenantId);



    }
}
