echo "Starting ipv6 forwarding"
echo 1 > /proc/sys/net/ipv6/conf/all/forwarding
echo "Modprobing bluetooth_6lowpan"
modprobe bluetooth_6lowpan
echo "Resetting and putting hci0 into master mode"
hciconfig hci0 reset
hciconfig hci0 leadv
echo "Enabling 6lowpan"
echo 1 > /sys/kernel/debug/bluetooth/6lowpan_enable

echo "Starting radvd"
systemctl start radvd
