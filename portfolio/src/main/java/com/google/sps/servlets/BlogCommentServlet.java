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
 
/** Servlet that returns comments under the respective blog posts*/
@WebServlet("blog-comment")
public class BlogCommentServlet extends HttpServlet {

  private class PostComment {
      private String comment; /** content of the comment */
      private Date date; /** timestamp for comment */
      private String name; /** name of the poster */
      private long postId; /** id of the post that it is under */
 
      public PostComment(String content, Date d, String n, long p) {
          comment = content;
          date = d;
          name = n;
          postId = p;
      }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int num = Integer.parseInt(request.getParameter("num"));
    int id = Integer.parseInt(request.getParameter("id"));

    // create query
    Query query = new Query("PostComment").addSort("time", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // get each result from datastore and generate comments 
    ArrayList<PostComment> comments = new ArrayList<PostComment>();
    int counter = 0;
    for (Entity entity : results.asIterable()) {
      if (counter < num || num == -1) {
        String content = (String) entity.getProperty("content");
        Date time = (Date) entity.getProperty("time");
        String name = (String) entity.getProperty("name");
        long postId = (Long) entity.getProperty("postid");
        if (postId != id)
          continue;

        PostComment comment = new PostComment(content, time, name, postId);
        comments.add(comment);
        if (num != -1) {
          counter++;
        }
      } else {
          break;
      }
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
    int id = Integer.parseInt(request.getParameter("id"));

    Entity newPostComment =  new Entity("PostComment");
    newPostComment.setProperty("content", content);
    newPostComment.setProperty("time", currentTime);
    newPostComment.setProperty("name", name);
    newPostComment.setProperty("postid", id);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newPostComment);

    response.sendRedirect("blog.html");
  }
 
  /**
   * Converts an ArrayList instance into a JSON string using the Gson library. 
   */
  private String convertToJson(ArrayList<PostComment> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}
