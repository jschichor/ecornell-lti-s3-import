package com.ecornell.lti;

enum ContentType {
    image, mp4, flash, pdf, xls, doc, html, mp3, zip, mov, flv, unknown;

    public static ContentType getType(String type) {
        if(type == null) return null;
        if(type.startsWith("image")) return image;
        if(type.contains("flash")) return flash;

        if(type.equals("application/msword")) return doc;
        if(type.equals("application/pdf")) return pdf;
        if(type.equals("application/vnd.ms-excel")) return xls;
        if(type.equals("application/zip")) return zip;

        if(type.equals("audio/mpeg")) return mp3;

        if(type.equals("text/html")) return html;

        if(type.equals("video/mp4")) return mp4;
        if(type.equals("video/quicktime")) return mov;
        if(type.equals("video/x-flv")) return flv;

        return unknown;
    } //getType//
} //ContentType//