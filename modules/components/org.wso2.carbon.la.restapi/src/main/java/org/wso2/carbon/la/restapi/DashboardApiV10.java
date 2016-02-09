package org.wso2.carbon.la.restapi;

/**
 * Created by vithulan on 2/2/16.
 */
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.la.commons.constants.LAConstants;
import org.wso2.carbon.la.commons.domain.QueryBean;
import org.wso2.carbon.la.commons.domain.RecordBean;
import org.wso2.carbon.la.commons.domain.config.LogFileConf;
import org.wso2.carbon.la.core.impl.LogFileProcessor;
import org.wso2.carbon.la.core.utils.LACoreServiceValueHolder;
import org.wso2.carbon.la.restapi.beans.LAErrorBean;
import org.wso2.carbon.la.restapi.util.Util;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.*;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


@Path("/dashboard")
public class DashboardApiV10 {

    private static final Log log = LogFactory.getLog(DashboardApiV10.class);
    private static final Gson gson = new Gson();
    @GET
    @Path("getFields")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getFields() throws AnalyticsException {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        //String username = carbonContext.getUsername();
        Map<String, ColumnDefinition> columns;
        List<String> fields = new ArrayList<String>();
        AnalyticsDataAPI analyticsDataAPI;
        AnalyticsSchema analyticsSchema;

        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        analyticsSchema = analyticsDataAPI.getTableSchema(tenantId, LAConstants.LOG_ANALYZER_STREAM_NAME.toUpperCase());
        columns = analyticsSchema.getColumns();
        for (ColumnDefinition alpha : columns.values()) {
            fields.add(alpha.getName());
        }
        return Response.ok(fields).build();
    }

    @POST
    @Path("/fieldData")
    @Produces("application/json")
    @Consumes("application/json")
    public StreamingOutput fieldData(final QueryBean query) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        //String username = carbonContext.getUsername();
        List <String> column = new ArrayList<String>();
        column.add(query.getQuery());
        AnalyticsDataAPI analyticsDataAPI;
        AnalyticsDataResponse analyticsDataResponse;
        //RecordGroup recordGroup [] ;


        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        try {
            analyticsDataResponse = analyticsDataAPI.get(tenantId,LAConstants.LOG_ANALYZER_STREAM_NAME.toUpperCase(),1,
                    column,query.getTimeFrom(),query.getTimeTo(),query.getStart(),-1);
           // recordGroup = analyticsDataResponse.getRecordGroups();
            final List<Iterator<Record>> iterators = Util.getRecordIterators(analyticsDataResponse,analyticsDataAPI);

            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    Map<String, Object> values;
                    Map<String,Integer> counter = new HashMap<String,Integer>();

                    int count=0;
                    String val;
                    recordWriter.write("[");
                    for (Iterator<Record> iterator : iterators) {
                        while (iterator.hasNext()) {
                            RecordBean recordBean = Util.createRecordBean(iterator.next());
                            values = recordBean.getValues();

                            if(values.get(query.getQuery())==null) {

                                    if (!counter.containsKey("NULLVALUE")) {
                                        counter.put("NULLVALUE", 1);
                                    } else {
                                        count = counter.get("NULLVALUE");
                                        count++;
                                        counter.put("NULLVALUE", count);
                                    }

                            }
                            else {
                                val=values.get(query.getQuery()).toString();
                                if (!counter.containsKey(val)) {
                                    counter.put(val, 1);
                                } else {
                                    count = counter.get(val);
                                    count++;
                                    counter.put(val, count);
                                }
                            }
                            //values.get(query.getQuery());

                           // recordWriter.write(gson.toJson(values.get(query.getQuery())));

                           // recordWriter.write(recordBean.toString());
                           // if (iterator.hasNext()) {
                                //recordWriter.write(",");
                            //}
                            if (log.isDebugEnabled()) {
                                log.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                        recordBean.toString());
                            }
                        }
                    }
                    int i=1;
                    for(Map.Entry<String,Integer> entry:counter.entrySet()){

                        recordWriter.write("\""+entry.getKey()+" : "+entry.getValue()+"||%\"");
                        if(i<counter.size()) {
                            recordWriter.write(",");
                            i++;
                        }
                    }
                    recordWriter.write("]");
                    recordWriter.flush();
                }
            };

        } catch (AnalyticsException e) {
            String msg = String.format( "Error occurred while retrieving field data");
            log.error(msg, e);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    recordWriter.write("Error in reading records");
                    recordWriter.flush();
                }
            };
        }


    }
}


