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

    // get each result from datastore and generate comments Z
    ArrayList<Comment> comments = service.findAllComments(num, 0, sort, false);

    // Send the JSON as the response
    String json = service.convertToJson(comments);
    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(json);
  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String action = request.getParameter("action");
    if (action == null ||!action.equals("delete")) {
      // get parameters andcreate a new entity
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
      String emoji = request.getParameter("emoji");
      // Get the URL of the image that the user uploaded to Blobstore.
      String imageUrl = service.getUploadedFileUrl(request, "image");
      float score = service.getSentimentScore(content);
      String classification = service.classifyContent(content);
      service.createNewComment(false, content, 0, name, currentTime, emoji, email, imageUrl, score, classification);

      response.sendRedirect("/comments.html");
    } else {
      String commentId = request.getParameter("id");
      int num = -1;
      int count;
      String sort = "time-desc";

      if (commentId != null) {
        long id = Long.parseLong(commentId);
        count = service.delete(id, false);
      } else {
        ArrayList<Comment> results = service.findAllComments(num, 0, sort, false);
        count = service.deleteAll(results, false);
      }

      // Send the number of comments deleted as the response
      response.setContentType("application/json;");
      response.getWriter().println(count);
    }
  }

}
