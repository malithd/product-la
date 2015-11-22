/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.la.database.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.la.commons.constants.LAConstants;
import org.wso2.carbon.la.commons.domain.config.LAConfiguration;
import org.wso2.carbon.la.database.DatabaseService;
import org.wso2.carbon.la.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.la.database.exceptions.LAConfigurationParserException;
import org.wso2.carbon.la.database.internal.ds.LocalDatabaseCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LADatabaseService implements DatabaseService {

    private static final Log logger = LogFactory.getLog(LADatabaseService.class);
    private LADataSource dbh;
    private LAConfiguration laConfig;
    private static final String DB_CHECK_SQL = "SELECT * FROM ML_PROJECT";

    public LADatabaseService() {
        
        LAConfigurationParser mlConfigParser = new LAConfigurationParser();
        try {
            laConfig = mlConfigParser.getLAConfiguration(LAConstants.LOG_ANALYZER_XML);
        } catch (LAConfigurationParserException e) {
            String msg = "Failed to parse machine-learner.xml file.";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        
        try {
            dbh = new LADataSource(laConfig.getDatasourceName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        String value = System.getProperty("setup");
        if (value != null) {
            LocalDatabaseCreator databaseCreator = new LocalDatabaseCreator(dbh.getDataSource());
            try {
                if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
                    databaseCreator.createRegistryDatabase();
                } else {
                    logger.info("Machine Learner database already exists. Not creating a new database.");
                }
            } catch (Exception e) {
                String msg = "Error in creating the Machine Learner database";
                throw new RuntimeException(msg, e);
            }
        }
        

    }
    
    public LAConfiguration getLaConfiguration() {
        return laConfig != null ? laConfig : new LAConfiguration();
    }
    

    public void shutdown() throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement("SHUTDOWN");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error has occurred while shutting down the database: "
                    + e.getMessage(), e);
        } finally {
            // Close the database resources.
            LADatabaseUtils.closeDatabaseResources(connection, statement);
        }
    }
}
