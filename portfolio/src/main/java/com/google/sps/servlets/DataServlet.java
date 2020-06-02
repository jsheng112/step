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
@WebServlet("data")
public class DataServlet extends HttpServlet {

  private class Comment {
      private String comment; /** content of the comment */
      private Date date; /** timestamp for comment */
      private String name; /** name of the poster */
 
      public Comment(String content, Date d, String n) {
          comment = content;
          date = d;
          name = n;
      }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // create query
    Query query = new Query("Comment").addSort("time", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // get each result from datastore and generate comments 
    ArrayList<Comment> comments = new ArrayList<Comment>();
    for (Entity entity : results.asIterable()) {
      String content = (String) entity.getProperty("content");
      Date time = (Date) entity.getProperty("time");
      String name = (String) entity.getProperty("name");

      Comment comment = new Comment(content, time, name);
      comments.add(comment);
    }

    // Send the JSON as the response
    String json = convertToJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);

  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // get parameters andcreate a new entity
    String content = request.getParameter("comment");
    String name = request.getParameter("name");
    Date currentTime = new Date();

    Entity newComment =  new Entity("Comment");
    newComment.setProperty("content", content);
    newComment.setProperty("time", currentTime);
    newComment.setProperty("name", name);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newComment);

    response.sendRedirect("comments.html");
  }
 
  /**
   * Converts an ArrayList instance into a JSON string using the Gson library. 
   */
  private String convertToJson(ArrayList<Comment> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}
