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
sudo systemctl start radvd
```
## Test Conversation

``` bash 
# Run on both devices
echo 1 > /sys/kernel/debug/bluetooth/6lowpan_enable

# master 
# advertise
hciconfig hci0 leadv

# slave
hcitool lecc XX:XX:XX:XX:XX:XX

# master or slave
echo “connect XX:XX:XX:XX:XX:XX 1” > /sys/kernel/debug/bluetooth/6lowpan_control
```

At this stage the `ifconfig` command should reveal interface bt0, and you can now ping shit

Try `tail -f /var/log/radvd.log` to see whats going on.

### Useful tools

`hciconfig hci0 reset # get into a known state`
`hcitool lescan # start looking around`

# Clients
