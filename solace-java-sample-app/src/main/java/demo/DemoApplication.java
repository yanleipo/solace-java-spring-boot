/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package demo;

import java.util.concurrent.TimeUnit;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.spring.cloud.core.SolaceMessagingInfo;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.SpringJCSMPFactoryCloudFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageProducer;

import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPTransportException;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Component
    static class Runner implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(Runner.class);

        private final Topic topic = JCSMPFactory.onlyInstance().createTopic("tutorial/topic");
        private final Topic topic2 = JCSMPFactory.onlyInstance().createTopic("tutorial/topic2");
        private final Topic topic3 = JCSMPFactory.onlyInstance().createTopic("tutorial/topic3");

        @Autowired private SpringJCSMPFactory solaceFactory;

        // Examples of other beans that can be used together to generate a customized SpringJCSMPFactory
        @Autowired(required=false) private SpringJCSMPFactoryCloudFactory springJCSMPFactoryCloudFactory;
        @Autowired(required=false) private SolaceServiceCredentials solaceServiceCredentials;
        @Autowired(required=false) private JCSMPProperties jcsmpProperties;
        /* Also, for backwards compatibility:
            - As before, this is populated only in the specific scenario where the app is deployed in Cloud Foundry.*/
        @Autowired(required=false) private SolaceMessagingInfo solaceMessagingInfo;

        private DemoMessageConsumer msgConsumer = new DemoMessageConsumer();
        private DemoMessageConsumerReconnect msgConsumerReconnect = new DemoMessageConsumerReconnect();
        private DemoApplicationSessionEvt sessionEvt = new DemoApplicationSessionEvt();
        private DemoPublishEventHandler pubEventHandler = new DemoPublishEventHandler();

        public void run(String... strings) throws Exception {
            final String msg = "Hello World test";
            final JCSMPSession session = solaceFactory.createSession(solaceFactory.getDefaultContext(), sessionEvt);

            XMLMessageConsumer cons = session.getMessageConsumer(msgConsumerReconnect, (XMLMessageListener) null);

            session.addSubscription(topic);
            session.addSubscription(topic2);
            session.addSubscription(topic3);
            logger.info("Connected. Awaiting message...");
            cons.start();

            // Consumer session is now hooked up and running!

            /** Anonymous inner-class for handling publishing events */
            //XMLMessageProducer prod = session.getMessageProducer(pubEventHandler);
            // Publish-only session is now hooked up and running!

            //TextMessage jcsmpMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            //jcsmpMsg.setText(msg);
            //jcsmpMsg.setDeliveryMode(DeliveryMode.PERSISTENT);

            //logger.info("============= Sending " + msg);
            //prod.send(jcsmpMsg, topic);

            /**
            try {
                // block here until message received, and latch will flip
                msgConsumer.getLatch().await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("I was awoken while waiting");
            }
            **/
            
            try {
            	while(true) {
            		//Thread.sleep(1000);
                    try {
                        BytesXMLMessage m = cons.receive(10000);
                        if (m != null) {
                            logger.info("msg received");
                        } else {
                            //logger.info("No message received.");
                        }

                    } catch (JCSMPTransportException e)
                    {
                        logger.info("---> JCSMPTransportException " + e.getMessage());
                    } catch (JCSMPException e) 
                    {
                        logger.info("---> JCSMPException " + e.getMessage());
                    } catch (Exception e)
                    {
                        logger.info("---> exception" + e.getMessage());
                    }
            	}
            } catch (Exception e)
            {
            	logger.error("Exit");
            }
            // Close consumer
            cons.close();
            logger.info("Exiting.");
            session.closeSession();
        } 
    }
}
