package com.google.sps.servlets;

import java.util.Date;

public class Comment {
    private String comment; /* content of the comment */
    private Date date; /* timestamp for comment */
    private String name; /* name of the poster */
    private long postId; /* id of the post that it is under */
    private long id; /* unique identifier of the comment*/
 
    public Comment(String content, Date d, String n, long p, long idNum) {
        comment = content;
        date = d;
        name = n;
        postId = p;
        id = idNum;
    }
}