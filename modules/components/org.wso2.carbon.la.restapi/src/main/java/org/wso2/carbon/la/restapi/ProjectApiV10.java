/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.la.restapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/projects")
public class ProjectApiV10 extends LARestAPI {

    private static final Log logger = LogFactory.getLog(ProjectApiV10.class);
    //private MLProjectHandler mlProjectHandler;

    public ProjectApiV10(){
    }

    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }

    /**
     * Create a new Project. No validation happens here. Please call {@link #getProject(String)} before this.
     * @param project {@link org.wso2.carbon.ml.commons.domain.MLProject} object
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response createProject() {
        return Response.ok("Hello World").build();
    }

}
