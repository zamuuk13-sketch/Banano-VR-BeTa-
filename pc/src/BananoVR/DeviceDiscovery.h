#pragma once
#include <cstdint>
#include <string>
#include <functional>
#include <thread>
#include <atomic>

namespace bananovr {
struct DeviceAnnouncement {
    std::string address;
    std::string deviceName;
    std::string protocol = "bananovr";
    std::uint16_t trackingPort = 27182;
};
class DeviceDiscovery {
public:
    using Callback = std::function<void(const DeviceAnnouncement&)>;
    bool start(std::uint16_t port = 27183);
    void stop();
    bool running() const noexcept;
    void setCallback(Callback callback);
private:
    void loop();
    std::atomic<bool> running_{false};
    std::uint16_t port_{27183};
    int socket_{-1};
    std::thread worker_;
    Callback callback_;
};
}
