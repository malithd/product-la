package org.wso2.carbon.la.core.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.la.commons.domain.config.LogFileConf;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Map;

public class LogFileProcessor {
    private static final Log log = LogFactory.getLog(LogFileProcessor.class);
    ArrayDeque<String> lines = new ArrayDeque<String>();
    String tempFolderLocation = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
            "data" + File.separator + "analyzer-logs";
    String logFileDir = "";

    public void process(LogFileConf logFileConf) {
    }

    private class LogReader implements Runnable {
        LogFileConf logFileConf;

        public LogReader(LogFileConf logFileConf) {
            this.logFileConf = logFileConf;
        }

        @Override
        public void run() {
            for (String name : logFileConf.getLogStream()) {
                if (logFileDir == "") {
                    logFileDir = name;
                } else {
                    logFileDir = logFileDir + "-" + name;
                }
            }
            File file = new File(tempFolderLocation + File.separator + logFileDir + File.separator + logFileConf.getFileName());
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                int offset = 0;
                while (true) {
                    String line = br.readLine();
                    if (line != null) {
                        lines.push(line);
                    } else {
                        break;
                    }
                }
                br.close();
            } catch (Exception ex) {
                log.error("Error reading", ex);
            }

        }
    }
}
