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
 
/** display an expanded version of the selected image in the right */
function expandImage(img) {
  const imgUrl = img.src;
  const imgElement = document.getElementById('selectedImg');
 
  imgElement.src = imgUrl;
  imgElement.parentElement.style.display = "block";
}
 
/** Adds a random fact to the index page */
function randomizeFacts() {
  const facts = ["I once knew how to ride a bike when I was little but now somehow I have forgotten how to ride a bike!", 
  "I have been drawing and painting for almost 13 years by now.", 
  "I have really good focus and sometimes can paint/study/do anything for hours on end without getting distracted.", 
  "I have wanted a dog for so many years now but I do not think this is going to happen anytime soon.", 
  "I have recently been very interested in UI design. In fact, check out my latest UI design at tigertickets.herokuapp.com"];
 
  // Pick a random fact.
  const randomFact = facts[Math.floor(Math.random() * facts.length)];
 
  // Add it to the page.
  const factContainer = document.getElementById('random-fact');
  factContainer.innerText = randomFact;
 
}

/** when first loading the page, either load the latest
post or the post last clicked on */
function loadPosts() {
  postId = localStorage.getItem("postid");
  if (postId == null) {
    document.getElementById("post4").click();
    setId(4);
  } else {
    document.getElementById("post" + postId).click();
    setId(postId);
  }
}
 
/** Expands the selected post to show the full content */
function expandPost(post, id) {
  const content = post.innerHTML;
 
  /** Display the content in the right panel */
  fullPost = document.getElementById("full-post");
  fullPost.innerHTML=content;

  /** set the last seen post to this one */
  localStorage.setItem("postid", id);

  /** show border only on the expanded post */
  for (var i = 1; i <= 4; i++) {
    if (i != id)
      document.getElementById("post" + i).style.borderStyle = "none";
  }
  post.style.borderStyle = "solid";
}

/** sets the id of the selected blog post */
function setId(id) {
  idInput = document.getElementById("id");
  idInput.value = id;
}

/** get all the comments under a specific blog post.
Each blog post is identifiable by a unique id (starting
from 1 being the id of the oldest post) */
function getPostComments() {
  var num = -1;
  if (!document.getElementById("showall").checked) {
    num = document.getElementById("quantity").value;
  }
  const id = document.getElementById("id").value;
  sort = document.getElementById("sort").value;
  /** get the comments for this specific blog post */
  fetch('blog-comment?num=' + num + "&id=" + id + '&sort=' + sort).then(response => response.json()).then((data) => {
    const commentDivElement = document.getElementById('data-container');
    commentDivElement.innerHTML = '';
    const isBlogComment = true;
    for (var i = 0; i < data.length; i++) {
      commentDivElement.appendChild(
      createDivElement(data[i], isBlogComment));
    }
  });
}

/** deletes a blog comment with specific comment id */
function deleteSpecificBlogComment(commentId) {
  // create and send a POST request for deleting data
  const id = document.getElementById("id").value;
  const request = new Request('delete-blog-comments?id=' + id + "&commentId=" + commentId, {method: 'POST'});
  var num = -1;
  if (!(document.getElementById("showall").checked)) {
    num = document.getElementById("quantity").value;
  }

  sort = document.getElementById("sort").value;
  // after POST returns response, create a GET request to get the data again
  fetch(request).then(response => fetch('blog-comment?num=' + num + '&id=' + id + "&sort=" + sort)).then(response => response.json()).then((data) => {
    const commentDivElement = document.getElementById('data-container');

    commentDivElement.innerHTML = '';
    for (var i = 0; i < data.length; i++) {
      commentDivElement.appendChild(
      createDivElement(data[i]));
    }
  });
}

/** deletes all comments from blog post with the specific id */
function deleteBlogComments() {
  // create and send a POST request for deleting data
  const id = document.getElementById("id").value;
  const request = new Request('delete-blog-comments?id=' + id, {method: 'POST'});

  // after POST returns response, create a GET request to get the data again
  // which returns 0 comments
  sort = document.getElementById("sort").value;
  fetch(request).then(response => fetch('blog-comment?num=0&id=' + id + "&sort=" + sort)).then(response => response.json()).then((data) => {
    const commentDivElement = document.getElementById('data-container');

    commentDivElement.innerHTML = '';
    for (var i = 0; i < data.length; i++) {
      commentDivElement.appendChild(
      createDivElement(data[i]));
    }
  });
}
 
