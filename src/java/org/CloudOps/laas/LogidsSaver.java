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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TimerTask;
import org.apache.log4j.Logger;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class LogidsSaver extends TimerTask
{
    static Logids lids=new Logids();
    static String jsonFilename=null;
    
    public LogidsSaver(String jsonFilename){
         super();
         this.jsonFilename=jsonFilename;
    }
    
    @Override
    public void run() {
        
        if(lids.isModified()){
            try {
                File outf=new File(jsonFilename);
                PrintWriter out = new PrintWriter(outf);
                              
                out.print("[");
                Boolean first=true;
                for(Iterator it=lids.getkeys(); it.hasNext();){
                    if( first )
                        first=false;
                    else
                        out.print(",");
                    String lid=it.next().toString();
                    Logid l=lids.logid(lid);
                    out.print(l.toJSON());
                }
                out.print("]");
                lids.isSaved();
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LogidsSaver.class.getName()).fatal(ex);
            }
        }
    }
}
