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
package org.wso2.carbon.la.alert.impl;

import org.wso2.carbon.la.alert.domain.SATaskInfo;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;

public interface ScheduleAlertController {
    /**
     * Register alert task
     * @param saTaskInfo Task info for scheduling
     * @param userName User Name of this alert task scheduler
     * @param tenantId user tenantId of this alert scheduler
     */
    void registerScheduleAlertTask(SATaskInfo saTaskInfo,String userName, int tenantId) throws TaskException;

    /**
     * Set task properties
     * @param saTaskInfo Task info for set task properties
     * @param userName User Name of this alert task scheduler
     * @return TaskInfo with triggerInfo
    */
    TaskInfo createScheduleAlertTask(SATaskInfo saTaskInfo, String userName);

}