/**Changes the background color of index.html according to
color picked by user */
function colorPicker() {
  const hex = document.querySelector("#hex");
  const color = document.querySelector("#color");
 
  /** upon detecting the enter key, set the background color to the input */
  if(event.key === 'Enter') {
    document.body.style.backgroundColor = hex.value;
    color.value = hex.value;
    /** store color in local storage */
    localStorage.setItem("bgColor", hex.value);
  }
 
  /** eventlistener for selection of color in the color palette */
  color.addEventListener("input", ()=>{
    const colorVal = color.value;
    hex.value = colorVal;
    document.body.style.backgroundColor = colorVal;
    localStorage.setItem("bgColor", colorVal);
  })
}
 
/**Changes the background color of index.html according to
color picked by user */
function colorPickerText() {
  const hex = document.querySelector("#hex-text");
  const color = document.querySelector("#color-text");
 
  /** upon detecting the enter key, set the background color to the input */
  if(event.key === 'Enter') {
    document.body.style.color = hex.value;
    color.value = hex.value;
    /** store color in local storage */
    localStorage.setItem("textColor", hex.value);
  }
 
  /** eventlistener for selection of color in the color palette */
  color.addEventListener("input", ()=>{
    const colorVal = color.value;
    hex.value = colorVal;
    document.body.style.color = colorVal;
    localStorage.setItem("textColor", colorVal);
  })
}
 
/** restores default background color */
function restoreDefaultColor() {
  document.body.style.backgroundColor = "#ffffff";
  localStorage.setItem("bgColor", "#ffffff");
  const hex = document.querySelector("#hex");
  const color = document.querySelector("#color");
  hex.value = "#ffffff";
  color.value = "#ffffff";
}
 
/** restores default text color */
function restoreDefaultTextColor() {
  document.body.style.color = "#595959";
  localStorage.setItem("textColor", "#595959");
  const hex = document.querySelector("#hex-text");
  const color = document.querySelector("#color-text");
  hex.value = "#595959";
  color.value = "#595959";
}
 
/** sets the background and text color for the settings page*/
function setColorSettings() {
  /** sets background color and values in the fields */
  const hex = document.querySelector("#hex");
  const color = document.querySelector("#color");
  var localColor = localStorage.getItem("bgColor");
  if (localColor == null) {
    hex.value = "#ffffff";
    color.value = "#ffffff";
    document.body.style.backgroundColor = "#ffffff";
  }
  else {
    hex.value = localColor;
    color.value = localColor;
    document.body.style.backgroundColor = localColor;
  }
    
 
  /** sets text color and value in the fields */
  const hexText = document.querySelector("#hex-text");
  const colorText = document.querySelector("#color-text");
  var localTextColor = localStorage.getItem("textColor");
  if (localTextColor == null) {
    hexText.value = "#595959";
    colorText.value = "#595959";
    document.body.style.color = "#595959";
  }
  else {
    hexText.value = localTextColor;
    colorText.value = localTextColor;
    document.body.style.color = localTextColor;
  }  
}
 
/** sets the background and text color according to the previously chosen colors */
function setColor() {
  document.body.style.backgroundColor = localStorage.getItem("bgColor");
  document.body.style.color = localStorage.getItem("textColor");
}
 
/**
 * Fetch comments from the server and adds them to the DOM.
 */
function getComment() {
  var num = -1;
  if (!(document.getElementById("showall").checked)) {
    num = document.getElementById("quantity").value;
  }
  sort = document.getElementById("sort").value;

  fetch('data?num=' + num + '&sort=' + sort).then(response => response.json()).then((data) => {
    const commentDivElement = document.getElementById('data-container');
    commentDivElement.innerHTML = '';
    const isBlogComment = false;
    for (var i = 0; i < data.length; i++) {
      commentDivElement.appendChild(
        createDivElement(data[i], isBlogComment));
    }
  });
}
 
