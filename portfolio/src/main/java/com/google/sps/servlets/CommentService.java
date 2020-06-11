
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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.util.Map;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* a class with useful functions for comments */
public class CommentService {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /* find and return blog comments in descending order by time and return the 
    first num numbers */
    public List<Entity> findAllComments(int num, int id, String sort, boolean isBlog) {
      // create filter
      Filter keyFilter = new FilterPredicate("postid", FilterOperator.EQUAL, id);

      // create query
      Query query;
      if (isBlog) {
          query = new Query("PostComment").setFilter(keyFilter);
      } else {
          query = new Query("Comment");
      }
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
    public int delete(long id, boolean isBlog) {
        int count = 1;
        Key taskEntityKey;
        if (isBlog) {
            taskEntityKey = KeyFactory.createKey("PostComment", id);
        } else {
            taskEntityKey = KeyFactory.createKey("Comment", id);
        }
        datastore.delete(taskEntityKey);
        return count;
    }

    /* create a new Comment entity with the fields provided */
    public void createNewComment(boolean isBlog, String content, int id, String name, Date currentTime, String emoji, String email, String image, float score, String classification) {
        Entity newComment;
        if (isBlog) {
            newComment = new Entity("PostComment");
            newComment.setProperty("postid", id);
        } else {
            newComment =  new Entity("Comment");
        }
        newComment.setProperty("content", content);
        newComment.setProperty("time", currentTime);
        newComment.setProperty("name", name);
        newComment.setProperty("emoji", emoji);
        newComment.setProperty("email", email);
        newComment.setProperty("image", image);
        newComment.setProperty("score", score);
        newComment.setProperty("classification", classification);
    
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(newComment);
    }

    /** Returns a key for the uploaded file, or null if the user didn't upload a file. */
  public String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    // Our form only contains a single file input, so get the first index.
    String blobKey;

    if (blobKeys == null || blobKeys.isEmpty()) {
        blobKey = null;
    } else {
        blobKey = blobKeys.get(0).getKeyString();
    }
    return blobKey;
  }
  
  /* returns the sentiment score of the message */
  public float getSentimentScore(String message) {
    
    try {
      Document doc = Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
      LanguageServiceClient languageService = LanguageServiceClient.create();
      
      Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
      float score = sentiment.getScore();
      languageService.close();
      return score;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /* returns the content classifications of the message */
  public String classifyContent(String message) {
    
    try {
      // content classification only works for 20+ words
      int length = message.split(" ").length;
      if (length > 20) {
        Document doc = Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
        LanguageServiceClient languageService = LanguageServiceClient.create();
        
        ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
        // detect categories in the given text
        ClassifyTextResponse response = languageService.classifyText(request);
        StringBuilder sb = new StringBuilder();
        for (ClassificationCategory category : response.getCategoriesList()) {
            sb.append(
                "Category name: " + category.getName() + ", Confidence: " + category.getConfidence() + "\n");
        }
        return sb.toString();
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return "";
  }
}