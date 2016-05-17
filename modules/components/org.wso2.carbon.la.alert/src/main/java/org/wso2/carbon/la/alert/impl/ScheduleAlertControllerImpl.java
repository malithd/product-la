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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.la.alert.domain.LAAlertConstant;
import org.wso2.carbon.la.alert.domain.ScheduleAlertBean;
import org.wso2.carbon.la.alert.exception.AlertPublisherException;
import org.wso2.carbon.la.alert.exception.ScheduleAlertException;
import org.wso2.carbon.la.alert.util.AlertConfigurationUtils;
import org.wso2.carbon.la.alert.util.AlertPublisherUtils;
import org.wso2.carbon.la.alert.util.AlertTaskSchedulingUtils;
import org.wso2.carbon.la.alert.util.LAAlertServiceValueHolder;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleAlertControllerImpl implements ScheduleAlertController {

    private static final Log log = LogFactory.getLog(ScheduleAlertControllerImpl.class);
    private Map<Integer, List<ScheduleAlertBean>> savedScheduleAlertList = new ConcurrentHashMap<>();

    /**
     * Creating a schedule alert task
     *
     * @param scheduleAlertBean -Alert info for scheduling
     * @param userName          -User Name of this alert scheduler
     * @param tenantId          -User tenantId of this alert scheduler
     * @throws ScheduleAlertException
     */
    public void createScheduleAlert(ScheduleAlertBean scheduleAlertBean, String userName, int tenantId)
            throws ScheduleAlertException {
        try {
            if (!isScheduleAlertAlreadyExist(tenantId, scheduleAlertBean.getAlertName())) {
                AlertPublisherUtils.createAlertPublisher(scheduleAlertBean.getAlertName(),
                        scheduleAlertBean.getAlertActionType(),
                        scheduleAlertBean.getAlertActionProperties());
                AlertConfigurationUtils.saveAlertConfiguration(scheduleAlertBean, tenantId);
                AlertTaskSchedulingUtils.scheduleAlertTask(scheduleAlertBean, userName);
                if (savedScheduleAlertList.containsKey(tenantId)) {
                    List<ScheduleAlertBean> savedAlertNameList = savedScheduleAlertList.get(tenantId);
                    savedAlertNameList.add(scheduleAlertBean);
                    savedScheduleAlertList.put(tenantId, savedAlertNameList);
                } else {
                    List<ScheduleAlertBean> alertNameList = new ArrayList<>();
                    alertNameList.add(scheduleAlertBean);
                    savedScheduleAlertList.put(tenantId, alertNameList);
                }
            } else {
                log.warn("Unable to create Alert " + scheduleAlertBean.getAlertName() + "already exist");
                throw new ScheduleAlertException("Unable to create Alert:" + scheduleAlertBean.getAlertName()
                        + "Already exist");
            }
        } catch (AlertPublisherException | TaskException | RegistryException e) {
            log.error("Unable to create alert " + e.getMessage(), e);
            throw new ScheduleAlertException("Unable to create Alert:" + scheduleAlertBean.getAlertName()
                    + " ERROR: " + e.getMessage());
        }
    }

    /**
     * This method updates the schedule alert
     *
     * @param scheduleAlertBean Task info for set task properties
     * @param userName          User Name of this alert task scheduler
     * @param tenantId          Tenant ID of this alert task scheduler
     * @throws ScheduleAlertException
     */
    public void updateScheduleAlertTask(ScheduleAlertBean scheduleAlertBean, String userName, int tenantId)
            throws ScheduleAlertException {
        try {
            AlertPublisherUtils.updateAlertPublisher(scheduleAlertBean.getAlertName(),
                    scheduleAlertBean.getAlertActionType(), scheduleAlertBean.getAlertActionProperties());
            AlertConfigurationUtils.updateAlertConfiguration(scheduleAlertBean, tenantId);
            AlertTaskSchedulingUtils.deleteScheduleTask(scheduleAlertBean.getAlertName());
            AlertTaskSchedulingUtils.scheduleAlertTask(scheduleAlertBean, userName);
        } catch (RegistryException | TaskException | AlertPublisherException e) {
            log.error("Unable to update Alert " + e.getMessage(), e);
            throw new ScheduleAlertException("Unable to update Alert:" + scheduleAlertBean.getAlertName()
                    + "ERROR: " + e.getMessage());
        }
    }

    /**
     * This method use to delete schedule alert by giving alertName
     *
     * @param alertName -alertName for delete scheduled alert
     * @param tenantId  -Tenant ID for get alert configuration location
     * @throws ScheduleAlertException
     */
    public void deleteScheduleAlert(String alertName, int tenantId) throws ScheduleAlertException {
        try {
            if (isScheduleAlertAlreadyExist(tenantId, alertName)) {
                AlertPublisherUtils.deleteAlertPublisher(alertName);
                AlertConfigurationUtils.deleteAlertConfiguration(alertName, tenantId);
                AlertTaskSchedulingUtils.deleteScheduleTask(alertName);
                List<ScheduleAlertBean> savedAlertNameList = savedScheduleAlertList.get(tenantId);
                Iterator<ScheduleAlertBean> alertList = savedAlertNameList.iterator();
                while (alertList.hasNext()) {
                    ScheduleAlertBean alertFromList = alertList.next();
                    if (alertFromList.getAlertName().equals(alertName)) {
                        alertList.remove();
                    }
                }
            } else {
                log.warn("Unable to delete Alert " + alertName + "is not exist");
                throw new ScheduleAlertException("Unable to delete Alert" + alertName + "is not exist");
            }
        } catch (RegistryException | AlertPublisherException | TaskException e) {
            log.error("Unable to delete Alert " + e.getMessage(), e);
            throw new ScheduleAlertException("Unable to create Alert:" + alertName
                    + "ERROR: " + e.getMessage());
        }
    }

    /**
     * Return all alert configurations
     *
     * @param tenantId Tenant ID of this alert task scheduler
     * @return List
     */
    public List<ScheduleAlertBean> getAllAlertConfigurations(int tenantId) {
        try {
            UserRegistry userRegistry = LAAlertServiceValueHolder.getInstance().getTenantConfigRegistry(tenantId);
          //  AlertConfigurationUtils.createConfigurationCollection(userRegistry);
            Collection configurationCollection = (Collection) userRegistry.get(LAAlertConstant.ALERT_CONFIGURATION_LOCATION);
            String[] configs = configurationCollection.getChildren();
            if (!configs.equals(null)) {
                List<ScheduleAlertBean> configurations = new ArrayList<>();
                for (String conf : configs) {
                    String content = RegistryUtils.decodeBytes((byte[]) userRegistry.get(conf).getContent());
                    configurations.add(getConfigurationContent(content));
                }
                if (savedScheduleAlertList.get(tenantId) == null) {
                    savedScheduleAlertList.put(tenantId, configurations);
                }
                return configurations;
            }
        } catch (RegistryException e) {
            log.error("Unable to get alert configuration from registry " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * this method returns ScheduleAlertBean object which request by alert Name
     *
     * @param alertName -alert name for get the alert configuration file
     * @param tenantId  -tenantId of alert scheduler
     * @return ScheduleAlertBean
     * @throws ScheduleAlertException
     */
    public ScheduleAlertBean getAlertConfiguration(String alertName, int tenantId) throws ScheduleAlertException {
        ScheduleAlertBean scheduleAlertBean = new ScheduleAlertBean();
        try {
            UserRegistry userRegistry = LAAlertServiceValueHolder.getInstance().getTenantConfigRegistry(tenantId);
            String fileLocation = AlertConfigurationUtils.getConfigurationLocation(alertName);
            if (userRegistry.resourceExists(fileLocation)) {
                scheduleAlertBean = getConfigurationContent(RegistryUtils.decodeBytes((byte[]) userRegistry.get(fileLocation).getContent()));
            } else {
                log.warn(alertName + " Resource not exist for tenantId " + tenantId);
                throw new ScheduleAlertException(alertName + "is not exist");
            }
        } catch (RegistryException e) {
            log.error("Unable to complete request " + e.getMessage(), e);
            throw new ScheduleAlertException("Unable to complete request " + e.getMessage());
        }
        return scheduleAlertBean;
    }

    /**
     * Create ScheduleAlertBean by using json alertContent
     *
     * @param alertContent -JSON string of alert configuration
     * @return ScheduleAlertBean
     */

    private ScheduleAlertBean getConfigurationContent(String alertContent) {
        Gson gson = new Gson();
        ScheduleAlertBean scheduleAlertBean = gson.fromJson(alertContent, ScheduleAlertBean.class);
        return scheduleAlertBean;
    }

    /**
     * Get all column names from LOGANALYZER schema
     *
     * @param tenantId -tenantId of column requester
     * @return Set of column names
     * @throws AnalyticsException
     */
    public Set getTableColumns(int tenantId) throws AnalyticsException {
        AnalyticsDataAPI analyticsDataAPI = LAAlertServiceValueHolder.getInstance().getAnalyticsDataAPI();
        AnalyticsSchema analyticsSchema = analyticsDataAPI.getTableSchema(tenantId, LAAlertConstant.LOG_ANALYZER_STREAM_NAME.toUpperCase());
        Map<String, ColumnDefinition> columns = analyticsSchema.getColumns();
        Set<String> columnSet = columns.keySet();
        return columnSet;
    }

    /**
     * Validate new alert Name is already exist or not
     *
     * @param tenantId     -tenantId of alert Creator
     * @param newAlertName -new alertName for check either exist or not
     * @return boolean
     */
    public boolean isScheduleAlertAlreadyExist(int tenantId, String newAlertName) {
        if (savedScheduleAlertList.size() > 0) {
            List<ScheduleAlertBean> savedAlertNameList = savedScheduleAlertList.get(tenantId);
            if (savedAlertNameList != null) {
                for (ScheduleAlertBean alertName : savedAlertNameList) {
                    if (alertName.getAlertName().equals(newAlertName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
