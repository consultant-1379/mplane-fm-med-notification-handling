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
package com.ericsson.oss.mediation.fm.oradio.ejb;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.EventNotificationBuffer;
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.OssPrefixCache;
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.SupervisionStateCache;
import com.ericsson.oss.mediation.fm.oradio.api.MPlaneAlarmService;
import com.ericsson.oss.mediation.translator.model.EventNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;


import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.*;
import static com.ericsson.oss.mediation.fm.util.EventNotificationUtil.setEventTimeAndTimeZone;

/**
 * ORadioAlarmManagementServiceImpl is the implementation class of @class ORadioAlarmManagementService entry point is processNotification
 * It will check the SupervisionStateCache to see if the notification should be dropped, gets the ossPrefix from the ossPrefixCache and then
 * add the EventNotification to the EventNotificationBuffer. If there is a EventNotification already in the EventNotificationBuffer that matches
 * the networkElementName and alarmId then it will be merged with the new Notification and sent through to APS directly.
 */
@Stateless
@Slf4j
public class ORadioAlarmManagementServiceImpl implements ORadioAlarmManagementServiceLocal, ORadioAlarmManagementServiceRemote {

    @EServiceRef
    private MPlaneAlarmService mplaneAlarmService;

    @Inject
    private SupervisionStateCache supervisionStateCache;

    @Inject
    private OssPrefixCache ossPrefixCache;

    @Inject
    private EventNotificationBuffer eventNotificationBuffer;


    @Override
    public List<String> processNotification(final String netconfPayload, final String networkElementName) {
        final String networkElementFdn = NETWORK_ELEMENT_FDN + networkElementName;

        final List<String> response = new ArrayList<>();
        response.add("Something has been received");
        log.debug("Received Notification for: {}, Netconf payload: {}", networkElementName, netconfPayload);
        //Check to see if FMAlarmSupervision is Enabled for the node
        if (!supervisionStateCache.isSupervisionActive(networkElementFdn)) {
            response.add("Dropping Alarm from " + networkElementName + " as FMAlarmSupervision is not enabled");
            log.info("Dropping Alarm from {}, as FMAlarmSupervision is not enabled", networkElementName);
            return response;
        }
        //Get FDN from the subnetwork cache
        final String ossPrefix = ossPrefixCache.getOssPrefix(networkElementFdn);

        response.add("Found ossPrefix: " + ossPrefix + " for node " + networkElementName);
        log.debug("Found OssPrefix: {} for node: {}", ossPrefix, networkElementName);

        /* TODO: Reinstate these two lines:
         * ORadioAlarmNotification parsedNotification = mplaneAlarmService.parseAlarm(netconfPayload, networkElementName);
         * EventNotification eventNotif = mplaneAlarmService.transformAlarm(parsedNotification, networkElementName, ossPrefix);
         */

        // TODO: Remove the below line once Transformation is in place, is for testing only
        response.add("Creating Dummy eventNotification for Testing ");
        EventNotification eventNotif = createTestEventNotification(networkElementFdn, ossPrefix);

        eventNotif.setExternalEventId(netconfPayload);
        response.add("Adding EventNotification to Eventbuffer: " + ossPrefix + " for node " + networkElementName);

        if(eventNotificationBuffer.eventNotificationExists(eventNotif.getExternalEventId(), networkElementName)) {
            final EventNotification bufferedEventNotification = eventNotificationBuffer
                    .getEventNotification(eventNotif.getExternalEventId(), networkElementName);
            //remove the notification from the cache as we are processing it
            eventNotificationBuffer.removeEventNotification(eventNotif.getExternalEventId(), networkElementName);
            //merge the notifications
            final EventNotification eventToSend = mergeEventNotification(eventNotif,bufferedEventNotification, netconfPayload);
            log.info("EVENT-TO-SEND:{}", eventToSend);
            mplaneAlarmService.sendAlarm(eventToSend);
        } else {
            log.info("EVENT-TO-BUFFER:{}", eventNotif);
            // There is no EventNotification In the buffer so store.
            eventNotificationBuffer.addEventNotification(eventNotif, networkElementName);
        }
        log.info("Finished ProcessNotification: response to UI: {}", response);
        return response;
    }

    @Override
    public Map<String, String> getSupervisionCache() {
        return supervisionStateCache.getSupervisionStateCache();
    }

    @Override
    public Map<String, String> getOssPrefixCache() {
        return ossPrefixCache.getCache();
    }

    @Override
    public Map<String,String> getCurrentEventNotificationBuffer(){
        return eventNotificationBuffer.getEventNotificationBuffer();
    }

    private EventNotification mergeEventNotification(final EventNotification newEvent, final EventNotification cachedEvent, final String netconfPayload){
        if(cachedEvent.getPerceivedSeverity().equals("ALARM")) {
            final Map<String,String> additionalAttr = cachedEvent.getAdditionalAttributes();
            final String targetAdditionalInfo = additionalAttr.get(TARGET_ATTRIBUTE_INFO);
            additionalAttr.replace(TARGET_ATTRIBUTE_INFO, targetAdditionalInfo + netconfPayload);
            cachedEvent.setAdditionalAttributes(additionalAttr);
            log.debug("merged received Alarm with Cleared alarm from the cache");
            return cachedEvent;

        } else {
            // Must be alarm then
            final Map<String,String> additionalAttr = cachedEvent.getAdditionalAttributes();
            final String targetAdditionalInfo = additionalAttr.get(TARGET_ATTRIBUTE_INFO);
            additionalAttr.replace(TARGET_ATTRIBUTE_INFO, targetAdditionalInfo + netconfPayload);
            newEvent.setAdditionalAttributes(additionalAttr);
            log.debug("merged received Cleared Alarm with Alarm from the cache");
            return newEvent;

        }
    }
    private EventNotification createTestEventNotification(final String fdn, final String ossPrefix){
        final EventNotification eventNotification = new EventNotification();
        if (ossPrefix != null && !ossPrefix.isEmpty()) {
            eventNotification.setManagedObjectInstance(ossPrefix);
        } else {
            eventNotification.setManagedObjectInstance(fdn);
        }

        eventNotification.setSourceType(ORADIO_NE_TYPE);
        eventNotification.addAdditionalAttribute("fdn", fdn);
        eventNotification.addAdditionalAttribute("behalf", "ManagementSystem=ENM");
        eventNotification.setPerceivedSeverity("INDETERMINATE");
        eventNotification.setRecordType("ALARM");
        setEventTimeAndTimeZone(eventNotification);
        eventNotification.setPerceivedSeverity("MAJOR");
        eventNotification.setSpecificProblem("Testing Failure");
        eventNotification.setEventType("Communications alarm");
        eventNotification.setProbableCause("Testing in LAN Error/Communication Error");
        eventNotification.addAdditionalAttribute("additionalText", "someAdditionalTextHere");

        return eventNotification;
    }
}
