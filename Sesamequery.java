/*
 * Author: Michael Kinuthia
 * Project Details: Endpoint Servive to query using Sesame API
 */

package com.michael.sesamequery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;

public class Sesamequery extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    String serverUrl = "Server link";
    String repositoryID ="Repository name";
    String QueryResult = "";
    String QueryArgument = "";
    RepositoryConnection con = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");           
            out.println("<head>\n" + "<title>Query Sesame</title>\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" 
                        + "<link rel='stylesheet' type='text/css' media=\"all\" href='style.css'/>\n" + "</head>");            
            out.println("<body>\n" + "<form name=\"QuerySesame\" method=\"post\" action=\"Sesamequery\">\n" + "<div>\n" +
                        "<div id=\"divHeader\">\n" + "Sesame Querying\n" + "</div>\n" + "<div id=\"divAlign\">\n" +
                        "<div id=\"divTitle\">SPARQL QUERY:</div>\n" + "<div id=\"divDetails\"><textarea rows=\"4\" cols=\"50\" name=\"QueryArgument\">"+QueryArgument+"</textarea></div>\n" +
                        "</div>\n" + "<div id=\"divAlign\">\n" + "<div id=\"divTitle\"></div>\n" +
                        "<div id=\"divDetails\"><input type=\"submit\" value=\"Query\"></div>\n" + "</div>\n" + "<div id=\"divAlign\">\n" +
                        "<div id=\"divTitle\">SPARQL QUERY RESULT:</div>\n");
            out.println("<div id=\"divDetails\"><textarea rows=\"4\" cols=\"50\" name=\"QueryResult\" disabled>\n" + QueryResult +"</textarea></div>\n");
            out.println("</div>\n" + "<div id=\"divAlign\">\n"  + "</div>\n" + "</div>\n" + "</form>\n" + "</body>");            
            out.println("</html>");            
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
            QueryArgument = request.getParameter("QueryArgument");             
            Repository myConnection  = new HTTPRepository(serverUrl, repositoryID);          
        try {
            
            myConnection.initialize();
            con = myConnection.getConnection(); // This is where Netbeans would no go past
            /*
            *Take SPARQL query and send it to Sesame
            *Return in XML format
            *Throws SesameException if there is an error querying the repository
            */
                try {
                    SPARQLParser parser = new SPARQLParser();
                    ParsedQuery parsedQuery = parser.parseQuery(QueryArgument, null);
                    if (parsedQuery instanceof ParsedTupleQuery){
                        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, QueryArgument);
                        SPARQLResultsXMLWriter writer = new SPARQLResultsXMLWriter(out);
                        query.evaluate(writer);
                        QueryResult = out.toString();                    
                    } else if(parsedQuery instanceof ParsedGraphQuery){
                        RDFXMLPrettyWriter writer = new RDFXMLPrettyWriter(out);
                        GraphQuery query = con.prepareGraphQuery(QueryLanguage.SPARQL, QueryArgument);
                        query.evaluate(writer);
                        QueryResult = out.toString();
                    } else if (parsedQuery instanceof ParsedBooleanQuery){
                        BooleanQuery query = con.prepareBooleanQuery(QueryLanguage.SPARQL, QueryArgument);
                        boolean result = query.evaluate();
                        QueryResult = Boolean.toString(result);                       
                    }                    
                } catch (MalformedQueryException | QueryEvaluationException | TupleQueryResultHandlerException | RDFHandlerException ex) {
                    Logger.getLogger(Sesamequery.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (RepositoryException ex) {
            Logger.getLogger(Sesamequery.class.getName()).log(Level.SEVERE, null, ex);
        }       
        processRequest(request, response);       
    }

      //Returns a short description of the servlet.
    @Override
    public String getServletInfo() {
        return "This Servlet handles query request of Select, Describe, Construct and Ask of Sesame Server.";
    }
    
}
