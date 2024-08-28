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

import java.util.regex.Pattern;

public final class Constants {
    private Constants(){

    }
    //Timer Timeout and timer Setup in seconds
    public static final int TIMEOUT_VALUE = 2;
    public static final String FM_ALARM_SUPERVISION_EVENT_ENDPOINT = "jms:/topic/FmSupervisionStatusTopic";
    //Clustered-dps-notification-event
    public static final String DPS_NOTIFICATION_EVENT_ENDPOINT = "jms:/topic/dps-notification-event";
    public static final String FM_ALARM_SUPERVISION_FILTER = "((bucketName IS NOT NULL) AND (bucketName = 'Live')) AND ((namespace IS NOT NULL) " +
            "AND (namespace = 'OSS_NE_FM_DEF')) AND ((type IS NOT NULL) AND (type='FmAlarmSupervision'))";
    public static final String OSS_NE_DEF_NS = "OSS_NE_DEF";
    public static final String ORADIO_NE_TYPE = "ORadio";
    public static final String NE_TYPE_ATTR = "neType";
    public static final String NETWORK_ELEMENT_TYPE = "NetworkElement";
    public static final String FM_ALARM_SUPERVISION_TYPE = "FmAlarmSupervision";
    public static final String FM_ALARM_SUPERVISION_RDN = FM_ALARM_SUPERVISION_TYPE + "=1";
    public static final String ACTIVE_ATTR = "active";
    public static final  Pattern NEID_PATTERN = Pattern.compile(".*?(NetworkElement)=([A-Za-z0-9-._:/?%&!\\s]*)");

    public static final String NETWORK_ELEMENT_FDN = "NetworkElement=";
    public static final String TARGET_ATTRIBUTE_INFO ="targetAttributeInfo";

}
