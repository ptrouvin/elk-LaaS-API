# elk-LaaS-API

Copyright 2015 Pascal TROUVIN <pascal.trouvin at o4s.fr>.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

<br>Build-Version: 0.3.2
<br>Build-Date: 2015-11-15 20:15 CEST
<ul>
<li>Fix bug in rules (=i ignorecase forgotten)</li>
</ul>
">LaaS - Log as a Service - Principles</h1>
    
    <div id="rootwizard">
    	<ul class="nav nav-pills">
            <li role="presentation" class="active">
                <a href="#tab1" data-toggle="tab">Readme first</a>
            </li>
            <li role="presentation">
                <a href="#tab2" data-toggle="tab">LaaS-API</a>
            </li>
            <li role="presentation"><a href="#tab3" data-toggle="tab">HowTo</a></li>
    	</ul>
    	<div class="tab-content">
    	    <div  role="tabpanel" class="tab-pane active" id="tab1">
                <h2>Readme First</h2>
                
                <div class="btn-group btn-group-justified" role="group" aria-label="...">
                    <div class="btn-group" role="group">
                      <button type="button" class="btn btn-default" onClick="$('#tab1_comp').hide();$('#tab1_archi').show()">Architecture</button>
                    </div>
                    <div class="btn-group" role="group">
                      <button type="button" class="btn btn-default" onClick="$('#tab1_archi').hide();$('#tab1_comp').show()">Components</button>
                    </div>
                </div>
                <img id="tab1_archi" src="../images/laas-architecture.png" alt="LaaS - Architecture">
                <img id="tab1_comp" src="../images/LaaS-Components.png" alt="LaaS - Components">
                
    	    </div>
    	    <div role="tabpanel" class="tab-pane" id="tab2">
                <h2>LaaS - API</h2>
                
                These API are used by the WAP/Automation.
                
                <div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne0">
                                    HTTP REST api
                                </a>
                            </h4>
                        </div>
                        <div id="collapseOne0" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <dl>
                                <!-- LOGID -->
                                    <dt>Create / Update a logid or an associated field</dt>
                                    <dd>
                                        Make an HTTP PUT request to web service <b>/LaaS-API/logid</b>
                                        with the following parameter in query string:
                                        <ul>
                                            <li>
                                                <b>lid</b> or <b>logid</b> the logid key, as a string
                                                <br><span class="glyphicon glyphicon-warning-sign" aria-hidden="true"></span>
                                                Be careful with this key, the value MUST not be easilly guessable for obvious security reasons.
                                                <br>You must integrate random data.
                                                <br>A proposal:  cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
                                            </li>
                                            <li>
                                                rule_name=&lt;RULE&gt;
                                                <br>With &lt;RULE&gt;:
                                                <br><b>fieldName</b><b>Comparator</b><b>fieldValue</b>
                                                <br><ul>
                                                    <li><b>=</b> equals</li>
                                                    <li><b>=i</b> equals ignore case</li>
                                                    <li><b>^=</b> starts with</li>
                                                    <li><b>=$</b> ends with</li>
                                                    <li><b>~</b> regular expression</li>
                                                </ul>
                                            </li>
                                            <li>All the rules defined on the same request are logically <b>AND</b></li>
                                        </ul>
                                        <pre>$ unset http_proxy; curl -XPUT "http://127.0.0.1:8080/laaslogid/logid?lid=RANDOMDATA&rule1=host=www.monsite.fr&rule2=uri^=monAppli"
[{"lid":"RANDOMDATA","rule1":["http://www.monsite.fr"],"status":"OK","comment":"CREATED"}]
                                        </pre>
                                    </dd>
                                    <dt>Delete a logid</dt>
                                    <dd>
                                        Make an HTTP DELETE request to web service <b>/LaaS-API/logid</b>
                                        with the following parameter in query string:
                                        <ul>
                                            <li>
                                                <b>lid</b> or <b>logid</b> the logid key, as a string
                                            </li>
                                        </ul>
                                    </dd>
                                    <dt>Get a logid details</dt>
                                    <dd>
                                        Make an HTTP GET request to web service <b>/LaaS-API/logid</b>
                                        with the following parameter in query string:
                                        <ul>
                                            <li>
                                                <b>lid</b> or <b>logid</b> the logid key, as a string
                                            </li>
                                        </ul>
                                        <pre>$ unset http_proxy; curl -XGET "http://127.0.0.1:8080/laaslogid/logid?lid=RANDOMDATA"
[{"lid":"RANDOMDATA","url":["http://www.monsite.fr"]}]
                                        </pre>
                                    </dd>
                                    <dt>Get the number of logids stored</dt>
                                    <dd>
                                        Make an HTTP GET request to web service <b>/LaaS-API/logid</b>
                                        with the following parameter in query string:
                                        <ul>
                                            <li>
                                                <b>action=count</b>
                                            </li>
                                        </ul>
                                        The returned value is sent as JSON.
                                        <pre>$ unset http_proxy; curl "http://127.0.0.1:8080/laaslogid/logid?action=count"   
{"count":1}
                                        </pre>
                                    </dd>
                                    <dt>Get all the logids stored</dt>
                                    <dd>
                                        Make an HTTP GET request to web service <b>/LaaS-API/logid</b>
                                        with the following parameter in query string:
                                        <ul>
                                            <li>
                                                <b>action=getall</b>
                                            </li>
                                        </ul>
                                        <pre>$ unset http_proxy; curl "http://127.0.0.1:8080/laaslogid/logid?action=getall"
