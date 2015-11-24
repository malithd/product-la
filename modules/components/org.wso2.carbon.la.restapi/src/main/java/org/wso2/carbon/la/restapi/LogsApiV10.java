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

package org.wso2.carbon.la.restapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.la.commons.domain.LogGroup;
import org.wso2.carbon.la.core.exceptions.LogsControllerException;
import org.wso2.carbon.la.core.impl.LogsController;
import org.wso2.carbon.la.restapi.beans.LAErrorBean;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/logs")
public class LogsApiV10 extends LARestApi {

    private static final Log logger = LogFactory.getLog(LogsApiV10.class);
    private LogsController logsController;

    public LogsApiV10(){
        logsController = new LogsController();
    }

    /**
     * HTTP Options method implementation for analysis API.
     *
     * @return
     */
    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createLogGroup(LogGroup logGroup){
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();

        try {
            logGroup.setTenantId(tenantId);
            logGroup.setName(userName);
            logsController.createLogGroup(logGroup);
            return Response.ok().build();
        } catch (LogsControllerException e) {
            String msg = String.format(
                    "Error occurred while creating [log group] %s of tenant [id] %s and [user] %s .", logGroup.getName(),
                    tenantId, userName);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new LAErrorBean(e.getMessage()))
                    .build();
        }
    }

    public void deleteLogGroup(String groupName){

    }

    public void getlAllLogGroups(){

    }

    public void creatLogStream(String streamId){

    }

    public void deleteLogStream(){

    }

    public void getAllLogStreams(){

    }

}
