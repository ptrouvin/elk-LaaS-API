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
 *
 *
 * ------------ DOC
 * This program is used to forward received updates to another node
 * must be configured in property
 * The updates= PUT|DELETE key=value
 */

package org.CloudOps.laas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TimerTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class ForwardUpdates extends TimerTask
{
    static String destinationURL=null;
    static ArrayList updates=new ArrayList();
    
    static Stats stat=new Stats("ForwardUpdates");
    
    static Logger log=Logger.getLogger("ForwardUpdates");
    
    public ForwardUpdates(String url){
         super();
         this.destinationURL=url;
    }
    
    @Override
    public void run() {

        stat.stat("run",1);
        
        while( ! updates.isEmpty()) {
            
            HttpClient client = HttpClientBuilder.create().build();
        
            ArrayList upd=(ArrayList) updates.remove(0);
            String httpCommand=(String) upd.get(0);
            String queryString=(String) upd.get(1);

            HttpUriRequest request=null;

            String url=destinationURL+"?Forward=1&"+queryString;
            
            switch(httpCommand){
                case "DELETE":
                    request = new HttpDelete(url);
                    break;
                case "PUT":
                    request = new HttpPut(url);
                    break;
                default:
                    log.error("Unsupported HTTP command '"+httpCommand+"' query("+queryString+")");
            }

            if( request!=null ){
                try {
                    HttpResponse response = client.execute(request);

                    int sts=response.getStatusLine().getStatusCode();
                    stat.stat(httpCommand+"."+Integer.toString(sts),1);
                    
                    log.info(httpCommand+"@"+url+" - Response Code : " + sts);

                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));

                    StringBuilder result = new StringBuilder();
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                } catch (IOException ex) {
                    stat.stat("ConnectionFailed",1);
                    log.fatal(ex);
                }
            }
        }
        
    }
    
    /*
     * addUpdate(
     * @param httpCommand
     * @param queryString
     */
    static public void addUpdate(String httpCommand, String queryString){
        ArrayList upd=new ArrayList();
        upd.add(httpCommand);
        upd.add(queryString);
        
        updates.add(upd);
    }
}
