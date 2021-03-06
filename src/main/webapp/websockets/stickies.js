var notes = [
];
var socket = null;
var wsClass = null;
if ( typeof WebSocket != 'undefined' ) {
    wsClass = WebSocket;
} else if ( typeof MozWebSocket != 'undefined' ) {
    wsClass = MozWebSocket;
}
try {
    if (!"WebSocket" in window) {
        alert("Your browser does not support web sockets.  Please try another browser.")
    } else {
        socket = new wsClass("ws://localhost:8080/stickies");
        socket.onmessage = function (evt) {
            process(evt.data);
        };

        socket.onclose = function (evt) {
            connectionProblems();
        }
    }
} catch(err) {
    connectionProblems();
}

function connectionProblems() {
    document.getElementById("welcome").innerHTML
        = "<span class=\"error\">We're having problems connecting to the server.  Reload to try reconnecting.</span>";
    document.getElementById("newNoteButton").style.display = "none";
}

var captured = null;
var highestZ = 0;
var highestId = 0;
function Note() {
    var self = this;
    var note = document.createElement('div');
    note.className = 'note';
    note.addEventListener('mousedown', function(e) {
        return self.onMouseDown(e)
    }, false);
    note.addEventListener('click', function() {
        return self.onNoteClick()
    }, false);
    this.note = note;
    var close = document.createElement('div');
    close.className = 'closebutton';
    close.addEventListener('click', function(event) {
        socket.send("delete-" + self.id);
        return self.close(event)
    }, false);
    note.appendChild(close);
    var edit = document.createElement('div');
    edit.className = 'edit';
    edit.setAttribute('contenteditable', true);
    edit.addEventListener('keyup', function() {
        return self.onKeyUp()
    }, false);
    note.appendChild(edit);
    this.editField = edit;
    var ts = document.createElement('div');
    ts.className = 'timestamp';
    ts.addEventListener('mousedown', function(e) {
        return self.onMouseDown(e)
    }, false);
    note.appendChild(ts);
    this.lastModified = ts;
    document.body.appendChild(note);
    return this;
}
Note.prototype = {
    get id() {
        if (!("_id" in this)) {
            this._id = 0;
        }
        return this._id;
    },

    set id(x) {
        this._id = x;
    },

    get text() {
        return this.editField.innerHTML;
    },

    set text(x) {
        this.editField.innerHTML = x;
    },

    get timestamp() {
        if (!("_timestamp" in this)) {
            this._timestamp = 0;
        }
        return this._timestamp;
    },

    set timestamp(x) {
        if (this._timestamp == x) {
            return;
        }
        this._timestamp = x;
        var date = new Date();
        date.setTime(parseFloat(x));
        this.lastModified.textContent = modifiedString(date);
    },

    get left() {
        return this.note.style.left;
    },

    set left(x) {
        this.note.style.left = x;
    },

    get top() {
        return this.note.style.top;
    },

    set top(x) {
        this.note.style.top = x;
    },

    get zIndex() {
        return this.note.style.zIndex;
    },

    set zIndex(x) {
        this.note.style.zIndex = x;
    },


    close: function() {
        this.cancelPendingSave();
        var note = this;
        var duration = 0.25;
        this.note.style.webkitTransition = '-webkit-transform ' + duration + 's ease-in, opacity ' + duration
            + 's ease-in';
        this.note.offsetTop; // Force style recalc
        this.note.style.webkitTransformOrigin = "0 0";
        this.note.style.webkitTransform = 'skew(30deg, 0deg) scale(0)';
        this.note.style.opacity = '0';
        var self = this;
        setTimeout(function() {
            document.body.removeChild(self.note)
        }, duration * 1000);
    },

    saveSoon: function() {
        this.cancelPendingSave();
        var self = this;
        this._saveTimer = setTimeout(function() {
            self.save()
        }, 200);
    },

    cancelPendingSave: function() {
        if (!("_saveTimer" in this)) {
            return;
        }
        clearTimeout(this._saveTimer);
        delete this._saveTimer;
    },

    save: function() {
        this.cancelPendingSave();
        if ("dirty" in this) {
            this.timestamp = new Date().getTime();
            delete this.dirty;
        }
        var note = this;
        socket.send("save-" + map(note));
    },

    saveAsNew: function() {
        this.timestamp = new Date().getTime();
        var note = this;
        socket.send("create-" + map(note));
    },

    onMouseDown: function(e) {
        captured = this;
        this.startX = e.clientX - this.note.offsetLeft;
        this.startY = e.clientY - this.note.offsetTop;
        this.zIndex = ++highestZ;
        var self = this;
        if (!("mouseMoveHandler" in this)) {
            this.mouseMoveHandler = function(e) {
                return self.onMouseMove(e)
            }
            this.mouseUpHandler = function(e) {
                return self.onMouseUp(e)
            }
        }
        document.addEventListener('mousemove', this.mouseMoveHandler, true);
        document.addEventListener('mouseup', this.mouseUpHandler, true);
        return false;
    },

    onMouseMove: function(e) {
        if (this != captured) {
            return true;
        }
        this.left = e.clientX - this.startX + 'px';
        this.top = e.clientY - this.startY + 'px';
        this.save();
        return false;
    },

    onMouseUp: function(e) {
        document.removeEventListener('mousemove', this.mouseMoveHandler, true);
        document.removeEventListener('mouseup', this.mouseUpHandler, true);
        this.save();
        return false;
    },

    onNoteClick: function(e) {
        this.editField.focus();
        getSelection().collapseToEnd();
    },

    onKeyUp: function() {
        this.dirty = true;
        this.saveSoon();
    }
}
function modifiedString(date) {
    return 'Last Modified: ' + date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate() + ' '
        + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
}
function map(note) {
    return "id:" + note.id + ",text:" + note.text + ",timestamp:" + note.timestamp + ",left:" + note.left
        + ",top:" + note.top + ",zIndex:" + note.zIndex;
}
function newNote() {
    socket.send("create-");
}
function createNote(piece) {
    eval(piece);
    var note = new Note();
    note.id = noteArray['id'];
    note.text = noteArray['text'];
    note.timestamp = noteArray['timestamp'];
    note.left = noteArray['left'];
    note.top = noteArray['top'];
    note.zIndex = noteArray['zIndex'];
    notes[note.id] = note;
}
function updateNote(piece) {
    eval(piece);
    var note = notes[noteArray['id']];
    note.id = noteArray['id'];
    note.text = noteArray['text'];
    note.timestamp = noteArray['timestamp'];
    note.left = noteArray['left'];
    note.top = noteArray['top'];
    note.zIndex = noteArray['zIndex'];
}
function deleteNote(id) {
    var note = notes[id];
    notes[id] = null;
    note.close();
}
function process(data) {
    var pieces = data.split("-");
    if (pieces[0] == "create") {
        createNote(pieces[1])
    } else {
        if (pieces[0] == "save") {
            updateNote(pieces[1])
        } else {
            if (pieces[0] == "delete") {
                deleteNote(pieces[1])
            }
        }
    }
}
