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


/** Servlet that returns comments under the respective blog posts*/
@WebServlet("blog-comment")
public class BlogCommentServlet extends HttpServlet {
  private CommentService service = new CommentService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int num = Integer.parseInt(request.getParameter("num")); // the number of comments we want
    int id = Integer.parseInt(request.getParameter("id")); // the id of the specific post
    String sort = request.getParameter("sort");

    // get each result from datastore and generate comments 
    ArrayList<Comment> comments = service.findAllComments(num, id, sort, true);
  
    // Send the JSON as the response
    String json = service.convertToJson(comments);
    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(json);
  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String action = request.getParameter("action");
    if (action == null || !action.equals("delete")) {
      // get parameters and create a new entity
      UserService userService = UserServiceFactory.getUserService();

      // Only logged-in users can post messages
      if (!userService.isUserLoggedIn()) {
        response.sendRedirect("/comments.html");
        return;
      }

      String email = userService.getCurrentUser().getEmail();
      String content = request.getParameter("comment");
      String name = request.getParameter("name");
      Date currentTime = new Date();
      int id = Integer.parseInt(request.getParameter("id"));
      String emoji = request.getParameter("emoji");

      // Get the URL of the image that the user uploaded to Blobstore.
      String image = service.getUploadedFileUrl(request, "image");
      float score = service.getSentimentScore(content);
      String classification = service.classifyContent(content);

      service.createNewComment(true, content, id, name, currentTime, emoji, email, image, score, classification);
      response.sendRedirect("/blog.html");

    } else {
      // id of the post that we are deleting all comments from
      int id = Integer.parseInt(request.getParameter("id"));
      int num = -1;
      String sort = "time-desc";
      String commentId = request.getParameter("commentId");

      int count;
      if (commentId == null) {
        ArrayList<Comment> results = service.findAllComments(num, id, sort, true);
        count = service.deleteAll(results, true);
      } else {
        long commentIdParsed = Long.parseLong(commentId);
        count = service.delete(commentIdParsed, true);
      }

      // Send the number of comments deleted as the response
      response.setContentType("application/json;");
      response.getWriter().println(count);
    }
  }
}

