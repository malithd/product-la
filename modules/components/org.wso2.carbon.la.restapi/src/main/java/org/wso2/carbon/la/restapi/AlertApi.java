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

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.la.alert.domain.SATaskInfo;
import org.wso2.carbon.la.alert.impl.ScheduleAlertControllerImpl;
import org.wso2.carbon.la.commons.constants.LAConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;


@Path("/alert")
public class AlertApi {
        ScheduleAlertControllerImpl scheduleAlertControllerImpl;
        public AlertApi(){
            scheduleAlertControllerImpl =new ScheduleAlertControllerImpl();
        }

    @POST
    @Path("/save")
    @Consumes("application/json")
    @Produces("application/json")
    public Response saveAlert(SATaskInfo saTaskInfo) {
        PrivilegedCarbonContext carbonContext=PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username=carbonContext.getUsername();
        int tenantId=carbonContext.getTenantId();
        saTaskInfo.setTableName(LAConstants.LOG_ANALYZER_STREAM_NAME);
        saTaskInfo.setStart(0);
        saTaskInfo.setLength(100);
        scheduleAlertControllerImpl.registerScheduleAlertTask(saTaskInfo,username,tenantId);
        return Response.ok().build();
    }

    @GET
    @Path("getAllScheduleAlerts")
    @Produces("application/json")
    public Response getAllScheduleAlerts() {
        PrivilegedCarbonContext carbonContext=PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId=carbonContext.getTenantId();
        List<SATaskInfo> saTaskInfoList= scheduleAlertControllerImpl.getAllAlertConfigurations(tenantId);
        return Response.ok(saTaskInfoList.toArray()).build();
    }

    @DELETE
    @Path("/delete/{alertName}")
    @Produces("application/json")
    @Consumes("application/json")
    public Response deleteScheduleAlert(@PathParam("alertName") String alertName) throws TaskException, RegistryException, EventStreamConfigurationException, EventPublisherConfigurationException {
        PrivilegedCarbonContext carbonContext=PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId=carbonContext.getTenantId();
        scheduleAlertControllerImpl.deleteAlertTask(alertName,tenantId);
        return Response.ok().build();
    }

    @GET
    @Path("getAlertContent/{alertName}")
    @Produces("application/json")
    public SATaskInfo getAlertContent(@PathParam("alertName") String alertName) throws RegistryException {
        PrivilegedCarbonContext carbonContext=PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId=carbonContext.getTenantId();
        SATaskInfo saTaskInfo=scheduleAlertControllerImpl.getAlertConfiguration(alertName,tenantId);
        return saTaskInfo;
    }

    @PUT
    @Path("/update")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateAlertContent(SATaskInfo saTaskInfo) throws RegistryException, TaskException, EventPublisherConfigurationException {
        PrivilegedCarbonContext carbonContext=PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username=carbonContext.getUsername();
        int tenantId=carbonContext.getTenantId();
        saTaskInfo.setTableName(LAConstants.LOG_ANALYZER_STREAM_NAME);
        saTaskInfo.setStart(0);
        saTaskInfo.setLength(100);
        scheduleAlertControllerImpl.updateScheduleAlertTask(saTaskInfo,username,tenantId);
        return Response.ok().build();
    }
}
