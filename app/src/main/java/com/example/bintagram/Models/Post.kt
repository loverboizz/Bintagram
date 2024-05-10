package com.example.bintagram.Models

class Post {
    var postId:String=""
    var postUrl:String=""
    var caption:String=""
    var uid:String=""
    var time:String=""
    constructor()
    constructor(postUrl: String, caption: String) {
        this.postUrl = postUrl
        this.caption = caption
    }

    constructor(postUrl: String, caption: String, uid: String, time: String) {
        this.postUrl = postUrl
        this.caption = caption
        this.uid = uid
        this.time = time
    }

    constructor(postId: String, postUrl: String, caption: String, uid: String, time: String) {
        this.postId = postId
        this.postUrl = postUrl
        this.caption = caption
        this.uid = uid
        this.time = time
    }


}