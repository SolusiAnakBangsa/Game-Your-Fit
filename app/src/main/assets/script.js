var debug = document.getElementById("debug")
var box = document.getElementById("dataBox")
function createPeer(roomID){
    // Constructor argument is the ID of the room
    peer = new Peer(roomID,
        {host: 'rtc.gameyourfit.com',
        secure:false,
        port:6311,
        config: {'iceServers': [
            {url: 'stun:stun.l.google.com:19302'},
            {url: 'turn:rtc.gameyourfit.com:3478', username: "test", credential: "test123" }
        ]}})
    peer.on('open', function(id){
        console.log("Peer opened, ID : " + id)
        debug.innerHTML += "Peer opened, ID : " + id
    })
    peer.on('connection',function(conn){
        console.log("Attempting connection")
        debug.innerHTML += "Connected"
        connection = conn
        onReceiveData(connection)
    })
    peer.on('error', function(err){
        console.log(err)
    })
    return roomID
}
function connectToOther(destinationID){
    connection = peer.connect(destinationID);
    connection.on('open', function(){
        debug.innerHTML += "Connection established : " + connection.peer
        console.log("Connection established : " + connection.peer)
    })
    onReceiveData(connection)
}
function sendData(data){
    box.innerHTML += "Local : " + data
    connection.send(data)
}

function onReceiveData(conn){
    conn.on('data', function(data){
        console.log("Remote : " + JSON.stringify(data))
        box.innerHTML += "Remote : " + JSON.stringify(data)
        Android.parseData(data)
    })
}
var roomNumber = Math.random().toString(36).slice(2).substr(0, 5)
createPeer(roomNumber)