/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.handler.authentication;


import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.authentication.step.AuthenticationStepHandler;
import org.wso2.carbon.identity.gateway.authentication.sequence.Sequence;
import org.wso2.carbon.identity.gateway.authentication.sequence.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.authentication.response.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;

public class AuthenticationHandler extends AbstractGatewayHandler {
    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {
        return true;
    }

    public GatewayHandlerResponse doAuthenticate(AuthenticationContext authenticationContext) throws
            AuthenticationHandlerException {

        AbstractSequenceBuildFactory abstractSequenceBuildFactory = getSequenceBuildFactory(authenticationContext);
        Sequence sequence = abstractSequenceBuildFactory.buildSequence(authenticationContext);
        authenticationContext.setSequence(sequence);

        AuthenticationStepHandler authenticationStepHandler =  getStepHandler(authenticationContext);
        AuthenticationResponse authenticationResponse = authenticationStepHandler.handleStepAuthentication(authenticationContext);

        return buildFrameworkHandlerResponse(authenticationResponse);
    }

    @Override
    public String getName() {
        return null;
    }

    private GatewayHandlerResponse buildFrameworkHandlerResponse(AuthenticationResponse handlerResponse) {
        GatewayHandlerResponse gatewayHandlerResponse = null;
        if (AuthenticationResponse.Status.AUTHENTICATED.equals(handlerResponse.status)) {
            gatewayHandlerResponse = new GatewayHandlerResponse();
        } else if (AuthenticationResponse.Status.INCOMPLETE.equals(handlerResponse.status)) {
            gatewayHandlerResponse = new GatewayHandlerResponse(GatewayHandlerResponse.Status.REDIRECT, handlerResponse.getGatewayResponseBuilder());
        }
        return gatewayHandlerResponse;
    }

    public AbstractSequenceBuildFactory getSequenceBuildFactory(AuthenticationContext authenticationContext) {
        return (AbstractSequenceBuildFactory) getHandler(GatewayServiceHolder.getInstance().getSequenceBuildFactories(), authenticationContext);
    }

    public AuthenticationStepHandler getStepHandler(AuthenticationContext authenticationContext) {
        return (AuthenticationStepHandler) getHandler(GatewayServiceHolder.getInstance().getAuthenticationStepHandlers(), authenticationContext);
    }
}