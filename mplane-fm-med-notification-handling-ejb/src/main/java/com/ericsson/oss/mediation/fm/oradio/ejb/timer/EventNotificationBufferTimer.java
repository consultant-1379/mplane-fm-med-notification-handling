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

package com.ericsson.oss.mediation.fm.oradio.ejb.timer;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.mediation.fm.oradio.api.MPlaneAlarmService;
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.EventNotificationBuffer;
import com.ericsson.oss.mediation.fm.oradio.ejb.models.EventNotificationMarker;
import com.ericsson.oss.mediation.translator.model.EventNotification;
import com.ericsson.oss.mediation.fm.oradio.ejb.exception.CreateEventBufferTimerException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.inject.Inject;

import static com.ericsson.oss.mediation.fm.oradio.ejb.Constants.TIMEOUT_VALUE;

/**
 * This class is instantiated at SG startup and starts a timer with the timeout value coming from TIMEOUT_VALUE / 2.
 * When the timer elapses it will call the timeout function and check the EventNotificationBuffer for any EventNotifications that
 * need to be sent to APS.
 * Currently the timeout is set to 2 seconds, however this may change depending on performance testing.
 * This means that the timer will fire every second to process the notifications.
 */
@Startup
@Singleton
@Slf4j
public class EventNotificationBufferTimer {

    @Inject
    private EventNotificationBuffer buffer;

    @EServiceRef
    private MPlaneAlarmService mplaneAlarmService;

    @Inject
    private TimerService timerService;

    private Timer timer;
    private static final long TIMEOUT = (TIMEOUT_VALUE / 2) * 1000L;

    @PostConstruct
    private void initialiseEventBufferTimer() throws CreateEventBufferTimerException {
        try {
            log.info("Initialising EventNotificationBufferTimer Service");
            timer = timerService.createIntervalTimer(TIMEOUT,TIMEOUT, new TimerConfig());
        } catch (final EJBException | IllegalStateException e) {
            log.error("Failed to initialise EventNotificationBufferTimer", e);
            throw new CreateEventBufferTimerException("Failed to create EventNotificationBufferTimer for: "+ this.getClass().getSimpleName());
        }
    }

    /**
     * Where the work is completed in this class, the timer will check the EventNotificationBuffer for any notifications that have elapsed the timeout
     * value and send them to APS
     * @param timer - the timer object that has elapsed.
     */
    @Timeout
    public void timeout(final Timer timer) {
        log.debug("EventNotificationBufferTimer has elapsed");

        final Map<EventNotificationMarker, EventNotification> notifList = buffer.getEventNotificationsfromTime();
        if(!notifList.isEmpty()) {
            buffer.removeEventNotificationList(notifList.keySet());
            log.info("Sending {} alarms", (long) notifList.values().size());
            mplaneAlarmService.sendAlarms(new ArrayList<>(notifList.values()));

            log.info("EventNotificationBufferTimer has finished processing");
        }
        log.debug("Nothing to process from the EventNotificationBuffer");
    }

    @PreDestroy
    private void onServiceStopping() {
        timer.cancel();
        log.info("Stopping EventNotificationBufferTimer");
    }
}
