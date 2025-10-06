# GameHub - projekt na przedmiot Aplikacje Mobilne
<p align="center">
 <a href="https://ibb.co/N26M9724"><img src="https://i.ibb.co/F4bF5n4P/ca34c4f3-3385-45f0-a00a-ccc49478f23c.jpg" alt="ekran główny" border="0" width=200></a>
</p>

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



