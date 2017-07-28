/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.core.db.DBInitializer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/** Tests {@link WorkspaceRuntimes}. */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRuntimesTest {

    @Mock
    private EventService eventService;

    @Mock
    private WorkspaceDao workspaceDao;

    @Mock
    private DBInitializer dbInitializer;

    @Mock
    private WorkspaceSharedPool sharedPool;

    private RuntimeInfrastructure infrastructure;
    private WorkspaceRuntimes     runtimes;

    @BeforeMethod
    public void setUp() {
        infrastructure = spy(new TestInfrastructure(eventService));

        runtimes = new WorkspaceRuntimes(eventService,
                                         Collections.singleton(infrastructure),
                                         sharedPool,
                                         workspaceDao,
                                         dbInitializer);
    }

    @Test
    public void runtimeIsRecovered() throws Exception {
        RuntimeIdentity identity = new RuntimeIdentityImpl("workspace123", "my-env", "me");

        mockWorkspace(identity);
        RuntimeContext context = mockContext(identity);
        when(context.getRuntime()).thenReturn(new TestInternalRuntime(context));
        doReturn(context).when(infrastructure).prepare(eq(identity), anyObject());

        // try recover
        runtimes.recoverOne(infrastructure, identity);

        WorkspaceImpl workspace = new WorkspaceImpl(identity.getWorkspaceId(), null, null);
        runtimes.injectRuntime(workspace);
        assertNotNull(workspace.getRuntime());
    }

    @Test
    public void runtimeIsNotRecoveredIfNoWorkspaceFound() throws Exception {
        RuntimeIdentity identity = new RuntimeIdentityImpl("workspace123", "my-env", "me");
        when(workspaceDao.get(identity.getWorkspaceId())).thenThrow(new NotFoundException("no!"));

        // try recover
        runtimes.recoverOne(infrastructure, identity);

        assertFalse(runtimes.hasRuntime(identity.getWorkspaceId()));
    }

    @Test
    public void runtimeIsNotRecoveredIfNoEnvironmentFound() throws Exception {
        RuntimeIdentity identity = new RuntimeIdentityImpl("workspace123", "my-env", "me");
        WorkspaceImpl workspace = mockWorkspace(identity);
        when(workspace.getConfig().getEnvironments()).thenReturn(Collections.emptyMap());

        // try recover
        runtimes.recoverOne(infrastructure, identity);

        assertFalse(runtimes.hasRuntime(identity.getWorkspaceId()));
    }

    @Test
    public void runtimeIsNotRecoveredIfInfraPreparationFailed() throws Exception {
        RuntimeIdentity identity = new RuntimeIdentityImpl("workspace123", "my-env", "me");

        EnvironmentImpl env = mockWorkspace(identity).getConfig().getEnvironments().get(identity.getEnvName());
        doThrow(new InfrastructureException("oops!")).when(infrastructure).prepare(identity, env);

        // try recover
        runtimes.recoverOne(infrastructure, identity);

        assertFalse(runtimes.hasRuntime(identity.getWorkspaceId()));
    }

    @Test
    public void runtimeIsNotRecoveredIfAnotherRuntimeWithTheSameIdentityAlreadyExists() throws Exception {
        RuntimeIdentity identity = new RuntimeIdentityImpl("workspace123", "my-env", "me");

        mockWorkspace(identity);
        RuntimeContext context = mockContext(identity);

        // runtime 1(has 1 machine) must be successfully saved
        Map<String, Machine> r1machines = ImmutableMap.of("m1", mock(Machine.class));
        InternalRuntime runtime1 = spy(new TestInternalRuntime(context, r1machines));
        when(context.getRuntime()).thenReturn(runtime1);
        runtimes.recoverOne(infrastructure, identity);

        // runtime 2 must not be saved
        Map<String, Machine> r2machines = ImmutableMap.of("m1", mock(Machine.class), "m2", mock(Machine.class));
        InternalRuntime runtime2 = new TestInternalRuntime(context, r2machines);
        when(context.getRuntime()).thenReturn(runtime2);
        runtimes.recoverOne(infrastructure, identity);

        WorkspaceImpl workspace = new WorkspaceImpl(identity.getWorkspaceId(), null, null);
        runtimes.injectRuntime(workspace);

        assertNotNull(workspace.getRuntime());
        assertEquals(workspace.getRuntime().getMachines().keySet(), r1machines.keySet());
    }

    private RuntimeContext mockContext(RuntimeIdentity identity) throws ValidationException, InfrastructureException {
        RuntimeContext context = mock(RuntimeContext.class);
        doReturn(context).when(infrastructure).prepare(eq(identity), anyObject());
        when(context.getInfrastructure()).thenReturn(infrastructure);
        when(context.getIdentity()).thenReturn(identity);
        return context;
    }

    private WorkspaceImpl mockWorkspace(RuntimeIdentity identity) throws NotFoundException, ServerException {
        EnvironmentImpl environment = mock(EnvironmentImpl.class);

        WorkspaceConfigImpl config = mock(WorkspaceConfigImpl.class);
        when(config.getEnvironments()).thenReturn(ImmutableMap.of(identity.getEnvName(), environment));

        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        when(workspace.getConfig()).thenReturn(config);
        when(workspace.getId()).thenReturn(identity.getWorkspaceId());

        when(workspaceDao.get(identity.getWorkspaceId())).thenReturn(workspace);

        return workspace;
    }

    private static class TestInfrastructure extends RuntimeInfrastructure {
        public TestInfrastructure(EventService eventService) {
            super("test", Collections.singleton("test"), eventService);
        }

        @Override
        public Environment estimate(Environment environment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws InfrastructureException {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestInternalRuntime extends InternalRuntime<RuntimeContext> {
        final Map<String, Machine> machines;

        TestInternalRuntime(RuntimeContext context, Map<String, Machine> machines) {
            super(context, null, false);
            this.machines = machines;
        }

        TestInternalRuntime(RuntimeContext context) {
            this(context, Collections.emptyMap());
        }

        @Override
        protected Map<String, Machine> getInternalMachines() {
            return machines;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.emptyMap();
        }

        @Override
        protected void internalStop(Map stopOptions) throws InfrastructureException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void internalStart(Map startOptions) throws InfrastructureException {
            throw new UnsupportedOperationException();
        }
    }
}
