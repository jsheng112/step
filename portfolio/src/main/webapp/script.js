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

/** Expands the selected post to show the full content */
function expandPost(post) {
    const content = post.innerHTML;
    // document.write(content);

    /** Display the content in the right panel */
    fullPost = document.getElementById("full-post");
    fullPost.innerHTML=content;
}
