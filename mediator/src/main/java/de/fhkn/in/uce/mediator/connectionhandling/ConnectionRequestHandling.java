/*
 * Copyright (c) 2012 Alexander Diener,
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.mediator.connectionhandling;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.mediator.techniqueregistry.MessageHandlerRegistry;
import de.fhkn.in.uce.mediator.techniqueregistry.MessageHandlerRegistryImpl;
import de.fhkn.in.uce.mediator.util.MediatorUtil;
import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;
import de.fhkn.in.uce.plugininterface.message.NATTraversalTechniqueAttribute;
import de.fhkn.in.uce.stun.message.Message;

/**
 * This {@link HandleMessage} for connection requests chooses the concrete
 * {@link HandleMessage} implementation by examining the
 * {@link NATTraversalTechniqueAttribute} which is provided by the connection
 * request message. The {@link MessageHandlerRegistry} delivers the used
 * implementation for the concrete {@link HandleMessage}.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
public final class ConnectionRequestHandling implements HandleMessage {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionRequestHandling.class);
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final MediatorUtil mediatorUtil;

    /**
     * Creates a {@link ConnectionRequestHandling} object.
     */
    public ConnectionRequestHandling() {
        this.messageHandlerRegistry = MessageHandlerRegistryImpl.getInstance();
        this.mediatorUtil = MediatorUtil.INSTANCE;
    }

    @Override
    public void handleMessage(final Message connectionRequestMessage, final Socket controlConnection) throws Exception {
        this.checkForRequiredTravTechAttribute(connectionRequestMessage);
        final NATTraversalTechniqueAttribute usedTravTech = connectionRequestMessage
                .getAttribute(NATTraversalTechniqueAttribute.class);
        final HandleMessage connectionRequestHandler = this.messageHandlerRegistry
                .getConnectionRequestHandlerByEncoding(usedTravTech.getEncoded());
        connectionRequestHandler.handleMessage(connectionRequestMessage, controlConnection);
    }

    private void checkForRequiredTravTechAttribute(final Message message) throws Exception {
        try {
            this.mediatorUtil.checkForAttribute(message, NATTraversalTechniqueAttribute.class);
        } catch (final Exception e) {
            final String errorMessage = "Required NATTraversalTechniqueAttribute is not provided, can not decide which handler to used"; //$NON-NLS-1$
            logger.error(errorMessage);
            throw new Exception(errorMessage, e);
        }
    }

    @Override
    public NATTraversalTechniqueAttribute getAttributeForTraversalTechnique() {
        return new NATTraversalTechniqueAttribute(Integer.MAX_VALUE);
    }
}
