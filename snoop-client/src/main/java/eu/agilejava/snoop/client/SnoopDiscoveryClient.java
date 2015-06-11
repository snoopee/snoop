/*
 * The MIT License
 *
 * Copyright 2015 Ivar Grimstad (ivar.grimstad@gmail.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package eu.agilejava.snoop.client;

import java.util.Optional;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 *
 * @author Ivar Grimstad (ivar.grimstad@gmail.com)
 */
public class SnoopDiscoveryClient {
   
   private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoop");
   private static final String DEFAULT_BASE_URI = "ws://localhost:8080/snoop-service/";
   
   private final String applicationName;
   private final String serviceUrl;
   
   @EJB
   private SnoopApplicationClient snoopClient;
   
   static final class Builder {

      private final String applicationName;
      private String serviceUrl = DEFAULT_BASE_URI;
      
      Builder(final String applicationName) {
         this.applicationName = applicationName;
      }
      
      Builder serviceUrl(final String serviceUrl) {
         this.serviceUrl = serviceUrl;
         return this;
      }
      
      SnoopDiscoveryClient build() {
         return new SnoopDiscoveryClient(this);
      }      
   }
   
   private SnoopDiscoveryClient(final Builder builder) {
      this.applicationName = builder.applicationName;
      this.serviceUrl = builder.serviceUrl;
      LOGGER.info(() -> "client created for " + applicationName);      
   }
   
   public WebTarget getServiceRoot() {
      
      SnoopConfig snoopConfig = getConfigFromSnoop();
      LOGGER.fine(() -> "looking up service for " + applicationName);
      return ClientBuilder.newClient()
              .target(snoopConfig.getApplicationHome())
              .path(snoopConfig.getApplicationServiceRoot());
   }
   
   public Optional<Response> simpleGet(String resourcePath) {
      
      return serviceUp() ? Optional.of(getServiceRoot()
              .path(resourcePath)
              .request()
              .get()) : Optional.empty();
   }
   
   private SnoopConfig getConfigFromSnoop() throws SnoopServiceUnavailableException {
      
      Response response = ClientBuilder.newClient()
              .target(serviceUrl)
              .path("api")
              .path("lookup")
              .path(applicationName)
              .request()
              .get();
      
      if(response.getStatus() == 200) {
         return response.readEntity(SnoopConfig.class);
      } else {
         throw new SnoopServiceUnavailableException();
      }
   }
   
   public boolean serviceUp() {
      
      // todo implement
      return true;
   } 
}