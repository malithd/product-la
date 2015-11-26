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
import org.wso2.carbon.la.commons.domain.LogGroup;
import org.wso2.carbon.la.core.exceptions.LogsControllerException;
import org.wso2.carbon.la.core.utils.LACoreServiceValueHolder;
import org.wso2.carbon.la.database.DatabaseService;
import org.wso2.carbon.la.database.exceptions.DatabaseHandlerException;

import java.util.ArrayList;
import java.util.List;

public class LogsController {

    private static final Log log = LogFactory.getLog(LogsController.class);

    private DatabaseService databaseService;

    public LogsController(){
        databaseService = LACoreServiceValueHolder.getInstance().getDatabaseService();
    }

    public void createLogGroup(LogGroup logGroup) throws LogsControllerException {
        try {
            databaseService.createLogGroup(logGroup);
            if(log.isDebugEnabled()){
                log.debug("Log Group created : " + logGroup.getName() );
            }
        } catch (DatabaseHandlerException e) {
            throw new LogsControllerException(e.getMessage(),e);
        }
    }

    public void deleteLogGroup(String name, int tenantId, String username) throws LogsControllerException{
        try {
            databaseService.deleteLogGroup(name,tenantId,username);
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
}
