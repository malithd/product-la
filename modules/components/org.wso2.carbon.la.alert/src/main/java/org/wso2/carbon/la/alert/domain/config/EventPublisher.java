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

package org.wso2.carbon.la.alert.domain.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "eventPublisher",namespace="http://wso2.org/carbon/eventpublisher")
@XmlAccessorType (XmlAccessType.FIELD)
public class EventPublisher {
    @XmlAttribute
    private String name;
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    private String statistics;
    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }

    public String getStatistics() {
        return statistics;
    }

    @XmlAttribute
    private String trace;
    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String getTrace() {
        return trace;
    }


    @XmlElement
    private From from;
    public void setFrom(From from) {
        this.from = from;
    }

    public From getFrom() {
        return from;
    }

    @XmlElement
    private Mapping mapping;

    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    public Mapping getMapping() {
        return mapping;
    }

    @XmlElement
    private To to;
    public void setTo(To to) {
        this.to= to;
    }

    public To getTo() {
        return to;
    }
}
