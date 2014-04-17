require([
  "jquery",
  "sinon",
  "views/post"
], function($, sinon, Post) {

  describe("PostSearch view", function() {

    var post, request;
    var findPostsSpy;

    beforeEach(function() {
      jasmine.Ajax.useMock();

      post = new Post({
        el: $("<html></html>")
      });

      // install spies
      request = mostRecentAjaxRequest();
      findPostsSpy = sinon.spy(post, "findPosts");
    });

    it("calls right URL", function() {
      expect(request.url).toEqual("/api/search/post");
    });
    it("calls find posts on constructor", function() {
      expect(findPostsSpy.calledWith({})).toBe(false);
    });
    it("starts loading 0 post", function() {
      expect(post.startingPost).toBe(0);
    });
  });
});
