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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import javax.servlet.ServletConfig;
import org.apache.log4j.Logger;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class Params {
    static Properties laasProperties=new Properties();
    static String propertiesFilename=null;
    
    static long lastUpdateTm=0L;

    public Params(ServletConfig config){
        //
        // CMaaS.properties
        //
        propertiesFilename=makeContextParameterToFilename(config, "LaaS.properties");
        if( propertiesFilename!=null ){
            this.load(propertiesFilename);
                      
        } else {
            Logger.getLogger(Params.class.getName()).info("No LaaS.properties parameter defined");
        }
        
    }
    public Params() throws Exception{
        if( laasProperties==null )
            throw new Exception("Params not initialized, MUST be called before with ServletConfig");
    }
    
    public Boolean reloadIfModified(){
        if( ! checkIfModified() )
            return false;
        load(propertiesFilename);
        return true;
    }
    public Boolean checkIfModified(){
        if( lastUpdateTm!=0 ){
            File f=new File(propertiesFilename);
            if( f.lastModified()>lastUpdateTm )
                return true;
        }
        return false;
    }
    private String makeContextParameterToFilename(ServletConfig config, String contextParameterName){
        String filename=config.getServletContext().getInitParameter(contextParameterName);
        if( filename!=null ){
            if( ! filename.startsWith("/") ){
                String hd =System.getProperty("user.dir");
                if( ! hd.endsWith("/") )
                    hd+="/";
                filename = hd+filename;
            }
        }
        return filename;
    }
    
    
    public void load(String filename){
        try {
            try (FileInputStream input = new FileInputStream(filename)) {
                laasProperties.load(input);
                StringBuilder st=new StringBuilder();
                for(String k : laasProperties.stringPropertyNames())
                    st.append(k).append("=").append(laasProperties.get(k)).append(" ");
                Logger.getLogger(this.getClass().getName()).info("LaaS Properties loaded from '"+filename+"': "+st.toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).error(ex);
        }
    }
    
    public String getProperty(String key){
        return laasProperties.getProperty(key);
    }
    
    public Properties merge(Properties props, String matching){
        
        for(Iterator it=laasProperties.keySet().iterator(); it.hasNext();){
            String k=(String) it.next();
            if( k.startsWith(matching) ){
                // remove the matching prefix
                props.put(k.substring(matching.length()), laasProperties.getProperty(k));
            }
                
        }
        
        return props;
    }
}
