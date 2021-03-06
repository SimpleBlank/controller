
/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.containermanager.internal;

import org.eclipse.osgi.framework.console.CommandProvider;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;
import org.opendaylight.controller.containermanager.IContainerManager;
import org.apache.felix.dm.Component;
import org.opendaylight.controller.clustering.services.ICacheUpdateAware;
import org.opendaylight.controller.clustering.services.IClusterGlobalServices;
import org.opendaylight.controller.configuration.IConfigurationAware;
import org.opendaylight.controller.configuration.IConfigurationService;
import org.opendaylight.controller.containermanager.IContainerAuthorization;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.opendaylight.controller.sal.core.IContainer;
import org.opendaylight.controller.sal.core.IContainerAware;
import org.opendaylight.controller.sal.core.IContainerListener;
import org.opendaylight.controller.sal.core.IContainerLocalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Activator extends ComponentActivatorAbstractBase {
    protected static final Logger logger = LoggerFactory.getLogger(Activator.class);


    /**
     * Function that is used to communicate to dependency manager the list of
     * known implementations for containerd-services
     *
     *
     * @return An array containing all the CLASS objects that will be
     *         instantiated in order to get an fully working implementation
     *         Object
     */
    @Override
    public Object[] getImplementations() {
        Object[] res = { ContainerImpl.class };
        return res;
    }

    /**
     * Function that is called when configuration of the dependencies is
     * required.
     *
     * @param c
     *            dependency manager Component object, used for configuring the
     *            dependencies exported and imported
     * @param imp
     *            Implementation class that is being configured, needed as long
     *            as the same routine can configure multiple implementations
     * @param containerName
     *            The containername being configured, this allow also optional
     *            per-container different behavior if needed, usually should not be
     *            the case though.
     */
    @Override
    public void configureInstance(Component c, Object imp, String containerName) {
        if (imp.equals(ContainerImpl.class)) {
            // export the service
            c.setInterface(new String[] { IContainer.class.getName() }, null);

            // private interface exported by ContainerManager to retrieve
            // internal data, this is mandatory to implement the
            // IContainer functionality
            c.add(createServiceDependency().setService(IContainerInternal.class)
                    .setCallbacks("setIContainerInternal", "unsetIContainerInternal")
                    .setRequired(true));
        }
    }

    /**
     * Method which tells how many NON-containerd implementations are supported by
     * the bundle. This way we can tune the number of components created. This
     * components will be created ONLY at the time of bundle startup and will be
     * destroyed only at time of bundle destruction, this is the major
     * difference with the implementation retrieved via getImplementations where
     * all of them are assumed to be containerd!
     *
     *
     * @return The list of implementations the bundle will support, in
     *         non-containerd version
     */
    @Override
    protected Object[] getGlobalImplementations() {
        Object[] res = { ContainerManager.class };
        return res;
    }

    /**
     * Configure the dependency for a given instance non-containerd
     *
     * @param c
     *            Component assigned for this instance, this will be what will
     *            be used for configuration
     * @param imp
     *            implementation to be configured
     * @param containerName
     *            container on which the configuration happens
     */
    @Override
    protected void configureGlobalInstance(Component c, Object imp) {
        if (imp.equals(ContainerManager.class)) {
            Dictionary<String, Set<String>> props = new Hashtable<String, Set<String>>();
            Set<String> propSet = new HashSet<String>();
            propSet.add("containermgr.event.containerChange");
            props.put("cachenames", propSet);

            // export the service
            c.setInterface(
                    new String[] { IContainerManager.class.getName(),
                            IContainerManager.class.getName(),
                            IConfigurationAware.class.getName(),
                            CommandProvider.class.getName(),
                            IContainerInternal.class.getName(),
                            IContainerAuthorization.class.getName(),
                            ICacheUpdateAware.class.getName()}, props);

            c.add(createServiceDependency()
                    .setService(IClusterGlobalServices.class)
                    .setCallbacks("setClusterServices", "unsetClusterServices")
                    .setRequired(true));

            c.add(createServiceDependency().setService(
                    IConfigurationService.class).setCallbacks(
                    "setConfigurationService",
                    "unsetConfigurationService").setRequired(true));

            // Key kick-starter for container creation in each component
            c.add(createServiceDependency().setService(IContainerAware.class)
                    .setCallbacks("setIContainerAware", "unsetIContainerAware")
                    .setRequired(false));

            // Optional interface expected to be exported by the
            // protocol plugins to setup proper filtering based on
            // slicing events
            c.add(createServiceDependency()
                    .setService(IContainerListener.class)
                    .setCallbacks("setIContainerListener", "unsetIContainerListener")
                    .setRequired(false));

            // Interface expected to be exported by the Functional Modules
            c.add(createServiceDependency()
                    .setService(IContainerLocalListener.class)
                    .setCallbacks("setIContainerLocalListener", "unsetIContainerLocalListener")
                    .setRequired(false));
        }
    }
}
