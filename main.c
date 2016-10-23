
#include "board.h"
#include "fsl_debug_console.h"
#include "light_sensor.c"
#include "touch_sensor.c"
#include "accel_i2c.c"

/* FreeRTOS includes. */
#include "FreeRTOS.h"
#include "task.h"
#include "queue.h"

#define LEFT_CLICK 0
#define RIGHT_CLICK 1

#define HAND_THRESHOLD 64256
#define LEFT_BTN 3
void sleep(uint32_t sleep){
  while(sleep){
    sleep--;
    asm("nop");
  }
}

// void gpio_init(void)
// {
//      SIM->SCGC5 |= SIM_SCGC5_PORTE_MASK;
//      PORTE->PCR[29] = PORT_PCR_MUX(1U);
//     //  PTE->PDDR |= (1U << 29U);
//
//      SIM->SCGC5 |= SIM_SCGC5_PORTD_MASK;
//      PORTD->PCR[5] = PORT_PCR_MUX(1U);
//     //  PTD->PDDR |= (1U << 5U);
// }


uint16_t threshold = 0xFA00;

#define size 64

uint8_t lc, rc;

int8_t X[size], Y[size], Z[size];
uint8_t T[size], T1[size];
uint16_t L[size];

int8_t i,j;

int16_t Xv, Yv, Zv;
int32_t Lv, Tv, T1v;

uint8_t baseline;
uint8_t baseline1;
uint16_t lightBaseline;
bool lbPressed;

typedef struct {
   uint8_t left_click;
   uint8_t right_click;
   uint8_t x;
   uint8_t y;
   uint8_t scroll;
   bool scrollmode;
   bool handOn;
   /* declare as many members as desired, but the entire structure size must be known to the compiler. */
} mouse_status;

mouse_status m_status;

uint32_t calibrate_touch_sensor(uint8_t pin){
  //take 64 samples, get average, be happy
  uint32_t total = 0;
  for (uint32_t i = 0; i < 64; i++){
    total += touch_sensor_read(9);
    sleep(5000);
  }
  return total;
}

uint32_t calibrate_light_sensor(){
  //take 64 samples, get average, be happy
  uint32_t total = 0;
  for (uint32_t i = 0; i < 64; i++){
    total += light_sensor_read();
    sleep(5000);
  }
  return total/64;
}

void scrollmodeCheck(){
  uint8_t lBtn = PTC->PDIR & (1U<<LEFT_BTN);
  if(lBtn && lbPressed){
    lbPressed = false;
    m_status.scrollmode = !m_status.scrollmode;
    if (m_status.scrollmode) {
      //red
      PTE -> PDOR &= ~(1U << 29U);
      PTD -> PDOR |= (1U << 5U);
    } else {
      //green
      PTE -> PDOR |= (1U << 29U);
      PTD -> PDOR &= ~(1U << 5U);
    }
  } else {
    if(!lBtn) lbPressed = true;
  }
  return;
}

void handOnCheck(){
  uint16_t light_intensity = light_sensor_read();
  // PRINTF("%d\t%d\r\n",light_intensity,lightBaseline);
  if (light_intensity > lightBaseline + 2500) {
    m_status.handOn = true;
    PRINTF("HAND ON\r\n");
  } else{
    PRINTF("HAND OFF\r\n");
  }
  return;
}

//emit every 8ms
static void mouseDataTask( void *pvParameters ){

  TickType_t xLastWakeTime;

  xLastWakeTime = xTaskGetTickCount();
  while(1)
  {

  	vTaskDelayUntil( &xLastWakeTime, 8 / portTICK_PERIOD_MS);

    scrollmodeCheck();
    handOnCheck();

    Xv -= X[i]; Yv -= Y[i]; Zv -= Z[i]; Lv -= L[i]; Tv -= T[i]; T1v -= T1[i];

    int8_t x = read_i2c(ACCEL_WRITE_CMD, ACCEL_READ_CMD, 0x01);
    int8_t y = read_i2c(ACCEL_WRITE_CMD, ACCEL_READ_CMD, 0x03);
    int8_t z = read_i2c(ACCEL_WRITE_CMD, ACCEL_READ_CMD, 0x05);
    int8_t t = touch_sensor_read(9);
    int8_t t1 = touch_sensor_read(10);
    uint16_t light_intensity = light_sensor_read();

    // get direction of scroll now
    // if (t*10 >= X)
    X[i] = x; Y[i] = y; Z[i] = z; L[i] = light_intensity; T[i] = t; T1[i] = t1;

    Xv += X[i]; Yv += Y[i]; Zv += Z[i]; Lv += L[i]; Tv += T[i]; T1v += T1[i];

    i++; i%=size;

    // PRINTF("x:%d\ty:%d\tz:%d\tl:%d\t\tt:%d\tt1:%d\r\n",Xv/size,Yv/size,Zv/size,Lv/size, Tv/size, T1v/size);

    if (abs(Tv - T1v) >= 3*size){
      uint8_t direction;
      if (Tv > T1v) {
        // PRINTF("TOUCHED RIGHT\r\n");
        PTE -> PDOR &= ~(1U << 29U);
        PTD -> PDOR |= (1U << 5U);
        direction = RIGHT_CLICK;
      } else {
        // PRINTF("TOUCHED LEFT\r\n");
        PTE -> PDOR |= (1U << 29U);
        PTD -> PDOR &= ~(1U << 5U);
        direction = LEFT_CLICK;
      }
      if (m_status.scrollmode) {
        m_status.scroll = direction;
      } else {
        if (direction == LEFT_CLICK){
          m_status.left_click = 1;
        } else {
          m_status.right_click = 1;
        }
      }
    } else {
      m_status.left_click = 0;
      m_status.right_click = 0;
      m_status.scroll = 0;
    }
  }
}

int main (void)
{
    hardware_init();
    light_sensor_init();
    touch_sensor_init();
    init_i2c();

    //calibration of light sensor fails otherwise!
    PTE -> PDOR &= ~(1U << 29U);
    PTD -> PDOR &= ~(1U << 5U);

    PRINTF("I2c initialized\r\n");

    baseline = calibrate_touch_sensor(9);
    baseline1 = calibrate_touch_sensor(10);
    lightBaseline = calibrate_light_sensor();

    //green as we start in click mode
    PTE -> PDOR |= (1U << 29U);
    PTD -> PDOR &= ~(1U << 5U);

    PRINTF("TSI calibrated\r\n");

    uint8_t id = read_i2c(ACCEL_WRITE_CMD, ACCEL_READ_CMD, 0x0D);
    PRINTF("Accelerometer ID %x\r\n",id);

    write_i2c(ACCEL_WRITE_CMD,ACCEL_CTRL_1,0x01); // bit 1 takes from standby to active.

    xTaskCreate( mouseDataTask, (const char *) "mouseTask",
      configMINIMAL_STACK_SIZE, NULL, tskIDLE_PRIORITY +2, NULL );

    vTaskStartScheduler();

    while(1);
}
