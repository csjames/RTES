#include "board.h"

void touch_sensor_init() {
  SIM->SCGC5 |= SIM_SCGC5_TSI_MASK;
  SIM->SCGC5 |= SIM_SCGC5_TSI_MASK;

  TSI0_TSHD = 0;
  TSI0_DATA = 0;
  // 16uA 600mV PS:4
  // Channels 9 and 10
  TSI0_GENCS =  (1 << 21) | (1 << 20) | (1 << 16) | (1 << 14) | (1 << 7);
  //               1uA      600mV    1uA         / 4     enable
}

uint16_t touch_sensor_read(uint8_t pin){
  TSI0_DATA = TSI_DATA_TSICH(pin);
  TSI0_DATA |= TSI_DATA_SWTS_MASK;
  while(!(TSI0_GENCS & (1 << 2))); //wait for scan to complete

  TSI0_GENCS |= (1 << 2);
  return TSI0_DATA & 0xFF;
}
