package com.cloudhopper.jetty;

/*
 * #%L
 * ch-jetty
 * %%
 * Copyright (C) 2012 - 2013 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.servlet.Servlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class JettyHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(JettyHttpServer.class);
    
    private HttpServerConfiguration configuration;
    // internal instance of the Jetty http server
    private Server server;
    private HandlerCollection handlers;
    private ContextHandlerCollection contexts;
    private ServletContextHandler rootServletContext;
    
    public JettyHttpServer(HttpServerConfiguration configuration, Server server, HandlerCollection handlers, ContextHandlerCollection contexts, ServletContextHandler rootServletContext) {
        this.configuration = configuration;
        this.server = server;
        this.handlers = handlers;
        this.contexts = contexts;
        this.rootServletContext = rootServletContext;
    }
    
    Server getServer() {
        return this.server;
    }
    
    public void join() throws InterruptedException {
        this.server.join();
    }
    
    /**
     * Starts the HTTP server.  If an exception is thrown during startup, Jetty
     * usually still runs, but this method will catch that and make sure it's
     * shutdown before re-throwing the exception.
     * @throws Exception Thrown if there is an error during start
     */
    public void start() throws Exception {
        // verify server is NOT null
        if (server == null) {
            throw new NullPointerException("Internal server instance was null, server not configured perhaps?");
        }

        logger.info("HttpServer [{}] version [{}]", configuration.safeGetName(), com.cloudhopper.jetty.Version.getLongVersion());
        logger.info("HttpServer [{}] on [{}] starting...", configuration.safeGetName(), configuration.getPortString());

        // try to start jetty server -- if it fails, it'll actually keep running
        // so if an exception occurs, we'll make to stop it afterwards and rethrow the exception
        try {
            server.start();
        } catch (Exception e) {
            // make sure we stop the server
            try { this.stop(); } catch (Exception ex) { }
            throw e;
        }

        logger.info("HttpServer [{}] on [{}] started", configuration.safeGetName(), configuration.getPortString());
    }
    
    /**
     * Stops the HTTP server.
     * @throws Exception Thrown if there is an error during stop
     */
    public void stop() throws Exception {
        // verify server isn't null
        if (server == null) {
            throw new NullPointerException("Internal server instance was null, server already stopped perhaps?");
        }
        
        logger.info("HttpServer [{}] on [{}] stopping...", configuration.safeGetName(), configuration.getPortString());

        server.stop();

        logger.info("HttpServer [{}] on [{}] stopped", configuration.safeGetName(), configuration.getPortString());
    }
    
    /**
     * Adds a directory for resources such as files containing html, images, etc.
     * to be served up automatically be this server.<br>
     * CAUTION: A resource base can only be added BEFORE the server is started.
     * @param resourceBaseDir The directory containing resources (basically the
     *      web root)
     */
    public void addResourceBase(String resourceBaseDir) {
        // handles resources like images, files, etc..
        logger.info("HttpServer [{}] adding resource base dir [{}]", configuration.safeGetName(), resourceBaseDir);
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(resourceBaseDir);
        handlers.addHandler(resourceHandler);
    }
    
    /**
     * Adds a servlet to this server.
     * @param servlet The servlet to add
     * @param uri The uri mapping (relative to root context /) to trigger this
     *      servlet to be executed. Wildcards are permitted.
     */
    public void addServlet(Servlet servlet, String uri) {
        // create a holder and then add it to the mapping
        ServletHolder servletHolder = new ServletHolder(servlet);
        rootServletContext.addServlet(servletHolder, uri);
    }

}
