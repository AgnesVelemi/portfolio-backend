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
    console.log("Initializing dashboard...");

    // 1. Initial UI state
    updateUI(false);

    // 2. Load historical messages from HTML
    const existingMessages = document.querySelectorAll('#messageContainer div');
    existingMessages.forEach(msgEl => {
        const text = msgEl.textContent;
        // Format: [2] FE: 2026.04.08.07:27:04 received from 127.0.0.1 | message: dfdfd
        const match = text.match(/\[(\d+)\] (\w+): ([\d\.:]+) received from ([\d\.:\[\]]+) \| message: (.*)/);
        if (match) {
            messages.push({
                arrivalNumber: parseInt(match[1]),
                sentFrom: match[2],
                timestamp: match[3],
                sentFromIP: match[4],
                outMessage: match[5],
                sentFromTimezone: "CET"
            });
            messageCount = Math.max(messageCount, parseInt(match[1]));
        }
    });
    console.log("Initialized from HTML: ", messages.length, "messages found.");
    renderMessages();

    // 3. Connect STOMP
    connectStomp();

    // 4. Add event listener for Enter key only after the DOM is fully loaded
    messageInput.addEventListener("keypress", function (event) {
        if (event.key === "Enter") {
            sendWSMessageFromBackend();
        }
    });
});

button.style.backgroundColor = "royalblue";

function updateUI(isConnected) {
    if (isConnected) {
        legend.textContent = "websocket-on";
        button.textContent = "Disconnect";
        button.style.backgroundColor = "#d19615";
        button.classList.add("pressed");
        button.classList.remove("off");
        btnSendWSMsg.style.backgroundColor = "royalblue";
        btnSendWSMsg.style.color = "black";
        messageInput.disabled = false;
        btnSendWSMsg.disabled = false;
    } else {
        legend.textContent = "websocket-off";
        button.textContent = "Connect";
        button.style.backgroundColor = "royalblue";
        btnSendWSMsg.style.backgroundColor = "lightgrey";
        button.classList.remove("pressed");
        button.classList.add("off");
        btnSendWSMsg.style.color = "darkgrey";
        messageInput.disabled = true;
        btnSendWSMsg.disabled = true;
    }
}

function connectStomp() {
    console.log("Attempting STOMP connection...");
    const host = window.location.host;
    const wsBase = window.location.protocol === "https:" ? `wss://${host}` : `ws://${host}`;

    stompClient = new StompJs.Client({
        brokerURL: `${wsBase}/ws/stomp`,
        connectHeaders: {
            "client-type": "frontend"
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = function (frame) {
        console.log('STOMP connection established.');
        stompClient.subscribe("/topic/greetings", function (message) {
            const messageData = JSON.parse(message.body);
            console.log("Received via STOMP broadcast:", messageData);

            const transformedData = {
                sentFrom: messageData.clientType === 'frontend' ? 'FE' : messageData.clientType,
                sentFromIP: messageData.ip || "unknown",
                outMessage: messageData.outMessage,
                timestamp: formatTimestamp(messageData.timestamp),
                sentFromTimezone: messageData.timezone || "CET"
            };

            processIncomingData(transformedData);
        });
    };

    stompClient.onStompError = function (frame) {
        console.error('STOMP Error:', frame.headers['message']);
    };

    stompClient.activate();
}

function connect() {
    console.log("Opening raw WebSocket to /ws/dashboard...");
    const host = window.location.host;
    const wsBase = window.location.protocol === "https:" ? `wss://${host}` : `ws://${host}`;

    socket = new WebSocket(`${wsBase}/ws/dashboard`);

    socket.onopen = function () {
        console.log("Raw WebSocket connection opened.");
        updateUI(true);
    };

    socket.onclose = function () {
        console.log("Raw WebSocket connection closed.");
        updateUI(false);
    };

    socket.onerror = function (error) {
        console.error("WebSocket Error: ", error);
    };

    socket.onmessage = function (event) {
        console.log("Raw message listener received: ", event.data);
        try {
            const messageData = JSON.parse(event.data);
            // The raw echo has the same structure as what we sent
            const transformedData = {
                sentFrom: messageData.sentFrom || "BE",
                sentFromIP: messageData.sentFromIP || "unknown",
                outMessage: messageData.outMessage,
                timestamp: messageData.timestamp || formatTimestamp(),
                sentFromTimezone: messageData.sentFromTimezone || "CET"
            };
            processIncomingData(transformedData);
        } catch (e) {
            console.error("Error parsing raw message: ", e);
        }
    };
}

function processIncomingData(data) {
    data.arrivalNumber = ++messageCount;
    if (data.arrivalNumber === 11) {
        archiveMessages();
        messageCount = 1;
        data.arrivalNumber = 1;
    }
    messages.push(data);
    renderMessages();
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
    const message = messageInput.value.trim();
    const host = window.location.host;

    if (!message) return;

    if (socket && socket.readyState === WebSocket.OPEN) {
        const messageData = {
            sentFrom: "BE",
            sentFromIP: host,
            outMessage: message,
            timestamp: formatTimestamp(),
            sentFromTimezone: "CET"
        };

        console.log("Sending BE message via raw socket...");
        socket.send(JSON.stringify(messageData));
        // No local UI update here; waiting for the server echo to trigger onmessage listener

        messageInput.value = "";
    } else {
        console.warn("Cannot send: WebSocket is not open.");
        alert("Please click 'Connect' before sending a message.");
    }
}

function formatMessage(data) {
    let displayMessage = data.outMessage;
    let displayIp = data.sentFromIP;

    if (data.sentFrom === 'BE' && typeof data.outMessage === 'string' && data.outMessage.trim().startsWith('{')) {
        try {
            const parsed = JSON.parse(data.outMessage);
            if (parsed.outMessage && parsed.sentFromIP) {
                displayMessage = parsed.outMessage;
                displayIp = parsed.sentFromIP;
            }
        } catch (e) { }
    }

    return `[${data.arrivalNumber}] ${data.sentFrom}: ${data.timestamp} received from ${displayIp} <b>| message: ${displayMessage}</b>`;
}

function displayMessage(formattedMessage) {
    messageContainer.insertAdjacentHTML('afterbegin', `<div>${formattedMessage}</div>`);
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
    console.log("Batch limit reached. Swapping to archive.");
    messagestoArchive = [...messages];
    archiveMessageContainer.innerHTML = messageContainer.innerHTML;
    messages = [];
    messageContainer.innerHTML = "";
}
