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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.la.alert.domain.LAAlertConstant;
import org.wso2.carbon.la.alert.domain.ScheduleAlertBean;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class AlertConfigurationUtils {
    private static final Log log = LogFactory.getLog(AlertPublisherUtils.class);

    /**
     * This method save the configuration of Alert at the Carbon Registry
     *
     * @param scheduleAlertBean -Alert info for save on Registry
     * @param tenantId          -user tenantId for get Tenant Registry
     * @throws RegistryException
     */
    public static void saveAlertConfiguration(ScheduleAlertBean scheduleAlertBean, int tenantId) throws RegistryException {
        try {
            UserRegistry userRegistry = LAAlertServiceValueHolder.getInstance().getTenantConfigRegistry(tenantId);
            createConfigurationCollection(userRegistry);
            String configurationLocation = getConfigurationLocation(scheduleAlertBean.getAlertName());
            if (!userRegistry.resourceExists(configurationLocation)) {
                Resource resource = getResource(userRegistry, scheduleAlertBean);
                if (!resource.equals(null)) {
                    userRegistry.put(configurationLocation, resource);
                } else {
                    log.warn("Unable to save Alert configurations, Resource contains NUll");
                    throw new RegistryException("Resource contains NULL");
                }
            }
        } catch (RegistryException e) {
            log.error("Unable to save Alert configurations " + e.getMessage(), e);
            throw new RegistryException("Unable to save Alert Configuration " + e.getMessage(), e);
        }
    }

    /**
     * This method update the configuration of an alert at carbon registry
     *
     * @param scheduleAlertBean -Alert info for update on Registry
     * @param tenantId          -user tenantId for get Tenant Registry
     * @throws RegistryException
     */
    public static void updateAlertConfiguration(ScheduleAlertBean scheduleAlertBean, int tenantId) throws RegistryException {
        try {
            UserRegistry userRegistry = LAAlertServiceValueHolder.getInstance().getTenantConfigRegistry(tenantId);
            String fileLocation = getConfigurationLocation(scheduleAlertBean.getAlertName());
            if (userRegistry.resourceExists(fileLocation)) {
                Resource resource = getResource(userRegistry, scheduleAlertBean);
                if (!resource.equals(null)) {
                    userRegistry.put(fileLocation, resource);
                } else {
                    log.warn("Unable to save Alert configurations, Resource contains NUll");
                    throw new RegistryException("Resource contains NULL");
                }
            }
        } catch (RegistryException e) {
            log.error("Unable to update Alert configurations " + e.getMessage(), e);
            throw new RegistryException("Unable to update Alert Configuration " + e.getMessage(), e);
        }
    }

    public static void deleteAlertConfiguration(String alertName, int tenantId) throws RegistryException {
        try {
            UserRegistry userRegistry = LAAlertServiceValueHolder.getInstance().getTenantConfigRegistry(tenantId);
            String fileLocation = getConfigurationLocation(alertName);
            if (userRegistry.resourceExists(fileLocation)) {
                userRegistry.delete(fileLocation);
            } else {
                log.warn(alertName + " alert configuration file is not exist.");
                throw new RegistryException(alertName + " alert configuration file is not exit.");
            }
        } catch (RegistryException e) {
            log.error("Unable to delete Alert configurations " + e.getMessage(), e);
            throw new RegistryException("Unable to delete Alert Configuration " + e.getMessage(), e);
        }
    }

    /**
     * this method create collection and put it to the registry location. saveConfiguration method
     * use this for save alert configuration
     *
     * @param userRegistry -Tenant Config Registry
     */
    public static void createConfigurationCollection(UserRegistry userRegistry) {
        try {
            if (!userRegistry.resourceExists(LAAlertConstant.ALERT_CONFIGURATION_LOCATION)) {
                Collection collection = userRegistry.newCollection();
                userRegistry.put(LAAlertConstant.ALERT_CONFIGURATION_LOCATION, collection);
            }
        } catch (RegistryException e) {
            log.error("Unable to create Configuration Collection in Registry " + e.getMessage(), e);
        }
    }

    /**
     * This method returns the created alert specific path
     *
     * @param alertName -alert Name to create alert file path
     * @return String alert file path
     */
    public static String getConfigurationLocation(String alertName) {
        return LAAlertConstant.ALERT_CONFIGURATION_LOCATION + RegistryConstants.PATH_SEPARATOR + alertName +
                LAAlertConstant.CONFIGURATION_EXTENSION_SEPARATOR + LAAlertConstant.CONFIGURATION_EXTENSION;
    }

    private static Resource getResource(UserRegistry userRegistry, ScheduleAlertBean scheduleAlertBean) {
        Resource resource = null;
        try {
            resource = userRegistry.newResource();
            Gson gson = new Gson();
            String json = gson.toJson(scheduleAlertBean);
            resource.setContent(json);
            resource.setMediaType(LAAlertConstant.CONFIGURATION_MEDIA_TYPE);
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return resource;
    }

//    /**
//     * This method return all configurations of the tenant
//     *
//     * @param tenantId -tenantId for get tenant configuration
//     * @return String[]
//     */
//    public static String[] getAllConfigurations(int tenantId) {
//        String[] allConfigurations = null;
//        try {
//            UserRegistry userRegistry = LAAlertServiceValueHolder.getInstance().getTenantConfigRegistry(tenantId);
//            createConfigurationCollection(userRegistry);
//            Collection configurationCollection = (Collection) userRegistry.get(LAAlertConstant.ALERT_CONFIGURATION_LOCATION);
//            allConfigurations = configurationCollection.getChildren();
//        } catch (RegistryException e) {
//
//        }
//        return allConfigurations;
//    }
}
