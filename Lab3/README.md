# Group 7 (ja5g14, skl1g14)  and 21 (ljk1g14, tde1g14)

# Server
Helpful source : [Nordic Semiconductor ping6 guide...](http://infocenter.nordicsemi.com/index.jsp?topic=%2Fcom.nordic.infocenter.iotsdk.v0.9.0%2Fiot_sdk_user_guides_linux_commands.html&cp=4_1_0_2_5)
## Setting up server

### Kernel Modules and Dependancies

These are only available post kernel 3.1 -- shouldn't be a problem for most

``` bash
  sudo pacman -S bluez bluez-utils
  sudo modprobe bluetooth
  sudo modprobe bluetooth_6lowpan
```

## Giving out IPv6 Addressess

Install radvd, config as so

``` bash
# /etc/radvd.conf
interface bt0
{
    AdvSendAdvert on;
    prefix 2001:db8::/64
    {
        AdvOnLink off;
        AdvAutonomous on;
        AdvRouterAddr on;
    };
};

# Set IPv6 forwarding
sudo echo 1 > /proc/sys/net/ipv6/conf/all/forwarding
# Run radvd daemon.
sudo systemctl radvd start
```
### Useful tools

hciconfig hci0 reset # get into a known state
hcitool scanle # start looking around

# Clients
