const button = document.getElementById("wsButton");
const legend = document.getElementById("wsLegend");
const btnSendWSMsg = document.getElementById("sendBtn");
const messageInput = document.getElementById("messageInput");
const messageContainer = document.getElementById("messageContainer");
const archiveMessageContainer = document.getElementById("archiveMessageContainer");

let socket = null; // Global variable to store raw WebSocket instance
let stompClient = null;
let messageCount = 0; // Count of messages
let messages = []; // Global array for all received messages (WS and STOMP)
let messagestoArchive = []; // Archive array for messages 1-10

button.style.backgroundColor = "royalblue"; // Set initial button color

function updateUI(isConnected) {
  if (isConnected) {
    legend.textContent = "websocket-on";
    button.textContent = "Disconnect";
    button.style.backgroundColor = "peru"; // Change to peru color
    button.classList.remove("off");
    button.classList.add("pressed");
    btnSendWSMsg.style.backgroundColor = "royalblue";
    btnSendWSMsg.style.color = "black";
    messageInput.disabled = false;
    btnSendWSMsg.disabled = false;
  } else {
    legend.textContent = "websocket-off";
    button.textContent = "Connect";
    button.style.backgroundColor = "royalblue"; // Change back to royalblue
    btnSendWSMsg.style.backgroundColor = "lightgrey";
    button.classList.remove("pressed");
    button.classList.add("off");
    btnSendWSMsg.style.color = "darkgrey";
    messageInput.disabled = true;
    btnSendWSMsg.disabled = true;
  }
}

// Set up event listeners after DOM is fully loaded
document.addEventListener("DOMContentLoaded", () => { // First webpage load or F5 refresh
  updateUI(false);
  connectStomp(); // Establish STOMP connection once on page load

  // Add event listener for Enter key only after the DOM is fully loaded
  messageInput.addEventListener("keypress", function (event) {
    if (event.key === "Enter") {
      sendWSMessageFromBackend();
    }
  });
});

function connectStomp() {
  stompClient = new StompJs.Client({
    brokerURL: "ws://localhost:8080/ws/stomp",
    connectHeaders: {
      "client-type": "frontend"
    },
    debug: function (str) {
      console.log("STOMP Debug:", str);
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  stompClient.onConnect = function (frame) {
    console.log('STOMP connection established: ' + frame);
    stompClient.subscribe("/topic/greetings", function (message) {
      const messageData = JSON.parse(message.body);
      console.log("Received from STOMP:", messageData);

      const transformedData = {
        sentFrom: messageData.clientType === 'frontend' ? 'FE' : messageData.clientType,
        sentFromIP: "192.168.1.10:8080",
        outMessage: messageData.outMessage,
        timestamp: formatTimestamp(messageData.timestamp),
        sentFromTimezone: "CET"
      };

      transformedData.arrivalNumber = ++messageCount;
      if (transformedData.arrivalNumber === 11) {
        archiveMessages();
        messageCount = 1;
        transformedData.arrivalNumber = messageCount;
      }
      messages.push(transformedData);

      renderMessages();

      window.dispatchEvent(new CustomEvent('stompMessageReceived', { detail: transformedData }));
    });
  };

  stompClient.onStompError = function (frame) {
    console.error('STOMP Error:', frame.headers['message']);
    console.error('Details:', frame.body);
  };

  stompClient.activate();
}

function connect() {
  socket = new WebSocket("ws://localhost:8080/ws/dashboard");

  socket.onopen = function () {
    console.log("WebSocket connection established.");
    updateUI(true);
  };

  socket.onclose = function () {
    console.log("WebSocket connection closed.");
    updateUI(false);
  };

  socket.onerror = function (error) {
    console.error("WebSocket Error: ", error);
  };

  socket.onmessage = function (event) {
    handleIncomingMessage(event.data);
  };
}

function disconnect() {
  if (socket !== null) {
    socket.close();
    socket = null;
  }
}

button.addEventListener("click", function () {
  if (socket === null || socket.readyState === WebSocket.CLOSED) {
    connect();
  } else {
    disconnect();
  }
});

function sendWSMessageFromBackend() {
  console.log("Button clicked!");
  const message = messageInput.value.trim();
  if (message && socket.readyState === WebSocket.OPEN) {
    const messageData = {
      sentFrom: "BE",
      sentFromIP: "192.168.1.10:8080",
      outMessage: message,
      timestamp: formatTimestamp(),
      sentFromTimezone: "CET"
    };
    socket.send(JSON.stringify(messageData));
    messageInput.value = "";
  } else {
    console.warn("WebSocket is not open.");
    alert("WebSocket is not open!");
  }
}

function handleIncomingMessage(data) {
  try {
    const messageData = JSON.parse(data);
    messageData.arrivalNumber = ++messageCount;
    if (messageData.arrivalNumber === 11) {
      archiveMessages();
      messageCount = 1;
      messageData.arrivalNumber = messageCount;
    }
    messages.push(messageData);
    renderMessages();
  } catch (error) {
    console.error("Error parsing message:", error);
  }
}

function formatMessage(data) {
  return `[${data.arrivalNumber}] ${data.sentFrom}: ${data.sentFromTimezone}:${data.timestamp} ip:${data.sentFromIP} <b>| message: ${data.outMessage}</b>`;
}

function displayMessage(formattedMessage) {
  messageContainer.insertAdjacentHTML('afterbegin', `<p>${formattedMessage}</p>`);
}

function formatTimestamp(date = new Date()) {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${year}.${month}.${day}. ${hours}:${minutes}:${seconds}`;
}

function renderMessages() {
  messageContainer.innerHTML = "";
  messages.forEach(msg => {
    const formatted = formatMessage(msg);
    displayMessage(formatted);
  });
}

function archiveMessages() {
  messagestoArchive = [...messages];
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: '/app/archive',
      body: JSON.stringify(messagestoArchive)
    });
  }
  messages = [];
  messageContainer.innerHTML = "";
  renderArchiveMessages();
}

function renderArchiveMessages() {
  archiveMessageContainer.innerHTML = "";
  messagestoArchive.forEach(msg => {
    const formatted = formatMessage(msg);
    archiveMessageContainer.insertAdjacentHTML('afterbegin', `<p>${formatted}</p>`);
  });
}
