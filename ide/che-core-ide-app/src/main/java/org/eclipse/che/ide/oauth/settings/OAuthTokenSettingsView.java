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

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 *
 */
@ImplementedBy(OAuthTokenSettingsViewImpl.class)
public interface OAuthTokenSettingsView extends View<OAuthTokenSettingsView.ActionDelegate> {

    void setDescriptors(List<OAuthDescriptorWithToken> descriptors);

    interface ActionDelegate {

        void invalidateTokenForProvider(OAuthDescriptorWithToken descriptor);
    }
}
