package org.wso2.carbon.la.log.agent.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by malith on 11/23/15.
 */
public class AgentConfig {
    public static Map<String, LogGroup> getLogGroups() {
        return logGroups;
    }

    public void setLogGroups(Map<String, LogGroup> logGroups) {
        this.logGroups = logGroups;
    }

    static Map<String,LogGroup> logGroups = new HashMap<String, LogGroup>();
}

