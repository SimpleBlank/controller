/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.netconf.persist.impl;

import org.opendaylight.controller.config.persist.api.Persister;
import org.opendaylight.controller.netconf.api.jmx.CommitJMXNotification;
import org.opendaylight.controller.netconf.api.jmx.DefaultCommitOperationMXBean;
import org.opendaylight.controller.netconf.api.jmx.NetconfJMXNotification;
import org.opendaylight.controller.netconf.client.NetconfClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.io.Closeable;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Responsible for listening for notifications from netconf containing latest
 * committed configuration that should be persisted, and also for loading last
 * configuration.
 */
@ThreadSafe
public class ConfigPersisterNotificationHandler implements NotificationListener, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ConfigPersisterNotificationHandler.class);
    private final MBeanServerConnection mBeanServerConnection;
    private final NetconfClient netconfClient;
    private final Persister persisterAggregator;
    private final Pattern ignoredMissingCapabilityRegex;

    public ConfigPersisterNotificationHandler(MBeanServerConnection mBeanServerConnection, NetconfClient netconfClient,
                                              Persister persisterAggregator, Pattern ignoredMissingCapabilityRegex) {
        this.mBeanServerConnection = mBeanServerConnection;
        this.netconfClient = netconfClient;
        this.persisterAggregator = persisterAggregator;
        this.ignoredMissingCapabilityRegex = ignoredMissingCapabilityRegex;
    }

    public void init() {
        registerAsJMXListener();
    }

    private void registerAsJMXListener() {
        logger.trace("Called registerAsJMXListener");
        try {
            mBeanServerConnection.addNotificationListener(DefaultCommitOperationMXBean.objectName, this, null, null);
        } catch (InstanceNotFoundException | IOException e) {
            throw new RuntimeException("Cannot register as JMX listener to netconf", e);
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification instanceof NetconfJMXNotification == false)
            return;

        // Socket should not be closed at this point
        // Activator unregisters this as JMX listener before close is called

        logger.trace("Received notification {}", notification);
        if (notification instanceof CommitJMXNotification) {
            try {
                handleAfterCommitNotification((CommitJMXNotification) notification);
            } catch (Throwable e) {
                // log exceptions from notification Handler here since
                // notificationBroadcastSupport logs only DEBUG level
                logger.warn("Exception occured during notification handling: ", e);
                throw e;
            }
        } else
            throw new IllegalStateException("Unknown config registry notification type " + notification);
    }

    private void handleAfterCommitNotification(final CommitJMXNotification notification) {
        try {
            persisterAggregator.persistConfig(new CapabilityStrippingConfigSnapshotHolder(notification.getConfigSnapshot(),
                    notification.getCapabilities(), ignoredMissingCapabilityRegex));
            logger.trace("Configuration persisted successfully");
        } catch (IOException e) {
            throw new RuntimeException("Unable to persist configuration snapshot", e);
        }
    }

    @Override
    public synchronized void close() {
        // unregister from JMX
        ObjectName on = DefaultCommitOperationMXBean.objectName;
        try {
            if (mBeanServerConnection.isRegistered(on)) {
                mBeanServerConnection.removeNotificationListener(on, this);
            }
        } catch (Exception e) {
            logger.warn("Unable to unregister {} as listener for {}", this, on, e);
        }
    }

    public NetconfClient getNetconfClient() {
        return netconfClient;
    }

}
