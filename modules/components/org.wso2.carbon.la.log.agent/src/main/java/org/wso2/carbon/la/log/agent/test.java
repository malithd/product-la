package org.wso2.carbon.la.log.agent;

import org.wso2.carbon.la.log.agent.conf.ServerConfig;

import java.util.regex.*;

public class test {
//    public static void main(String[] args)
//    {
//        String txt="TID: [0] [BAM] [2015-10-22 14:58:20,964] INFO WARN   {org.wso2.carbon.core.internal.CarbonCoreActivator} -  Starting WSO2 Carbon... {org.wso2.carbon.core.internal.CarbonCoreActivator}";
//
//        String re1=".*?";	// Non-greedy match on filler
//        String re2="(INFO|DEBUG|ERROR|WARN|ALL|TRACE|OFF|FATAL)";
//
//        Pattern p = Pattern.compile(re1+re2,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//        Matcher m = p.matcher(txt);
//        if (m.find())
//        {
//            String var1=m.group(1);
//            System.out.print("("+var1.toString()+")"+"\n");
//        }
//    }

    /**
     * Example main. Beware: the watch listens on a whole folder, not on a single
     * file. Any update on a file within the folder will trigger a read-update.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String fn = "/home/malith/Products/WSO2/BAM/2-5-0/wso2bam-2.5.0/repository/logs/wso2carbon.log";
        ServerConfig serverConfig = new ServerConfig("ssl://10.100.0.88:7711", "tcp://10.100.0.88:7611", "admin", "admin");

        //LogReader t = new LogReader(new File(fn), true, serverConfig);

        //t.start();
    }
}
