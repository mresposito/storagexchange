define([
  "underscore"
], function(_) {
  return _.template('\
    <div class="post well col-md-12"> \
      <div class="row"> \
        <div class="col-sm-8"> \
        <b>Description</b><br> \
        <span class="description"><%= description %></span> \
        </div> \
        <div class="cos-sm-2"> \
          <b>Size: </b><span class="size"><%= storageSize %></span> ftsq \
        </div> \
      </div> \
    </div> \
  ')
});
