/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.cluster.raft;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import akka.actor.ActorRef;
import akka.dispatch.Dispatchers;
import com.google.common.base.Function;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.controller.cluster.raft.RaftActorLeadershipTransferCohort.OnComplete;

/**
 * Unit tests for RaftActorLeadershipTransferCohort.
 *
 * @author Thomas Pantelis
 */
public class RaftActorLeadershipTransferCohortTest extends AbstractActorTest {
    private final TestActorFactory factory = new TestActorFactory(getSystem());
    private MockRaftActor mockRaftActor;
    private RaftActorLeadershipTransferCohort cohort;
    private final OnComplete onComplete = mock(OnComplete.class);
    private final DefaultConfigParamsImpl config = new DefaultConfigParamsImpl();
    private Function<Runnable, Void> pauseLeaderFunction;

    @After
    public void tearDown() {
        factory.close();
    }

    private void setup() {
        doNothing().when(onComplete).onSuccess(any(ActorRef.class), any(ActorRef.class));
        doNothing().when(onComplete).onFailure(any(ActorRef.class), any(ActorRef.class));

        String persistenceId = factory.generateActorId("leader-");
        mockRaftActor = factory.<MockRaftActor>createTestActor(MockRaftActor.builder().id(persistenceId).config(
                config).pauseLeaderFunction(pauseLeaderFunction).props().withDispatcher(Dispatchers.DefaultDispatcherId()),
                persistenceId).underlyingActor();
        cohort = new RaftActorLeadershipTransferCohort(mockRaftActor, null);
        cohort.addOnComplete(onComplete);
        mockRaftActor.waitForInitializeBehaviorComplete();
    }

    @Test
    public void testOnNewLeader() {
        setup();
        cohort.setNewLeaderTimeoutInMillis(20000);

        cohort.onNewLeader("new-leader");
        verify(onComplete, never()).onSuccess(mockRaftActor.self(), null);

        cohort.transferComplete();

        cohort.onNewLeader(null);
        verify(onComplete, never()).onSuccess(mockRaftActor.self(), null);

        cohort.onNewLeader("new-leader");
        verify(onComplete).onSuccess(mockRaftActor.self(), null);
    }

    @Test
    public void testNewLeaderTimeout() {
        setup();
        cohort.setNewLeaderTimeoutInMillis(200);
        cohort.transferComplete();
        verify(onComplete, timeout(3000)).onSuccess(mockRaftActor.self(), null);
    }

    @Test
    public void testNotLeaderOnRun() {
        config.setElectionTimeoutFactor(10000);
        setup();
        cohort.doTransfer();
        verify(onComplete).onSuccess(mockRaftActor.self(), null);
    }

    @Test
    public void testAbortTransfer() {
        setup();
        cohort.abortTransfer();
        verify(onComplete).onFailure(mockRaftActor.self(), null);
    }

    @Test
    public void testPauseLeaderTimeout() {
        pauseLeaderFunction = new Function<Runnable, Void>() {
            @Override
            public Void apply(Runnable input) {
                return null;
            }
        };

        setup();
        cohort.init();
        verify(onComplete, timeout(2000)).onFailure(mockRaftActor.self(), null);
    }
}
