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

package org.wso2.carbon.la.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.la.commons.constants.LAConstants;
import org.wso2.carbon.la.commons.domain.LogGroup;
import org.wso2.carbon.la.commons.domain.LogStream;
import org.wso2.carbon.la.core.exceptions.LogsControllerException;
import org.wso2.carbon.la.core.utils.LACoreServiceValueHolder;
import org.wso2.carbon.la.database.DatabaseService;
import org.wso2.carbon.la.database.exceptions.DatabaseHandlerException;

import java.util.*;

public class LogsController {

    private static final Log log = LogFactory.getLog(LogsController.class);

    private DatabaseService databaseService;

    public LogsController(){
        databaseService = LACoreServiceValueHolder.getInstance().getDatabaseService();

    }

    public int  createLogGroup(LogGroup logGroup) throws LogsControllerException {
        try {
            return databaseService.createLogGroup(logGroup);
        } catch (DatabaseHandlerException e) {
            throw new LogsControllerException(e.getMessage(),e);
        }
    }

    public void deleteLogGroup(String name, int tenantId, String username) throws LogsControllerException{
        try {
            databaseService.deleteLogGroup(name, tenantId, username);
            if(log.isDebugEnabled()){
                log.debug("Log Group deleted : " + name );
            }
        } catch (DatabaseHandlerException e) {
            throw new LogsControllerException(e.getMessage(),e);
        }
    }

    public List<LogGroup> getAllLogGroups(int tenantId, String username) throws LogsControllerException{
        return null;
    }

    public List<String> getAllLogGroupNames(int tenantId, String username) throws LogsControllerException{
        try {
            List<String> logGroupList = databaseService.getAllLogGroupNames(tenantId, username);
            return logGroupList;
        } catch (DatabaseHandlerException e) {
            throw new LogsControllerException(e.getMessage(),e);
        }
    }

    public void createLogStream(LogStream logStream) throws LogsControllerException{
        try {
            databaseService.createLogStream(logStream);
            if(log.isDebugEnabled()){
                log.debug("Log Stream created : " + logStream.getName() );
            }
        } catch (DatabaseHandlerException e) {
            throw new LogsControllerException(e.getMessage(),e);
        }
    }

    public void deleteLogStream(String name, int logGroupId) throws LogsControllerException{
        try {
            databaseService.deleteLogStream(name, logGroupId);
            if(log.isDebugEnabled()){
                log.debug("Log stream deleted : " + name + " log groupId : " + logGroupId);
            }
        } catch (DatabaseHandlerException e) {
            throw new LogsControllerException(e.getMessage(),e);
        }
    }

    public List<String> getAllLogStreamNames(int logGroupId) throws LogsControllerException{
        try {
            List<String> logStreamList = databaseService.getAllLogStreamNamesOfLogGroup(logGroupId);
            return logStreamList;
        } catch (DatabaseHandlerException e) {
            throw new LogsControllerException(e.getMessage(),e);
        }
    }

    public void publishLogEvent(Map<String, String> rawEvent, int tenantId, String username) throws LogsControllerException{
        if(!rawEvent.containsKey(LAConstants.LOG_GROUP)){
            log.error("loggroup doesn't exist in the event, hence avoiding event persistence");
            return;
        }

        if(!rawEvent.containsKey(LAConstants.LOG_STREAM)){
            log.error("logstream doesn't exist in the event, hence avoiding event persistence");
            return;
        }

        String logGroup = rawEvent.get(LAConstants.LOG_GROUP);
        String logStream = rawEvent.get(LAConstants.LOG_STREAM);

        rawEvent.remove(LAConstants.LOG_GROUP);
        rawEvent.remove(LAConstants.LOG_STREAM);

        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(LAConstants.LOG_ANALYZER_STREAM_NAME, LAConstants.LOG_ANALYZER_STREAM_VERSION);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Unable to create stream definition", e);
        }
        Map<String, ColumnDefinition> newArbitrayColumns = getNewArbitraryFields(rawEvent,username);
        if(newArbitrayColumns.size() > 0){
           updateSchema(newArbitrayColumns, username);
           updateLogStreamMetadata(newArbitrayColumns.keySet());
        }

        if(streamDefinition != null){
            EventStreamService eventStreamService = LACoreServiceValueHolder.getInstance().getEventStreamService();
            if (eventStreamService != null) {
                Event tracingEvent = new Event();
                tracingEvent.setTimeStamp(System.currentTimeMillis());
                tracingEvent.setStreamId(streamDefinition.getStreamId());
                tracingEvent.setTimeStamp(System.currentTimeMillis());
                tracingEvent.setPayloadData(new Object[]{logGroup, logStream});
                tracingEvent.setArbitraryDataMap(rawEvent);

                eventStreamService.publish(tracingEvent);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully published event " + tracingEvent.toString());
                }
            }
        }
    }

    private void updateSchema(Map<String, ColumnDefinition> newArbitrayColumns, String username) throws LogsControllerException {
        AnalyticsDataAPI analyticsDataAPIService = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        AnalyticsSchema analyticsSchema;
        try {
            analyticsSchema = analyticsDataAPIService.getTableSchema(username, LAConstants.LOG_ANALYZER_STREAM_NAME);
            Map<String, ColumnDefinition> previousColumns = analyticsSchema.getColumns();
            previousColumns.putAll(newArbitrayColumns);

            List<String> primaryKeys = new ArrayList<>();
            primaryKeys.add(LAConstants.LOG_GROUP);
            primaryKeys.add(LAConstants.LOG_STREAM);
            AnalyticsSchema updatedAnalyticsSchema =  new AnalyticsSchema(new ArrayList(previousColumns.values()), primaryKeys);
            analyticsDataAPIService.setTableSchema(username, LAConstants.LOG_ANALYZER_STREAM_NAME, updatedAnalyticsSchema);
            if(log.isDebugEnabled()){
                log.debug("Log Analyzer schema updated successfully");
            }
        } catch (AnalyticsException e) {
            throw new LogsControllerException("Error occurred while updating loganalyzer schema " + e.getMessage(), e);
        }
    }

    private Map<String, ColumnDefinition> getNewArbitraryFields(Map<String, String> rawEvent, String username) throws LogsControllerException {
        Map<String, ColumnDefinition> newColumns = new HashMap<>();
        AnalyticsDataAPI analyticsDataAPIService = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        try {
            //TODO : make getSchema inmemory operation
            AnalyticsSchema analyticsSchema = analyticsDataAPIService.getTableSchema(username, LAConstants.LOG_ANALYZER_STREAM_NAME);
            Map<String, ColumnDefinition> indexedColumns = analyticsSchema.getColumns();
            for(String arbitraryKey : rawEvent.keySet()){
                String columnName = LAConstants.ARBITRARY_FIELD_PREFIX + arbitraryKey;
                if(!indexedColumns.containsKey(columnName)){
                    //TODO : infer the column type
                    newColumns.put(columnName, new ColumnDefinition(columnName, AnalyticsSchema.ColumnType.STRING, true, false));
                }
            }
        } catch (AnalyticsException e) {
           throw new LogsControllerException("Error occurred while getting new arbitrary fields of the event " + e.getMessage(), e);
        }
        return newColumns;
    }

    private void updateLogStreamMetadata(Set<String> strings) {

    }
}
