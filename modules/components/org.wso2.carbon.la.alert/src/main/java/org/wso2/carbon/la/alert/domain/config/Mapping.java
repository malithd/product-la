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

@XmlRootElement(name="mapping")
@XmlAccessorType (XmlAccessType.FIELD)
public class Mapping {
    @XmlAttribute()
    private String customMapping;

    public void setCustomMapping(String customMapping) {
        this.customMapping = customMapping;
    }
    public String getCustomMapping() {
        return customMapping;
    }

    @XmlAttribute()
    private String type;
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    @XmlElement
    private String inline;

    public void setInline(String inline) {
        this.inline = inline;
    }

    public String getInline() {
        return inline;
    }
}
