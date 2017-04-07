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
package org.eclipse.che.ide.api.auth;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Sergii Leschenko
 */
public class OAuthServiceClientImpl implements OAuthServiceClient {
    private final AsyncRequestFactory asyncRequestFactory;
    private final String              restContext;
    private final DtoUnmarshallerFactory unmarshallerFactory;

    @Inject
    public OAuthServiceClientImpl(AppContext appContext,
                                  AsyncRequestFactory asyncRequestFactory,
                                  DtoUnmarshallerFactory unmarshallerFactory
    ) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.restContext = appContext.getMasterEndpoint() + "/oauth/";
        this.unmarshallerFactory = unmarshallerFactory;
    }

    @Override
    public Promise<Void> invalidateToken(String oauthProvider) {
        return asyncRequestFactory.createDeleteRequest(restContext + "token?oauth_provider=" + oauthProvider)
                .send();
    }

    @Override
    public Promise<OAuthToken> getToken(String oauthProvider) {
        Unmarshallable<OAuthToken> unmarshaller = unmarshallerFactory.newUnmarshaller(OAuthToken.class);
        return asyncRequestFactory.createGetRequest(restContext + "token?oauth_provider=" + oauthProvider)
                .send(unmarshaller);
    }

    @Override
    public Promise<List<OAuthAuthenticatorDescriptor>> getRegisteredAuthenticators() {
        Unmarshallable<List<OAuthAuthenticatorDescriptor>> unmarshallable = unmarshallerFactory.newListUnmarshaller(OAuthAuthenticatorDescriptor.class);
        return asyncRequestFactory.createGetRequest(restContext).header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(unmarshallable);
    }
}
