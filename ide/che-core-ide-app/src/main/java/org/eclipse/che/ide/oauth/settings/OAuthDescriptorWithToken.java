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
package org.eclipse.che.ide.oauth.settings;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;

/**
 * Data class, contains {@link org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor} and {@link org.eclipse.che.api.auth.shared.dto.OAuthToken}
 */
public class OAuthDescriptorWithToken {

    private final OAuthAuthenticatorDescriptor descriptor;
    private final OAuthToken token;

    public OAuthDescriptorWithToken(OAuthAuthenticatorDescriptor descriptor, OAuthToken token) {
        this.descriptor = descriptor;
        this.token = token;
    }

    public OAuthAuthenticatorDescriptor getDescriptor() {
        return descriptor;
    }

    public OAuthToken getToken() {
        return token;
    }
}
