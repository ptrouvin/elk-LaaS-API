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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class Rule {
    
    private String field=null;
    private String value=null;
    public enum Comparator {
        Equals, StartsWith, EndsWith, EqualsIgnoreCase, Regexp
    }
    private Comparator cmp=Comparator.Equals;
    
    static Pattern patRule=Pattern.compile("(.*?)(=i|\\^=|=\\$|~|=)(.*)");
        
    // Constructor
    public Rule(String rule) throws Exception{
        Matcher m=patRule.matcher(rule);
        if( ! m.matches() ){
            throw new Exception("Invalid rule syntax");
        }
        this.field=m.group(1);
        switch(m.group(2)){
            case "=":
                this.cmp=Comparator.Equals;
                break;
            case "=i":
                this.cmp=Comparator.EqualsIgnoreCase;
                break;
            case "^=":
                this.cmp=Comparator.StartsWith;
                break;
            case "=$":
                this.cmp=Comparator.EndsWith;
                break;
            case "~":
                this.cmp=Comparator.Regexp;
                break;
        }
        this.value=m.group(3);
    }
    public Rule(String field, String value, Comparator cmp){
        this.field=field;
        this.value=value;
        this.cmp=cmp;
    }
    
    /**
     * check the compliancy of the given string to rule pattern
     * @param str
     * @return true if string is a rule, otherwise false
     */
    public static Boolean check(String str){
        return patRule.matcher(str).matches();
    }
    
    /**
     * match: test if the given message matches the rule
     * @param msg
     * @return true if match, false otherwise
     */
    public Boolean match(MessageJson msg){
        String fvalue=msg.getField(field);
        if( fvalue!=null ){
            switch(cmp){
                case Equals:
                    return fvalue.equals(value);
                    
                case StartsWith:
                    return fvalue.startsWith(value);
                    
                case EndsWith:
                    return fvalue.endsWith(value);
                    
                case EqualsIgnoreCase:
                    return fvalue.equalsIgnoreCase(value);
                    
                case Regexp:
                    return Pattern.matches(value, fvalue);
                    
            }
        }
        return false;
    }
    
    @Override
    public String toString(){
        StringBuilder st=new StringBuilder();
        
        st.append(field);
        switch(cmp){
            case Equals:
                st.append("=");
                break;

            case StartsWith:
                st.append("^=");
                break;

            case EndsWith:
                st.append("=$");
                break;

            case EqualsIgnoreCase:
                st.append("=i");
                break;

            case Regexp:
                st.append("~");
                break;

        }
        
        st.append(value);
        
        return st.toString();
    }
}
