#pragma once
#include <cstdint>
#include <string>
#include <functional>

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
    void setCallback(Callback callback);
};

} // namespace bananovr
