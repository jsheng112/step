
package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.ArrayList;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.FetchOptions;
import java.util.List;
import java.util.Date;

/* a class with useful functions for blog post comments */
public class BlogCommentService {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /* find and return blog comments in descending order by time and return the 
    first num numbers */
    public List<Entity> findAllComments(int num, int id, String sort) {
      // create filter
      Filter keyFilter = new FilterPredicate("postid", FilterOperator.EQUAL, id);

      // create query
      Query query = new Query("PostComment").setFilter(keyFilter);
      // select sorts based on input
      if (sort.equals("time-desc"))
        query.addSort("time", SortDirection.DESCENDING);
      else if (sort.equals("time-asc"))
        query.addSort("time", SortDirection.ASCENDING);
      else if (sort.equals("content-asc"))
        query.addSort("content", SortDirection.ASCENDING);
      else
        query.addSort("name", SortDirection.ASCENDING);

      PreparedQuery results = datastore.prepare(query);
      if (num == -1)
        return results.asList(FetchOptions.Builder.withDefaults());
      else
        return results.asList(FetchOptions.Builder.withLimit(num));
    }

    /* find and return the comment with the specific id */
    public Entity findSpecificComments(long id) {
      // create filter
      Filter keyFilter = new FilterPredicate("id", FilterOperator.EQUAL, id);
      Query query = new Query("Comment").setFilter(keyFilter);

      PreparedQuery results = datastore.prepare(query);
      List<Entity> list = results.asList(FetchOptions.Builder.withDefaults());

      // catch the output in case it returns null
      if (list.isEmpty()) {
        return null;
      } else {
        return list.get(0);
      }
    }

    /* delete all entities */
    public int deleteAll(Entity... entities) {
      int count = 0;

      // get each result from datastore and delete comments if that comment belongs to post
      // the specific id
      for (Entity entity : entities) {
        long postId = (long) entity.getProperty("postid");
        Key taskEntityKey = entity.getKey();
        datastore.delete(taskEntityKey);
        count++;
      }
      return count;
    }

    /* delete entity with the specific comment id */
    public int delete(long id) {
      int count = 1;
      Key taskEntityKey = KeyFactory.createKey("PostComment", id);
      datastore.delete(taskEntityKey);
      return count;
    }

    /* create a new PostComment entity with the fields provided */
    public void createNewComment(String content, int id, Date currentTime, String name, String emoji, String email, String image) {
      Entity newPostComment =  new Entity("PostComment");
      newPostComment.setProperty("content", content);
      newPostComment.setProperty("postid", id);
      newPostComment.setProperty("time", currentTime);
      newPostComment.setProperty("name", name);
      newPostComment.setProperty("emoji", emoji);
      newPostComment.setProperty("email", email);
      newPostComment.setProperty("image", image);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(newPostComment);
    }
}