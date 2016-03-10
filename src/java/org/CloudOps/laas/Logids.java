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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class Logids {
    
    static ConcurrentHashMap<String, Logid> logid=new ConcurrentHashMap<>();
    
    static final Logger log=Logger.getLogger("Logids");
    
    static Boolean massiveLoad=false;
    static Boolean updated=false;
    
    // Constructor
    public Logids(){
        log.info("Logids started");
    }
    
    /**
     * create logids and assign a rule list
     * @param lid
     * @param rulesString
     * @return 
     */
    public Boolean create(String lid, ArrayList<String> rulesString) throws Exception{
        Logid l=logid.get(lid);
        if( l==null )
            l=new Logid(lid,rulesString.toArray(new String[rulesString.size()]));
        else
            l.add(rulesString.toArray(new String[rulesString.size()]));
        logid.put(lid, l);
        
        if( ! massiveLoad ){
            StringBuilder str=new StringBuilder();
            Boolean first=true;
            for(String rs: rulesString){
                if( first )
                    first=false;
                else
                    str.append(" AND ");
                str.append(rs);
            }
            log.log(Level.INFO, "LogId.create OK lid("+lid+") "+str);
            updated=true;
        }
        return true;
    }
    
    public Boolean delete(String lid){
        Boolean isSuccess=false;
        
        Logid l=logid.get(lid);
        if( l==null ){
            log.warn("LogId.delete failed lid("+lid+") does not exist");
            isSuccess=false;
        } else {
            logid.remove(lid);
            
            log.info("LogId.delete lid("+lid+") successful");
            isSuccess=true;
            
            updated=true;
        }
        
        return isSuccess;
    }
    
    public Logid logid(String lid){
        return logid.get(lid);
    }
    
    public Integer count(){
        return logid.size();
    }
    public Iterator getkeys(){
        return logid.keySet().iterator();
    }
    
    /**
     * loadFromFile : reads JSON from given filename
     * @param filename
     * @throws FileNotFoundException
     * @throws IOException 
     * 
     * 
     * [{"lid":"RANDOMDATA","rules":[["host=appli.axa.com","uri^=/monappli/path"],["vmdid=1.2.3.4.aaaaabc"]]}]
     */
    public void loadFromFile(String filename) throws FileNotFoundException, IOException, Exception{
        File json=new File(filename);
        massiveLoad=true;
        if( json.exists() ){
            log.info("Loading data from '"+filename+"'");
            
            long n=0; // number of character read
            int nObjectFound=0; // number of objects found
            int nObjectLoaded=0;
            StringBuilder sb = null;
            
            // [{"lid":"88","rules":[["vid=88"]]},...
            Pattern patAllFields=Pattern.compile("\\{\"lid\":\"([^\"]+)\",\"rules\":\\[([^}]*)\\]\\}");
            
            try(BufferedReader br = new BufferedReader( new FileReader(json))) {
                Boolean atStart=true;
                while(true){
                    char c=' ';
                    int ci;
                
                    while(true){
                        ci=br.read();
                        if( ci==-1 ) // END-OF-FILE
                            break;
                        n++;
                        c=(char)ci;
                        if( atStart ){
                            atStart=false;
                            if( c=='[' )
                                continue;
                        }
                        break;
                    }
                    if( ci==-1 ) // END-OF-FILE
                        break;
                    
                    // {"lid":"88","sid":"88","vid":"88"}
                    switch(c){
                        case '\t':
                        case '\r':
                        case '\n':
                            break;
                        case '{':
                            sb = new StringBuilder("{");
                            break;
                        case '}':
                            if( sb==null ){
                                log.warn("Invalid JSON format at "+n+" '"+c+"'");
                                massiveLoad=false;
                                return;
                            }
                            nObjectFound++;
                            sb.append("}");
                            Matcher maf=patAllFields.matcher(sb.toString());
                            if( ! maf.matches() ){
                                log.warn("Invalid JSON format at "+n+" '"+c+"'");
                                massiveLoad=false;
                                return;
                            }
                            String lid=maf.group(1);
                            String rs=maf.group(2);
                            // removing leading [ and trailing ]
                            if( rs.startsWith("[") && rs.endsWith("]")  ){
                                rs=rs.substring(1,rs.length()-1);
                            }
                            
                            for(String r: rs.split("\\],\\[")){
                                
                                if( r.startsWith("\"") && r.endsWith("\"")  ){
                                    r=r.substring(1,r.length()-1);
                                }
                                String[] rules= r.split("\",\"");
                                create(lid, new ArrayList<>(Arrays.asList(rules)));
                                nObjectLoaded++;
                            }
                            
                            sb=null;
                            break;
                        default:
                            if( sb==null ){
                                if( c!=',' && c!=']' ){
                                    // ',' as logid fields separator
                                    // the last ']' at the end of the file
                                    log.warn("Invalid JSON format at "+n+" '"+c+"'");
                                    massiveLoad=false;
                                    return;
                                }
                            } else {
                                sb.append(c);
                            }
                            break;
                    } 
                }
            }
            log.info("loadFromFile objects Loaded("+nObjectLoaded+") Found("+nObjectFound+")");
        } else {
            log.info("JSON file '"+filename+"' not found");
        }
        massiveLoad=false;
    }
    
    public Boolean isModified(){
        return updated;
    }
    public void isSaved(){
        updated=false;
    }
}
