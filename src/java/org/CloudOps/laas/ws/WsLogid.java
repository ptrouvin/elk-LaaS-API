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
package org.CloudOps.laas.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.CloudOps.laas.ForwardUpdates;
import org.CloudOps.laas.Logid;
import org.CloudOps.laas.Logids;
import org.CloudOps.laas.LogidsSaver;
import org.CloudOps.laas.MessageJson;
import org.CloudOps.laas.Params;
import org.CloudOps.laas.Rule;
import org.CloudOps.laas.Stats;
import org.CloudOps.laas.mq.Kafka;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class WsLogid extends HttpServlet {
    
    static Logids lids=new Logids();
    static String jsonFilename=null;
    static String propertiesFilename=null;
    static Params laasProperties=null;
    
    static Pattern noAuthenticationFor=null;
    
    static Timer timer = new Timer();
    static TimerTask lidsSaveTask = null; 
    static TimerTask forwardUpdatesTask = null; 
    
    static Logger log=Logger.getLogger("WsLogid");
    
    static Thread kafka=null;
    
    static Stats stat=new Stats("WsLogid");
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        laasProperties=new Params(config);
        
        jsonFilename=makeContextParameterToFilename(config, "loadDataFrom");
        if( jsonFilename!=null ){
            try {
                lids.loadFromFile(jsonFilename);
            } catch (IOException ex) {
                log.log(Level.FATAL, null, ex);
            } catch (Exception ex) {
                log.log(Level.FATAL, null, ex);
            }
            
            lidsSaveTask=new LogidsSaver(jsonFilename);
            timer.schedule(lidsSaveTask, 1000, 60000);
                       
        } else {
            log.log(Level.INFO, "No loadDataFrom parameter defined");
        }
        
        stat.reset("LaaSid", lids.count());

        //
        // LaaS.properties
        //
        // forward-updates-to
        String forwardURL=laasProperties.getProperty("forward-updates-to");
        if( forwardURL!=null ){
            log.log(Level.INFO, "Forward Updates to "+forwardURL);
            forwardUpdatesTask=new ForwardUpdates(forwardURL);
            timer.schedule(forwardUpdatesTask, 1000, 60000);
        } else {
            log.log(Level.INFO, "Forward Updates DISABLED, missing parameter ForwardURL in "+propertiesFilename);

        }

        // no-authentication-for
        String noAuthParam=laasProperties.getProperty("no-authentication-for");
        if( noAuthParam!=null ){
            log.log(Level.INFO, "APIKEY Authentication: Bypass authentication for '"+noAuthParam+"'");
            noAuthenticationFor=Pattern.compile(noAuthParam);
        } else {
            log.log(Level.INFO, "APIKEY Authentication: APIKEY required for everyone");
        }

        
        if( kafka!=null ){
            log.info("A kafka process is already running");
        } else {
            kafka=new Thread(new Kafka());
            kafka.start();
        }
        
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        stat.stat("GET", 1);
        
        if( ! checkIfLogged(request, response) )
            return;
        
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String action=request.getParameter("action");
            if( action!=null ){
                switch (action) {
                    case "count":
                        out.print("{\"count\":"+lids.count()+"}");
                        break;
                    case "getall":
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
                        break;
                    default:
                        response.sendError(400, "Unknown action command");
                        break;
                }
            }
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        stat.stat("POST", 1);
        
        if( ! checkIfLogged(request, response) )
            return;
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                out.println("<"+line);
                MessageJson msg=new MessageJson(line);
                for(Iterator it=lids.getkeys(); it.hasNext();){
                    String lid=(String) it.next();
                    Logid logid=lids.logid(lid);
                    if( logid.match(msg) ){
                        // at least one rule list had matched
                        out.println(">"+logid.toString());
                    }
                }
            }
        }
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        stat.stat("PUT", 1);
        
         if( ! checkIfLogged(request, response) )
            return;
        
        String lid=request.getParameter("logid");
        if( lid==null )
            lid=request.getParameter("lid");
        Boolean isForward=request.getParameter("Forward")!=null;

        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if( lid==null  ){
                out.print("{\"status\":\"FAIL\",\"comment\":\"lid field is missing\"}");
            } else {
                ArrayList<String> rulesString=new ArrayList<>();
                for(Enumeration e=request.getParameterNames(); e.hasMoreElements(); ){
                    String pn=(String)e.nextElement();
                    if( pn.equals("lid") )
                        continue;
                    String pv=request.getParameter(pn);
                    if( ! Rule.check(pv) )
                        continue;
                    
                    // the parameter value match the rule syntax
                    rulesString.add(pv);
                }
                
                Logid l=null;
                try {
                    if( lids.create(lid, rulesString) ){
                        l=lids.logid(lid);
                        out.print(l.toJSON("\"status\":\"OK\",\"comment\":\"CREATED\""));

                        stat.reset("LaaSid", lids.count());
                    
                        StringBuilder str=new StringBuilder();
                        for(int i=0; i<rulesString.size(); i++){
                            if( i>0 )
                                str.append("&");
                            str.append("rule").append(i).append("=").append(hexaCodeSpecialChars(rulesString.get(i)));
                        }
                        
                        if( ! isForward )
                            ForwardUpdates.addUpdate("PUT", str.toString());
                    }
                } catch (Exception ex) {
                    log.fatal(ex);
                }
                
                if( l==null ) {
                    out.print("{\"lid\":\""+lid+"\","
                            + "\"status\":\"FAIL\","
                            + "\"comment\":\"Failed to CREATE\"}");
                } else {
                    out.print(l.toJSON());
                }
            }
        }
    }
    
    private String hexaCodeSpecialChars(String str){
        StringBuilder s=new StringBuilder();
        
        for(int i=0; i<str.length(); i++){
            char c=str.charAt(i);
            if( (c>='A' && c<='Z') || (c>='a' && c<='z') || (c>='0' && c<='9')  )
                s.append(c);
            else
                s.append('%').append(Integer.toHexString(c));
        }
        
        return s.toString();
    }

    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        stat.stat("DELETE", 1);

        if( ! checkIfLogged(request, response) )
            return;
        
        Logid l=null;
        
        String lid=request.getParameter("logid");
        if( lid==null )
            lid=request.getParameter("lid");
        if( lid!=null ){
            l=lids.logid(lid);
        }
        
        Boolean isForward=request.getParameter("Forward")!=null;
        
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if( l==null ){
                out.print("{\"lid\":\""+lid+"\","
                        + "\"status\":\"FAIL\","
                        + "\"comment\":\"lid not found\"}");
            } else {
                if( lids.delete(lid) ){
                    out.print(l.toJSON("\"status\":\"OK\",\"comment\":\"DELETED\""));
                    
                    if( ! isForward )
                        ForwardUpdates.addUpdate("DELETE", "lid="+lid);
                    
                    stat.reset("LaaSid", lids.count());
                    
                } else {
                    out.print(l.toJSON("\"status\":\"FAIL\",\"comment\":\"Error in deletion\""));
                }
            }
        }
    }

    /**
     * checkIfLogged
     * @param request
     * @param response
     * @return true if client is logged with an API key, else false
     * @throws IOException 
     */
    public static Boolean checkIfLogged(HttpServletRequest request, HttpServletResponse response) throws IOException{
        HttpSession session=request.getSession();
        if( session==null || session.getAttribute("apikey")==null ){
            if( noAuthenticationFor!=null ){
                // an authentication bypass configured
                String clientIP=request.getHeader("X-Real-IP");
                if( clientIP==null )
                    clientIP=request.getRemoteAddr();
                Matcher m=noAuthenticationFor.matcher(clientIP);
                if( m.find() ){
                    if( session!=null ){
                        session.invalidate();
                    }
                    // create a new session, everytime for security reason
                    session=request.getSession(true);
                    session.setAttribute("apikey", "BYPASS");
                    
                    stat.stat("AuthenticationByPass", 1);
                    
                    log.info("checkIfLogged from Authentication Bypassed for '"+clientIP+"'");
                    return true;
                }
                log.info("checkIfLogged from Authentication required for '"+clientIP+"'");
            }

            stat.stat("AuthenticationRequired", 1);
                    
            response.sendRedirect("login?referer="+request.getRequestURL().toString()+"?"+request.getQueryString());
            return false;
        }
        return true;
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
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
