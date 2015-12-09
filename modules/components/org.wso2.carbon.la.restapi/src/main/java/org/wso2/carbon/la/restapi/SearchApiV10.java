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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.la.commons.domain.QueryBean;
import org.wso2.carbon.la.commons.domain.RecordBean;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/search")
public class SearchApiV10 {

    private static final Log log = LogFactory.getLog(SearchApiV10.class);
    /**
     * Search records.
     * @param queryBean the query bean
     * @return the response
     * @throws AnalyticsException
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/")
    public Response search(QueryBean queryBean) throws AnalyticsException {
//        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//        int tenantId = carbonContext.getTenantId();
//        String username = carbonContext.getUsername();
//
//        if (log.isDebugEnabled()) {
//            log.debug("Invoking search for tableName : " + queryBean.getTableName());
//        }
//        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
//        if (queryBean != null) {
//            List<SearchResultEntry> searchResults = analyticsDataService.search(username,
//                    queryBean.getTableName(), queryBean.getQuery(),
//                    queryBean.getStart(), queryBean.getCount());
//            List<String> ids = Utils.getRecordIds(searchResults);
//            AnalyticsDataResponse resp = analyticsDataService.get(username, queryBean.getTableName(), 1, null, ids);
//
//            List<RecordBean> recordBeans = Utils.createRecordBeans(AnalyticsDataServiceUtils.listRecords(analyticsDataService,
//                    resp));
//            if (log.isDebugEnabled()) {
//                for (RecordBean recordBean : recordBeans) {
//                    log.debug("Search Result -- Record Id: " + recordBean.getId() + " values :" +
//                            recordBean.toString());
//                }
//            }
//            return Response.ok(recordBeans).build();
//        } else {
//            throw new AnalyticsException("Search parameters not provided");
//        }
        return null;
    }
}
