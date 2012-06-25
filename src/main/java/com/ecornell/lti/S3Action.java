package com.ecornell.lti;

enum S3Action {
    list  (true , "view"),
    link  (true , "link"),
    media (false, null),
    flash (true , "flash"),
    html  (true , "html"),
    iframe(true , "iframe"),
    oembed(false, null),
    returnLink  (false,null),
    returnOembed(false,null);

    boolean display;
    String view;

    S3Action(boolean display,String view) {
        this.display = display;
        this.view = view;
    } //constructor//

    public boolean isDisplay() { return this.display; }
    public String getView() { return this.view; }

} //S3Action//
