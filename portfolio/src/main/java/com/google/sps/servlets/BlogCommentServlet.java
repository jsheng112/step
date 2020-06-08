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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;

/** Servlet that returns comments under the respective blog posts*/
@WebServlet("blog-comment")
public class BlogCommentServlet extends HttpServlet {
  private BlogCommentService service = new BlogCommentService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int num = Integer.parseInt(request.getParameter("num")); // the number of comments we want
    int id = Integer.parseInt(request.getParameter("id")); // the id of the specific post
    String sort = request.getParameter("sort");

    // get each result from datastore and generate comments 
    List<Entity> results = service.findAllComments(num, id, sort);
    ArrayList<Comment> comments = new ArrayList<Comment>();
    for (Entity entity : results) {
      String content = (String) entity.getProperty("content");
      Date time = (Date) entity.getProperty("time");
      String name = (String) entity.getProperty("name");
      long postId = (Long) entity.getProperty("postid");
      long commentId = (Long) entity.getKey().getId();
      String emoji = (String) entity.getProperty("emoji");

      Comment comment = new Comment(content, time, name, postId, commentId, emoji);
      comments.add(comment);
    }

    // Send the JSON as the response
    String json = convertToJson(comments);
    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(json);
  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // get parameters and create a new entity
    String content = request.getParameter("comment");
    String name = request.getParameter("name");
    Date currentTime = new Date();
    int id = Integer.parseInt(request.getParameter("id"));
    String emoji = request.getParameter("emoji");

    service.createNewComment(content, id, currentTime, name, emoji);
    response.sendRedirect("blog.html");
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
