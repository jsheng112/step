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
import com.google.appengine.api.datastore.Entity;
import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
 
/** Servlet that returns comments*/
@WebServlet("data")
public class CommentServlet extends HttpServlet {
  private CommentService service = new CommentService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int num = Integer.parseInt(request.getParameter("num")); // number of comments to return
    String sort = request.getParameter("sort");
    boolean isBlog = Boolean.parseBoolean(request.getParameter("is-blog")); 
    int id = (isBlog) ? Integer.parseInt(request.getParameter("id")) : 0;

    // get each result from datastore and generate comments Z
    ArrayList<Comment> comments = service.findAllComments(num, id, sort, isBlog);

    // Send the JSON as the response
    String json = service.convertToJson(comments);
    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(json);
  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String action = request.getParameter("action");
    if (action == null ||!action.equals("delete")) {
      createComment(request, response);
    } else {
      deleteComment(request, response);
    }
  }

  private void createComment(HttpServletRequest request, HttpServletResponse response) {
    // get parameters andcreate a new entity
    UserService userService = UserServiceFactory.getUserService();
    boolean isBlog = Boolean.parseBoolean(request.getParameter("is-blog"));

    String redirect;
    if (isBlog) {
        redirect = "/blog.html";
    } else {
        redirect = "/comments.html";
    }

    // Only logged-in users can post messages
    if (!userService.isUserLoggedIn()) {
      try {
        response.sendRedirect(redirect);
        return;
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    String email = userService.getCurrentUser().getEmail();
    String content = request.getParameter("comment");
    String name = request.getParameter("name");
    Date currentTime = new Date();
    int id = (isBlog) ? Integer.parseInt(request.getParameter("id")) : 0;
    String emoji = request.getParameter("emoji");
    
    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = service.getUploadedFileUrl(request, "image");
    float score = service.getSentimentScore(content);
    String classification = service.classifyContent(content);
    service.createNewComment(isBlog, content, id, name, currentTime, emoji, email, imageUrl, score, classification);

    try {
      response.sendRedirect(redirect);
      return;
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void deleteComment(HttpServletRequest request, HttpServletResponse response) {
    String commentId = request.getParameter("commentId");
    boolean isBlog = Boolean.parseBoolean(request.getParameter("is-blog"));
    int num = -1;
    int count;
    String sort = "time-desc";
    int id = (isBlog) ? Integer.parseInt(request.getParameter("id")) : 0;

    // deal with the case of deleting all comments versus deleting a single comment
    if (commentId != null) {
      long commentIdParsed = Long.parseLong(commentId);
      count = service.delete(commentIdParsed, isBlog);
    } else {
      ArrayList<Comment> results = service.findAllComments(num, id, sort, isBlog);
      count = service.deleteAll(results, isBlog);
    }
    
    // Send the number of comments deleted as the response
    response.setContentType("application/json;");

    try {
      response.getWriter().println(count);
    } catch(Exception e) {
      e.printStackTrace();
    }   
  }
}
