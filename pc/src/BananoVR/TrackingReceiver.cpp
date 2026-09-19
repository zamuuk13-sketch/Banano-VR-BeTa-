#include "TrackingReceiver.h"
#ifdef _WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib, "Ws2_32.lib")
#else
#include <arpa/inet.h>
#include <sys/socket.h>
#include <unistd.h>
#endif
#include <chrono>
#include <cstring>

namespace bananovr {

TrackingReceiver::TrackingReceiver() = default;
TrackingReceiver::~TrackingReceiver() { stop(); }

bool TrackingReceiver::start(std::uint16_t port) {
    if (running_) return true;
#ifdef _WIN32
    WSADATA data{};
    if (WSAStartup(MAKEWORD(2,2), &data) != 0) return false;
#endif
    socket_ = static_cast<int>(::socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP));
    if (socket_ < 0) {
#ifdef _WIN32
        WSACleanup();
#endif
        return false;
    }

    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port = htons(port);
    if (::bind(socket_, reinterpret_cast<sockaddr*>(&addr), sizeof(addr)) < 0) {
#ifdef _WIN32
        closesocket(socket_); WSACleanup();
#else
        close(socket_);
#endif
        socket_ = -1;
        return false;
    }

    port_ = port;
    running_ = true;
    worker_ = std::thread(&TrackingReceiver::loop, this);
    return true;
}

void TrackingReceiver::stop() {
    if (!running_.exchange(false)) return;
#ifdef _WIN32
    if (socket_ >= 0) { closesocket(socket_); socket_ = -1; }
#else
    if (socket_ >= 0) { close(socket_); socket_ = -1; }
#endif
    if (worker_.joinable()) worker_.join();
#ifdef _WIN32
    WSACleanup();
#endif
}

bool TrackingReceiver::running() const noexcept { return running_; }

void TrackingReceiver::setPacketCallback(PacketCallback callback) {
    callback_ = std::move(callback);
}

void TrackingReceiver::loop() {
    char buffer[65536];
    while (running_) {
        sockaddr_in from{};
#ifdef _WIN32
        int fromLen = sizeof(from);
#else
        socklen_t fromLen = sizeof(from);
#endif
        const int received = ::recvfrom(
            socket_, buffer, static_cast<int>(sizeof(buffer)-1), 0,
            reinterpret_cast<sockaddr*>(&from), &fromLen);
        if (received <= 0) {
            if (running_) continue;
            break;
        }
        buffer[received] = '\0';

        char address[INET_ADDRSTRLEN]{};
        inet_ntop(AF_INET, &from.sin_addr, address, sizeof(address));

        TrackingPacket packet;
        packet.json.assign(buffer, static_cast<std::size_t>(received));
        packet.address = address;
        packet.port = ntohs(from.sin_port);
        packet.receivedAtMs = static_cast<std::uint64_t>(
            std::chrono::duration_cast<std::chrono::milliseconds>(
                std::chrono::steady_clock::now().time_since_epoch()).count());

        if (callback_) callback_(packet);
    }
}

} // namespace bananovr
