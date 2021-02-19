var debug = document.getElementById("debug")
var box = document.getElementById("dataBox")
function createPeer(roomID){
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
        console.log("Remote : " + data)
        box.innerHTML += "Remote : " + data
        // Upon receiving data, call function sendToAndroid, which will run in the WebAppInterface file
        Android.sendToAndroid(data)
    });
}