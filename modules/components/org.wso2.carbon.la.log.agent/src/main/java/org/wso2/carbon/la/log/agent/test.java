package org.wso2.carbon.la.log.agent;

import java.util.regex.*;

public class test
{
    public static void main(String[] args)
    {
        String txt="TID: [0] [BAM] [2015-10-22 14:58:20,964] INFO WARN   {org.wso2.carbon.core.internal.CarbonCoreActivator} -  Starting WSO2 Carbon... {org.wso2.carbon.core.internal.CarbonCoreActivator}";

        String re1=".*?";	// Non-greedy match on filler
        String re2="(INFO|DEBUG|ERROR|WARN|ALL|TRACE|OFF|FATAL)";

        Pattern p = Pattern.compile(re1+re2,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        if (m.find())
        {
            String var1=m.group(1);
            System.out.print("("+var1.toString()+")"+"\n");
        }
    }
}
