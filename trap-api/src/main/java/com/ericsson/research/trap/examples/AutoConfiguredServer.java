package com.ericsson.research.trap.examples;

/*
 * ##_BEGIN_LICENSE_##
 * Transport Abstraction Package (trap)
 * ----------
 * Copyright (C) 2014 Ericsson AB
 * ----------
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Ericsson AB nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ##_END_LICENSE_##
 */



import java.util.HashSet;

import com.ericsson.research.trap.TrapEndpoint;
import com.ericsson.research.trap.TrapException;
import com.ericsson.research.trap.TrapFactory;
import com.ericsson.research.trap.TrapListener;
import com.ericsson.research.trap.delegates.OnAccept;
import com.ericsson.research.trap.delegates.OnClose;
import com.ericsson.research.trap.delegates.OnData;
import com.ericsson.research.trap.utils.StringUtil;

/**
 * Shows how to use automatic configuration to simplify client connections using the new Trap 1.2 API. This example
 * assumes knowledge of {@link EchoServer}. As such, all comments have been stripped, save for the ones that talk about
 * the configuration
 * <p>
 * {@sample ../../../src/main/java/com/ericsson/research/trap/examples/AutoConfiguredServer.java sample}
 * 
 * @author Vladimir Katardjiev
 * @since 1.2
 */
//BEGIN_INCLUDE(sample)
public class AutoConfiguredServer implements OnAccept, OnData, OnClose
{
    static TrapListener          echoServer;
    static final HashSet<TrapEndpoint> clients      = new HashSet<TrapEndpoint>();
    static String                clientConfig = null;
    
    public static void main(String[] args) throws Throwable
    {
        // Create a new listener
        echoServer = TrapFactory.createListener();
        
        /*
         * We will use the semi-automatic configuration. By default, all transports will listen to 0.0.0.0 as of Trap 1.2
         * so all we need to do is tell Trap which host to use.
         */
        
        echoServer.listen(new AutoConfiguredServer());
        
        /*
         * This is the only place we need to configure. Simply tell the configuration which hostname to use.
         * For this example, we use fuf.me -- a hostname that resolves to localhost.
         */
        
        clientConfig = echoServer.getClientConfiguration("fuf.me");
        System.out.println("New clients should connect to the following configuration:");
        System.out.println(clientConfig);
    }
    
    public void incomingTrapConnection(TrapEndpoint endpoint, TrapListener listener, Object context)
    {
        // Endpoints received by this method are NOT strongly referenced, and may be GC'd. Thus, we must retain
        clients.add(endpoint);
        
        // We also want feedback from the endpoint.
        endpoint.setDelegate(this, true);
    }
    
    public void trapData(byte[] data, int channel, TrapEndpoint endpoint, Object context)
    {
        // Log!
        System.out.println("Echo Server Got message: [" + StringUtil.toUtfString(data) + "] of length " + data.length);
        
        try
        {
            // Echo the data back with the same parameters.
            endpoint.send(data, channel, false);
        }
        catch (TrapException e)
        {
            e.printStackTrace();
        }
    }
    
    public void trapClose(TrapEndpoint endpoint, Object context)
    {
        // Remove the strong reference to allow garbage collection
        clients.remove(endpoint);
    }
    
}
//END_INCLUDE(sample)
