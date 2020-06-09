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

import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // get the correct link to redirect to
    String redirect = request.getParameter("redirect");

    UserService userService = UserServiceFactory.getUserService();
    Map<String, String> result = new HashMap<String, String>();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/" + redirect + ".html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      result.put("user", userEmail);
      result.put("url", logoutUrl);
    } else {
      String urlToRedirectToAfterUserLogsIn = "/" + redirect + ".html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      result.put("user", "Stranger");
      result.put("url", loginUrl);
    }
    // Send the JSON as the response
    String json = convertToJson(result);
    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(json);
  }
  /**
   * Converts a String Map instance into a JSON string using the Gson library. 
   */
  private String convertToJson(Map<String, String> s) {
    Gson gson = new Gson();
    String json = gson.toJson(s);
    return json;
  }
}
