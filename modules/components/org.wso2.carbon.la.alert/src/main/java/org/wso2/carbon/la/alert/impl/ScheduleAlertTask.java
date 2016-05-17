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

package org.wso2.carbon.la.alert.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.la.alert.domain.LAAlertConstant;
import org.wso2.carbon.la.alert.util.LAAlertServiceValueHolder;
import org.wso2.carbon.la.commons.domain.QueryBean;
import org.wso2.carbon.la.commons.domain.RecordBean;
import org.wso2.carbon.la.core.impl.SearchController;
import org.wso2.carbon.ntask.core.AbstractTask;

import javax.xml.bind.DatatypeConverter;
import java.util.List;
import java.util.Map;


public class ScheduleAlertTask extends AbstractTask {
    private static final Log log = LogFactory.getLog(ScheduleAlertTask.class);

    @Override
    public void execute() {
        SearchController searchController = new SearchController();
        Map<String, String> taskProperties = this.getProperties();
        String username = taskProperties.get(LAAlertConstant.USER_NAME);
        String alertName = taskProperties.get(LAAlertConstant.ALERT_NAME);
        String version = "1.0.0";
        String condition = taskProperties.get(LAAlertConstant.CONDITION);
        int conditionValue = Integer.valueOf(taskProperties.get(LAAlertConstant.CONDITION_VALUE));
        long timeStamp = System.currentTimeMillis();
        long timeFrom = Long.valueOf(taskProperties.get(LAAlertConstant.TIME_FROM));
        long timeTo = Long.valueOf(taskProperties.get(LAAlertConstant.TIME_TO));
        if (Long.valueOf(taskProperties.get(LAAlertConstant.TIME_FROM)) != 0) {
            long timeDifference = Long.valueOf(taskProperties.get(LAAlertConstant.TIME_DIFF));
            timeFrom = timeStamp - timeDifference;
            timeTo = timeStamp;
        }
        QueryBean queryBean = createQueryBean(taskProperties,timeStamp, timeFrom,timeTo);
        String[] fields = null;
        if (taskProperties.containsKey(LAAlertConstant.FIELDS)) {
            fields = taskProperties.get(LAAlertConstant.FIELDS).split(",");
        }
        Object[] payload;
        try {
            List<RecordBean> recordBeans = searchController.search(queryBean, username);
            StringBuilder resultBuilder = new StringBuilder();
            if (taskProperties.containsKey(LAAlertConstant.FIELDS)) {
                for (RecordBean record : recordBeans) {
                    Map<String, Object> recordValues = record.getValues();
                    String dataOb = "<div>";
                    if (!fields.equals(null)){
                        for (String field : fields) {
                            dataOb += field + ":" + recordValues.get(field) + ",";
                        }
                        dataOb += "</div>";
                        resultBuilder.append(dataOb);
                    }
                }
            }

            String base64EncodeQuery = DatatypeConverter.printBase64Binary(taskProperties.get(LAAlertConstant.QUERY).getBytes());
            String stringTimeFrom = String.valueOf(timeFrom);
            String stringTimeTo = String.valueOf(timeTo);
            String base64EncodeTimeFrom = DatatypeConverter.printBase64Binary(stringTimeFrom.getBytes());
            String base64EncodeTimeTo= DatatypeConverter.printBase64Binary(stringTimeTo.getBytes());
//            URIBuilder uriBuilder = new URIBuilder();
//            uriBuilder.setPath("https://10.100.4.179:9443/loganalyzer/site/search/search.jag?");
//            uriBuilder.addParameter("query", base64EncodeQuery);
//            uriBuilder.addParameter("timeFrom", String.valueOf(timeFrom));
//            uriBuilder.addParameter("timeTo",String.valueOf(timeTo));
//            String url=uriBuilder.toString();

            String url="https://10.100.4.179:9443/loganalyzer/site/search/search.jag?" + "query=" + base64EncodeQuery+
                    "&"+"timeFrom="+base64EncodeTimeFrom+"&"+"timeTo="+base64EncodeTimeTo;

            int recodeListSize = recordBeans.size();
            payload = new Object[]{url, new Long(recodeListSize)};
            switch (condition) {
                case LAAlertConstant.CONDITION_GREATER_THAN:
                    if (recodeListSize > conditionValue) {
                       createEvent(alertName,version,timeStamp,payload);
                    }
                    break;
                case LAAlertConstant.CONDITION_LESS_THAN:
                    if (recodeListSize < conditionValue) {
                        createEvent(alertName, version, timeStamp, payload);
                    }
                    break;
                case LAAlertConstant.CONDITION_EQUALS:
                    if (recodeListSize == conditionValue) {
                        createEvent(alertName, version, timeStamp, payload);
                    }
                    break;
                case LAAlertConstant.CONDITION_GREATER_THAN_OR_EQUAL:
                    if (recodeListSize >= conditionValue) {
                        createEvent(alertName, version, timeStamp, payload);
                    }
                    break;
                case LAAlertConstant.CONDITION_LESS_THAN_OR_EQUAL:
                    if (recodeListSize <= conditionValue) {
                        createEvent(alertName, version, timeStamp, payload);
                    }
                    break;
                case LAAlertConstant.CONDITION_NOT_EQUALS:
                    if (recodeListSize != conditionValue) {
                        createEvent(alertName,version,timeStamp,payload);
                    }
                    break;
            }
        } catch (AnalyticsException e) {
            log.error("Unable to perform schedule alert task due to " + e.getMessage(), e);
        }
    }

    private QueryBean createQueryBean (Map<String,String> taskProperties, long timeStamp, long timeFrom, long timeTo) {
        QueryBean queryBean = new QueryBean();

        queryBean.setTableName(taskProperties.get(LAAlertConstant.TABLE_NAME));
        queryBean.setQuery(taskProperties.get(LAAlertConstant.QUERY));
        queryBean.setTimeFrom(timeFrom);
        queryBean.setTimeTo(timeTo);
        queryBean.setStart(Integer.valueOf(taskProperties.get(LAAlertConstant.START)));
        queryBean.setLength(Integer.valueOf(taskProperties.get(LAAlertConstant.LENGTH)));
        return queryBean;
    }

    private void createEvent(String alertName,String version, long timeStamp,Object[] payload){
        Event event = new Event(DataBridgeCommonsUtils.generateStreamId(alertName, version), timeStamp, null, null, payload);
        LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
    }
}


