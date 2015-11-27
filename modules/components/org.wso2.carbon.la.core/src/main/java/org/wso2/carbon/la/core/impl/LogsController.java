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
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.la.commons.constants.LAConstants;
import org.wso2.carbon.la.commons.domain.LogGroup;
import org.wso2.carbon.la.commons.domain.LogStream;
import org.wso2.carbon.la.core.exceptions.LogsControllerException;
import org.wso2.carbon.la.core.utils.LACoreServiceValueHolder;
import org.wso2.carbon.la.database.DatabaseService;
import org.wso2.carbon.la.database.exceptions.DatabaseHandlerException;

import java.util.HashMap;
import java.util.List;

public class LogsController {

    private static final Log log = LogFactory.getLog(LogsController.class);

    private DatabaseService databaseService;
    private DataPublisher dataPublisher;

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

    public void publishLogEvent(HashMap<String, String> rawEvent, int tenantId, String username) throws LogsControllerException{
        Event event = new Event();
        if(!rawEvent.containsKey(LAConstants.LOG_GROUP)){
            throw new LogsControllerException("Log group doesn't exist in the event");
        }

        if(!rawEvent.containsKey(LAConstants.LOG_STREAM)){
            throw new LogsControllerException("Logs stream doesn't exist in the event");
        }

        String logGroup = rawEvent.get(LAConstants.LOG_GROUP);
        String logStream = rawEvent.get(LAConstants.LOG_STREAM);

        rawEvent.remove(LAConstants.LOG_GROUP);
        rawEvent.remove(LAConstants.LOG_STREAM);

        event.setStreamId(LAConstants.LOG_STREAM_ID);
        event.setTimeStamp(System.currentTimeMillis());
        event.setPayloadData(new Object[]{logGroup, logStream});
        event.setArbitraryDataMap(rawEvent);

        dataPublisher.publish(event);
        // event.setPayloadData();

    }
}
