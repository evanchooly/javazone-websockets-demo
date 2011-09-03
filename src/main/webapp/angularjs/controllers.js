function StickyCtrl(stickySvc) {
  var scope = this,
      notes = scope.notes = {};

  scope.noWebsockets = !stickySvc;

  scope.newNote = function() {
    stickySvc.newNote();
  };

  scope.deleteNote = function(noteId) {
    stickySvc.deleteNote(noteId);
    delete notes[noteId];
  };
  
  stickySvc
    .on('create, save', function(note) {
      notes[note.id] = note;
    })
    .on('delete', function(noteId) {
      delete notes[noteId];
    });
}