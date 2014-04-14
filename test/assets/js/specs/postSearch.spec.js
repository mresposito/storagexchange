require([
  "jquery",
  "squire",
  "sinon"
], function($, Squire, sinon) {

  $('<div class="content"></div>').appendTo("body");
  var $el = $(".content");
  var injector = new Squire();

  //FIXME: need to wait for Jasmine 2.0
  // describe("A PostSearch view should", function() {
  //   var post = {
  //     description: "my first real post", 
  //     size: 42809
  //   }
      // it("finds content", function() {
      //   expect($(".content").length).toBe(1);
      // });
      // it("render one post", function() {
      //   expect($el.find(".post").length).toBe(1);
      // });
    // });
});
