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
package org.eclipse.che.api.languageserver.launcher;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;

/**
 * Interface realize running some preparation project procedures before launch language server for this project.
 * {@link LanguageServerLauncher} should implements this interface in case if IDE need to do some special tasks
 * to prepare project before connection to language server. It's can be some inner long-term operation (build some index storage,
 * restore dependencies or so on...), without this operations language server doesn't work, or works without full functionality.
 *
 * @author Alexander Andrienko
 */
public interface LanguageServerPrepareProjectProvider {

    int DEFAULT_TIME_OUT = 15;

    /**
     * Prepare project before launching language server for this project.
     * @param timeOut - time out for preparation procedures.
     * todo maybe timeUnit ?
     **/
    void prepareProject(int timeOut, String projectPath) throws LanguageServerException;

    /**
     * @return timeOut for preparation tasks.
     */
    default int getTimeOut() {
        return DEFAULT_TIME_OUT;
    }
}
