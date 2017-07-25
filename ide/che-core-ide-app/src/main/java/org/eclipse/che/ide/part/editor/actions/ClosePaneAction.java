/*
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.eclipse.che.ide.part.editor.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorPartStack;

/**
 * Performs closing current pane and all opened editors for this one.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ClosePaneAction extends EditorAbstractAction {

  @Inject
  public ClosePaneAction(
      EditorAgent editorAgent, EventBus eventBus, CoreLocalizationConstant locale) {
    super(
        locale.editorClosePane(), locale.editorClosePaneDescription(), null, editorAgent, eventBus);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    EditorPartStack currentPartStack = getEditorPane(event);
    for (EditorPartPresenter editorPart : editorAgent.getOpenedEditorsFor(currentPartStack)) {
      editorAgent.closeEditor(editorPart);
    }
  }
}
