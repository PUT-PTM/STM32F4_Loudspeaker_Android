#include "main.h"
#include "usbd_hid_core.h"
#include "usbd_usr.h"
#include "usbd_desc.h"

//Define USB OTG
#ifdef USB_OTG_HS_INTERNAL_DMA_ENABLED
  #if defined ( __ICCARM__ ) /*!< IAR Compiler */
    #pragma data_alignment = 4
  #endif
#endif
__ALIGN_BEGIN USB_OTG_CORE_HANDLE  USB_OTG_dev __ALIGN_END;

//Guiziczek wcisniety?
__IO uint8_t UserButtonPressed = 0x00;
//Bufory USB, odczyt i wys³anie do USB
uint8_t InBuffer[64], OutBuffer[63];

//tablica czestotliwosci poszczegolnych nut,
const float nuty[]=
{
	//     C          C#         D          D#         E          F          F#  	    G          G#         A          A#         B
		 16.35f,    17.32f,    18.35f,    19.45f,    20.60f,    21.83f,    23.12f,    24.50f,    25.96f,    27.50f,    29.14f,    30.87f,
		 32.7f, 	34.65f,    36.71f,    38.89f,    41.20f,    43.65f,    46.25f,    49.00f,    51.91f,    55.00f,    58.27f,    61.74f,
		 65.41f,    69.30f,    73.42f,    77.78f,    82.41f,    87.31f,    92.50f,    98.00f,   103.83f,   110.00f,   116.54f,   123.47f,
		130.81f,   138.59f,   146.83f,   155.56f,   164.81f,   174.61f,   185.00f,   196.00f,   207.65f,   220.00f,   233.08f,   246.94f,
		261.63f,   277.18f,   293.66f,   311.13f,	329.63f,   349.23f,   369.99f,   392.00f,  	415.30f,   440.00f,   466.16f,   493.88f,
		523.25f,   554.37f,   587.33f,   622.25f,   659.26f,   698.46f,   739.99f,   783.99f,   830.61f,   880.00f,   932.33f,   987.77f,
	   1046.50f,  1108.73f,  1174.66f,  1244.51f,  1318.51f,  1396.91f,  1479.98f,  1567.98f,  1661.22f,  1760.00f,  1864.66f,  1975.53f,
	   2093.00f,  2217.46f,  2349.32f, 	2489.02f,  2637.02f,  2793.83f,  2959.96f,  3135.96f,  3322.44f,  3520.00f,  3729.31f,  3951.07f,
	   4186.01f,  4434.92f,  4698.64f, 	4978.03f
};



