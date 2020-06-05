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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.util.ArrayList;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
 
/** Servlet that deletes all blog comments for a specific blog post in datastore */
@WebServlet("delete-blog-comments")
public class DeleteBlogCommentsServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // id of the post that we are deleting all comments from
    int id = Integer.parseInt(request.getParameter("id"));
    String commentId = request.getParameter("commentId");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    if (commentId == null) {
      // create filter
      Filter keyFilter = new FilterPredicate("postid", FilterOperator.EQUAL, id);
      
      Query query = new Query("PostComment").setFilter(keyFilter).addSort("time", SortDirection.DESCENDING);
      PreparedQuery results = datastore.prepare(query);
      
      int count = 0;
      // get each result from datastore and delete comments if that comment has the specific id
      for (Entity entity : results.asIterable()) {
        long postId = (long) entity.getProperty("postid");
        Key taskEntityKey = entity.getKey();
        datastore.delete(taskEntityKey);
        count++;
      }
    
      // Send the number of comments deleted as the response
      response.setContentType("application/json;");
      response.getWriter().println(count);
    } else {
      long commentIdParsed = Long.parseLong(commentId);
      Key taskEntityKey = KeyFactory.createKey("PostComment", commentIdParsed);
      datastore.delete(taskEntityKey);
      int count = 1;

      // Send the number of comments deleted as the response
      response.setContentType("application/json;");
      response.getWriter().println(count);
    }
    

    
  }
  /**
   * Converts an ArrayList instance into a JSON string using the Gson library. 
   */
  private String convertToJson(ArrayList<String> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}
