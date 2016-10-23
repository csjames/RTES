
#include "board.h"
#include "pin_mux.h"
#include "fsl_clock_manager.h"
#include "fsl_debug_console.h"

void hardware_init(void) {

  SIM->SCGC5 |= SIM_SCGC5_PORTA_MASK;
  SIM->SCGC5 |= SIM_SCGC5_PORTC_MASK;
  SIM->SCGC5 |= SIM_SCGC5_PORTD_MASK;
  SIM->SCGC5 |= SIM_SCGC5_PORTE_MASK;

  //left and right buttons

  PORTC->PCR[3] |= PORT_PCR_MUX(1U)|3;
  PORTC->PCR[12] |= PORT_PCR_MUX(1U)|3;

  PTC->PDDR |= (0U << 3U);
  PTC->PDDR |= (0U << 12U);

  //green & red led

  PORTE->PCR[29] = PORT_PCR_MUX(1U);
  PTE->PDDR |= (1U << 29U);

  PORTD->PCR[5] = PORT_PCR_MUX(1U);
  PTD->PDDR |= (1U << 5U);

  /* enable clock for PORTs */
  CLOCK_SYS_EnablePortClock(PORTA_IDX);
  CLOCK_SYS_EnablePortClock(PORTE_IDX);

  /* Init board clock */
  BOARD_ClockInit();
  dbg_uart_init();
}
int main(int argc, char const *argv[]) {
  /* code */
  return 0;
}
