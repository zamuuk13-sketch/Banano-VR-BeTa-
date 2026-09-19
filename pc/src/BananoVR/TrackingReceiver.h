#pragma once
#include <cstdint>
#include <functional>
#include <string>
#include <thread>
#include <atomic>

namespace bananovr {

struct TrackingPacket {
    std::string json;
    std::string address;
    std::uint16_t port = 0;
    std::uint64_t receivedAtMs = 0;
};

class TrackingReceiver {
public:
    using PacketCallback = std::function<void(const TrackingPacket&)>;

    TrackingReceiver();
    ~TrackingReceiver();

    bool start(std::uint16_t port = 27182);
    void stop();
    bool running() const noexcept;
    void setPacketCallback(PacketCallback callback);

private:
    void loop();

    std::atomic<bool> running_{false};
    std::uint16_t port_{27182};
    int socket_{-1};
    std::thread worker_;
    PacketCallback callback_;
};

} // namespace bananovr
