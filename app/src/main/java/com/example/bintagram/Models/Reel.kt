package com.example.bintagram.Models

class Reel {
    var reelId:String=""
    var reelUrl:String=""
    var caption:String=""
    var uid:String=""
    constructor()
    constructor(reelUrl: String, caption: String) {
        this.reelUrl = reelUrl
        this.caption = caption
    }

    constructor(reelId: String, reelUrl: String, caption: String) {
        this.reelId = reelId
        this.reelUrl = reelUrl
        this.caption = caption
    }

    constructor(reelId: String, reelUrl: String, caption: String, uid: String) {
        this.reelId = reelId
        this.reelUrl = reelUrl
        this.caption = caption
        this.uid = uid
    }


}