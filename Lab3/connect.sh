echo "Connecting with le"
hcitool lecc B8:27:EB:20:1F:14
echo "Waiting for some time"
sleep 10
echo "Connecting interface with 6lowpan"
echo "connect B8:27:EB:20:1F:14 1" > /sys/kernel/debug/bluetooth/6lowpan_control"

