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

import java.util.ArrayList;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class Logid {
    
    // rules=[ [rule1.1 & rule1.2] | [rule2.1 & rule2.2] | ... ]
    private ArrayList<ArrayList<Rule>> rules=new ArrayList();
    private String lid=null;
    
    // Constructor
    public Logid(){
        
    }
    public Logid(String lid){
        this.lid=lid;
    }
    public Logid(String lid, String[] rules) throws Exception{
        this.lid=lid;
        this.add(rules);
    }
    
    public final void add(String[] rulesString) throws Exception{
        ArrayList<Rule> rs=new ArrayList<>();
        for(String r: rulesString){
            Rule rule=new Rule(r);
            rs.add(rule);
        }
        this.rules.add(rs);
    }
    
    public String getLid(){
        return lid;
    }
    public String get(){
        return lid;
    }
    
    public void setLid(String lid){
        set(lid);
    }
    public void set(String lid){
        this.lid=lid;
    }
    
    /**
     * match rules against json message
     * @param msg
     * @return the ruleList if one match
     */
    public Boolean match(MessageJson msg){
        for(ArrayList<Rule> rs:rules){
            Boolean allRules=true;
            for(Rule r: rs)
                if( ! r.match(msg) ){
                    allRules=false;
                    break;
                }
            if( allRules )
                return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        return toJSON("");
    }
    
    public String toJSON(){
        return toJSON("");
    }
    public String toJSON(String fieldsToAdd){
        StringBuilder st = new StringBuilder("{");
        st.append("\"lid\":\"").append(lid).append("\",\"rules\":[");
        Boolean first1=true;
        for (ArrayList<Rule> rs : rules) {
            if( first1 )
                first1=false;
            else
                st.append(",");
            st.append("[");
            Boolean first=true;
            for(Rule r: rs){
                if( first )
                    first=false;
                else
                    st.append(",");
                st.append("\"").append(r.toString()).append("\"");
            }
            st.append("]");
        }
        st.append("]");
        
        if( fieldsToAdd.length()>0 )
            st.append(",").append(fieldsToAdd);
        
        st.append("}");
        
        return st.toString();
    }
    
}
