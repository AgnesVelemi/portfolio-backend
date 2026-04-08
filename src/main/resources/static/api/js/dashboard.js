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

// Initialize messages array from existing HTML elements rendered by Thymeleaf
document.addEventListener('DOMContentLoaded', () => {
  const existingMessages = document.querySelectorAll('#messageContainer div');
  existingMessages.forEach(msgEl => {
    const text = msgEl.textContent;
    // New Format: [2] FE: 2026.04.08.07:27:04 received from 127.0.0.1 | message: dfdfd
    const match = text.match(/\[(\d+)\] (\w+): ([\d\.:]+) received from ([\d\.:\[\]]+) \| message: (.*)/);
    if (match) {
      messages.push({
        arrivalNumber: parseInt(match[1]),
        sentFrom: match[2],
        timestamp: match[3],
        sentFromIP: match[4],
        outMessage: match[5],
        sentFromTimezone: "CET" // Defaulting since it's removed from display
      });
      messageCount = Math.max(messageCount, parseInt(match[1]));
    }
  });
  console.log("Initialized from HTML: ", messages.length, "messages found.");
});

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

// Dynamic WebSocket URL determination
const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const host = window.location.host;
const wsBase = `${protocol}//${host}`;

function connectStomp() {
  stompClient = new StompJs.Client({
    brokerURL: `${wsBase}/ws/stomp`,
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
        sentFromIP: messageData.ip || "unknown",
        outMessage: messageData.outMessage,
        timestamp: formatTimestamp(messageData.timestamp),
        sentFromTimezone: messageData.timezone || "CET"
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
  socket = new WebSocket(`${wsBase}/ws/dashboard`);

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
      sentFromIP: host,
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
  let displayMessage = data.outMessage;
  let displayIp = data.sentFromIP;

  // If it's a BE message and looks like JSON, parse it for display
  if (data.sentFrom === 'BE' && typeof data.outMessage === 'string' && data.outMessage.trim().startsWith('{')) {
    try {
      const parsed = JSON.parse(data.outMessage);
      if (parsed.outMessage && parsed.sentFromIP) {
        displayMessage = parsed.outMessage;
        displayIp = parsed.sentFromIP;
      }
    } catch (e) {
      // Non-JSON or parsing error, use original
    }
  }

  return `[${data.arrivalNumber}] ${data.sentFrom}: ${data.timestamp} received from ${displayIp} <b>| message: ${displayMessage}</b>`;
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
  return `${year}.${month}.${day}.${hours}:${minutes}:${seconds}`;
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
