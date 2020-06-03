// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
package com.google.sps.servlets;
 
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.Date;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.util.ArrayList;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
 
/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("contact-me")
public class ContactServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // get parameters andcreate a new entity
    String firstName = request.getParameter("firstname");
    String lastName = request.getParameter("lastname");
    String email = request.getParameter("email");
    String subject = request.getParameter("subject");
    Date currentTime = new Date();

    Entity newEmail =  new Entity("Email");
    newEmail.setProperty("firstName", firstName);
    newEmail.setProperty("lastName", lastName);
    newEmail.setProperty("email", email);
    newEmail.setProperty("subject", subject);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newEmail);

    response.sendRedirect("contact.html");
  }
}
