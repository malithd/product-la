package org.wso2.carbon.la.commons.domain.config;

import java.util.Map;

/**
 * Log file upload configuration
 */
public class LogFileConf {
    public String logStream;
    public String fileName;
    public Map logPatterns;

    public Map getLogPatterns() {
        return logPatterns;
    }

    public void setLogPatterns(Map logPatterns) {
        this.logPatterns = logPatterns;
    }

    public String getLogStream() {
        return logStream;
    }

    public void setLogStream(String logStream) {
        this.logStream = logStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
