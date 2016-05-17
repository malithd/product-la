/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.la.alert.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.la.alert.domain.LAAlertConstant;
import org.wso2.carbon.la.alert.domain.ScheduleAlertBean;
import org.wso2.carbon.la.alert.impl.ScheduleAlertTask;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

import java.util.HashMap;
import java.util.Map;

public class AlertTaskSchedulingUtils {

    public static final Log log = LogFactory.getLog(AlertTaskSchedulingUtils.class);

    /**
     * This method is to schedule alert task
     *
     * @param scheduleAlertBean - Alert info for schedule task
     * @param userName          -UserName of the task scheduler
     * @throws org.wso2.carbon.ntask.common.TaskException
     */
    public static void scheduleAlertTask(ScheduleAlertBean scheduleAlertBean, String userName) throws TaskException {
        try {
            TaskManager taskManager = LAAlertServiceValueHolder.getInstance().getTaskService().getTaskManager(LAAlertConstant.SCHEDULE_ALERT_TASK_TYPE);
            TaskInfo scheduleAlertTaskInfo = getScheduleAlertTaskInfo(scheduleAlertBean, userName);
            taskManager.registerTask(scheduleAlertTaskInfo);
            taskManager.rescheduleTask(scheduleAlertTaskInfo.getName());
        } catch (TaskException e) {
            log.error("Unable to schedule task " + e.getMessage(), e);
            throw new TaskException("Unable to schedule task" + e.getMessage(), e.getCode(), e);
        }
    }

    /**
     * This method use to delete schedule task
     *
     * @param alertName -alertName is task name use to delete
     * @throws TaskException
     */
    public static void deleteScheduleTask(String alertName) throws TaskException {
        try {
            TaskManager taskManager = LAAlertServiceValueHolder.getInstance().getTaskService().getTaskManager(LAAlertConstant.SCHEDULE_ALERT_TASK_TYPE);
            taskManager.deleteTask(alertName);
        } catch (TaskException e) {
            log.error("Unable to delete scheduled task " + e.getMessage(), e);
            throw new TaskException("Unable to delete scheduled task " + e.getMessage(), e.getCode(), e);
        }
    }

    /**
     * This method create TaskInfo and scheduleAlertTask method use this TaskInfo to schedule task
     *
     * @param scheduleAlertBean -Alert info for scheduling
     * @param userName          -user name of the alert scheduler
     * @return TaskInfo
     */
    private static TaskInfo getScheduleAlertTaskInfo(ScheduleAlertBean scheduleAlertBean, String userName) {
        String taskName = scheduleAlertBean.getAlertName();
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(scheduleAlertBean.getCronExpression());
        Map<String, String> taskProperties = getTaskProperties(scheduleAlertBean, userName);
        return new TaskInfo(taskName, ScheduleAlertTask.class.getName(), taskProperties, triggerInfo);
    }

    /**
     * This method create task properties for schedule alert task
     *
     * @param scheduleAlertBean -Alert info for scheduling
     * @param userName          -user name of the alert scheduler
     * @return TaskProperties Map Object
     */
    private static Map<String, String> getTaskProperties(ScheduleAlertBean scheduleAlertBean, String userName) {
        Map<String, String> taskProperties = new HashMap<>();
        long timeDiff = scheduleAlertBean.getTimeTo() - scheduleAlertBean.getTimeFrom();
        taskProperties.put(LAAlertConstant.TABLE_NAME, scheduleAlertBean.getTableName());
        taskProperties.put(LAAlertConstant.QUERY, scheduleAlertBean.getQuery());
        taskProperties.put(LAAlertConstant.USER_NAME, userName);
        taskProperties.put(LAAlertConstant.TIME_DIFF, String.valueOf(timeDiff));
        taskProperties.put(LAAlertConstant.TIME_FROM, String.valueOf(scheduleAlertBean.getTimeFrom()));
        taskProperties.put(LAAlertConstant.TIME_TO, String.valueOf(scheduleAlertBean.getTimeTo()));
        taskProperties.put(LAAlertConstant.START, String.valueOf(scheduleAlertBean.getStart()));
        taskProperties.put(LAAlertConstant.LENGTH, String.valueOf(scheduleAlertBean.getLength()));
        taskProperties.put(LAAlertConstant.ALERT_NAME, scheduleAlertBean.getAlertName());
        taskProperties.put(LAAlertConstant.CONDITION, scheduleAlertBean.getCondition());
        taskProperties.put(LAAlertConstant.CONDITION_VALUE, String.valueOf(scheduleAlertBean.getConditionValue()));
        if (!scheduleAlertBean.getFields().isEmpty()) {
            Map<String, String> fields = scheduleAlertBean.getFields();
            StringBuilder fieldString = new StringBuilder();
            for (String field : fields.values()) {
                fieldString.append(field).append(",");
            }
            fieldString.deleteCharAt(fieldString.length() - 1);
            taskProperties.put(LAAlertConstant.FIELDS, fieldString.toString());
        }
        return taskProperties;
    }
}
