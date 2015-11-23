package org.wso2.carbon.la.log.agent.conf;

import org.wso2.carbon.la.log.agent.filters.AbstractFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by malith on 11/23/15.
 */
public class LogGroup {
    public List<AbstractFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<AbstractFilter> filters) {
        this.filters = filters;
    }

    List<AbstractFilter> filters = new ArrayList<AbstractFilter>();

}
