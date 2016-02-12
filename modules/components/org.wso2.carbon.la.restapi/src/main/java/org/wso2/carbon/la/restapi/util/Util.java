package org.wso2.carbon.la.restapi.util;

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.la.commons.domain.RecordBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vithulan on 2/3/16.
 */
public class Util {
    public static List<Iterator<Record>> getRecordIterators(AnalyticsDataResponse resp,
                                                            SecureAnalyticsDataService analyticsDataService)
            throws AnalyticsException {
        List<Iterator<Record>> iterators = new ArrayList<>();
        for (RecordGroup recordGroup : resp.getRecordGroups()) {
            iterators.add(analyticsDataService.readRecords(resp.getRecordStoreName(), recordGroup));
        }
       // IteratorUtils iteratorUtils = new IteratorUtils();
        //Iterator iterator = iteratorUtils.chainedIterator(iterators);
        return iterators;
    }

    public static RecordBean createRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setId(record.getId());
        recordBean.setTableName(record.getTableName());
        recordBean.setTimestamp(record.getTimestamp());
        recordBean.setValues(record.getValues());
        return recordBean;
    }

    public static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }
}
