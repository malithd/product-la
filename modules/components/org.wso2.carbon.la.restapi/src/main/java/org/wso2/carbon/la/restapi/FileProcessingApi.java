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

import javax.activation.DataHandler;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.analytics.activitydashboard.admin.ActivityDashboardAdminService;
import org.wso2.carbon.analytics.activitydashboard.admin.ActivityDashboardException;
import org.wso2.carbon.analytics.activitydashboard.admin.bean.ActivitySearchRequest;
import org.wso2.carbon.analytics.activitydashboard.commons.InvalidExpressionNodeException;
import org.wso2.carbon.analytics.activitydashboard.commons.Query;
import org.wso2.carbon.analytics.activitydashboard.commons.SearchExpressionTree;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Multipart("file") Attachment attachment){
        DataHandler dataHandler = attachment.getDataHandler();
        try {
            InputStream stream = dataHandler.getInputStream();
            MultivaluedMap<String, String> map = attachment.getHeaders();
            System.out.println("fileName Here" + getFileName(map));
            String currentDir;
            if ((currentDir = System.getProperty("carbon.home")).equals(".")) {
                currentDir = new File(".").getAbsolutePath();
            }
            OutputStream out = new FileOutputStream(new File(currentDir+ "/repository/data/" + getFileName(map)));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = stream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            stream.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(200).build();
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String exactFileName = name[1].trim().replaceAll("\"", "");
                return exactFileName;
            }
        }
        return "unknown";
    }

    @GET
    @Path("getLogs")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getLogs( @QueryParam("noOfLines") int noOfLines,  @QueryParam("fileName") String fileName) {
        Object [] logLines;

        logLines = readLogs(noOfLines, fileName);

        return Response.ok(Arrays.copyOf(logLines, logLines.length, String[].class)).build();
    }

    private  Object[] readLogs(int noOfLines, String fileName) {
        List<String> lines = new ArrayList<>();
        String currentDir;
        if ((currentDir = System.getProperty("carbon.home")).equals(".")) {
            currentDir = new File(".").getAbsolutePath();
        }
        File file = new File(currentDir+ "/repository/data/" + fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            int offset=0;
            while (true) {
                String line = br.readLine();
                if (line != null && offset < noOfLines) {
                    lines.add(line);
                    offset ++;
                } else {
                    break;
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            //logger.log(Level.SEVERE, "Error reading", ex);
        }
        return  lines.toArray();
    }

}
