/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {ICommunicationClient, JsonRpcClient} from './json-rpc-client';

/**
 * This
 *
 * @author Ann Shumilova
 */
export class CheJsonRpcApiClient {

  private jsonRpcClient: JsonRpcClient;

  constructor (client: ICommunicationClient) {
    this.jsonRpcClient = new JsonRpcClient(client);
  }

  subscribe(method: string, params: any, handler: Function): void {
    //TODO
    this.jsonRpcClient.addNotificationHandler(method, handler);
    this.jsonRpcClient.notify(method, params);
  }

  unsubscribe(method: string, handler: Function): void {
    //TODO
    this.jsonRpcClient.removeNotificationHandler(method, handler);
  }
}


