package main

import (
  "os"
  "os/exec"
  "fmt"
  "bufio"
  "strings"
  "strconv"
)

var lockCmd *exec.Cmd
var locked bool

var mac = "04:02:EC:25:00:FD"
var THRESHOLD = 65

func main(){
  fmt.Println("Started")

  btmonCmd := exec.Command("sudo", "btmon")

  sop, err := btmonCmd.StdoutPipe()
  if err != nil {
    fmt.Println(err)
    os.Exit(1)
  }

  exec.Command("sudo", "hciconfig", "hci0", "reset").Output()
  err = exec.Command("sudo", "hcitool", "lescan", "--duplicates").Start()
  if err != nil {
    fmt.Println(err)
    os.Exit(1)
  }

  btmonCmd.Start()

  var rssi = 0
  var matched = false
  var outofrange = 0
  sc := bufio.NewScanner(sop)
  for (sc.Scan()) {
    s := sc.Text()
    // fmt.Println(s)
    if strings.Contains(s, "Address:"){
      matched = strings.Contains(s, mac)
    } else if strings.Contains(s, "RSSI:") {
      if matched {
        spl := strings.Split(s," ")
        rssi, _ = strconv.Atoi(spl[9])
        fmt.Println(rssi)
        if rssi < -THRESHOLD {
          fmt.Println("Locking")
          if outofrange == 2 {
            lockScreen()
          }
          outofrange ++
        } else {
          fmt.Println("Unlocking/Allowing Button Unlock!")
          unlockScreen()
          outofrange = 0
        }
      }
    }
  }
}

func waitForCommandToFinish(){
  lockCmd.Wait()
  locked = false
}
func lockScreen(){
  if !locked {
    locked = true
    lockCmd = exec.Command("i3lock","-f","--nofork","-cff6347")
    lockCmd.Start()

    go waitForCommandToFinish()
  }
}

func unlockScreen(){
  if locked {
    lockCmd.Process.Signal(os.Kill)
    locked = false
  }
}
