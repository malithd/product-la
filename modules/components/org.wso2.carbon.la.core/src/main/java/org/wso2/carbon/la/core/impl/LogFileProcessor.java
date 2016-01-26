package org.wso2.carbon.la.core.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.la.commons.domain.config.LogFileConf;
import org.wso2.carbon.la.core.utils.LogPatternExtractor;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class LogFileProcessor {
    private static final Log log = LogFactory.getLog(LogFileProcessor.class);
    static final ArrayDeque<Map> logEvents = new ArrayDeque();
    static final String tempFolderLocation = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
            "data" + File.separator + "analyzer-logs";

    public LogFileProcessor() {
        //read info from config
        LogPublisher logPublisher = new LogPublisher("localhost", "9763", "admin", "admin");
        new Thread(logPublisher).start();
    }

    public void processLogfile(LogFileConf logFileConf) {
        LogReader logReader = new LogReader(logFileConf);
        new Thread(logReader).start();
    }

    private class LogReader implements Runnable {
        LogFileConf logFileConf;
        String logFileDir = "";

        public LogReader(LogFileConf logFileConf) {
            this.logFileConf = logFileConf;
        }

        @Override
        public void run() {
            String logFileDir = logFileConf.getLogStream();

            if (logFileDir != "") {
                logFileDir = logFileDir.replace(',', '_');
            }

            File file = new File(tempFolderLocation + File.separator + logFileDir + File.separator + logFileConf.getFileName());
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = br.readLine();
                    if (line != null) {
                        Map<String, String> logEvent = createLogEvent(line, logFileConf);
                        logEvents.push(logEvent);
                    } else {
                        break;
                    }
                }
                br.close();

                //remove the temp log file
                FileUtils.deleteDirectory(new File(tempFolderLocation + File.separator + logFileDir));//TODO : delete file and dir
            } catch (Exception ex) {
                log.error("Error reading", ex);
            }

        }
    }

    private class LogPublisher implements Runnable {
        String host;
        String port;
        String userName;
        String password;

        public LogPublisher(String host, String port, String userName, String password) {
            this.host = host;
            this.port = port;
            this.userName = userName;
            this.password = password;
        }

        @Override
        public void run() {
            while (true) {
                while (!logEvents.isEmpty()) {
                    Map<String, String> logEvent = logEvents.removeLast();
                    if (logEvent != null) {
                        String logEventStream = new Gson().toJson(logEvent);
                        try {
                            URL url = new URL("http://" + host + ":" + port + "/api/logs/publish"); //http://10.100.0.88:9763/api/logs/publish
                            String encoding = DatatypeConverter.printBase64Binary((userName + ":" + password).getBytes("UTF-8"));//parse params from a conf file
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setDoOutput(true);
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setRequestProperty("Authorization", "Basic " + encoding);
                            OutputStream os = conn.getOutputStream();
                            os.write(logEventStream.getBytes());
                            os.flush();
                            os.close();
                            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                throw new RuntimeException("Failed : HTTP error code : "
                                        + conn.getResponseCode());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //conn.disconnect();// TODO close connection properly
                }
            }
        }
    }

    private Map<String, String> createLogEvent(String logLine, LogFileConf logFileConf) {
        String streamId = "[";
        Map<String, String> logEvent = new HashMap();
        String[] logStreams = logFileConf.getLogStream().split(",");
        if (logStreams.length > 0) {
            for (String subStream : logStreams)
                streamId = streamId + "'" + subStream.trim() + "'" + ",";
        }
        streamId = streamId.substring(0, (streamId.length() - 1)) + "]";
        if(logFileConf.getDelimiter()!=null){
            logEvent = LogPatternExtractor.processDelimiter(logLine, logFileConf.getDelimiter());
        }else if(logFileConf.getRegExPatterns()!=null){
            logEvent = LogPatternExtractor.processRegEx(logLine, logFileConf.getRegExPatterns());
        }
        //set @logstream
        logEvent.put("@logstream", streamId);
        //set @filename
        logEvent.put("@filename", logFileConf.getFileName());
        //set log message
        logEvent.put("message", logLine.trim());
        return logEvent;
    }

}
