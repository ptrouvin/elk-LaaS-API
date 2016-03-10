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
package org.CloudOps.laas;

import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class MessageJson {
    
    private HashMap<String, String> fields=new HashMap();
    
    public MessageJson(String msgString){
        if( msgString.startsWith("{") && msgString.endsWith("}") )
            msgString=msgString.substring(1, msgString.length()-1);
        String[] segments=msgString.split(",\"");
        
        for(String s: segments){
            if( s.startsWith("\"") && s.endsWith("\"") )
                s=s.substring(1, s.length()-1);
            String[] fs=s.split("\":\"");
            if( fs.length==2 )
                fields.put(fs[0], fs[1]);
            else
                Logger.getLogger("MessageJson").warn("Drop segment:  '"+s+"'");
        }
    }
    
    public String getField(String fieldName){
        return fields.get(fieldName);
    }
}
