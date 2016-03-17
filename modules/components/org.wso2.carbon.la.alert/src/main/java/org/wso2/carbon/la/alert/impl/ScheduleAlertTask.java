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

import java.util.List;
import java.util.Map;


public class ScheduleAlertTask extends AbstractTask {
    private static final Log log = LogFactory.getLog(ScheduleAlertTask.class);
    private SearchController searchController;
    private QueryBean queryBean;

    public ScheduleAlertTask() {
        searchController = new SearchController();
        queryBean = new QueryBean();
    }

    @Override
    public void execute() {
        Map<String, String> taskProperties = this.getProperties();
        String name = taskProperties.get(LAAlertConstant.ALERT_NAME);
        String version = "1.0.0";
        long timeStamp = System.currentTimeMillis();
        String username = taskProperties.get(LAAlertConstant.USER_NAME);String condition=taskProperties.get(LAAlertConstant.CONDITION);
        int conditionValue=Integer.valueOf(taskProperties.get(LAAlertConstant.CONDITION_VALUE));
        queryBean.setTableName(taskProperties.get(LAAlertConstant.TABLE_NAME));
        queryBean.setQuery(taskProperties.get(LAAlertConstant.QUERY));
        queryBean.setTimeFrom(Long.valueOf(taskProperties.get(LAAlertConstant.TIME_FROM)));
        queryBean.setTimeTo(Long.valueOf(taskProperties.get(LAAlertConstant.TIME_TO)));
        queryBean.setStart(Integer.valueOf(taskProperties.get(LAAlertConstant.START)));
        queryBean.setLength(Integer.valueOf(taskProperties.get(LAAlertConstant.LENGTH)));
        Object[] payload;

        try {
            List<RecordBean> recordBeans = searchController.search(queryBean, username);
            int recodeListSize = recordBeans.size();
            payload = new Object[]{new Long(recodeListSize)};
            switch (condition) {
                case "gt" :
                    if (recodeListSize > conditionValue) {
                        Event event = new Event(DataBridgeCommonsUtils.generateStreamId(name, version), timeStamp, null, null, payload);
                        LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
                    }
                    break;
                case "lt" :
                    if (recodeListSize < conditionValue) {
                        Event event = new Event(DataBridgeCommonsUtils.generateStreamId(name, version), timeStamp, null, null, payload);
                        LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
                    }
                    break;
                case "eq" :
                    if (recodeListSize == conditionValue) {
                        Event event = new Event(DataBridgeCommonsUtils.generateStreamId(name, version), timeStamp, null, null, payload);
                        LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
                    }
                    break;
                case "gteq" :
                    if (recodeListSize >= conditionValue) {
                        Event event = new Event(DataBridgeCommonsUtils.generateStreamId(name, version), timeStamp, null, null, payload);
                        LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
                    }
                    break;
                case "lteq" :
                    if (recodeListSize <= conditionValue) {
                        Event event = new Event(DataBridgeCommonsUtils.generateStreamId(name, version), timeStamp, null, null, payload);
                        LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
                    }
                    break;
                case "nteq" :
                    if (recodeListSize != conditionValue) {
                        Event event = new Event(DataBridgeCommonsUtils.generateStreamId(name, version), timeStamp, null, null, payload);
                        LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
                    }
                    break;
            }


//            if (recodeListSize > 3) {
//                Event event = new Event(DataBridgeCommonsUtils.generateStreamId(name, version), timeStamp, null, null, payload);
//                LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
//            }

        } catch (AnalyticsException e) {
            log.error("Unable to perform schedule alert task due to " + e.getMessage(), e);
        }
    }
}


