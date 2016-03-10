/*
 * Copyright 2015 Pascal TROUVIN <pascal.trouvin at o4s.fr>.
 *
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
 */
package org.CloudOps.laas.mq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.CloudOps.laas.Logid;
import org.CloudOps.laas.Logids;
import org.CloudOps.laas.MessageJson;
import org.CloudOps.laas.Params;
import org.CloudOps.laas.Stats;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Web application lifecycle listener.
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class Kafka implements Runnable {
    
    static final Logger log=Logger.getLogger("LaaSKafka");
    static Stats stat=new Stats("Kafka");

    @Override
    public void run() {
        
        String clientName="LaaS-API-Consumer";
        String topicIn="logstashOut";
        String topicOut="logstash";
        
        Logids lids=new Logids();
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        Properties propsConsumer = new Properties();
        try {
            // load a properties file
            propsConsumer.load(classLoader.getResourceAsStream("zookeeper.properties"));
        } catch (FileNotFoundException ex) {
            log.fatal(ex);
            return;
        } catch (IOException ex) {
            log.fatal(ex);
            return;
        }
        Params p;
        try {
            p = new Params();
        } catch (Exception ex) {
            log.error(ex);
            return;
        }
        p.merge(propsConsumer, "zookeeper.");
        

        Properties propsProducer = new Properties();
        try {
            // load a properties file
            propsProducer.load(classLoader.getResourceAsStream("kafka.properties"));
        } catch (FileNotFoundException ex) {
            log.fatal(ex);
            return;
        } catch (IOException ex) {
            log.fatal(ex);
            return;
        }
        p.merge(propsProducer, "kafka.");

        ConsumerConnector consumer=null;
        Producer producer=null;
        
        int retries=5;
        
        while(!Thread.currentThread().isInterrupted()){
            try{
                log.info("started Consumer clientName("+clientName+") topic("+topicIn+")");

                ConsumerConfig cfg= new ConsumerConfig(propsConsumer);
                consumer = Consumer.createJavaConsumerConnector(cfg);
                Map<String, Integer> topicCount = new HashMap<>();
                topicCount.put(topicIn, 1);

                log.info("started producer topic("+topicOut+")");
                producer = new Producer<>(new ProducerConfig(propsProducer));
                
                stat.stat("started", 1.0);
                log.info("started listening on topic("+topicOut+")");

                Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
                List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(topicIn);
                for (final KafkaStream stream : streams) {
                    ConsumerIterator<byte[], byte[]> it = stream.iterator();
                    while (it.hasNext()) {
                        String str=new String(it.next().message());
                        MessageJson msg=new MessageJson(str);
                        
                        stat.stat("MessageReceived" , 1);
                        stat.stat("MessageReceivedSize" , str.length());

                        int lidFound=0;
                        
                        for(Iterator it2=lids.getkeys(); it2.hasNext();){
                            String lid=(String) it2.next();
                            Logid logid=lids.logid(lid);
                            if( logid.match(msg) ){
                                // at least one rule list had matched
                                log.debug("LID="+logid.getLid());
                                stat.stat("MessageSent",1);
                                lidFound++;
                                KeyedMessage<Integer, String> data = new KeyedMessage<>(topicOut, "LID="+logid.getLid()+" "+str);
                                producer.send(data);
                            }
                        }
                        if( lidFound==0 )
                            stat.stat("MessageDiscarded", 1);
                    }
                }
            } catch (IllegalStateException e) {
                log.fatal(e);
                retries--;
                log.error("Unable to start KAFKA retries("+retries+")");
                if( retries>=0 )
                    continue;
                // leave, so stop thread
                break;
            } catch (NullPointerException e) {
                log.fatal(e.getMessage());
                break;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException ex) {
                log.log(Level.ERROR, null, ex);
                break;
            }
        }
        log.info("Closing KAFKA");
        if(consumer!=null)
            consumer.shutdown();
        if(producer!=null)
            producer.close();
        
        stat.stat("Close",1);
      
    }
    
}
