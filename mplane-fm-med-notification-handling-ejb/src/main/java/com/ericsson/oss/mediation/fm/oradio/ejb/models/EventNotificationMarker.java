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

package com.ericsson.oss.mediation.fm.oradio.ejb.models;

import java.util.Objects;

/**
 * The EventNotificationMarker is used as a Key for the EventNotificationBuffer, it is designed to hold all the information that may be needed to
 * determine if a notification is in the cache, along with the time it was entered.
 * the entryTime is not used to determine equality between EventNotificationMarkers, only networkElementName and eventNotificationId is
 */

public class EventNotificationMarker {

    /**
     * Constructor of the EventNotificationMarker class, takes in networkElementName and eventNotificationId to uniquely identify the EventNotification
     * going into the EventNotificationBufferCache
     * @param networkElementName - Name of the networkElement
     * @param eventNotificationId - Unique Id of the alarm
     */
    public EventNotificationMarker(final String networkElementName, final String eventNotificationId) {
        this.networkElementName = networkElementName;
        this.eventNotificationId = eventNotificationId;
    }

    private String networkElementName;

    private String eventNotificationId;

    private long entryTime;

    /**
     * Gets the networkElementName which is associated with the EventNotification going into the EventNotificationBufferCache
     * @return network element name
     */
    public String getNetworkElementName() {
        return networkElementName;
    }

    /**
     * Gets the eventNotificationId associated with the EventNotification going into the EventNotificationBufferCache
     * @return eventNotificationId taken from the EventNotification as externalId
     */
    public String getEventNotificationId() {
        return eventNotificationId;
    }

    /**
     * Gets the Entry time in milliseconds since epoch that the EventNotification was added to the EventNotificationBufferCache
     * @return long - entry time of the EventNotification into the cache in milliseconds
     */
    public long getEntryTime() {
        return entryTime;
    }

    /**
     * Sets the entry time into the cache
     * @param entryTimeMilliseconds the time in milliseconds since epoch that the entry was entered into the EventNotificationBufferCache
     */

    public void setEntryTime(long entryTimeMilliseconds){
        this.entryTime = entryTimeMilliseconds;
    }

    /**
     * Overriden to ensure that equality is only on networkElementName and eventNotificationId, discards entryTime
     * @param o - object to compare for equality
     * @return boolean if the object tested is equal to this
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EventNotificationMarker that = (EventNotificationMarker) o;
        return Objects.equals(networkElementName, that.networkElementName) && Objects.equals(eventNotificationId, that.eventNotificationId);
    }

    /**
     * Overriden to ensure that equality is only on networkElementName and eventNotificationId, discards entryTime
     * @return hashcode of the instance, discarding entryTime from the calculation.
     */
    @Override
    public int hashCode() {
        return Objects.hash(networkElementName, eventNotificationId);
    }

    /**
     * provides a string representation of the EventNotificationMarker
     * @return string representation of the EventNotificationMarker
     */
    @Override
    public String toString() {
        return "EventNotificationMarker{" + "networkElementName='" + networkElementName + '\'' + ", eventNotificationId='" + eventNotificationId
                + '\'' + ", entryTime=" + entryTime + '}';
    }
}
