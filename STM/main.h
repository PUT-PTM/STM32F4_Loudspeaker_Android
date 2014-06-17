#ifndef __STM32F4_DISCOVERY_USB_HID_H
#define __STM32F4_DISCOVERY_USB_HID_H

#include "stm32f4_discovery.h"
#include "stm32f4xx_gpio.h"
#include "stm32f4xx_rcc.h"
#include "codec.h"
#include <stdio.h>

//Wartosc absolutna
#define ABS(x)         (x < 0) ? (-x) : x
//Wartosc maksymalna
#define MAX(a,b)       (a < b) ? (b) : a

//Funkcje Delay
void TimingDelay_Decrement(void);
void Delay(__IO uint32_t nTime);
//Fail wychwytacz
void Fail_Handler(void);

#define NOTEFREQUENCY 0.015		//Ustalona czestotliwosc fali: f_0 = 0.5 * NOTEFREQUENCY * 48000 (=sample rate)
#define NOTEAMPLITUDE 500.0		//Amplituda fali

//Struktura dziweku
typedef struct {
	float tabs[8];
	float params[8];
	uint8_t currIndex;
} fir_8;

//Struktura potzebna do inicjalizacji Pinu 4
GPIO_InitTypeDef GPIO_InitStructure;

//zmienna licziczaca wykonanie "dzwieku"
volatile uint32_t sampleCounter = 0;
//zmienna dzwiekowa
volatile int16_t sample = 0;

//zmienna fali dzwiekowej
float sawWave = 0.0;
//zmienna fali dziwkowej
float filteredSaw = 0.0;

//funkcja zmieniajaca fale dziwkowa
float updateFilter(fir_8* theFilter, float newValue);

//inicjalizacja fali
void initFilter(fir_8* theFilter);

#endif
