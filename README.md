STM32F4_Loudspeaker_Android
===========================

Aplikacja Android:
===========================

Wykorzystano środowisko Android Developer Tools. Mikrokontroler STM32F4 wykrywany jest w trybie USB HID.
Wciśnięcie odpowiedniego przycisku powoduje wysłanie 1 znaku (bajta) do mikrokontrolera, który ten znak
interpretuje i zamienia na odpowiedni dźwięk o odpowiedniej częstotliwości. Pod przyciskami wyświetla się
log, w którym możemy zaobserwować co dokładnie zostało wysłane. Log może zostać wyczyszczony. Na początku
musimy wybrać odpowiednie urządzenie HID.

Sterowniki i narzędzia dla mikrokontrolera STM32F4xx
===========================

Sterowniki dla STM32F4xx: http://www.st.com/web/en/catalog/tools/PF258167
Kompilator ARM-GCC from: https://launchpad.net/gcc-arm-embedded/+download
