package main

import (
  "os"
  "os/exec"
  "fmt"
  "bufio"
  "strings"
  "strconv"
  "flag"
  "io"
  "time"
  "C"
)

var lockCmd *exec.Cmd
var locked bool

var mac = flag.String("mac", "04:02:EC:25:00:FD", "BTaddr")
var beaconMode = flag.Bool("beacon", false, "")

var THRESHOLD = 65

func init(){
}

func main(){
  flag.Parse()

  if *mac == "required" {
    fmt.Println("Please specify a mac address next time with mac=04:02:EC:25:00:FD")
    // os.Exit(1)
  }

  if *beaconMode {
    enterBeaconMode()
  } else {
    enterButtonMode()
  }

}

func enterBeaconMode(){
  fmt.Println("Started in beacon mode")

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
      matched = strings.Contains(s, *mac)
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

var sip io.WriteCloser
var btnVal = int64(13345325)
var connected = false

func enterButtonMode(){
  fmt.Println("Started in button mode")
  gatttCommand := exec.Command("sudo", "gatttool", "-b", *mac,"-I")

  sop, err := gatttCommand.StdoutPipe()
  if err != nil {
    fmt.Println(err)
    os.Exit(1)
  }

  var err1 error
  sip, err1 = gatttCommand.StdinPipe()
  if err1 != nil {
    fmt.Println(err1)
    os.Exit(1)
  }

  gatttCommand.Start()

  sip.Write([]byte("connect\r\n"))

  sc := bufio.NewScanner(sop)
  fmt.Print("Awaiting connection")
  for (!connected && sc.Scan()) {
    s := sc.Text()
    if strings.Contains(s,"Connection successful"){
      connected = true
    }
    fmt.Print(".")
  }
  fmt.Println("\r\nCONNECTED!")

  defer sip.Write([]byte("disconnect\r\n"))

  go monitorButton()
  go emitKey()
  go maintainConnection()

  for (sc.Scan()){
    s := sc.Text()
    fmt.Println(s)
    if strings.Contains(s, "Invalid file descriptor") {
      fmt.Println("Disconnected!")
      connected = false
      sip.Write([]byte("connect\r\n"))
    } else if strings.Contains(s, "value/descriptor:") {
      spl := strings.Split(s, ": ")
      numStr := strings.TrimSpace(spl[1])

      tBtnVal, _ := strconv.ParseInt(numStr,16,64)

      fmt.Printf("Button Value %d\r\n",tBtnVal)

      if (tBtnVal != btnVal) && btnVal != 13345325 {
        changeLockState()
      }

      btnVal = tBtnVal
    } else if strings.Contains(s, "written successfully") {
    }
  }

  enterButtonMode()

}

func monitorButton(){
  for {
    _ , err := sip.Write([]byte("char-read-hnd 0x0027\r\n"))
    if err != nil {
      fmt.Println(err)
      //we've dcd
      return
    }
    time.Sleep(500*time.Millisecond)
  }
}

func emitKey(){
  key := 1
  for {
    time.Sleep(3*time.Second)
    _,err := sip.Write([]byte(fmt.Sprintf("char-write-req 0x002a %02x\r\n",key)))
    if err != nil {
      fmt.Println(err)
      //we've dcd
      return
    }
    key++
  }
}

func maintainConnection(){
  for {
    if !connected{
      _,err := sip.Write([]byte("connect\r\n"))
      if err != nil {
        fmt.Println(err)
        //we've dcd
        return
      }
    }
    time.Sleep(1*time.Second)
  }
}

func changeLockState(){
  if locked {
    unlockScreen()
  } else {
    lockScreen()
  }
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