int main(void)
{
	//Inicjalizacja Systemu
	SystemInit();
	//Inicjalizacja
	fir_8 filt;
	//define iluminestencyjne diody polprzewodnikowe, oraz niebieski guziczek
	STM32F4_Discovery_LEDInit(LED3);
	STM32F4_Discovery_LEDInit(LED4);
	STM32F4_Discovery_LEDInit(LED5);
	STM32F4_Discovery_LEDInit(LED6);
	STM32F4_Discovery_PBInit(BUTTON_USER, BUTTON_MODE_GPIO);

	STM32F4_Discovery_LEDOn(LED3);

	//init usb HID
	USBD_Init(&USB_OTG_dev,
		#ifdef USE_USB_OTG_HS
			USB_OTG_HS_CORE_ID,
		#else
			USB_OTG_FS_CORE_ID,
		#endif
		&USR_desc,
		&USBD_HID_cb,
		&USR_cb);

	STM32F4_Discovery_LEDOn(LED4);

	//Ustawienie Reset Clock Controla
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);

  	//GPIO_InitStructure.GPIO_Pin = GPIO_Pin_15;
  	//GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
  	//GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
  	//GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

  	//GPIO_Init(GPIOD, &GPIO_InitStructure);

  	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

  	//inicjalizacja kodeka dzwieku
  	codec_init();
  	//zakonczenie inicjalizacji kodeka
  	codec_ctrl_init();

  	//uruchomienie I2S
  	I2S_Cmd(CODEC_I2S, ENABLE);

  	//zainicjowanie dziwku
  	initFilter(&filt);

  	//zmienna pomocnicza, do ustalania roznych dziwkow
  	int16_t t = 0;

  	//zmienna pomocnicza, czy fala jest rosnaca, czy spadajaca
  	int x = 0;


  	//glowna peta programu
  	while (1)
  	{
  		/*
	  	  //W zaleznosci, od liczby jaka dostaniesz, ta dioda zrob mryg!
	  	  	  if (OutBuffer[0]&1)  	STM32F4_Discovery_LEDOn(LED3);
	  		  	  	  else			STM32F4_Discovery_LEDOff(LED3);
	  	  	  if (OutBuffer[0]&2)  	STM32F4_Discovery_LEDOn(LED4);
	  		  	  	  else			STM32F4_Discovery_LEDOff(LED4);
	  	  	  if (OutBuffer[0]&4)  	STM32F4_Discovery_LEDOn(LED5);
	  		  	  	  else			STM32F4_Discovery_LEDOff(LED5);
	  	  	  if (OutBuffer[0]&8)  	STM32F4_Discovery_LEDOn(LED6);
	  		  	  	  else			STM32F4_Discovery_LEDOff(LED6);
	  	  */

  	/*
	  switch(OutBuffer[0]) {
	  case 48: t = 0; break;
	  case 49: t = 1; break;
	  case 50: t = 2; break;
	  case 51: t = 3; break;
	  case 52: t = 4; break;
	  case 53: t = 5; break;
	  case 54: t = 6; break;
	  case 55: t = 9; break;
	  case 56: t = 8; break;
	  case 57: t = 9; break;
	  case 97: t =10; break;
	  case 98: t =11; break;
	  case 99: t =12; break;
	  case 100: t =13; break;
	  case 101: t =14; break;
	  case 102: t =15; break;
	  case 103: t =16; break;
	  case 104: t =17; break;
	  case 105: t =18; break;
	  case 106: t =19; break;
	  case 107: t =20; break;
	  case 108: t =21; break;
	  case 109: t =22; break;
	  case 110: t =23; break;
	  case 111: t =24; break;
	  case 112: t =25; break;
	  case 113: t =26; break;
	  case 114: t =27; break;
	  case 115: t =28; break;
	  case 116: t =29; break;
	  case 117: t =30; break;
	  case 118: t =31; break;
	  case 119: t =32; break;
	  case 120: t =33; break;
	  case 121: t =34; break;
	  case 122: t =35; break;


	  default: t = 0;
	  }
	   */
	  switch(OutBuffer[0]) {

	  case 48: t = (int16_t)nuty[0]; 	break;
		  case 49: t = (int16_t)nuty[1]; 	break;
		  case 50: t = (int16_t)nuty[2]; 	break;
		  case 51: t = (int16_t)nuty[3]; 	break;
		  case 52: t = (int16_t)nuty[4]; 	break;
		  case 53: t = (int16_t)nuty[5]; 	break;
		  case 54: t = (int16_t)nuty[6]; 	break;
		  case 55: t = (int16_t)nuty[7]; 	break;
		  case 56: t = (int16_t)nuty[8]; 	break;
		  case 57: t = (int16_t)nuty[9]; 	break;
		  case 97: t = (int16_t)nuty[10]; 	break;
		  case 98: t = (int16_t)nuty[11]; 	break;
		  case 99: t = (int16_t)nuty[12]; 	break;
		  case 100: t = (int16_t)nuty[13]; 	break;
		  case 101: t = (int16_t)nuty[14]; 	break;
		  case 102: t = (int16_t)nuty[15]; 	break;
		  case 103: t = (int16_t)nuty[16]; 	break;
		  case 104: t = (int16_t)nuty[17]; 	break;
		  case 105: t = (int16_t)nuty[18]; 	break;
		  case 106: t = (int16_t)nuty[19]; 	break;
		  case 107: t = (int16_t)nuty[20];	break;
		  case 108: t = (int16_t)nuty[21]; 	break;
		  case 109: t = (int16_t)nuty[22]; 	break;
		  case 110: t = (int16_t)nuty[23]; 	break;
		  case 111: t = (int16_t)nuty[24]; 	break;
		  case 112: t = (int16_t)nuty[25]; 	break;
		  case 113: t = (int16_t)nuty[26]; 	break;
		  case 114: t = (int16_t)nuty[27]; 	break;
		  case 115: t = (int16_t)nuty[28]; 	break;
		  case 116: t = (int16_t)nuty[29]; 	break;
		  case 117: t = (int16_t)nuty[30]; 	break;
		  case 118: t = (int16_t)nuty[31]; 	break;
		  case 119: t = (int16_t)nuty[32]; 	break;
		  case 120: t = (int16_t)nuty[33];	break;
		  case 121: t = (int16_t)nuty[34]; 	break;
		  case 122: t = (int16_t)nuty[35]; 	break;

		  default: t = 0;
	  }


	  //SPI_I2S_SendData(CODEC_I2S, t);

	  //Czy Codek I2S moze przyjac kolejne dane
	  if (SPI_I2S_GetFlagStatus(CODEC_I2S, SPI_I2S_FLAG_TXE))
	  {
		  //Wyslanie danych do I2S
		  SPI_I2S_SendData(CODEC_I2S, sample);

	      //czy licznik petli jest nieparzysty?
	      if (sampleCounter & 0x00000001)
	      {
	    	  if (x == 0)
	    	  {
	    		  sawWave += NOTEFREQUENCY;
	    		  if (sawWave > 1.0)
	    			  x = 1;
	    	  }
	    	  if (x == 1)
	    	  {
	    		  sawWave -= NOTEFREQUENCY;
	    		  if (sawWave < -1.0)
	    			  x = 0;
	    	  }

	    	  filteredSaw = updateFilter(&filt, sawWave);

	    	  sample = (int16_t)(NOTEAMPLITUDE*filteredSaw)*t;
	      }
	      sampleCounter++;
	   }
	   if (sampleCounter >= 100000)
		   sampleCounter = 0;
  }
}



