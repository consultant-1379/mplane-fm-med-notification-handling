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

import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectDeletedEvent;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.eventbus.annotation.Consumes;
import com.ericsson.oss.mediation.fm.oradio.ejb.dps.DpsAccess;
import lombok.extern.slf4j.Slf4j;

import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.NETWORK_ELEMENT_TYPE;
import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.OSS_NE_DEF_NS;
import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.NE_TYPE_ATTR;
import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.ORADIO_NE_TYPE;
import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.DPS_NOTIFICATION_EVENT_ENDPOINT;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * This class is used to store the ossPrefix of ORadio Nodes so that they are available during the transformation of the raw notifications into
 * EventNotifications. This may be surplus and might be removed in time as the notification may come through with this information.
 */
@Singleton
@Startup
@Slf4j
public class OssPrefixCache {

    @Inject
    private DpsAccess dpsAccess;

    private final Map<String,String> ossPrefixCache = new ConcurrentHashMap<>();

    @PostConstruct
    void initialiseOssPrefixCache() {
        try {
            log.debug("Initialising Local OssPrefixCache");

            final List<ManagedObject> networkElementMoList = dpsAccess.createRestrictionTypeQuery(OSS_NE_DEF_NS, NETWORK_ELEMENT_TYPE,
                    NE_TYPE_ATTR, ORADIO_NE_TYPE);

            log.debug("Found potential NetworkElement MOs to add to cache {}", networkElementMoList);

            for (final ManagedObject networkElementMo : networkElementMoList) {
                final String networkElementFdn = networkElementMo.getFdn();
                log.debug("Found NetworkElement MO with fdn {}", networkElementFdn);
                final String ossPrefixAttribute = networkElementMo.getAttribute("ossPrefix");

                //If we get a null here something has gone wrong with the dps query
                //Empty String is a valid ossPrefix e.g. ""
                if (ossPrefixAttribute == null) {
                    log.debug("Node with fdn {} does not have an ossprefix child so not adding to cache.", networkElementFdn);
                    continue;
                }

                log.debug("Adding fdn {} with ossPrefix {} to cache.", networkElementMo.getFdn(), ossPrefixAttribute);
                ossPrefixCache.put(networkElementFdn, ossPrefixAttribute);
            }
            log.debug("Initialised ossPrefix with {} entries.", ossPrefixCache.size());
        } catch (final Exception e) {
            // TODO catch proper exception and decide what to do with it
            log.error("Failed to initialise supervision state cache", e);
        }
    }

    @PreDestroy
    void onServiceStopping() {
        ossPrefixCache.clear();
        log.info("Stopping OssPrefixCache");
    }

    /**
     * Used to catch DPS notifications and process them, specifically this will catch the DpsObjectDeletedEvent when a Network Element is deleted
     * and remove the nodes ossPrefix from the cache
     * @param event - Event received from the DPS_NOTIFICATION_EVENT_ENDPOINT
     */

    public void onNodeDeletedEvent(@Observes
                                   @Consumes(endpoint = DPS_NOTIFICATION_EVENT_ENDPOINT, filter = "type = 'NetworkElement'")
                                   final DpsObjectDeletedEvent event) {
        log.info("Received DpsObjectDeletedEvent {}. Removing node from cache if it exists.", event);
        //Have to process this to get just the nodename
        ossPrefixCache.remove(event.getFdn());
    }

    /**
     * Used to catch DPS notifications and process them, specifically this will catch the DpsObjectCreatedEvent when a Network Element is added
     * and add the nodes ossPrefix to the cache
     * @param event - Event received from the DPS_NOTIFICATION_EVENT_ENDPOINT
     */
    public void onNodeCreatedEvent(@Observes
                                   @Consumes(endpoint = DPS_NOTIFICATION_EVENT_ENDPOINT, filter = "type = 'NetworkElement'")
                                   final DpsObjectCreatedEvent event) {
        log.info("Received DpsObjectCreatedEvent {}. Adding to the cache if it does not exist already", event);
        //Have to process this to get just the nodename
        log.info("event attributeValues: {}", event.getAttributeValues());
        log.info("netype of the event from attributeValues: {}", event.getAttributeValues().get("neType").toString());
        if (event.getAttributeValues().get("neType").toString().equals(ORADIO_NE_TYPE)) {
            ossPrefixCache.put(event.getFdn(), event.getAttributeValues().get("ossPrefix").toString());
        }
    }

    /**
     * returns the ossPrefix for a node
     * @param nodeName - the nodeName to get the ossPrefix for
     * @return ossPrefix
     */
    public String getOssPrefix(final String nodeName) {
        return ossPrefixCache.get(nodeName);
    }

    /**
     * DEBUG ONLY: returns the full cache
     * @return  full cache
     */
    public Map<String,String> getCache(){
        return ossPrefixCache;
    }

}
