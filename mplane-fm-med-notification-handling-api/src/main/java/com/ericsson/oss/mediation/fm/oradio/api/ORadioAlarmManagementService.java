/*
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *******************************************************************************
 * -------------------------------------------------------------------------------
 */
package com.ericsson.oss.mediation.fm.oradio.api;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;

import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 * ORadioAlarmManagementService is used to generate EventNotifications and pass it to APS, it does this by
 * Checking to see if FMAlarmSupervision is active on the node, gets the ossPrefix for the node and generates the EventNotification
 * This notification will then be placed in the EventNotificationBuffer where it may be merged with another incoming notification or
 * passed to APS after a certain amount of time (defined by the TIMEOUT_VALUE in the constants)
 */
@Local
@EService
public interface ORadioAlarmManagementService {

    /**
     * The main function of this interface, will process the received notification and send the process notification to APS in the
     * form of an EventNotification
     *
     * @param netconfPayload - the raw netconf XML that has been passed from the node to ENM
     * @param networkElementName - the name of the network element that the notification came from
     * @return List - String Currently used for testing, should be removed once the orchestrator is in place - will return void
     */
    List<String> processNotification(final String netconfPayload, final String networkElementName);

    /**
     * Used for testing, specifically in the WAR module to be able to inspect the supervision cache during run time.
     *
     * This will be removed once the orchestrator is in place
     *
     * @return Map of String String, containing a copy of the current state of the supervision cache
     */
    Map<String,String> getSupervisionCache();

    /**
     * Used for testing, specifically in the WAR module to be able to inspect the ossPrefix cache during run time.
     *
     * This will be removed once the orchestrator is in place
     *
     * @return Map of String String, containing a copy of the current state of the ossPrefix cache
     */
    Map<String,String> getOssPrefixCache();

    /**
     * Used for testing, specifically in the WAR module to be able to inspect the eventNotificationBuffer cache during run time.
     *
     * This will be removed once the orchestrator is in place
     *
     * @return Map of String String, containing a copy of the current state of the eventNotificationBuffer cache
     */
    Map<String, String> getCurrentEventNotificationBuffer();

}