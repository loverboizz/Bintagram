package com.example.bintagram.Models

class User {
    var image:String?=null
    var name:String?=null
    var email:String?=null
    var uid:String?=null
    var caption:String?=null
    constructor()

    constructor(image: String?, name: String?, email: String?, uid: String?) {
        this.image = image
        this.name = name
        this.email = email
        this.uid = uid
    }

    constructor(name: String?, email: String?, uid: String?) {
        this.name = name
        this.email = email
        this.uid = uid
    }

    constructor(email: String?, uid: String?) {
        this.email = email
        this.uid = uid
    }

    constructor(image: String?, name: String?, email: String?, uid: String?, caption: String?) {
        this.image = image
        this.name = name
        this.email = email
        this.uid = uid
        this.caption = caption
    }


}