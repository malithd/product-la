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

package org.wso2.carbon.la.alert.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.la.alert.domain.config.*;
import org.wso2.carbon.la.alert.exception.AlertPublisherException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;

public class AlertPublisherUtils {

    private static final Log log = LogFactory.getLog(AlertPublisherUtils.class);

    /**
     * This method is to create and deploy output publisher per alert
     *
     * @param alertName             -name of the alert
     * @param alertActionType       -type of the alert action/ Publisher type
     * @param alertActionProperties -properties of the alert
     * @throws AlertPublisherException
     */
    public static void createAlertPublisher(String alertName, String alertActionType, Map<String, String> alertActionProperties) throws AlertPublisherException {
        try {
            createOutputStream(alertName);
            EventPublisherService eventPublisherService = LAAlertServiceValueHolder.getInstance().getEventPublisherService();
            StringWriter stringWriter = createPublisherXML(alertName, alertActionType, alertActionProperties);
            eventPublisherService.deployEventPublisherConfiguration(stringWriter.toString());
        } catch (EventPublisherConfigurationException | JAXBException | EventStreamConfigurationException | MalformedStreamDefinitionException e) {
            log.error("Unable to deploy Event Publish Configuration " + e.getMessage(), e);
            throw new AlertPublisherException("Unable to deploy Event Publisher " + e.getMessage(), e);
        }
    }

    /**
     * This method is to update alert output publisher
     *
     * @param alertName             -name of the alert
     * @param alertActionType       -type of the alert action/ Publisher type
     * @param alertActionProperties -properties of the alert
     * @throws AlertPublisherException
     */
    public static void updateAlertPublisher(String alertName, String alertActionType, Map<String, String> alertActionProperties) throws AlertPublisherException {
        try {
            StringWriter publisherXml = createPublisherXML(alertName, alertActionType, alertActionProperties);
            EventPublisherService eventPublisherService = LAAlertServiceValueHolder.getInstance().getEventPublisherService();
            eventPublisherService.editActiveEventPublisherConfiguration(publisherXml.toString(), alertName);
        } catch (JAXBException | EventPublisherConfigurationException e) {
            log.error("Unable to update Event Publish Configuration " + e.getMessage(), e);
            throw new AlertPublisherException("Unable to update Event Publisher " + e.getMessage(), e);
        }
    }

    /**
     * This method is to delete alert output publisher
     *
     * @param alertName -name of the alert
     * @throws AlertPublisherException
     */
    public static void deleteAlertPublisher(String alertName) throws AlertPublisherException {
        try {
            EventPublisherService eventPublisherService = LAAlertServiceValueHolder.getInstance().getEventPublisherService();
            eventPublisherService.undeployActiveEventPublisherConfiguration(alertName);
            deleteOutputStream(alertName);
        } catch (EventPublisherConfigurationException | EventStreamConfigurationException e) {
            log.error("Unable to delete Event Publish Configuration " + e.getMessage(), e);
            throw new AlertPublisherException("Unable to delete Event Publisher " + e.getMessage(), e);
        }
    }

    /**
     * This method is creating Publisher XML according the given properties
     *
     * @param alertName             -name of the alert
     * @param alertActionType       -type of the alert action/ Publisher type
     * @param alertActionProperties -properties of the alert
     * @return generated Publisher XML
     * @throws JAXBException
     */
    private static StringWriter createPublisherXML(String alertName, String alertActionType, Map<String, String> alertActionProperties) throws JAXBException {
        EventPublisher eventPublisherObject = getEventPublisher(alertName, alertActionType, alertActionProperties);
        StringWriter eventPublisherXML = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EventPublisher.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.marshal(eventPublisherObject, eventPublisherXML);
        } catch (JAXBException e) {
            log.error("Error while creating Publisher XML" + e.getMessage(), e);
            throw new JAXBException("Error while crating Publisher XML" + e.getMessage(), e);
        }
        return eventPublisherXML;
    }

    /**
     * This is initializing alert Action properties to Event Publisher Object
     *
     * @param alertName             -name of the alert
     * @param alertActionType       -type of the alert action/ Publisher type
     * @param alertActionProperties -properties of the alert
     * @return initialized EventPublisher Object
     */
    private static EventPublisher getEventPublisher(String alertName, String alertActionType, Map<String, String> alertActionProperties) {
        EventPublisher eventPublisher = new EventPublisher();
        From from = new From();
        Mapping mapping = new Mapping();
        To to = new To();
        ArrayList<Property> properties = new ArrayList<>();
        String actionPropertyKey;
        eventPublisher.setName(alertName);
        eventPublisher.setStatistics("disable");
        eventPublisher.setTrace("disable");
        from.setStreamName(alertName);
        from.setVersion("1.0.0");
        mapping.setCustomMapping("enable");
        mapping.setType("text");
        mapping.setInline(alertActionProperties.get("message"));
        to.setEventAdapterType(alertActionType);
        for (Map.Entry<String, String> actionProperty : alertActionProperties.entrySet()) {
            if (!actionProperty.getKey().equals("message")) {
                Property property = new Property();
                actionPropertyKey = actionProperty.getKey();
                actionPropertyKey = actionPropertyKey.replace("_", ".");
                property.setName(actionPropertyKey);
                property.setValue(actionProperty.getValue());
                properties.add(property);
            }
        }
        to.setProperty(properties);
        eventPublisher.setFrom(from);
        eventPublisher.setMapping(mapping);
        eventPublisher.setTo(to);
        return eventPublisher;
    }

    /**
     * Create Out_put Stream per alert
     *
     * @param alertName -alert name for stream name
     */
    private static void createOutputStream(String alertName) throws MalformedStreamDefinitionException, EventStreamConfigurationException {
        try {
            EventStreamService eventStreamService = LAAlertServiceValueHolder.getInstance().getEventStreamService();
            StreamDefinition streamDefinition = new StreamDefinition(alertName, "1.0.0");
            streamDefinition.addPayloadData("values", AttributeType.STRING);
            streamDefinition.addPayloadData("count", AttributeType.LONG);
            eventStreamService.addEventStreamDefinition(streamDefinition);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream definition " + e.getMessage(), e);
            throw new MalformedStreamDefinitionException("Malformed Stream definition " + e.getMessage(), e);
        } catch (EventStreamConfigurationException e) {
            log.error("Unable to add event stream definition " + e.getMessage(), e);
            throw new EventStreamConfigurationException("Unable to create event stream definition " + e.getMessage(), e);
        }
    }

    /**
     * Delete output stream of the Alert
     *
     * @param alertName -alert name for stream name
     * @throws EventStreamConfigurationException
     */
    private static void deleteOutputStream(String alertName) throws EventStreamConfigurationException {
        try {
            EventStreamService eventStreamService = LAAlertServiceValueHolder.getInstance().getEventStreamService();
            eventStreamService.removeEventStreamDefinition(alertName, "1.0.0");
        } catch (EventStreamConfigurationException e) {
            log.error("Unable to delete event stream definition " + e.getMessage(), e);
            throw new EventStreamConfigurationException("Unable to delete event stream definition " + e.getMessage(), e);
        }
    }
}
