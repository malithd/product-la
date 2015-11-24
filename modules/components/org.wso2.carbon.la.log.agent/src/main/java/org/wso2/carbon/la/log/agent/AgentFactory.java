package org.wso2.carbon.la.log.agent;

import org.wso2.carbon.la.log.agent.conf.AgentConfig;
import org.wso2.carbon.la.log.agent.conf.LogGroup;
import org.wso2.carbon.la.log.agent.data.LogPublisher;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by malith on 11/24/15.
 */
public class AgentFactory {
    private LogPublisher logPublisher;

    private AgentConfig agentConfig;

    private void init(){
        agentConfig = new AgentConfig();
        logPublisher = new LogPublisher(agentConfig.getLogOutput().getServerConfig());
        startLogPublishers();
    }

    private void startLogPublishers(){

        for (LogGroup logGroup:agentConfig.getLogGroups()){

            try {
                LogReader logReader =  new LogReader(logPublisher, logGroup);
                logReader.start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
