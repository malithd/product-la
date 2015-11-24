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

package org.wso2.carbon.la.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.la.core.utils.LACoreServiceValueHolder;
import org.wso2.carbon.la.database.DatabaseService;

/**
 * @scr.component name="la.core" immediate="true"
 * @scr.reference name="databaseService" interface="org.wso2.carbon.la.database.DatabaseService" cardinality="1..1"
 *                policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 *
 */
public class LACoreDS {

    private static final Log log = LogFactory.getLog(LACoreDS.class);

    protected void activate(ComponentContext context) {

    }

    protected void deactivate(ComponentContext componentContext){
        if(log.isDebugEnabled()){
            log.debug("log analyzer core component deactivated");
        }
    }

    protected void setDatabaseService(DatabaseService databaseService) {
        LACoreServiceValueHolder.getInstance().registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService) {
        LACoreServiceValueHolder.getInstance().registerDatabaseService(databaseService);
    }


}
