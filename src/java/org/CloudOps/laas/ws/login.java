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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.CloudOps.laas.Params;
import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Pascal TROUVIN <pascal.trouvin at o4s.fr>
 */
public class login extends HttpServlet {

    static Properties apikeys = new Properties();
    static File apikeysProperties=null;
    long apikeysMtime=0;
    
    static final Logger log = Logger.getLogger("LaaS.login");
    
    static Params param=null;
    static Pattern noAuthenticationFor=null;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        String apikeysPropertiesFilename=config.getServletContext().getInitParameter("apikeys.properties");
        if( apikeysPropertiesFilename!=null ){
            if( ! apikeysPropertiesFilename.startsWith("/") ){
                String hd =System.getProperty("user.dir");
                if( ! hd.endsWith("/") )
                    hd+="/";
                apikeysPropertiesFilename = hd+apikeysPropertiesFilename;
            }
            apikeysProperties=new File(apikeysPropertiesFilename);
            reloadApikeys();
        } else {
            ServletContext context = config.getServletContext();
            String path = context.getRealPath("/");
            log.info("No API keys defined parameter must be defined 'apikeys.properties', webapp deployed at '"+path+"'");
        }
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session=request.getSession();
        if( session==null || session.getAttribute("apikey")==null ){
            // no session or not authenticate
            // Check for HTTP header: Authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                StringTokenizer st = new StringTokenizer(authHeader);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();

                    if (basic.equalsIgnoreCase("Basic")) {
                        String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
                        log.debug("Credentials: "+ credentials);
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String login = credentials.substring(0, p).trim();
                            String password = credentials.substring(p + 1).trim();
                            
                            reloadApikeys();

                            if( apikeys.containsKey(login) && apikeys.get(login).equals(password) ){
                                log.debug("APIKEY {0} authenticated"+ login);
                                if(session!=null)
                                    session.invalidate();
                                session=request.getSession(true);
                                session.setAttribute("apikey", login);
                            } else {
                                log.info("Invalid APIKEY("+login+" : "+password+")");
                                sendHttpAuth(request,response);
                                return;
                            }

                        } else {
                            log.error("Invalid authentication token "+ authHeader);
                            sendHttpAuth(request,response);
                            return;
                        }
                    } else {
                        log.warn("Unsupported HTTP authentication method '"+basic+"', Authorization HTTP header: '"+authHeader+"'");
                        sendHttpAuth(request,response);
                        return;
                    }
                } else {
                    log.warn("Invalid HTTP authentication request '"+ authHeader+"'");
                    sendHttpAuth(request,response);
                    return;
                }
            } else {
                // request for HTTP authentication
                sendHttpAuth(request,response);
                return;
            }
        }
        
        if( request.getParameter("logout")!=null ){
            session.invalidate();
            // request for HTTP authentication
            sendHttpAuth(request,response);
            return;
        }
        
        String referer=request.getHeader("X-Referer");
        if( referer==null ) referer=request.getParameter("referer");
        if( referer==null ) referer=request.getHeader("Referer");
        if( referer!=null ){
            response.sendRedirect(referer);
            return;
        }
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>CloudOps-LaaS - API login</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>You are logged with APIkey '"+session.getAttribute("apikey")+"'</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        processRequest(request, response);
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
        processRequest(request, response);
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

    
    private void sendHttpAuth(HttpServletRequest request, HttpServletResponse response) throws IOException{
        // request for HTTP authentication
        String referer=request.getParameter("referer");
        if( referer!=null && ! referer.endsWith("login") )
            response.addHeader("X-Referer", referer);
        response.addHeader("WWW-Authenticate", "Basic \"CloudOps-LaaS- API key\"");
        response.sendError(401, "API key required, Access to this site is restricted.\n" +
            "   Unauthorized access and trespass upon connection is prohibited\n" +
            "      and will be prosecuted to the fullest extent of the law.");

    }
    
    private void reloadApikeys(){
        // check if file has been modified
        if( apikeysMtime==0 || apikeysProperties.lastModified()>apikeysMtime ){
            try {
                log.info("Loading APIKEYS From '"+ apikeysProperties.getName()+"'");
                // load a properties file
                try (FileInputStream input = new FileInputStream(apikeysProperties)) {
                    apikeys.load(input);
                }
                apikeysMtime=apikeysProperties.lastModified();
                
                for(Iterator it=apikeys.keySet().iterator(); it.hasNext();){
                    String k=(String) it.next();
                    log.info("APIKEY ("+k+") loaded");
                }
                
            } catch (IOException ex) {
                log.fatal(ex);
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
    public Boolean checkIfLogged(HttpServletRequest request, HttpServletResponse response, String wsName) throws IOException{
        HttpSession session=request.getSession();
        if( session==null || session.getAttribute("apikey")==null ){
            if( ! checkAuthorization(request, response) ){
                response.sendRedirect("login?referer="+request.getRequestURL().toString()+"?"+request.getQueryString());
                return false;
            }
            session=request.getSession(true);
        }
        
        String apikey=(String) session.getAttribute("apikey");
        
        if( wsName==null )
            return true;
        
        String[] authorizations=(String[]) session.getAttribute("authorizations");
        
        if( param==null )
            try {
                param=new Params();
            } catch (Exception ex) {
                log.error(ex);
                return false;
            }
        
        if( param.reloadIfModified() || authorizations==null ){
            String auths=param.getProperty("authorization."+apikey);
        
            if( auths==null ){
                String authNone=param.getProperty("apikey_no_authorization");
                if( authNone==null || authNone.equalsIgnoreCase("deny") ){
                    log.warn("No authorization defined for apikey '"+ apikey+"'");
                    response.sendError(403, "ACCESS to web service FORBIDDEN");
                    return false;
                }
                // access granted if nothing explicitly declared for this apikey
                return true;
            }
            authorizations=auths.split(",");
            
            session.setAttribute("authorizations", authorizations);
        }
        for(String a: authorizations)
            if( a.equals(wsName) )
                return true;
        
        log.warn("Authorization '"+wsName+"' denied for apikey '"+apikey+"'");
        response.sendError(403, "ACCESS to web service DENIED");
        return false;
    }
    public Boolean checkIfLogged(HttpServletRequest request, HttpServletResponse response) throws IOException{
        return checkIfLogged(request, response, null);
    }
    
    private Boolean checkAuthorization(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException{
        HttpSession session=request.getSession();
        if( session==null || session.getAttribute("apikey")==null ){
            // no session or not authenticate
            // Check for HTTP header: Authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                StringTokenizer st = new StringTokenizer(authHeader);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();

                    if (basic.equalsIgnoreCase("Basic")) {
                        String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
                        log.debug("Credentials: "+ credentials);
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String login = credentials.substring(0, p).trim();
                            String password = credentials.substring(p + 1).trim();
                            
                            reloadApikeys();

                            if( apikeys.containsKey(login) && apikeys.get(login).equals(password) ){
                                log.debug("APIKEY '"+login+"' authenticated");
                                if(session!=null)
                                    session.invalidate();
                                session=request.getSession(true);
                                session.setAttribute("apikey", login);
                                
                                return true;
                            } else {
                                log.info("Invalid APIKEY("+login+":"+ password+")");
                            }
                        } else {
                            log.error("Invalid authentication token "+ authHeader);
                        }
                    } else {
                        log.warn("Unsupported HTTP authentication method '"+basic+"', Authorization HTTP header: '"+authHeader+"'");
                    }
                } else {
                    log.warn("Invalid HTTP authentication request '"+ authHeader+"'");
                }
            }
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
                    
                    log.info("checkAuthorization from Authentication Bypassed for '"+clientIP+"'");
                    return true;
                }
                log.info("checkAuthorization from Authentication required for '"+clientIP+"'");
            }
        }

        return false;
    }
    
    

}