/**
  * @brief  Inserts a delay time.
  * @param  nTime: specifies the delay time length.
  * @retval None
  */
void Delay(__IO uint32_t nTime)
{
  if (nTime != 0x00)
  {
    nTime--;
  }
}


#ifdef  USE_FULL_ASSERT

/**
  * @brief  Reports the name of the source file and the source line number
  *   where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t* file, uint32_t line)
{
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

  /* Infinite loop */
  while (1)
  {
  }
}
#endif

/**
  * @}
  */



// filtrowanie sygnalu dzwieku
float updateFilter(fir_8* filt, float val)
{
	uint16_t valIndex;
	uint16_t paramIndex;
	float outval = 0.0;

	valIndex = filt->currIndex;
	filt->tabs[valIndex] = val;

	for (paramIndex=0; paramIndex<8; paramIndex++)
	{
		outval += (filt->params[paramIndex]) * (filt->tabs[(valIndex+paramIndex)&0x07]);
	}

	valIndex++;
	valIndex &= 0x07;

	filt->currIndex = valIndex;

	return outval;
}

//inicjalizacja sygna³u
void initFilter(fir_8* theFilter)
{
	uint8_t i;

	theFilter->currIndex = 0;

	for (i=0; i<8; i++)
		theFilter->tabs[i] = 0.0;

	theFilter->params[0] = 0.01;
	theFilter->params[1] = 0.05;
	theFilter->params[2] = 0.10;
	theFilter->params[3] = 0.25;
	theFilter->params[4] = 0.25;
	theFilter->params[5] = 0.10;
	theFilter->params[6] = 0.05;
	theFilter->params[7] = 0.01;
}
