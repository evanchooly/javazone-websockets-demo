angular.service('websocketFactory', function($window) {
  var wsClass;

  if ('WebSocket' in $window) {
    wsClass = WebSocket;
  } else if ('MozWebSocket' in $window) {
    wsClass = MozWebSocket;
  }

  return wsClass
    ? function(url) { return new wsClass(url); }
    : undefined;
});


angular.service('stickySvc', function(websocketFactory, $updateView) {
  if (!websocketFactory) return;

  var socket = websocketFactory('ws://localhost:8080/stickies'),
      listeners = {};
  
  socket.onmessage = function (evt) {
    var message = evt.data.split('-'),
        event = message[0],
        payload = JSON.parse(message[1].replace(/var noteArray = /, '')
                                       .replace(/(\w+):/g, '"\$1":')
                                       .replace(/'/g, '"'));

    angular.forEach(listeners[event], function(listener) {
      listener(payload);
      $updateView();
    });
  };

  socket.onclose = function (evt) {
    //TODO(i): display some error to the user
    console.log('Websocket connection was closed!');
  }
  

  return {
    newNote: function() {
      socket.send('create-');
    },

    deleteNote: function(noteId) {
      socket.send('delete-' + noteId);
    },

    saveNote: function(note) {
      note.timestamp = Date.now();
      socket.send('save-' + JSON.stringify(note).replace(/["\{\}]/g, ''));
    },
    

    on: function(events, callback) {
      angular.forEach(events.split(/,?\s+/), function(event) {
        listeners[event]
          ? listeners[event].push(callback)
          : listeners[event] = [callback];
      });
      return this;
    }
  }
});