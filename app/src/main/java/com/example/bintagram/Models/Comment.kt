package com.example.bintagram.Models

class Comment {
    var comment:String? = null
    var commentorId:String? = null

    constructor()
    constructor(comment: String?, commentorId: String?) {
        this.comment = comment
        this.commentorId = commentorId
    }


}