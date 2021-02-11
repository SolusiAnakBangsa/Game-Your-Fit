var debug = document.getElementById("debug")
var box = document.getElementById("chatbox")
function createPeer(roomID){
    // Constructor argument is the ID of the room

    /////////////////////////
    // Configure account here
    peer = new Peer(roomID,
        {config:
            {'iceServers': [
                {url: 'stun:stun.l.google.com:19302'},
                {url: 'turn:numb.viagenie.ca:3478', username: 'email', credential: 'password' }]
            }
        })
    peer.on('open', function(id){
        console.log("Peer ID : " + id)
        debug.innerHTML += "Peer opened, ID = " + id
    })
    peer.on('connection',function(conn){
        console.log("Connected")
        debug.innerHTML += "Connected"
        setConnectionListener(conn)
    })
    peer.on('error', function(err){
        console.log(err)
    })
}
function connectToOther(destinationID){
    connection = peer.connect(destinationID,
        {config:
            {'reliable' : 'true'}
        }
        );
    setListener(connection)
}

// Sends data to the established connection, does nothing if not connected
function sendData(data){
    try{
        connection.send(data)
        box.innerHTML += "Local : " + data
    }
    catch(err){
    }
}

// Helper function
function setConnectionListener(connection){
    connection.on('data',function(data){
        console.log("Data : " + data)
        box.innerHTML += "Remote : " + data
        Android.sendToAndroid(data)
    });
}