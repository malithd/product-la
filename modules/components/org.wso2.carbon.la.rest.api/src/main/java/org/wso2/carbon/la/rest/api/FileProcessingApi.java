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

package org.wso2.carbon.la.rest.api;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.la.rest.api.beans.ConfigurationBean;
import org.wso2.carbon.la.rest.api.beans.ErrorBean;

/**
 * File Processing API
 */

@Path("/files")
public class FileProcessingApi {

    private static final Log logger = LogFactory.getLog(FileProcessingApi.class);

    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }

 /*   @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDataset(@Multipart("datasetName") String datasetName, @Multipart("version") String version,
                                  @Multipart("description") String description, @Multipart("sourceType") String sourceType,
                                  @Multipart("destination") String destination, @Multipart("sourcePath") String sourcePath,
                                  @Multipart("dataFormat") String dataFormat, @Multipart("containsHeader") boolean containsHeader,
                                  @Multipart("file") InputStream inputStream) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        ConfigurationBean configuration = new ConfigurationBean();
        try {
            // validate input parameters
            if (sourceType == null || sourceType.isEmpty()) {
                String msg = "Required parameters are missing.";
                logger.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorBean(msg)).build();
            }

            dataset.setName(datasetName);
            dataset.setVersion(version);
            dataset.setSourcePath(sourcePath);
            dataset.setDataSourceType(sourceType);
            dataset.setComments(description);
            dataset.setDataTargetType(destination);
            dataset.setDataType(dataFormat);
            dataset.setTenantId(tenantId);
            dataset.setUserName(userName);
            dataset.setContainsHeader(containsHeader);

            datasetProcessor.process(dataset, inputStream);
            return Response.ok(dataset).build();
        } catch (MLInputValidationException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while uploading a [dataset] %s of tenant [id] %s and [user] %s .", dataset,
                    tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
                    .entity(new MLErrorBean(e.getMessage())).build();

        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while uploading a [dataset] %s of tenant [id] %s and [user] %s .", dataset,
                    tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }*/
}
