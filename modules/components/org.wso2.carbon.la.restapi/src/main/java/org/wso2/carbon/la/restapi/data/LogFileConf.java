package org.wso2.carbon.la.restapi.data;

import org.mvel2.MacroProcessor;

import java.util.Map;

/**
 * Log file upload configuration
 */
public class LogFileConf {
    public String logGroup;
    public Map logPatterns;

    public Map getLogPatterns() {
        return logPatterns;
    }

    public void setLogPatterns(Map logPatterns) {
        this.logPatterns = logPatterns;
    }

    public String getLogGroup() {
        return logGroup;
    }

    public void setLogGroup(String logGroup) {
        this.logGroup = logGroup;
    }
}
