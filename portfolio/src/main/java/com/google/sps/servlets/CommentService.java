
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

/* a class with useful functions for regular comments */
public class CommentService {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /* find and return comments in descending order by time and return the 
    first num numbers */
    public List<Entity> findAllComments(int num) {
        Query query = new Query("Comment").addSort("time", SortDirection.DESCENDING);
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
        if (list.isEmpty()) {
            System.err.println("EMPTY" + id);
            return null;
        } else {
            return list.get(0);
        }
    }

    /* delete all entities */
    public int deleteAll(Entity... entities) {
        int count = 0;
        // get each result from datastore and delete comments 
        for (Entity entity : entities) {
            Key taskEntityKey = entity.getKey();
            datastore.delete(taskEntityKey);
            count++;
        }
        return count;
    }

    /* delete entity with the specific id */
    public int delete(long id) {
        int count = 1;
        Key taskEntityKey = KeyFactory.createKey("Comment", id);
        datastore.delete(taskEntityKey);
        return count;
    }

    /* create a new Comment entity with the fields provided */
    public void createNewComment(String content, String name, Date currentTime, String emoji) {
        Entity newComment =  new Entity("Comment");
        newComment.setProperty("content", content);
        newComment.setProperty("time", currentTime);
        newComment.setProperty("name", name);
        newComment.setProperty("emoji", emoji);
    
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(newComment);
    }
}