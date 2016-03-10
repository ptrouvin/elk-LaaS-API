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

import java.util.Date;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class Stat {
    private Double value=0.0;
    private Date createdAt=new Date();
    
    public Stat(Double v){
        value=v;
    }
    public Stat(){
        
    }
    
    public void reset(){
        reset(0.0);
    }
    public void reset(Double v){
        value=v;
    }
    
    public void sum(Double v){
        value+=v;
    }
    
    @Override
    public String toString(){
        long now=new Date().getTime()-createdAt.getTime();
        if( now==0 )
            return Double.toString(value);
        return value+" "+(value/now/1000.0)+"/s";
    }
}
