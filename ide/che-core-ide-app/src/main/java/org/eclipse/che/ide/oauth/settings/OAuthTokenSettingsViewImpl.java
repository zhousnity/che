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

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.cellview.CellTableResources;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 *
 */
@Singleton
public class OAuthTokenSettingsViewImpl implements OAuthTokenSettingsView {


    private CellTable<OAuthDescriptorWithToken> authenticatorCellTable;
    private ActionDelegate delegate;

    @Inject
    public OAuthTokenSettingsViewImpl(CellTableResources res, CoreLocalizationConstant constant) {
        authenticatorCellTable = new CellTable<>(15, res);
        authenticatorCellTable.setWidth("100%");
        authenticatorCellTable.getElement().getStyle().setMarginLeft(14, Style.Unit.PX);
        TextColumn<OAuthDescriptorWithToken> providerColumn = new TextColumn<OAuthDescriptorWithToken>() {
            @Override
            public String getValue(OAuthDescriptorWithToken descriptor) {
                return descriptor.getDescriptor().getName();
            }
        };
        authenticatorCellTable.addColumn(providerColumn);


        Column<OAuthDescriptorWithToken, String> invalidateColumn = new Column<OAuthDescriptorWithToken, String>(new ButtonCell()) {
            @Override
            public String getValue(OAuthDescriptorWithToken object) {
                return constant.oauthSettingsProviderInvalidate();
            }
        };
        authenticatorCellTable.addColumn(invalidateColumn);
        authenticatorCellTable.setColumnWidth(invalidateColumn, "50%");
        providerColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        invalidateColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        invalidateColumn.setFieldUpdater((i, obj, val)-> delegate.invalidateTokenForProvider(obj));
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return authenticatorCellTable;
    }

    @Override
    public void setDescriptors(List<OAuthDescriptorWithToken> descriptors) {
        authenticatorCellTable.setRowData(descriptors);
    }
}
