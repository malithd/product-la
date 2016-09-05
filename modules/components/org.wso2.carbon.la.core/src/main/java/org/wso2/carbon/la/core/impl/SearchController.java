package org.wso2.carbon.la.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.la.commons.domain.QueryBean;
import org.wso2.carbon.la.commons.domain.RecordBean;
import org.wso2.carbon.la.core.utils.LACoreServiceValueHolder;

import java.util.ArrayList;
import java.util.List;

public class SearchController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    public List<RecordBean> search(QueryBean query, String username) throws AnalyticsException {
        AnalyticsDataAPI analyticsDataService = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        if (query != null) {
            query.setQuery(appendTimeRangeToSearchQuery(query.getQuery(), query.getTimeFrom(), query.getTimeTo()));
            List<SearchResultEntry> searchResults = analyticsDataService.search(username,query.getTableName(),
                    query.getQuery(),query.getStart(), query.getLength(),null);
            List<String> ids = getRecordIds(searchResults);
            AnalyticsDataResponse resp = analyticsDataService.get(username, query.getTableName(), 1, null, ids);

            List<RecordBean> recordBeans = createRecordBeans(AnalyticsDataServiceUtils.listRecords(analyticsDataService,
                    resp));
            if (log.isDebugEnabled()) {
                for (RecordBean recordBean : recordBeans) {
                    log.debug("Search Result -- Record Id: " + recordBean.getId() + " values :" +
                            recordBean.toString());
                }
            }
            return recordBeans;
        } else {
            throw new AnalyticsException("Search parameters not provided");
        }
    }

    private String appendTimeRangeToSearchQuery(String query, long timeFrom, long timeTo) {
        String searchQuery = "";
        if (!"".equals(query)) {
            searchQuery = query + " AND ";
        }
        return searchQuery + "_timestamp:[" + timeFrom + " TO " + timeTo + "]";
    }

    /**
     * Gets the record ids from search results.
     *
     * @param searchResults the search results
     * @return the record ids from search results
     */
    public static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    /**
     * Creates the record beans from records.
     *
     * @param records the records
     * @return the list of recordBeans
     */
    public static List<RecordBean> createRecordBeans(List<Record> records) {
        List<RecordBean> recordBeans = new ArrayList<>();
        for (Record record : records) {
            RecordBean recordBean = createRecordBean(record);
            recordBeans.add(recordBean);
        }
        return recordBeans;
    }

    /**
     * Create a RecordBean object out of a Record object
     *
     * @param record the record object
     * @return RecordBean object
     */
    public static RecordBean createRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setId(record.getId());
        recordBean.setTableName(record.getTableName());
        recordBean.setTimestamp(record.getTimestamp());
        recordBean.setValues(record.getValues());
        return recordBean;
    }

    public int getRecordCount(String userName, QueryBean query) throws AnalyticsException {
        AnalyticsDataAPI analyticsDataService = LACoreServiceValueHolder.getInstance().getAnalyticsDataAPI();
        if(query!=null){
            long recordCount = analyticsDataService.getRecordCount(userName, query.getTableName(), query.getTimeFrom(), query.getTimeTo());
            return (int)recordCount;
        }else{
            throw new AnalyticsException("Search parameters not provided");
        }
    }

}
