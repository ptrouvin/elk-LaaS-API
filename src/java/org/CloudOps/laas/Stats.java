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
import java.util.Iterator;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class Stats {
    static final HashMap<String,HashMap<String,Stat>> stats=new HashMap<>();
    
    private String module=null;
    
    public Stats(){
        
    }
    public Stats(String module){
        this.module=module;
    }

    public void reset(String name, int v){
        reset(name, new Double(v));
    }
    public void reset(String name, Double v){
        Stat s=get(name);
        if( s==null )
            stat(module, name, v);
        else
            s.reset(v);
    }
    
    public void stat(String name, Integer val){
        stat(name,new Double(val));
    }
    public void stat(String name, Double val){
        stat(module,name,val);
    }
    
    public void stat(String module, String name, Double val){
        HashMap ss=stats.get(module);
        if( ss==null )
            ss=new HashMap();
        Stat ov=(Stat) ss.get(name);
        if( ov==null )
            ov=new Stat(val);
        else
            ov.sum(val);
        ss.put(name, ov);
        stats.put(module, ss);
    }
    
    public Iterator getIterator(){
        if( module==null )
            return stats.keySet().iterator();
        return getIterator(module);
    }
    public Iterator getIterator(String module){
        HashMap ss=stats.get(module);
        if( ss==null )
            return null;
        return ss.keySet().iterator();
    }
    private HashMap _get(String module){
        return stats.get(module);
    }
    public Stat get(String module, String name){
        HashMap ss=_get(module);
        if( ss==null )
            return null;
        return (Stat) ss.get(name);
    }
    public Stat get(String name){
        HashMap ss=_get(module);
        if( ss==null )
            return null;
        return (Stat) ss.get(name);
    }
}
