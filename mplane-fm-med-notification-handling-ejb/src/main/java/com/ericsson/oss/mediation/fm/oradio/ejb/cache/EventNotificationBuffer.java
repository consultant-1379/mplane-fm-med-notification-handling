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

package com.ericsson.oss.mediation.fm.oradio.ejb.cache;

import com.ericsson.oss.mediation.fm.oradio.ejb.models.EventNotificationMarker;
import com.ericsson.oss.mediation.translator.model.EventNotification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.Singleton;

import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.TIMEOUT_VALUE;

/**
 * Used to store EventNotifications after transformation received from the node. The EventNotificationBufferTimer class reads this and clears the cache if the
 * EventNotifcation has been in the cache for over a certain amount of time, defined by the TIMEOUT_VALUE within the constants class.
 *
 * Specifically the O-RU node as defined by the ORAN specifications says that updates to alarms are a 2 notification process:
 * 1 - A notification to clear the current alarm
 * 2 - A notification with an Alarm with the updated values.
 *
 * This class and ORadioAlarmManagementService are used to merge these two notifications in the case of an update.
 * The EventNotificationBuffertimer is used to ensure that new Alarms or Clears are still sent to APS in a reasonable amount of time.
 */
@Singleton
public class EventNotificationBuffer {

    private final Map<EventNotificationMarker, EventNotification> eventNotificationCache = new ConcurrentHashMap<>();

    /**
     * Adds an EventNotification to the eventNotificationCache object.
     * @param eventNotif - an instance of @class EventNotification to store to the cache
     * @param networkElementName - name of the node which has sent the EventNotification
     */
    public void addEventNotification(final EventNotification eventNotif, final String networkElementName) {
        final EventNotificationMarker marker = generateMarker(networkElementName,eventNotif.getExternalEventId());
        marker.setEntryTime(System.currentTimeMillis());
        eventNotificationCache.put(marker, eventNotif);
    }

    /**
     * Removes an EventNotification from the cache
     * @param alarmId - @class String, the AlarmId which has parsed from the notification
     * @param networkElementName - name of the node.
     */
    public void removeEventNotification(final String alarmId, final String networkElementName) {
        eventNotificationCache.remove(generateMarker(networkElementName,alarmId));
    }

    /**
     * Removes a list of EventNotifications from the cache.
     * @param eventNotifList - @class EventNotificationMarker, List of Markers which is used as keys to remove from the cache.
     */
    public void removeEventNotificationList(final Set<EventNotificationMarker> eventNotifList) {
        eventNotifList.forEach(event -> removeEventNotification(event.getEventNotificationId(), event.getNetworkElementName()));
    }

    /**
     * Gets an EventNotification from the cache
     * @param alarmId - Alarm id of the notification to get
     * @param networkElementName node name of the notification to get.
     * @return @class EventNotification, the EventNotification which matches the alarmId and networkElementName.
     */
    public EventNotification getEventNotification(final String alarmId, final String networkElementName){
        return eventNotificationCache.get(generateMarker(networkElementName,alarmId));
    }

    /**
     * Checks to see if there is an EventNotification in the cache with the given alarmId and networkElementName
     * @param alarmId - id of the alarm from the notification
     * @param networkElementName - name of the node.
     * @return
     */
    public boolean eventNotificationExists(final String alarmId, final String networkElementName){
        return eventNotificationCache.containsKey(generateMarker(networkElementName,alarmId));
    }

    /**
     * DEBUG only: Gets the full Notification buffer as a Map(String,String)
     * @return Map of the full eventNotificationCache.
     */
    public Map<String,String> getEventNotificationBuffer(){
        HashMap<String,String> returnMap = new HashMap<>();

        eventNotificationCache.forEach((k,v) -> returnMap.put(k.toString(),v.toString()));

        return returnMap;
    }

    /**
     * Used to return the list of notifications which have elapsed and should be sent to APS
     * @return Map of EventNotificationMarker, EventNotification the Map of EventNotifications which need to be sent to APS
     */
    public Map<EventNotificationMarker, EventNotification> getEventNotificationsfromTime() {
        final Map<EventNotificationMarker, EventNotification> returnNotificationList = new HashMap<>();
        eventNotificationCache.keySet().forEach(marker -> {
            long timePlusTwoSeconds = marker.getEntryTime() + (TIMEOUT_VALUE* 1000L);
            //Is the notification over 2 seconds old?
                if(timePlusTwoSeconds < System.currentTimeMillis()){
                    returnNotificationList.put(marker,eventNotificationCache.get(marker));
                }
        });
        return returnNotificationList;
    }

    private EventNotificationMarker generateMarker(final String networkElementName, final String eventNotificationId) {
        return new EventNotificationMarker(networkElementName, eventNotificationId);
    }

}
