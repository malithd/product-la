package org.wso2.carbon.la.restapi.util;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.la.commons.domain.RecordBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by vithulan on 2/3/16.
 */
public class Util {
    private static final Log log = LogFactory.getLog(Util.class);
    private static final String day = "day";
    private static final String week = "week";
    private static final String month = "month";
    private static final String year = "year";
    private static final String auto = "auto";

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

    public static Map<String, Map<String, Integer>> getGrouping(Map<Long, Map<String, Integer>> allDayMap, String method) {
        Map<String, Map<String, Integer>> grouped = new HashMap<>();
        switch (method) {
            case auto:
                grouped = groupbyAuto(allDayMap);
                break;
            case day:
                grouped = groupbyDay(allDayMap);
                break;
            case week:
                grouped = groupbyWeek(allDayMap);
                break;
            case month:
                grouped = groupbyMonth(allDayMap);
                break;
            case year:
                grouped = groupbyYear(allDayMap);
                break;

        }

        return grouped;
    }

    public static Map<String, Map<String, Integer>> groupbyAuto(Map<Long, Map<String, Integer>> allDayMap) {
        Map<String, Map<String, Integer>> grouped = new HashMap<>();
        int days = allDayMap.size();
        if (days <= 10) {
            grouped = groupbyDay(allDayMap);
        } else if (days > 10 && days <= 70) {
            grouped = groupbyWeek(allDayMap);
        } else if (days > 70 && days <= 365 * 3) {
            grouped = groupbyMonth(allDayMap);
        } else {
            grouped = groupbyYear(allDayMap);
        }
        return grouped;
    }

    public static Map<String, Map<String, Integer>> groupbyDay(Map<Long, Map<String, Integer>> allDayMap) {
        Map<String, Map<String, Integer>> grouped = new HashMap<>();
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        //long temp = epoch/1000L;

        for (Map.Entry<Long, Map<String, Integer>> entry : allDayMap.entrySet()) {
            Date expiry = new Date(entry.getKey());
            String str_week = format.format(expiry);
            grouped.put(str_week, entry.getValue());
        }

        return grouped;
    }

    public static Map<String, Map<String, Integer>> groupbyWeek(Map<Long, Map<String, Integer>> allDayMap) {

        String pattern = "Y/MM:W";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        //long temp = epoch/1000L;
        Map<String, List<Map<String, Integer>>> grouped = new HashMap<>();
        for (Map.Entry<Long, Map<String, Integer>> entry : allDayMap.entrySet()) {
            Date expiry = new Date(entry.getKey());
            String str_week = format.format(expiry);
            if (!grouped.containsKey(str_week)) {
                List<Map<String, Integer>> list = new ArrayList<>();
                list.add(entry.getValue());
                grouped.put(str_week, list);
            } else {
                List<Map<String, Integer>> list = grouped.get(str_week);
                list.add(entry.getValue());
                grouped.put(str_week, list);
            }
            //grouped.put(str_week,entry.getValue());

        }

        Map<String, Map<String, Integer>> Fgrouped = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Integer>>> entry : grouped.entrySet()) {
            List<Map<String, Integer>> list = entry.getValue();
            Map<String, Integer> data = new HashMap<>();
            //Map<String,Integer> counter = entry.getValue();

            for (Map<String, Integer> dataMap : list) {
                for (Map.Entry<String, Integer> entrr : dataMap.entrySet()) {
                    if (!data.containsKey(entrr.getKey())) {
                        data.put(entrr.getKey(), entrr.getValue());
                    } else {
                        int k = data.get(entrr.getKey());
                        data.put(entrr.getKey(), k + entrr.getValue());
                    }
                }
            }
            Fgrouped.put(entry.getKey(), data);
        }

        return Fgrouped;
    }

    public static Map<String, Map<String, Integer>> groupbyMonth(Map<Long, Map<String, Integer>> allDayMap) {

        String pattern = "Y-MM";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        //long temp = epoch/1000L;
        Map<String, List<Map<String, Integer>>> grouped = new HashMap<>();
        for (Map.Entry<Long, Map<String, Integer>> entry : allDayMap.entrySet()) {
            Date expiry = new Date(entry.getKey());
            String str_week = format.format(expiry);
            if (!grouped.containsKey(str_week)) {
                List<Map<String, Integer>> list = new ArrayList<>();
                list.add(entry.getValue());
                grouped.put(str_week, list);
            } else {
                List<Map<String, Integer>> list = grouped.get(str_week);
                list.add(entry.getValue());
                grouped.put(str_week, list);
            }
            //grouped.put(str_week,entry.getValue());

        }

        Map<String, Map<String, Integer>> Fgrouped = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Integer>>> entry : grouped.entrySet()) {
            List<Map<String, Integer>> list = entry.getValue();
            Map<String, Integer> data = new HashMap<>();
            //Map<String,Integer> counter = entry.getValue();

            for (Map<String, Integer> dataMap : list) {
                for (Map.Entry<String, Integer> entrr : dataMap.entrySet()) {
                    if (!data.containsKey(entrr.getKey())) {
                        data.put(entrr.getKey(), entrr.getValue());
                    } else {
                        int k = data.get(entrr.getKey());
                        data.put(entrr.getKey(), k + entrr.getValue());
                    }
                }
            }
            Fgrouped.put(entry.getKey(), data);
        }

        return Fgrouped;
    }

    public static Map<String, Map<String, Integer>> groupbyYear(Map<Long, Map<String, Integer>> allDayMap) {

        String pattern = "YYYY";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        //long temp = epoch/1000L;
        Map<String, List<Map<String, Integer>>> grouped = new HashMap<>();
        for (Map.Entry<Long, Map<String, Integer>> entry : allDayMap.entrySet()) {
            Date expiry = new Date(entry.getKey());
            String str_week = format.format(expiry);
            if (!grouped.containsKey(str_week)) {
                List<Map<String, Integer>> list = new ArrayList<>();
                list.add(entry.getValue());
                grouped.put(str_week, list);
            } else {
                List<Map<String, Integer>> list = grouped.get(str_week);
                list.add(entry.getValue());
                grouped.put(str_week, list);
            }
            //grouped.put(str_week,entry.getValue());

        }

        Map<String, Map<String, Integer>> Fgrouped = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Integer>>> entry : grouped.entrySet()) {
            List<Map<String, Integer>> list = entry.getValue();
            Map<String, Integer> data = new HashMap<>();
            //Map<String,Integer> counter = entry.getValue();

            for (Map<String, Integer> dataMap : list) {
                for (Map.Entry<String, Integer> entrr : dataMap.entrySet()) {
                    if (!data.containsKey(entrr.getKey())) {
                        data.put(entrr.getKey(), entrr.getValue());
                    } else {
                        int k = data.get(entrr.getKey());
                        data.put(entrr.getKey(), k + entrr.getValue());
                    }
                }
            }
            Fgrouped.put(entry.getKey(), data);
        }

        return Fgrouped;
    }

    public static String appendTimeStamp(String query, String timeFrom, String timeTo){
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date fromDate =null;
        Date toDate = null;
        try {
            fromDate = format.parse(timeFrom);
            toDate = format.parse(timeTo);
        } catch (ParseException e) {
            log.error(e);
            e.printStackTrace();
        }
        long timeFromEpoch = fromDate.getTime();
        long timeToEpoch = toDate.getTime();
        String searchQuery = "";
        if (!"".equals(query)) {
            searchQuery = query + " AND ";
        }
        return searchQuery + "_eventTimeStamp:[" + timeFromEpoch + " TO " + timeToEpoch + "]";
    }
}
