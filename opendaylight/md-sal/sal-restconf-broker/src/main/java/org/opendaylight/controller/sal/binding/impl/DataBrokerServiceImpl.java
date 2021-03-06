/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.sal.binding.impl;

import java.net.URL;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.controller.sal.binding.api.data.DataChangeListener;
import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.controller.sal.common.DataStoreIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContextFactory;
import org.opendaylight.yangtools.restconf.client.api.UnsupportedProtocolException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBrokerServiceImpl implements DataBrokerService {

    private static final Logger logger = LoggerFactory.getLogger(DataBrokerServiceImpl.class.toString());
    private final RestconfClientContext restconfClientContext;
    private final RestconfClientContextFactory restconfClientContextFactory = null;

    public DataBrokerServiceImpl(URL baseUrl) throws UnsupportedProtocolException {
        this.restconfClientContext = restconfClientContextFactory.getRestconfClientContext(baseUrl);
    }
    @Override
    public <T extends DataRoot> T getData(DataStoreIdentifier store, Class<T> rootType) {
        return null;
    }

    @Override
    public <T extends DataRoot> T getData(DataStoreIdentifier store, T filter) {
        return null;
    }

    @Override
    public <T extends DataRoot> T getCandidateData(DataStoreIdentifier store, Class<T> rootType) {
        return null;
    }

    @Override
    public <T extends DataRoot> T getCandidateData(DataStoreIdentifier store, T filter) {
        return null;
    }

    @Override
    public RpcResult<DataRoot> editCandidateData(DataStoreIdentifier store, DataRoot changeSet) {
        return null;
    }

    @Override
    public Future<RpcResult<Void>> commit(DataStoreIdentifier store) {
        return null;
    }

    @Override
    public DataObject getData(InstanceIdentifier<? extends DataObject> data) {
        return null;
    }

    @Override
    public DataObject getConfigurationData(InstanceIdentifier<?> data) {
        return null;
    }

    @Override
    public DataModificationTransaction beginTransaction() {
        //TODO implementation using sal-remote
        return null;
    }

    @Override
    public void registerChangeListener(InstanceIdentifier<? extends DataObject> path, DataChangeListener changeListener) {

    }

    @Override
    public void unregisterChangeListener(InstanceIdentifier<? extends DataObject> path, DataChangeListener changeListener) {

    }

    @Override
    public DataObject readConfigurationData(InstanceIdentifier<? extends DataObject> path) {
        //TODO implementation using restconf-client
        return null;

    }

    @Override
    public DataObject readOperationalData(InstanceIdentifier<? extends DataObject> path) {
        //TODO implementation using restconf-client
        return null;
    }

    @Override
    public ListenerRegistration<DataChangeListener> registerDataChangeListener(InstanceIdentifier<? extends DataObject> path, DataChangeListener listener) {
        //TODO implementation using restconf-client
        return null;
    }





}
