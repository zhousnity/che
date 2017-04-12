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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
@Singleton
public class OAuthTokenSettingsPresenter extends AbstractPreferencePagePresenter implements OAuthTokenSettingsView.ActionDelegate {

    private final OAuthTokenSettingsView view;
    private final OAuthServiceClient serviceClient;
    private final NotificationManager notificationManager;
    private final CoreLocalizationConstant constant;

    private List<OAuthDescriptorWithToken> descriptors;

    @Inject
    public OAuthTokenSettingsPresenter(OAuthTokenSettingsView view,
                                       OAuthServiceClient serviceClient,
                                       NotificationManager notificationManager,
                                       CoreLocalizationConstant constant) {
        super(constant.oauthSettingsTitle(), constant.oauthSettingsGroup());
        this.view = view;
        this.serviceClient = serviceClient;
        this.notificationManager = notificationManager;
        this.constant = constant;
        view.setDelegate(this);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        loadProviders();
    }

    private void loadProviders() {
        serviceClient.getRegisteredAuthenticators().then(list -> {
            descriptors = new ArrayList<>();
            Iterator<OAuthAuthenticatorDescriptor> iterator = list.iterator();
            loadTokens(iterator);
        }).catchError(err -> {
            Log.error(getClass(), err);
        });
    }

    private void loadTokens(Iterator<OAuthAuthenticatorDescriptor> iterator) {
        if (iterator.hasNext()) {
            OAuthAuthenticatorDescriptor descriptor = iterator.next();
            serviceClient.getToken(descriptor.getName()).then(token -> {
                descriptors.add(new OAuthDescriptorWithToken(descriptor, token));
                loadTokens(iterator);
            }).catchError(err -> {
                //service return 404 if provider doesn't has token, so ignore errors and load next token
                loadTokens(iterator);
            });

        } else {
            view.setDescriptors(descriptors);
        }
    }


    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void storeChanges() {

    }

    @Override
    public void revertChanges() {

    }

    @Override
    public void invalidateTokenForProvider(OAuthDescriptorWithToken descriptor) {
        serviceClient.invalidateToken(descriptor.getDescriptor().getName()).then(v -> {
            notificationManager.notify(constant.oauthInvalidateTokenSuccess(descriptor.getDescriptor().getName()), "", StatusNotification.Status.SUCCESS, StatusNotification.DisplayMode.FLOAT_MODE);
            loadProviders();
        }).catchError(err -> {
            Log.error(getClass(), err);
            notificationManager.notify("Error", err.getMessage(), StatusNotification.Status.FAIL, StatusNotification.DisplayMode.FLOAT_MODE);
        });
    }
}
