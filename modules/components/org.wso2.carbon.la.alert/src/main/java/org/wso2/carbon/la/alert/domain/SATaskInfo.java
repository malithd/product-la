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

package org.wso2.carbon.la.alert.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public class SATaskInfo {
    @XmlElement(name= "alertName")
    private String alertName;

    @XmlElement(name= "description", required = false)
    private String description;

    @XmlElement(name= "tableName", required = false)
    private String tableName;

    @XmlElement(name= "query")
    private String query;

    @XmlElement(name= "start", required = false)
    private int start;

    @XmlElement(name= "timeFrom")
    private long timeFrom;

    @XmlElement(name= "timeTo")
    private long timeTo;

    @XmlElement(name= "length", required = false)
    private int length;

    @XmlElement(name= "cronExpression")
    private String cronExpression;

    @XmlElement(name= "condition")
    private String condition;

    @XmlElement(name= "conditionValue")
    private String conditionValue;

    @XmlElement(name="alertActionType")
    private String alertActionType;

    @XmlElement(name="alertActionProperties")
    private Map<String,String> alertActionProperties;

    public SATaskInfo() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {

        this.tableName = tableName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setTimeTo(long timeTo) {
        this.timeTo = timeTo;
    }

    public void setTimeFrom(long timeFrom) {
        this.timeFrom = timeFrom;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void setAlertActionProperties(Map<String, String> alertActionProperties) {
        this.alertActionProperties = alertActionProperties;
    }

    public void setAlertActionType(String alertActionType) {
        this.alertActionType = alertActionType;
    }

    public String getAlertName() {

        return alertName;
    }

    public String getDescription() {
        return description;
    }

    public String getQuery() {
        return query;
    }

    public int getStart() {
        return start;
    }

    public long getTimeFrom() {
        return timeFrom;
    }

    public long getTimeTo() {
        return timeTo;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public int getLength() {
        return length;
    }

    public String getCondition() {
        return condition;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public String getAlertActionType() {

        return alertActionType;
    }

    public Map<String, String> getAlertActionProperties() {

        return alertActionProperties;
    }
}
