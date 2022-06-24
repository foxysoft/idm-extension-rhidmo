/*******************************************************************************
 * Copyright 2017 Lambert Boskamp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package de.foxysoft.rhidmo;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.codec.binary.Base64;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.UniqueTag;
import de.foxysoft.rhidmo.ErrorException;
import de.foxysoft.rhidmo.Log;
import de.foxysoft.rhidmo.PackageScript;

public class Utl {
    private static final Log LOG = Log.get(Utl.class);
    
    protected RhidmoConfiguration m_rhidmoConfiguration;

    public Connection getConnection() throws Exception {
        final String M = "getConnection: ";
        LOG.debug(M + "Entering");
        InitialContext ic = new InitialContext();
        // TODO: lookup doesn't work with java:comp/env prefix
        DataSource datasource = (DataSource) ic
                .lookup("jdbc/RHIDMO_RT");
        Connection result = datasource.getConnection();
        LOG.debug(M + "Returning {}",
                result);
        return result;
    }

    public void registerPublicMethodsInScope(Class<?> c,
            Scriptable parentScope, Scriptable scope) {
        final String M = "registerGlobalFunctions: ";
        Method[] methods = c.getMethods();
        FunctionObject functionObject = null;
        for (int i = 0; i < methods.length; ++i) {
            int modifiers = methods[i].getModifiers();
            if (Modifier.isPublic(modifiers)
                    && (methods[i].getName().startsWith("u") || methods[i].getName().startsWith("rhidmo"))) {

                String methodName = methods[i].getName();
                functionObject = new MyFunctionObject(methodName,
                        methods[i],
                        scope);
                parentScope.put(methodName,
                        parentScope,
                        functionObject);
                LOG.debug(M + "Registered {}",
                        methodName);
            }

        } //for (int i = 0; i < methods.length; ++i) {
    }

    public Function execScriptsInScope(
            List<PackageScript> packageScripts,
            Scriptable scope,
            Context context,
            int taskId) throws Exception {

        final String M = "execScriptsInScope: ";
        Function result = null;

        for (int i = 0; i < packageScripts.size(); ++i) {
            PackageScript psi = packageScripts.get(i);

            if (psi.getScriptSource() != null) {
                Script script = context.compileString(
                        psi.getScriptSource(),
                        psi.getScriptName(),
                        1,
                        null);

                script.exec(context,
                        scope);
            } // if
            else {
                LOG.warn(M
                        + "Script {} referenced by parameters of task {} does not exist",
                        psi.getScriptName(),
                        taskId);
            }
        } // for (int i = 0; i < packageScripts.size(); ++i) {

        String mainScriptName = packageScripts.get(0)
                .getScriptName();
        Object resultObj = scope.get(mainScriptName,
                scope);

        if (UniqueTag.NOT_FOUND.equals(resultObj)) {
            throw new ErrorException("Script " + mainScriptName
                    + " doesn't define any property named "
                    + mainScriptName);
        }

        if (resultObj instanceof Function) {
            result = (Function) resultObj;
        } else {
            throw new ErrorException("Property " + mainScriptName
                    + " is not a function. Class name: "
                    + resultObj.getClass()
                            .getName());
        }

        LOG.debug(M + "Returning ",
                result);
        return result;
    }

    /**
     * <div>Shallow copy package scripts with qualified names (package name + script name) from all to qualifiedNames.</div>
     * <div>Shallow copy package scripts with simple names (script name only) from all to simpleNames.</div>
     * @param all superset, will not be modified
     * @param qualifiedNames subset of all with qualified names; might be added to
     * @param simpleNames subset of all with simple names; might be added to
     */
    protected void filterPackageScripts(List<PackageScript> all, List<PackageScript> qualifiedNames, List<PackageScript> simpleNames) {
      for(int i=0;i<all.size();++i) {
        PackageScript ps = all.get(i);
        if(ps.getPackageName() != null) {
          qualifiedNames.add(ps);
        }
        else {
          simpleNames.add(ps);
        }
      }
    }

    /**
     * <div>Find the script with the given scriptName/packageName combination in packageScripts
     * using linear search and set script source to the BASE64 decoded version of encodedScript. 
     * If encodedScript is not BASE64 encoded (this is used by some unit tests), use its content 
     * as script source directly.</div>
     * <div>If scriptName/packageName doesn't exist in packageScripts, nothing will be changed.</div>
     * 
     * @param packageScripts
     * @param scriptName
     * @param packageName may be null
     * @param encodedScript
     * @throws UnsupportedEncodingException
     */
    protected void setScriptSource(List<PackageScript> packageScripts, String scriptName, String packageName, String encodedScript) throws UnsupportedEncodingException {
      final String M="setScriptSource: ";
      String decodedScript = null;

      if(encodedScript.startsWith("{B64}")) {
        decodedScript = new String(
                Base64.decodeBase64(encodedScript.substring("{B64}".length())),
                "UTF-8");
        LOG.debug(M + "decodedScript = {}",
            decodedScript);
      }
      else {
        decodedScript = encodedScript;
      }

      // Expect i <= 10, so O(i^2) cost for nested loop is
      // acceptable
      for (int i = 0; i < packageScripts.size(); ++i) {
          PackageScript psi = packageScripts.get(i);
          if (scriptName.equals(psi.getScriptName())) {
            if( ( packageName != null && packageName.equals(psi.getPackageName()) )
                || ( packageName == null && psi.getPackageName() == null ) ) {
              psi.setScriptSource(decodedScript);
              break;
            }
          }
      }
    }

    /**
     * Fetch and BASE64 decode the mc_package_scripts.mcscriptdefinition for the
     * scripts whose mcscriptname is that given in simpleNames and whose mcpackageid
     * is that of the task given by taskId.
     * 
     * Internally reads from database tables mc_package_scripts and mxp_tasks.
     *  
     * @param simpleNames list of package scripts that only have a name, but no package
     * @param taskId 
     * @throws Exception
     */
    protected void fetchScriptSourceFromTaskPackage(Connection c, List<PackageScript> simpleNames, int taskId) throws Exception {
      final String M = "fetchScriptSourceFromTaskPackage: ";
      LOG.debug(M + "Entering simpleNames = {}",
              simpleNames);
    
      PreparedStatement ps = null;
      ResultSet rs = null;
      String scriptName = null;
      String encodedScript = null;
    
      try {
          //@formatter:off
          StringBuffer sb = new StringBuffer(
                  "select a.mcscriptdefinition,"
                          + " a.mcscriptname"
                          + " from mc_package_scripts a"
                          + " inner join mxp_tasks b"
                          + " on a.mcpackageid=b.mcpackageid"
                          + " and b.taskid=?"
                          + " where a.mcscriptname in (");
          //@formatter:on
          for (int i = 0; i < simpleNames.size(); ++i) {
              if (i > 0) {
                  sb.append(',');
              }
              sb.append('?');
          }
          sb.append(") and a.mcEnabled = 1 and a.mcScriptLanguage = 'JScript'");
          
          if(m_rhidmoConfiguration.getIsObsoletedColumnAvailable()) {
              sb.append(" and a.mcIsObsoleted = 0");
          }
          String sql = sb.toString();
          LOG.debug(M + "sql = {}",
                  sql);
    
          ps = c.prepareStatement(sql);
    
          ps.setInt(1,
                  taskId);
    
          for (int i = 0; i < simpleNames.size(); ++i) {
              ps.setString(2 + i,
                      simpleNames.get(i)
                              .getScriptName());
          }
    
          ps.execute();
          rs = ps.getResultSet();
    
          while (rs.next()) {
              scriptName = rs.getString(2);
              LOG.debug(M + "scriptName = {}",
                      scriptName);
              
              encodedScript = rs.getString(1);
              LOG.debug(M + "encodedScript = {}",
                  encodedScript);
              
              setScriptSource(simpleNames, scriptName, null, encodedScript);
    
          } // while(rs.next())
      } finally {
          if (rs != null) {
              try {
                  rs.close();
              } catch (Exception e) {
              }
          }
          if (ps != null) {
              try {
                  ps.close();
              } catch (Exception e) {
              }
          }
      }
    
      LOG.debug(M + "Returning void, simpleNames = {}",
              simpleNames);
    }

    /**
     * Fetch and BASE64 decode the mc_package_scripts.mcscriptdefinition for the
     * scripts whose mcscriptname is that given in qualifiedNames and whose mcpackageid
     * is that corresponding to the package name given in qualifiedNames.
     * 
     * Internally reads from database tables mc_package_scripts and mc_package.
     *  
     * @param qualifiedNames list of package scripts that have both a script and package name
     * @throws Exception
     */
    protected void fetchScriptSourceFromNamedPackages(Connection c, List<PackageScript> qualifiedNames) throws Exception {
      final String M = "fetchScriptSourceFromNamedPackages: ";
      LOG.debug(M + "Entering qualifiedNames = {}",
              qualifiedNames);
    
      PreparedStatement ps = null;
      ResultSet rs = null;
      String scriptName = null;
      String packageName = null;
      String encodedScript = null;
    
      try {
          //@formatter:off
          StringBuffer sb = new StringBuffer(
                  "select a.mcscriptdefinition,"
                          + " a.mcscriptname,"
                          + " b.mcqualifiedname"
                          + " from mc_package_scripts a"
                          + " inner join mc_package b"
                          + " on a.mcpackageid=b.mcpackageid"
                          + " where (");
          //@formatter:on
          for (int i = 0; i < qualifiedNames.size(); ++i) {
              if (i > 0) {
                  sb.append(" or ");
              }
              sb.append("( a.mcscriptname=? and b.mcqualifiedname=? )");
          }
          sb.append(") and a.mcEnabled = 1 and a.mcScriptLanguage = 'JScript'");
          
          if(m_rhidmoConfiguration.getIsObsoletedColumnAvailable()) {
              sb.append(" and a.mcIsObsoleted = 0");
          }
          String sql = sb.toString();
          LOG.debug(M + "sql = {}",
                  sql);
    
          ps = c.prepareStatement(sql);
    
          for (int i = 0; i < qualifiedNames.size(); ++i) {
              PackageScript packageScript = qualifiedNames.get(i);
              ps.setString(2 * i + 1, packageScript.getScriptName());
              ps.setString(2 * i + 2, packageScript.getPackageName());
          }
    
          ps.execute();
          rs = ps.getResultSet();
    
          while (rs.next()) {
              scriptName = rs.getString(2);
              LOG.debug(M + "scriptName = {}",
                      scriptName);
    
              packageName = rs.getString(3);
              LOG.debug(M + "packageName = {}",
                      packageName);
              
              encodedScript = rs.getString(1);
              LOG.debug(M + "encodedScript = {}",
                  encodedScript);
              
              setScriptSource(qualifiedNames, scriptName, packageName, encodedScript);
    
          } // while(rs.next())
      } finally {
          if (rs != null) {
              try {
                  rs.close();
              } catch (Exception e) {
              }
          }
          if (ps != null) {
              try {
                  ps.close();
              } catch (Exception e) {
              }
          }
      }
    
    LOG.debug(M + "Returning void, qualifiedNames = {}",
            qualifiedNames);
      
    }

    public void fetchScriptSource(
            List<PackageScript> packageScripts,
            int taskId) throws Exception {
        final String M = "fetchScriptSource: ";
        LOG.debug(M + "Entering packageScripts = {}",
                packageScripts);
        if (packageScripts != null && !packageScripts.isEmpty()) {
          List<PackageScript> qualifiedNames = new java.util.ArrayList<PackageScript>(packageScripts.size());  
          List<PackageScript> simpleNames = new java.util.ArrayList<PackageScript>(packageScripts.size());
          
          filterPackageScripts(packageScripts, qualifiedNames, simpleNames);
          
          // Open/close here in outer method as we may need two subsequent queries
          Connection c = null;
          try {
            c = this.getConnection();
            
            if(qualifiedNames.size() > 0) {
              fetchScriptSourceFromNamedPackages(c, qualifiedNames);
            }
            
            if(simpleNames.size() > 0) {
              fetchScriptSourceFromTaskPackage(c, simpleNames, taskId);
            }
          } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                }
            }
          }

          // Note that the references in packageScripts are the union of those in qualifiedNames and 
          // those in simpleNames. Hence, processing everything in qualifiedNames and in simpleNames
          // has effectively processed everything in packageScripts as well at this point.

        } // if (packageScripts != null && !packageScripts.isEmpty()) {

        LOG.debug(M + "Returning void, packageScripts = {}",
                packageScripts);
    }
    
    public List<PackageScript> getScriptNamesOfTask(Object task,
            String eventName) throws Exception {
        final String M = "getScriptNamesOfTask: ";

        List<PackageScript> result = new ArrayList<PackageScript>();

        String mainScriptName = (String) task.getClass()
                .getMethod("getParameter",
                        new Class<?>[] { String.class })
                .invoke(task,
                        new Object[] { eventName });

        LOG.debug(M + "Main script: {} = {}",
                eventName,
                mainScriptName);

        if (mainScriptName != null) {
            result.add(new PackageScript(mainScriptName));
        } else {
            throw new ErrorException(
                    "Missing task parameter " + eventName);
        }

        // Always try all parameter names from REQ0 to REQ9,
        // and do not require continuous numbering. That is,
        //
        // REQ0 = x, REQ1 = y, REQ2 = z
        //
        // would be OK, as well as
        //
        // REQ3 = x, REQ5 = y, REQ9 = z
        //
        // Starting with key REQ10, however, keys are required
        // to use CONTINUOUS numbering. The first key that
        // does not exist will stop processing.
        //
        // REQ1 = x, REQ10 = y, REQ11 = z
        //
        // will be OK. However,
        //
        // REQ1 = x, REQ10 = y, REQ12 = z
        //
        // will NOT work as expected because the processing
        // will stop after trying REQ11, which doesn't exist.
        // Script z, given as REQ12, will not be loaded.
        boolean haveMoreParameters = true;
        for (int i = 0; i < 10 || haveMoreParameters; ++i) {
            String requiredScriptParameterName = "REQ" + i;
            String requiredScriptParameterValue = (String) task
                    .getClass()
                    .getMethod("getParameter",
                            new Class<?>[] { String.class })
                    .invoke(task,
                            new Object[] {
                                    requiredScriptParameterName });
            if (requiredScriptParameterValue != null) {
                LOG.debug(M + "Required scripts: {} = {}",
                        requiredScriptParameterName,
                        requiredScriptParameterValue);
                result.add(new PackageScript(
                        requiredScriptParameterValue));
            } else {
                haveMoreParameters = false; // ======= exit on next iteration
            }
        }
        return result;
    }

    @SuppressWarnings("serial")
    private static class MyFunctionObject extends FunctionObject {
        
        private MyFunctionObject(String name, Member methodOrConstructor, Scriptable parentScope) {
            super(name, methodOrConstructor, parentScope);
        }
        
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            return super.call(cx, scope, getParentScope(), args);
        }
    }

    public void setRhidmoConfiguration(RhidmoConfiguration rhidmoConfiguration) {
      m_rhidmoConfiguration = rhidmoConfiguration;
      
    }
}
