/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.mediation.fm.impl;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;

@Singleton
public class ConfigurationBean {

    @Inject
    private Logger logger;

    /*
     * Inject the "testNumber" configuration property value. We assume that "testNumber" is a modeled configuration
     * property created via Model Service, which is available for injection.
     * 
     * According to the requirement of Model Service, there must be clear separation between service code and models
     * code. A model (cache, configuration property, channel or event) should be defined and created in it own project.
     * See Model Service SDK for more details. Here we created several modeled configuration properties in jee testsuite
     * for demo and test purpose only.
     */
    @Inject
    @Configured(propertyName = "testNumber")
    private Integer integerValue;

    void listenForChanges(@Observes @ConfigurationChangeNotification(propertyName = "testNumber") final Integer value) {
        logger.info("Received notification that value changed to {}", value);
        integerValue = value;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

}
