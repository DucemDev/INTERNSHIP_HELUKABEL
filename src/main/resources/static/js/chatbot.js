/**
 * Helukabel CRM Chatbot Widget
 * Dynamically creates a modern, sleek floating chat assistant
 * and handles secure communication with the Spring Boot backend proxy.
 */

document.addEventListener("DOMContentLoaded", () => {
    // 1. Create HTML elements dynamically to keep HTML clean
    createChatbotWidget();

    // 2. DOM Elements
    const chatBubble = document.getElementById("helu-chat-bubble");
    const chatWindow = document.getElementById("helu-chat-window");
    const closeBtn = document.getElementById("helu-chat-close");
    const chatInput = document.getElementById("helu-chat-input");
    const sendBtn = document.getElementById("helu-chat-send");
    const chatMessages = document.getElementById("helu-chat-messages");
    const suggestionChips = document.querySelectorAll(".helu-chip");

    // Toggle Chat Window
    chatBubble.addEventListener("click", () => {
        chatWindow.classList.toggle("hidden");
        chatInput.focus();
        scrollToBottom();
    });

    closeBtn.addEventListener("click", () => {
        chatWindow.classList.add("hidden");
    });

    // Send Message Event
    sendBtn.addEventListener("click", handleSend);
    chatInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            handleSend();
        }
    });

    // Suggestion Chips Click
    suggestionChips.forEach(chip => {
        chip.addEventListener("click", () => {
            const question = chip.getAttribute("data-question");
            chatInput.value = question;
            handleSend();
        });
    });

    // Handle Send Logic
    async function handleSend() {
        const question = chatInput.value.trim();
        if (!question) return;

        // Add user message
        appendMessage(question, "user");
        chatInput.value = "";

        // Add typing indicator
        const typingId = appendTypingIndicator();

        try {
            const response = await fetch("/api/chatbot/ask", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ question })
            });

            // Remove typing indicator
            removeElement(typingId);

            if (response.ok) {
                const data = await response.json();
                appendMessage(data.answer, "bot");
            } else {
                appendMessage("Xin lỗi, tổng đài AI đang gặp sự cố kết nối. Vui lòng thử lại sau.", "bot");
            }
        } catch (error) {
            removeElement(typingId);
            appendMessage("Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại mạng.", "bot");
        }
    }

    // Helper: Render messages
    function appendMessage(text, sender) {
        const msgDiv = document.createElement("div");
        msgDiv.className = `flex ${sender === "user" ? "justify-end" : "justify-start"}`;

        // Format code text with line breaks
        const formattedText = text.replace(/\n/g, "<br>");

        if (sender === "user") {
            msgDiv.innerHTML = `
                <div class="max-w-[80%] bg-red-600 text-white rounded-2xl rounded-tr-none px-4 py-2 text-sm shadow-md">
                    ${formattedText}
                </div>
            `;
        } else {
            msgDiv.innerHTML = `
                <div class="max-w-[80%] bg-gray-100 text-gray-800 rounded-2xl rounded-tl-none px-4 py-2 text-sm shadow-sm border border-gray-200/50 leading-relaxed">
                    ${formattedText}
                </div>
            `;
        }

        chatMessages.appendChild(msgDiv);
        scrollToBottom();
    }

    // Helper: Render typing indicator
    function appendTypingIndicator() {
        const id = "typing-" + Date.now();
        const msgDiv = document.createElement("div");
        msgDiv.id = id;
        msgDiv.className = "flex justify-start";
        msgDiv.innerHTML = `
            <div class="bg-gray-100 rounded-2xl rounded-tl-none px-4 py-3 text-sm border border-gray-200/50 flex items-center gap-1.5 shadow-sm">
                <span class="w-1.5 h-1.5 bg-gray-500 rounded-full animate-bounce" style="animation-delay: 0.1s"></span>
                <span class="w-1.5 h-1.5 bg-gray-500 rounded-full animate-bounce" style="animation-delay: 0.2s"></span>
                <span class="w-1.5 h-1.5 bg-gray-500 rounded-full animate-bounce" style="animation-delay: 0.3s"></span>
            </div>
        `;
        chatMessages.appendChild(msgDiv);
        scrollToBottom();
        return id;
    }

    function removeElement(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom() {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Generate Widget HTML markup dynamically
    function createChatbotWidget() {
        // Main Container
        const container = document.createElement("div");
        container.className = "fixed bottom-6 right-6 z-[9999] font-sans antialiased";
        container.innerHTML = `
            <!-- Chat Bubble Toggle -->
            <button id="helu-chat-bubble" class="w-14 h-14 bg-red-600 hover:bg-red-700 text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-300 transform hover:scale-105 active:scale-95 focus:outline-none cursor-pointer">
                <i data-lucide="message-square" class="w-6 h-6"></i>
            </button>

            <!-- Chat Window Panel -->
            <div id="helu-chat-window" class="hidden absolute bottom-20 right-0 w-96 h-[520px] bg-white rounded-2xl shadow-2xl flex flex-col overflow-hidden border border-gray-100 transition-all duration-300 ease-in-out">
                <!-- Header -->
                <div class="bg-gradient-to-r from-red-600 to-red-700 p-4 text-white flex justify-between items-center shadow-md">
                    <div class="flex items-center gap-3">
                        <div class="w-9 h-9 bg-white/10 rounded-xl flex items-center justify-center">
                            <i data-lucide="bot" class="w-5 h-5 text-white"></i>
                        </div>
                        <div>
                            <h4 class="font-semibold text-sm">Helukabel CRM Assistant</h4>
                            <div class="flex items-center gap-1.5 mt-0.5">
                                <span class="w-1.5 h-1.5 bg-green-400 rounded-full"></span>
                                <span class="text-[10px] text-white/80">Hỗ trợ trực tuyến</span>
                            </div>
                        </div>
                    </div>
                    <button id="helu-chat-close" class="hover:bg-white/10 p-1.5 rounded-lg transition-colors focus:outline-none cursor-pointer">
                        <i data-lucide="x" class="w-4 h-4"></i>
                    </button>
                </div>

                <!-- Messages -->
                <div id="helu-chat-messages" class="flex-1 p-4 overflow-y-auto space-y-4 bg-gray-50/50">
                    <div class="flex justify-start">
                        <div class="max-w-[85%] bg-gray-100 text-gray-800 rounded-2xl rounded-tl-none px-4 py-3 text-sm border border-gray-200/50 leading-relaxed shadow-sm">
                            Xin chào! Tôi là <b>Trợ lý AI của Helukabel</b>. Bạn có thể hỏi tôi về tổng quan lead, doanh thu, hiệu suất seller hoặc các dự báo kinh doanh.
                        </div>
                    </div>
                </div>

                <!-- Suggestions -->
                <div class="px-4 py-2.5 bg-white border-t border-gray-100 flex flex-wrap gap-1.5 max-h-24 overflow-y-auto">
                    <button class="helu-chip text-xs bg-gray-100 hover:bg-red-50 hover:text-red-600 border border-gray-200 hover:border-red-200 text-gray-600 px-2.5 py-1 rounded-lg transition-all cursor-pointer font-medium" data-question="Tình hình lead hiện tại?">
                        📊 Tổng quan Lead
                    </button>
                    <button class="helu-chip text-xs bg-gray-100 hover:bg-red-50 hover:text-red-600 border border-gray-200 hover:border-red-200 text-gray-600 px-2.5 py-1 rounded-lg transition-all cursor-pointer font-medium" data-question="Doanh thu theo seller?">
                        💰 Doanh thu Seller
                    </button>
                    <button class="helu-chip text-xs bg-gray-100 hover:bg-red-50 hover:text-red-600 border border-gray-200 hover:border-red-200 text-gray-600 px-2.5 py-1 rounded-lg transition-all cursor-pointer font-medium" data-question="Top 5 lý do Lost phổ biến nhất?">
                        ⚠️ Lý do Lost
                    </button>
                    <button class="helu-chip text-xs bg-gray-100 hover:bg-red-50 hover:text-red-600 border border-gray-200 hover:border-red-200 text-gray-600 px-2.5 py-1 rounded-lg transition-all cursor-pointer font-medium" data-question="Dự đoán doanh thu tháng sau?">
                        📈 Dự báo doanh thu
                    </button>
                </div>

                <!-- Input Footer -->
                <div class="p-3 bg-white border-t border-gray-100 flex items-center gap-2">
                    <input id="helu-chat-input" type="text" placeholder="Nhập câu hỏi tại đây..." class="flex-1 py-2 px-3 border border-gray-200 rounded-xl focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500 text-sm transition-all" />
                    <button id="helu-chat-send" class="w-9 h-9 bg-red-600 hover:bg-red-700 text-white rounded-xl flex items-center justify-center transition-all cursor-pointer focus:outline-none active:scale-95">
                        <i data-lucide="send" class="w-4 h-4"></i>
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(container);

        // Re-run Lucide icons for dynamically added HTML
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }
});
