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
import org.wso2.carbon.analytics.dataservice.commons.*;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        List<String> column = new ArrayList<String>();
        column.add(query.getQuery());
        AnalyticsDataAPI analyticsDataAPI;
        AnalyticsDataResponse analyticsDataResponse;
        //RecordGroup recordGroup [] ;


        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        try {
            analyticsDataResponse = analyticsDataAPI.get(tenantId, LAConstants.LOG_ANALYZER_STREAM_NAME.toUpperCase(), 1,
                    column, query.getTimeFrom(), query.getTimeTo(), query.getStart(), -1);
            // recordGroup = analyticsDataResponse.getRecordGroups();
            final List<Iterator<Record>> iterators = Util.getRecordIterators(analyticsDataResponse, analyticsDataAPI);

            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    Map<String, Object> values;
                    Map<String, Integer> counter = new HashMap<String, Integer>();

                    int count = 0;
                    String val;
                    recordWriter.write("[");
                    for (Iterator<Record> iterator : iterators) {
                        while (iterator.hasNext()) {
                            RecordBean recordBean = Util.createRecordBean(iterator.next());
                            values = recordBean.getValues();

                            if (values.get(query.getQuery()) == null) {

                                if (!counter.containsKey("NULLVALUE")) {
                                    counter.put("NULLVALUE", 1);
                                } else {
                                    count = counter.get("NULLVALUE");
                                    count++;
                                    counter.put("NULLVALUE", count);
                                }

                            } else {
                                val = values.get(query.getQuery()).toString();
                                if (!counter.containsKey(val)) {
                                    counter.put(val, 1);
                                } else {
                                    count = counter.get(val);
                                    count++;
                                    counter.put(val, count);
                                }
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                        recordBean.toString());
                            }
                        }
                    }
                    int i = 1;
                    for (Map.Entry<String, Integer> entry : counter.entrySet()) {

                        recordWriter.write("[[\"" + entry.getKey() + "\"],[\"" + entry.getValue() + "\"]]");

                        if (i < counter.size()) {
                            recordWriter.write(",");
                            i++;
                        }
                    }
                    recordWriter.write("]");
                    recordWriter.flush();
                }
            };

        } catch (AnalyticsException e) {
            String msg = String.format("Error occurred while retrieving field data");
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

    @POST
    @Path("/filterData")
    @Produces("application/json")
    @Consumes("application/json")
    public StreamingOutput filterData(final QueryBean query) throws AnalyticsException {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String username = carbonContext.getUsername();
        AnalyticsDataAPI analyticsDataAPI;
        AnalyticsDataResponse analyticsDataResponse;
        String logstream = "logstream";
        //RecordGroup recordGroup [] ;
        AnalyticsDrillDownRequest analyticsDrillDownRequest = new AnalyticsDrillDownRequest();
        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        if (query != null) {
            String q[] = query.getQuery().split(",,");
            String searchQuery = q[0];
            final String col = q[1];
            List<String> column = new ArrayList<>();
            column.add(col);

            String facetPath = query.getFacetPath();
            Map<String, List<String>> categoryPath = new HashMap<>();
            List<String> pathList = new ArrayList<>();
            if (facetPath.equals("None")) {
                categoryPath.put(logstream, pathList);
            } else {
                String pathArray[] = facetPath.split(",");
                for (String path : pathArray) {
                    pathList.add(path);
                }
                categoryPath.put(logstream, pathList);
            }
            analyticsDrillDownRequest.setTableName(query.getTableName());
            analyticsDrillDownRequest.setQuery(searchQuery);
            analyticsDrillDownRequest.setRecordCount(query.getLength());
            analyticsDrillDownRequest.setRecordStartIndex(query.getStart());
            analyticsDrillDownRequest.setCategoryPaths(categoryPath);
            List<SearchResultEntry> searchResults = analyticsDataAPI.drillDownSearch(username, analyticsDrillDownRequest);

            List<String> ids = Util.getRecordIds(searchResults);
            analyticsDataResponse = analyticsDataAPI.get(username, query.getTableName(), 1, column, ids);
            final List<Iterator<Record>> iterators = Util.getRecordIterators(analyticsDataResponse, analyticsDataAPI);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    Map<String, Object> values;
                    Map<String, Integer> counter = new HashMap<String, Integer>();

                    int count = 0;
                    String val;
                    recordWriter.write("[");
                    for (Iterator<Record> iterator : iterators) {
                        while (iterator.hasNext()) {
                            RecordBean recordBean = Util.createRecordBean(iterator.next());
                            values = recordBean.getValues();

                            if (values.get(col) == null) {

                                if (!counter.containsKey("NULLVALUE")) {
                                    counter.put("NULLVALUE", 1);
                                } else {
                                    count = counter.get("NULLVALUE");
                                    count++;
                                    counter.put("NULLVALUE", count);
                                }

                            } else {
                                val = values.get(col).toString();
                                if (!counter.containsKey(val)) {
                                    counter.put(val, 1);
                                } else {
                                    count = counter.get(val);
                                    count++;
                                    counter.put(val, count);
                                }
                            }

                            if (log.isDebugEnabled()) {
                                log.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                        recordBean.toString());
                            }
                        }
                    }
                    int i = 1;
                    for (Map.Entry<String, Integer> entry : counter.entrySet()) {

                        recordWriter.write("[[\"" + entry.getKey() + "\"],[\"" + entry.getValue() + "\"]]");
                        if (i < counter.size()) {
                            recordWriter.write(",");
                            i++;
                        }
                    }
                    recordWriter.write("]");
                    recordWriter.flush();
                }
            };
        } else {
            String msg = String.format("Error occurred while retrieving field data");
            log.error(msg);
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
/*
    @POST
    @Path("/timeData")
    @Produces("application/json")
    @Consumes("application/json")
    public StreamingOutput timeData(final QueryBean query) throws AnalyticsException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String username = carbonContext.getUsername();
        AnalyticsDataAPI analyticsDataAPI;
        AnalyticsDataResponse analyticsDataResponse;
        //RecordGroup recordGroup [] ;
        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        if (query != null) {
            String q[] = query.getQuery().split(",,");
            String searchQuery = q[0];
            final String col = q[1];
            final String timestamp = "_timestamp2";
            List<String> column = new ArrayList<>();
            column.add(col);
            column.add(timestamp);

            List<SearchResultEntry> searchResults = analyticsDataAPI.search(username,
                    query.getTableName(), searchQuery,
                    query.getStart(), query.getLength());
            List<String> ids = Util.getRecordIds(searchResults);
            analyticsDataResponse = analyticsDataAPI.get(username, query.getTableName(), 1, column, ids);
            final List<Iterator<Record>> iterators = Util.getRecordIterators(analyticsDataResponse, analyticsDataAPI);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    Map<String, Object> values;

                    Map<String, Map<String, Integer>> stamper = new HashMap<>();
                    int count = 0;
                    String val;
                    String time;
                    recordWriter.write("[");
                    for (Iterator<Record> iterator : iterators) {
                        while (iterator.hasNext()) {
                            RecordBean recordBean = Util.createRecordBean(iterator.next());
                            values = recordBean.getValues();
                            String temp[];
                            time = values.get(timestamp).toString();
                            temp = time.split(" ");
                            time = temp[0];
                            if (values.get(col) != null) {
                                val = values.get(col).toString();

                                if (!stamper.containsKey(time)) {
                                    Map<String, Integer> counter = new HashMap<String, Integer>();
                                    counter.put(val, 1);
                                    stamper.put(time, counter);
                                } else {
                                    Map<String, Integer> counter = stamper.get(time);
                                    if (!counter.containsKey(val)) {
                                        counter.put(val, 1);
                                        stamper.put(time, counter);
                                    } else {
                                        count = counter.get(val);
                                        count++;
                                        counter.put(val, count);
                                        stamper.put(time, counter);
                                    }
                                }

                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                        recordBean.toString());
                            }
                        }
                    }
                    int i = 1;
                    int j = 1;
                    for (Map.Entry<String, Map<String, Integer>> entry : stamper.entrySet()) {
                        Map<String, Integer> counter = entry.getValue();
                        for (Map.Entry<String, Integer> infom : counter.entrySet()) {
                            recordWriter.write("[[\"" + entry.getKey() + "\"],[\"" + infom.getKey() + "\"],[\"" + infom.getValue() + "\"]]");
                            if (i < counter.size()) {
                                recordWriter.write(",");
                                i++;
                            }
                        }
                        i = 1;
                        if (j < stamper.size()) {
                            recordWriter.write(",");
                            j++;
                        }

                    }
                    recordWriter.write("]");
                    recordWriter.flush();
                }
            };
        } else {
            String msg = String.format("Error occurred while retrieving field data");
            log.error(msg);
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

    @POST
    @Path("/epochTimeData")
    @Produces("application/json")
    @Consumes("application/json")
    public StreamingOutput EpochtimeData(final QueryBean query) throws AnalyticsException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String username = carbonContext.getUsername();
        AnalyticsDataAPI analyticsDataAPI;
        AnalyticsDataResponse analyticsDataResponse;
        //RecordGroup recordGroup [] ;
        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        if (query != null) {
            String q[] = query.getQuery().split(",,");
            String searchQuery = q[0];
            final String col = q[1];
            final String timestamp = "_timestamp2";
            final long dayGap = 86400000;
            final String pattern = "yyyy-MM-dd";
            final SimpleDateFormat format = new SimpleDateFormat(pattern);
            List<String> column = new ArrayList<>();
            column.add(col);
            column.add(timestamp);

            List<SearchResultEntry> searchResults = analyticsDataAPI.search(username,
                    query.getTableName(), searchQuery,
                    query.getStart(), query.getLength());
            List<String> ids = Util.getRecordIds(searchResults);
            analyticsDataResponse = analyticsDataAPI.get(username, query.getTableName(), 1, column, ids);
            final List<Iterator<Record>> iterators = Util.getRecordIterators(analyticsDataResponse, analyticsDataAPI);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    Map<String, Object> values;

                    Map<Long, Map<String, Integer>> stamper = new HashMap<>();
                    int count = 0;
                    String val;
                    String time;
                    recordWriter.write("[");
                    for (Iterator<Record> iterator : iterators) {
                        while (iterator.hasNext()) {
                            RecordBean recordBean = Util.createRecordBean(iterator.next());
                            values = recordBean.getValues();
                            String temp[];
                            time = values.get(timestamp).toString();
                            temp = time.split(" ");
                            time = temp[0];

                            Date date = null;
                            try {
                                date = format.parse(time);
                                //System.out.println(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            long epoch = date.getTime();
                            if (values.get(col) != null) {
                                val = values.get(col).toString();

                                if (!stamper.containsKey(epoch)) {
                                    Map<String, Integer> counter = new HashMap<String, Integer>();
                                    counter.put(val, 1);
                                    stamper.put(epoch, counter);
                                } else {
                                    Map<String, Integer> counter = stamper.get(epoch);
                                    if (!counter.containsKey(val)) {
                                        counter.put(val, 1);
                                        stamper.put(epoch, counter);
                                    } else {
                                        count = counter.get(val);
                                        count++;
                                        counter.put(val, count);
                                        stamper.put(epoch, counter);
                                    }
                                }

                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                        recordBean.toString());
                            }
                        }
                    }

                    Map<Long, Map<String, Integer>> sortedMap = new TreeMap<Long, Map<String, Integer>>(stamper);
                    int i = 1;
                    int j = 1;
                    long lastDay = 0;
                    long presentDay = 0;
                    long k = 1;
                    for (Map.Entry<Long, Map<String, Integer>> entry : sortedMap.entrySet()) {

                        if (j > 1) {
                            presentDay = entry.getKey();
                            long dif = presentDay - lastDay;
                            k = dif / dayGap;

                        }
                        if (k > 1) {
                            int m = (int) k;
                            long newDate = lastDay;
                            while (k > 1) {
                                newDate = newDate + dayGap;
                                long temp = newDate / 1000L;
                                Date expiry = new Date(temp * 1000L);
                                String str_date = format.format(expiry);
                                recordWriter.write("[[\"" + str_date + "\"],[\"" + "No Entry" + "\"],[\"" + 0 + "\"]]");
                                k--;
                                if (k != 1) {
                                    recordWriter.write(",");
                                }
                            }
                            recordWriter.write(",");
                            k = 1;
                            lastDay = newDate;
                        }
                        if (k == 1) {
                            Map<String, Integer> counter = entry.getValue();
                            for (Map.Entry<String, Integer> infom : counter.entrySet()) {
                                long temp = entry.getKey() / 1000L;
                                Date expiry = new Date(temp * 1000L);
                                String str_date = format.format(expiry);
                                recordWriter.write("[[\"" + str_date + "\"],[\"" + infom.getKey() + "\"],[\"" + infom.getValue() + "\"]]");
                                if (i < counter.size()) {
                                    recordWriter.write(",");
                                    i++;
                                }
                            }
                            i = 1;
                            if (j < sortedMap.size()) {
                                recordWriter.write(",");
                                j++;
                            }
                            lastDay = entry.getKey();
                        }

                    }
                    recordWriter.write("]");
                    recordWriter.flush();
                }
            };
        } else {
            String msg = String.format("Error occurred while retrieving field data");
            log.error(msg);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    recordWriter.write("Error in reading records");
                    recordWriter.flush();
                }
            };
        }

    } */

    @POST
    @Path("/epochTimeDataFinal")
    @Produces("application/json")
    @Consumes("application/json")
    public StreamingOutput EpochtimeDataFinal(final QueryBean query) throws AnalyticsException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String username = carbonContext.getUsername();
        AnalyticsDataAPI analyticsDataAPI;
        AnalyticsDataResponse analyticsDataResponse;
        //RecordGroup recordGroup [] ;
        AnalyticsDrillDownRequest analyticsDrillDownRequest = new AnalyticsDrillDownRequest();
        String logstream = "logstream";
        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        if (query != null) {
            String q[] = query.getQuery().split(",,");
            String searchQuery = q[0];
            final String col = q[1];
            final String groupBy = q[2];
            final String timestamp = "_timestamp2";
            final long dayGap = 86400000;
            final String pattern = "yyyy-MM-dd";
            final SimpleDateFormat format = new SimpleDateFormat(pattern);
            List<String> column = new ArrayList<>();
            column.add(col);
            column.add(timestamp);

            String facetPath = query.getFacetPath();
            Map<String, List<String>> categoryPath = new HashMap<>();
            List<String> pathList = new ArrayList<>();
            if (facetPath.equals("None")) {
                categoryPath.put(logstream, pathList);
            } else {
                String pathArray[] = facetPath.split(",");
                for (String path : pathArray) {
                    pathList.add(path);
                }
                categoryPath.put(logstream, pathList);
            }
            analyticsDrillDownRequest.setTableName(query.getTableName());
            analyticsDrillDownRequest.setQuery(searchQuery);
            analyticsDrillDownRequest.setRecordCount(query.getLength());
            analyticsDrillDownRequest.setRecordStartIndex(query.getStart());
            analyticsDrillDownRequest.setCategoryPaths(categoryPath);
            List<SearchResultEntry> searchResults = analyticsDataAPI.drillDownSearch(username, analyticsDrillDownRequest);


            List<String> ids = Util.getRecordIds(searchResults);
            analyticsDataResponse = analyticsDataAPI.get(username, query.getTableName(), 1, column, ids);
            final List<Iterator<Record>> iterators = Util.getRecordIterators(analyticsDataResponse, analyticsDataAPI);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    Map<String, Object> values;

                    Map<Long, Map<String, Integer>> stamper = new HashMap<>();
                    int count = 0;
                    String val;
                    String time;
                    for (Iterator<Record> iterator : iterators) {
                        while (iterator.hasNext()) {
                            RecordBean recordBean = Util.createRecordBean(iterator.next());
                            values = recordBean.getValues();
                            String temp[];
                            time = values.get(timestamp).toString();
                            temp = time.split(" ");
                            time = temp[0];

                            Date date = null;
                            try {
                                date = format.parse(time);
                                //System.out.println(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            long epoch = date.getTime();
                            if (values.get(col) != null) {
                                val = values.get(col).toString();

                                if (!stamper.containsKey(epoch)) {
                                    Map<String, Integer> counter = new HashMap<String, Integer>();
                                    counter.put(val, 1);
                                    stamper.put(epoch, counter);
                                } else {
                                    Map<String, Integer> counter = stamper.get(epoch);
                                    if (!counter.containsKey(val)) {
                                        counter.put(val, 1);
                                        stamper.put(epoch, counter);
                                    } else {
                                        count = counter.get(val);
                                        count++;
                                        counter.put(val, count);
                                        stamper.put(epoch, counter);
                                    }
                                }

                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                        recordBean.toString());
                            }
                        }
                    }

                    Map<Long, Map<String, Integer>> sortedMap = new TreeMap<Long, Map<String, Integer>>(stamper);
                    Map<Long, Map<String, Integer>> allDayMap = new HashMap<>();
                    int j = 1;
                    long lastDay = 0;
                    long presentDay = 0;
                    long k = 1;

                    for (Map.Entry<Long, Map<String, Integer>> entry : sortedMap.entrySet()) {

                        if (j > 1) {
                            presentDay = entry.getKey();
                            long dif = presentDay - lastDay;
                            k = dif / dayGap;

                        }
                        if (k > 1) {
                            long newDate = lastDay;
                            while (k > 1) {
                                newDate = newDate + dayGap;
                                Map<String, Integer> tempMap = new HashMap<>();
                                tempMap.put("No Entry", 0);
                                allDayMap.put(newDate, tempMap);
                                k--;
                            }
                            k = 1;
                            lastDay = newDate;
                        }
                        if (k == 1) {
                            Map<String, Integer> counter = entry.getValue();
                            if (j < sortedMap.size()) {
                                j++;
                            }
                            lastDay = entry.getKey();
                            allDayMap.put(lastDay, counter);
                        }

                    }
                    allDayMap = new TreeMap<Long, Map<String, Integer>>(allDayMap);
                    Map<String, Map<String, Integer>> grouped = Util.getGrouping(allDayMap, groupBy);
                    grouped = new TreeMap<>(grouped);

                    int i = 1;
                    int l = 1;
                    recordWriter.write("[");
                    for (Map.Entry<String, Map<String, Integer>> entry : grouped.entrySet()) {
                        Map<String, Integer> counter = entry.getValue();
                        for (Map.Entry<String, Integer> infom : counter.entrySet()) {

                            recordWriter.write("[[\"" + entry.getKey() + "\"],[\"" + infom.getKey() + "\"],[\"" + infom.getValue() + "\"]]");
                            if (i < counter.size()) {
                                recordWriter.write(",");
                                i++;
                            }
                        }
                        i = 1;
                        if (l < grouped.size()) {
                            recordWriter.write(",");
                            l++;
                        }
                    }
                    recordWriter.write("]");
                    recordWriter.flush();

                }
            };
        } else {
            String msg = String.format("Error occurred while retrieving field data");
            log.error(msg);
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

    @POST
    @Path("/logStreamData")
    @Produces("application/json")
    @Consumes("application/json")
    public StreamingOutput logStreamData(final QueryBean query) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        AnalyticsDataAPI analyticsDataAPI;
        String q[] = query.getQuery().split(",,");
        String path[] = null;
        if (!q[1].equals(" ")) {
            String pathName = q[1];
            path = pathName.split(",");
        }
        String fieldName = q[0];

        CategoryDrillDownRequest categoryDrillDownRequest = new CategoryDrillDownRequest();
        categoryDrillDownRequest.setTableName(query.getTableName());
        categoryDrillDownRequest.setFieldName(fieldName);
        categoryDrillDownRequest.setPath(path);
        final List<CategorySearchResultEntry> list;
        analyticsDataAPI = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        try {
            SubCategories subCategories = analyticsDataAPI.drillDownCategories(tenantId, categoryDrillDownRequest);
            list = subCategories.getCategories();

            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    recordWriter.write("[");
                    int i = 1;
                    for (CategorySearchResultEntry lister : list) {
                        recordWriter.write("\"" + lister.getCategoryValue() + "\"");
                        if (i < list.size()) {
                            recordWriter.write(",");
                            i++;
                        }
                    }
                    recordWriter.write("]");
                    recordWriter.flush();
                }
            };

        } catch (AnalyticsException e) {
            String msg = String.format("Error occurred while retrieving field data");
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


