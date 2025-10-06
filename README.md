# GameHub - projekt na przedmiot Aplikacje Mobilne

![ekran główny](https://i.ibb.co/F4bF5n4P/ca34c4f3-3385-45f0-a00a-ccc49478f23c.jpg)

<!-- <a href="https://ibb.co/6cfgn8TM"><img src="https://i.ibb.co/HLknFCbS/bcc9c0f4-0b53-4b14-8e03-2b1c47496164.jpg" alt="bcc9c0f4-0b53-4b14-8e03-2b1c47496164" border="0"></a>
<a href="https://ibb.co/d8jZ5BX"><img src="https://i.ibb.co/w5Btzy8/eaec5345-15c8-48f7-bb12-84adc358f56b.jpg" alt="eaec5345-15c8-48f7-bb12-84adc358f56b" border="0"></a>
<a href="https://ibb.co/N26M9724"><img src="" alt="ca34c4f3-3385-45f0-a00a-ccc49478f23c" border="0"></a> -->

W ramach projektu na przedmiot Aplikacje Mobilne stworzyliśmy platformę GameHub, umożliwiającą umawianie się ze znajomymi i granie w gry wieloosobowe. Wśród dotychczas zaimplementowanych gier są:

- Snake
- Arkanoid
- Minesweeper
- Sudoku
- Flappy Bird

Z powyższych gier tylko snake oferuje rozgrywkę wieloosobową.
Poza tym, aplikacja umożliwia również dzielenie się zdobytymi osiągnięciami w grach ze znajomymi



## Szczegóły techniczne

Aplikacja została zaimplementowana w Kotlinie 1.8.21. Do stworzenia interfejsu użytkownika użyto deklaratywnego frameworka Jetpack Compose, który podobnie jak inne frameworki, pozwala w intuicyjny sposób tworzyć komponenty, które mogą być ponownie użyte.
Wszystkie komponenty zostały stworzone przy użyciu Material 3.
Użyto BaaS Firebase, który pozwolił na zaimplementowanie następujących funkcjonalności:
 - obsługa konta (w tym możliwośc ustawienia zdjęcia profilowego)
 - wyszukiwanie i zapraszanie znajomych
 - rozgrywka wieloosobowa (za pomocą Firebase Realtime Database)



