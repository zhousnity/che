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
import {ICommunicationClient} from './json-rpc-client';

/**
 * The implementation for JSON RPC protocol communication through websocket.
 *
 * @author Ann Shumilova
 */
export class WebsocketClient implements ICommunicationClient {
  private $websocket: ng.websocket.IWebSocketProvider;
  private $q: ng.IQService;
  private entrypoint: string;
  private websocketStream;

  constructor ($websocket: ng.websocket.IWebSocketProvider, $q: ng.IQService) {
    this.$websocket = $websocket;
    this.$q = $q;
  }

  connect(): ng.IPromise<any> {
    let deferred = $q.defer();

    this.websocketStream = this.$websocket(this.entrypoint);
    this.websocketStream.onOpen(() => {
      deferred.resolve();
    });

    this.websocketStream.onError(() => {
      deferred.reject();
    });

    return deferred.promise;
  }

  close(): void {
    if (this.websocketStream) {
      this.websocketStream.close();
    }
  }

  send(): void {
    this.websocketStream.s
  }



}