/** Creates a <div> element containing text. */
function createDivElement(comment, isBlogComment) {
  const divElement = document.createElement('div');
  divElement.setAttribute("class", "panel");
 
  const pElementName = document.createElement('p');
  pElementName.innerText = comment.name;
  divElement.appendChild(pElementName);

  const pElement = document.createElement('p');
  pElement.innerText = comment.comment;
  divElement.appendChild(pElement);
 
  const pElementEmoji = document.createElement('p');
  pElementEmoji.innerText = comment.emoji;
  divElement.appendChild(pElementEmoji);

  const imgElement = document.createElement('a');
  imgElement.innerHTML = "<a href=\"" + comment.image + "\"><img src=\"" + comment.image + "\" /></a>"
  divElement.appendChild(imgElement);

  const pElementDate = document.createElement('p');
  pElementDate.innerText = comment.date;
  divElement.appendChild(pElementDate);

  const button = document.createElement('button');
  button.innerText = "Delete this comment";
  if (isBlogComment) {
    button.addEventListener("click", (button) => deleteSpecificBlogComment(comment.id));
  } else {
    button.addEventListener("click", (button) => deleteSpecificComment(comment.id));
  }
  divElement.appendChild(button);
  return divElement;
}

/** deletes a comment with specific comment id */
function deleteSpecificComment(commentId) {
  // create and send a POST request for deleting data
  const request = new Request('delete-data?id=' + commentId, {method: 'POST'});
  var num = -1;
  if (!(document.getElementById("showall").checked)) {
    num = document.getElementById("quantity").value;
  }
  
  // after POST returns response, create a GET request to get the data again
  sort = document.getElementById("sort").value;
  fetch(request).then(response => fetch('data?num=' + num + "&sort=" + sort)).then(response => response.json()).then((data) => {
    const commentDivElement = document.getElementById('data-container');

    commentDivElement.innerHTML = '';
    for (var i = 0; i < data.length; i++) {
      commentDivElement.appendChild(
      createDivElement(data[i]));
    }
  });
}


/** deletes all posts upon clicking the button */
function deletePosts() {
  // create and send a POST request for deleting data
  const request = new Request('delete-data', {method: 'POST'});
  
  // after POST returns response, create a GET request to get the data again
  // which returns 0 comments
  sort = document.getElementById("sort").value;
  fetch(request).then(response => fetch('data?num=0' + "&sort=" + sort)).then(response => response.json()).then((data) => {
    const commentDivElement = document.getElementById('data-container');

    commentDivElement.innerHTML = '';
    for (var i = 0; i < data.length; i++) {
      commentDivElement.appendChild(
      createDivElement(data[i]));
    }
  });
}

/** checks whether the user is authenticated and adjust elements 
according to whether the user is logged in or logged out */
function checkAuth(redirect){
  // send request for information on login status
  fetch('auth?redirect=' + redirect).then(response => response.json()).then((data) => {
    const commentDivElement = document.getElementById('auth-container');
    const hElement = document.createElement('h1');
    const navElement = document.getElementById("nav");
    const liElement = document.createElement('li');
    
    // adjust visibility and login/logout button according to 
    // whether user is logged in or not
    if (data[0] != "Stranger") {
      hElement.innerHTML = "Hello " + data[0];
      document.getElementById("comment-form").style.display = "block";
      document.getElementById("delete-button").style.display = "block";
      liElement.innerHTML = "<a href=\"" + data[1] + "\">Logout</a>";
      
    
    } else {
      hElement.innerHTML = "Hello! Please login to post a comment";
      document.getElementById("comment-form").style.display = "none";
      document.getElementById("delete-button").style.display = "none";
      liElement.innerHTML = "<a href=\"" + data[1] + "\">Login</a>";
    }
    commentDivElement.appendChild(hElement);
    navElement.appendChild(liElement);
    
  });
}

function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('comment-form');
        messageForm.action = imageUploadUrl;
      });
}

function init(page) {
    setColor();
    loadPosts();
    checkAuth(page);
}