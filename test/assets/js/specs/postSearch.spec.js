require([
  "jquery",
  "squire",
  "sinon"
], function($, Squire, sinon) {

  $('<div class="content"></div>').appendTo("body");
  var $el = $(".content");
  var injector = new Squire();

  describe("A PostSearch view should", function() {
    var post = {
      description: "my first real post", 
      size: 42809
    }
    var callback = sinon.stub().returns([post]);
    injector
    .mock("search/elastic", {getPosts: callback})
    .require(["views/post"], function(PostSearch) {
      beforeEach(function() {
        this.post = new PostSearch ({ el: $el });
      })

      it("get posts at beginning", function() {
        expect(callback.calledOnce).toBe(true);
      });
      // it("finds content", function() {
      //   expect($(".content").length).toBe(1);
      // });
      // it("render one post", function() {
      //   expect($el.find(".post").length).toBe(1);
      // });
    });
  });
});
