package org.wso2.carbon.la.alert.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.la.alert.domain.LAAlertConstant;
import org.wso2.carbon.la.alert.util.LAAlertServiceValueHolder;
import org.wso2.carbon.la.commons.domain.QueryBean;
import org.wso2.carbon.la.commons.domain.RecordBean;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.la.core.impl.SearchController;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class ScheduleAlertTask extends AbstractTask {
    private SearchController searchController;
    private QueryBean queryBean;
    private static final Log log= LogFactory.getLog(ScheduleAlertTask.class);

    public ScheduleAlertTask(){
        searchController=new SearchController();
        queryBean=new QueryBean();
    }

    @Override
    public void execute() {
        Map <String,String> taskProperties=this.getProperties();
        String name=taskProperties.get(LAAlertConstant.ALERT_NAME);
        String version="1.0.0";
        long timeStamp=System.currentTimeMillis();
        String username=taskProperties.get(LAAlertConstant.USER_NAME);
        queryBean.setTableName(taskProperties.get(LAAlertConstant.TABLE_NAME));
        queryBean.setQuery(taskProperties.get(LAAlertConstant.QUERY));
        queryBean.setTimeFrom(Long.valueOf(taskProperties.get(LAAlertConstant.TIME_FROM)));
        queryBean.setTimeTo(Long.valueOf(taskProperties.get(LAAlertConstant.TIME_TO)));
        queryBean.setStart(Integer.valueOf(taskProperties.get(LAAlertConstant.START)));
        queryBean.setLength(Integer.valueOf(taskProperties.get(LAAlertConstant.LENGTH)));
        Object[] payload;

        try {
            List<RecordBean> recordBeans = searchController.search(queryBean, username);
            int i=recordBeans.size();
            payload=new Object[]{new Long(i)};
            if (i>3) {
                Event event=new Event(DataBridgeCommonsUtils.generateStreamId(name,version),timeStamp,null,null,payload);
                LAAlertServiceValueHolder.getInstance().getEventStreamService().publish(event);
            }

        }catch (AnalyticsException e){
            log.error("Unable to perform schedule alert task due to " + e.getMessage(), e);
        }
    }
}


