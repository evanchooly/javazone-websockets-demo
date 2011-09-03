angular.directive('ngx:draggable', function(noteExp, templateElement) {
  return angular.extend(function ($document, $updateView, stickySvc, linkElement) {
    var scope = this,
        note = scope.$eval(noteExp),
        offsetX, offsetY;

    linkElement.bind('mousedown', function(e) {
      $document.mousemove(mouseMoveHandler);
      $document.mouseup(mouseUpHandler);
      offsetX = e.clientX - note.left;
      offsetY = e.clientY - note.top;
    });
    
    function mouseMoveHandler(e) {
      note.left = e.clientX - offsetX;
      note.top = e.clientY - offsetY;
      stickySvc.saveNote(note);
      $updateView();
    }
    
    function mouseUpHandler(e) {
      $document.unbind('mousemove', mouseMoveHandler);
      $document.unbind('mouseup', mouseUpHandler);
    }
    
  }, {$inject: ['$document', '$updateView', 'stickySvc']});
});


angular.widget("@ngx:contenteditable", function(expression, template){
  template.attr('contenteditable', 'true');

  return angular.extend(function($updateView, stickySvc, instance) {
    var scope = this,
        lastHtml = instance.html();

    instance.bind('blur keyup paste', function(){
      scope.$set(expression, lastHtml = instance.html());
      stickySvc.saveNote(scope.note);
      $updateView();
    });

    this.$watch(expression, function(value){
      if (value != lastHtml)
        instance.html(value);
    });
  }, {$inject:['$updateView', 'stickySvc']});
})