[{"lid":"RANDOMDATA","url":["http://www.monsite.fr"]}]
                                        </pre>
                                    </dd>
                                    <dt>Test the rules</dt>
                                    <dd>
                                        Make an HTTP POST request to web service <b>/LaaS-API/logid</b>
                                        with the JSON lines you want to test.
                                        <br>One JSON message par line
                                    </dd>
                                    
                                </dl>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
                                    Add/Get APIKEYS
                                </a>
                            </h4>
                        </div>
                        <div id="collapseOne" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <p>
                                    <b>cat /usr/share/tomcat/LaaS.apikeys.properties</b>
                                </p>
                                <pre>
ABCDEFGHIJK010203=Mon API de TEST
MLDMDLMSDL?QLKS?KQ?SK=api test 2
                                </pre>
                                
                                <p>Just edit the file to add a new api key, it will be usable from now.</p>
                                <p>The API will detect the file update and reload them.</p>
                                
                                <p><span class="glyphicon glyphicon-warning-sign" aria-hidden="true"></span>
                                    Think to synchronize the file with the other nodes.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">
                                    LaaS ids?
                                </a>
                            </h4>
                        </div>
                        <div id="collapseTwo" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <p>LaaSIds are create and maintain by WAP/Automation.</p>
                                <p>They are stored as JSON file: <b>/usr/share/tomcat/laas-logids.json</b></p>
                                <pre>cat /usr/share/tomcat/laas-logid.json
[{"lid":"88","vid":["v88","v89"],"uid":["u88"]},{"lid":"89","vid":["89"],"sid":["89"]},{"lid":"90","vid":["90"],"sid":["90"]},{"lid":"91","vid":["91"],"sid":["91"]},{"lid":"92","vid":["92"],"sid":["92"]},{"lid":"93","vid":["93"],"sid":["93"]},{"lid":"94","vid":["94"],"sid":["94"]},{"lid":"95","vid":["95"],"sid":["95"]},{"lid":"96","vid":["96"],"sid":["96"]},{"lid":"97","v</pre>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion" href="#collapseFour">
                                    Parameters
                                </a>
                            </h4>
                        </div>
                        <div id="collapseFour" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <p>
                                    <b>cat /usr/share/tomcat/LaaS.properties</b>
                                </p>
                                <pre>
no-authentication-for=^(10\\.225\\.138\\.[0-9]+)$
forward-updates-to=http://10.225.138.11/LaaS-API/logid
                                </pre>
                                
                                <dl>
                                    <dt>no-authentication-for</dt>
                                    <dd>
                                        A regular expression that define a filter to client IP
                                        address 
                                        (use X-Forwarded-For HTTP header if present,and 
                                        inserted by nginx)
                                    </dd>
                                    <dt>forward-updates-to</dt>
                                    <dd>
                                        Define the URL to send all updates to.
                                        <br><span class="glyphicon glyphicon-warning-sign" aria-hidden="true"></span>
                                        Think to disable the APIKEY authentication for this host.(parameter no-authentication-for on the remote host)
                                    </dd>
                                </dl>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion" href="#collapseThree">
                                    Tests
                                </a>
                            </h4>
                        </div>
                        <div id="collapseThree" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <ol>
                                    <li>Login: <pre>$ unset http_proxy; curl -D - -L --user "ABCDEFGHIJK010203:Mon API de TEST" "http://127.0.0.1:8080/laaslogid/login"
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=F870D000C9943B34237470519B9918E7; Path=/laaslogid/; HttpOnly
Content-Type: text/html;charset=UTF-8
Content-Length: 167
Date: Wed, 16 Sep 2015 14:40:05 GMT

You are logged with APIkey 'ABCDEFGHIJK010203'
</pre>
                                    </li>
                                    <li>
                                        <pre>$ unset http_proxy; curl -D - --cookie "JSESSIONID=F265A5ADD3E719741E3E73514A9595CA" "http://127.0.0.1:8080/laaslogid/logid?action=count"
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Content-Length: 13
Date: Wed, 16 Sep 2015 14:46:28 GMT

{"count":100}
</pre>
                                    </li>
                                    <li>
                                        logout: <pre>$ unset http_proxy; curl -D - --cookie "JSESSIONID=F265A5ADD3E719741E3E73514A9595CA" "http://127.0.0.1:8080/laaslogid/login?logout"
HTTP/1.1 401 Non-Autorisé
Server: Apache-Coyote/1.1
WWW-Authenticate: Basic "CloudOps-LaaS- API key"
Content-Type: text/html;charset=utf-8
Content-Language: fr
Content-Length: 1369
Date: Wed, 16 Sep 2015 14:47:13 GMT

</pre>
                                    </li>
                                </ol>
                            </div>
                        </div>
                    </div>
                </div>
    	    </div>
            <div role="tabpanel" class="tab-pane" id="tab3">
                <h2>HowTo</h2>
                
                <div class="panel-group" id="accordion3">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne30">
                                    Get the API component version
                                </a>
                            </h4>
                        </div>
                        <div id="collapseOne30" class="panel-collapse collapse in">
                            <div class="panel-body">
                                Open the documentation page: <a href="principles.html">click here</a>
                                <br>Move your mouse on the page title <img src="../images/laas-version-title.png" width="250">
                                
                                <br><img src="../images/laas-version.png">
                            </div>
                        </div>
                    </div>
                </div>
    	    </div>
    	</div>	
    </div>

